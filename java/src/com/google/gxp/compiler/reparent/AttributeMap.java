/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gxp.compiler.reparent;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.InvalidAttributeValueError;
import com.google.gxp.compiler.alerts.common.MissingAttributeError;
import com.google.gxp.compiler.alerts.common.MultiValueAttributeError;
import com.google.gxp.compiler.alerts.common.RequiredAttributeHasCondError;
import com.google.gxp.compiler.alerts.common.UnknownAttributeError;
import com.google.gxp.compiler.base.AttributeName;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.MultiLanguageAttrValue;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.parser.CppNamespace;
import com.google.gxp.compiler.parser.JavaNamespace;
import com.google.gxp.compiler.parser.JavaScriptNamespace;
import com.google.gxp.compiler.parser.Namespace;
import com.google.gxp.compiler.parser.NullNamespace;
import com.google.gxp.compiler.parser.OutputLanguageNamespace;
import com.google.gxp.compiler.parser.ScalaNamespace;

import java.util.*;

/**
 * Assists in the handling of attributes. It is intended to be used in two
 * phases:
 *
 * <p>In the first phase the {@code AttributeMap} is populated with attributes
 * (using the {@code add*} methods). Attributes are recorded in order of first
 * appearance, and duplicate attributes are discarded after reporting an {@code
 * Alert}.
 *
 * <p>In the second phase, attributes are retrieved using the {@code get*}
 * methods. For non-existent attributes a fallback value is used, and an Alert
 * is reported (unless a {@code getOptional*} method was used). The {@code
 * reportUnusedAttributes} method should be at the end of the second phase.
 * Once {@code reportUnusedAttributes} is called, {@code Alert}s are generated
 * about any attributes that were specified but not actually used.
 *
 * <p>This allows a fairly natural way of fetching attributes without having to
 * worry too much about explicit error handling.
 */
class AttributeMap {
  private final AlertSink alertSink;
  private final Node forNode;
  private final Map<AttributeName, Attribute> namesToAttrs =
      Maps.newLinkedHashMap();
  private final Set<AttributeName> used = Sets.newHashSet();

  /**
   * @param alertSink where {@code Alert}s are reported
   * @param forNode the element that will be "requesting" the attributes. This
   * is used as the position for {@code Alert}s about missing attributes.
   */
  public AttributeMap(AlertSink alertSink, Node forNode) {
    this.alertSink = Preconditions.checkNotNull(alertSink);
    this.forNode = Preconditions.checkNotNull(forNode);
  }

  /**
   * Adds the specified {@code Attribute}.
   */
  public void add(final Attribute attr) {
    AttributeName key = new AttributeName(attr.getNamespace(), attr.getName());
    if (namesToAttrs.containsKey(key)) {
      alertSink.add(new MultiValueAttributeError(forNode, attr));
    } else {
      namesToAttrs.put(key, attr);
    }
  }

  /**
   * Converts any remaining unprefixed attributes to {@code NativeExpression}s.
   */
  public void convertAllAttributesToExpressions() {
    for (Map.Entry<AttributeName, Attribute> entry : namesToAttrs.entrySet()) {
      Attribute attr = entry.getValue();
      Expression value = attr.getValue();
      if ((attr.getNamespace() instanceof NullNamespace) && value instanceof StringConstant) {
        String s = ((StringConstant) value).evaluate();
        entry.setValue(attr.withValue(new NativeExpression(value, new MultiLanguageAttrValue(s))));
      }
    }
  }

  /**
   * @return a (new) list containing all of the unused {@code Attribute}s in
   * the order they were specified. Note that this marks all attributes as
   * "used", so the caller is responsible for reporting unused attributes on
   * their own. This function also combines multi-lingual expression attributes
   * into a single attribute.
   */
  public List<Attribute> getUnusedAttributes() {
    List<Attribute> result = Lists.newArrayList();
    Set<String> usedLocalNames = Sets.newHashSet();

    for (AttributeName attrName : namesToAttrs.keySet()) {
      if (!used.contains(attrName)) {
        Namespace ns = attrName.getNamespace();
        String localName = attrName.getLocalName();
        if (!(ns instanceof OutputLanguageNamespace) && !(ns instanceof NullNamespace)) {
          used.add(attrName);
          result.add(namesToAttrs.get(attrName));
        } else if (!usedLocalNames.contains(localName)) {
          usedLocalNames.add(localName);
          // if the default attribute for this name is anything but a
          // NativeExpression then it cannot be combined with OutputLanguage
          // specific attribute, so use it as is.
          Attribute nullAttr = getAttribute(localName);
          if (nullAttr != null && !(nullAttr.getValue() instanceof NativeExpression)) {
            result.add(nullAttr);
          } else {
            result.add(new Attribute(forNode, localName,
                                     getExprImpl(localName, null, false), null));
          }
        }
      }
    }

    // at this point attributes that are unused aren't strictly unknown, so
    // much as incompatible with other attributes, so add Alerts indicating this.
    for (Map.Entry<AttributeName, Attribute> entry : namesToAttrs.entrySet()) {
      if (!used.contains(entry.getKey())) {
        used.add(entry.getKey());
        alertSink.add(new MultiValueAttributeError(forNode, entry.getValue()));
      }
    }

    return ImmutableList.copyOf(result);
  }

  // TODO(laurence): switch get* methods to take AttributeNames instead of
  // Namespaces and Strings?

  public Attribute getAttribute(Namespace ns, String name) {
    AttributeName key = new AttributeName(ns, name);
    used.add(key);
    return namesToAttrs.get(key);
  }

  /**
   * Core implementation of {@link #getValue(String,Expression)}.
   */
  private Expression getValueImpl(Namespace ns, String name,
                                  Expression fallback,
                                  boolean optional) {
    Attribute attr = getAttribute(ns, name);
    if (attr == null) {
      if (!optional) {
        alertSink.add(new MissingAttributeError(forNode, name));
      }
      return fallback;
    } else {
      return attr.getValue();
    }
  }

  /**
   * Core implementation of {@link #getExprValue(String,Expression)}.
   */
  private Expression getExprImpl(String name, Expression fallback, boolean optional) {
    // first check for a <gxp:attr> attribute, in which case the attribute
    // isn't multi lingual.
    Expression value = getValueImpl(NullNamespace.INSTANCE, name, null, true);
    if (value != null &&
        !(value instanceof StringConstant) && !(value instanceof NativeExpression)) {
      return value;
    }

    MultiLanguageAttrValue mlar = getMultiLanguageAttrValue(name, true);
    if (mlar.isEmpty()) {
      if (!optional) {
        alertSink.add(new MissingAttributeError(forNode, name));
      }
      return fallback;
    }
    return new NativeExpression(forNode.getSourcePosition(), "'" + name + "' attribute", mlar);
  }

  /**
   * Gets the specified attribute.
   *
   * @param name local name of the attribute. This method only works for
   * attributes that have no namespace.
   * @param fallback value to use if a value is not available. An {@code
   * Alert} will be generated if the fallback is used.
   */
  public Expression getValue(Namespace ns, String name, Expression fallback) {
    return getValueImpl(ns, name, fallback, false);
  }

  /**
   * Core implementation of {@link #get(String,String)} and {@link
   * #getOptional(String,String)}.
   */
  private String getImpl(Namespace ns, String name, final String fallback,
                         boolean optional) {
    final Expression value = getValueImpl(ns, name, null, optional);
    if (value == null) {
      return fallback;
    } else {
      return value.getStaticString(alertSink, fallback);
    }
  }

  /**
   * Gets the static value of the specified attribute.
   *
   * @param name local name of the attribute. This method only works for
   * attributes that have no namespace.
   * @param fallback value to use if a static value is not available. An {@code
   * Alert} will be generated if the fallback is used.
   */
  public String get(Namespace ns, String name, final String fallback) {
    return getImpl(ns, name, fallback, false);
  }

  /**
   * Gets the static value of the specified optional attribute. This is exactly
   * like {@link #get(String,String)} except that an {@code Alert} is
   * <em>not</em> generated if the fallback is used.
   */
  public String getOptional(Namespace ns, String name, final String fallback) {
    return getImpl(ns, name, fallback, true);
  }

  public Expression getValue(String name, Expression fallback) {
    return getValue(NullNamespace.INSTANCE, name, fallback);
  }

  public String get(String name, String fallback) {
    return get(NullNamespace.INSTANCE, name, fallback);
  }

  public String getOptional(String name, String fallback) {
    return getOptional(NullNamespace.INSTANCE, name, fallback);
  }

  public Expression getExprValue(String name, Expression fallback) {
    return getExprImpl(name, fallback, false);
  }

  public Expression getOptionalExprValue(String name, Expression fallback) {
    return getExprImpl(name, fallback, true);
  }

  public Attribute getAttribute(String name) {
    return getAttribute(NullNamespace.INSTANCE, name);
  }

  private static final ImmutableList<OutputLanguageNamespace> outputLanguageNamespaces =
      ImmutableList.of(CppNamespace.INSTANCE,
                       JavaNamespace.INSTANCE,
                       JavaScriptNamespace.INSTANCE,
                       ScalaNamespace.INSTANCE);

  public static Iterable<OutputLanguageNamespace> getOutputLanguageNamespaces() {
    return outputLanguageNamespaces;
  }

  /**
   * Constructs a static {@link MultiLanguageAttrValue} for the given {@code name}.
   */
  public MultiLanguageAttrValue getMultiLanguageAttrValue(String name) {
    return getMultiLanguageAttrValue(name, false);
  }

  /**
   * Constructs an {@link MultiLanguageAttrValue} for the given {@code name}.
   */
  public MultiLanguageAttrValue getMultiLanguageAttrValue(String name, boolean forExpr) {
    Map<OutputLanguage, String> map = Maps.newHashMap();
    for (OutputLanguageNamespace ns : outputLanguageNamespaces) {
      String value = getOptional(ns, name, null);
      if (value != null) {
        map.put(ns.getOutputLanguage(), value);
      }
    }

    String defaultStr = null;
    if (forExpr) {
      Expression expr = getValueImpl(NullNamespace.INSTANCE, name, null, true);
      if (expr != null) {
        defaultStr = (expr instanceof NativeExpression)
            ? ((NativeExpression) expr).getDefaultNativeCode()
            : expr.getStaticString(alertSink, null);
      }
    } else {
      defaultStr = getOptional(name, null);
    }

    return new MultiLanguageAttrValue(map, defaultStr);
  }

  /**
   * Get a boolean value for an attribute that can take the value "true"
   * or "false" (anything else causes an alert). Returns false if the
   * attribute is not present.
   */
  public boolean getBooleanValue(String name) {
    String str = getOptional(name, null);
    if (str == null || str.equals("false")) {
      return false;
    } else if (str.equals("true")) {
      return true;
    } else {
      alertSink.add(new InvalidAttributeValueError(getAttribute(name)));
      return false;
    }
  }

  /**
   * Gets an Expression for the given attribute name.  Will be one of:
   * <ol>
   *   <li>
   *     If there is a &lt;gxp:attr&gt; or null prefixed attribute, then
   *     the value for that attribue.
   *   </li>
   *   <li>
   *     Otherwise a multi-lingual {@code NativeExpression} based on expr:
   *     and language specific prefixed attributes.
   *   </li>
   *   <li>
   *     Or the fallback, if none of the above are available.
   *   </li>
   * </ol>
   */
  public Expression getOptionalAttributeValue(String name, Expression fallback) {
    Expression result = null;
    Attribute nullAttr = getAttribute(name);
    if (nullAttr != null) {
      if (nullAttr.getCondition() != null) {
        alertSink.add(new RequiredAttributeHasCondError(forNode, nullAttr));
      }
      result = nullAttr.getValue();
    }

    // if we don't have anything yet, or if we got a NativeExpression
    // (which would be the default) try to get an Expression attribute
    if (result == null || result instanceof NativeExpression) {
      result = getOptionalExprValue(name, null);
    } else {
      // if we aren't getting delimiter as an expresion attribute then all OL
      // specific attribute conflict with whatever we are using
      for (OutputLanguageNamespace ns : outputLanguageNamespaces) {
        Attribute attr = getAttribute(ns, name);
        if (attr != null) {
          alertSink.add(new ConflictingAttributesError(forNode, nullAttr, attr));
        }
      }
    }

    return (result == null)
        ? fallback
        : result;
  }

  /**
   * Should be called by client after retrieving all of the attributes they
   * care about. Any unused attributes will be reported via the {@code
   * AlertSink} registered at construction.
   */
  public void reportUnusedAttributes() {
    for (Map.Entry<AttributeName, Attribute> entry : namesToAttrs.entrySet()) {
      if (!used.contains(entry.getKey())) {
        alertSink.add(new UnknownAttributeError(forNode, entry.getValue()));
      }
    }
  }
}
