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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.MissingAttributeError;
import com.google.gxp.compiler.alerts.common.MultiValueAttributeError;
import com.google.gxp.compiler.alerts.common.UnknownAttributeError;
import com.google.gxp.compiler.base.AttributeName;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.MultiLanguageAttrValue;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.parser.CppNamespace;
import com.google.gxp.compiler.parser.JavaNamespace;
import com.google.gxp.compiler.parser.Namespace;
import com.google.gxp.compiler.parser.NullNamespace;
import com.google.gxp.compiler.parser.OutputLanguageNamespace;

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
    this.alertSink = Objects.nonNull(alertSink);
    this.forNode = Objects.nonNull(forNode);
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
   * @return a (new) list containing all of the unused {@code Attribute}s in
   * the order they were specified. Note that this marks all attributes as
   * "used", so the caller is responsible for reporting unused attributes on
   * their own.
   */
  public List<Attribute> getUnusedAttributes() {
    List<Attribute> result = Lists.newArrayList();
    for (Map.Entry<AttributeName, Attribute> entry : namesToAttrs.entrySet()) {
      AttributeName key = entry.getKey();
      if (!used.contains(key)) {
        used.add(key);
        result.add(entry.getValue());
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
   * Core implementation of {@link #getExprValue(String,Expression)} and {@link
   * #getValue(String,Expression)}.
   */
  private Expression getValueImpl(Namespace ns, String name,
                                  Expression fallback,
                                  boolean convertToExpression,
                                  boolean optional) {
    Attribute attr = getAttribute(ns, name);
    if (attr == null) {
      if (!optional) {
        alertSink.add(new MissingAttributeError(forNode, name));
      }
      return fallback;
    } else {
      return convertToExpression ? attr.getExprValue() : attr.getValue();
    }
  }

  /**
   * Gets the specified attribute.
   *
   * @param name local name of the attribute. This method only works for
   * attributes that have no namespace.
   * @param fallback value to use if a value is not available. An {@code
   * Alert} will be generated if the fallback is used.
   */
  public Expression getExprValue(Namespace ns, String name,
                                 Expression fallback) {
    return getValueImpl(ns, name, fallback, true, false);
  }

  /**
   * Gets the specified optional attribute.
   *
   * @param name local name of the attribute. This method only works for
   * attributes that have no namespace.
   * @param fallback value to use if a value is not available.
   */
  public Expression getOptionalExprValue(Namespace ns, String name,
                                         Expression fallback) {
    return getValueImpl(ns, name, fallback, true, true);
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
    return getValueImpl(ns, name, fallback, false, false);
  }

  /**
   * Core implementation of {@link #get(String,String)} and {@link
   * #getOptional(String,String)}.
   */
  private String getImpl(Namespace ns, String name, final String fallback,
                         boolean optional) {
    final Expression value = getValueImpl(ns, name, null, false, optional);
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
    return getExprValue(NullNamespace.INSTANCE, name, fallback);
  }

  public Expression getOptionalExprValue(String name, Expression fallback) {
    return getOptionalExprValue(NullNamespace.INSTANCE, name, fallback);
  }

  public Attribute getAttribute(String name) {
    return getAttribute(NullNamespace.INSTANCE, name);
  }

  private static final ImmutableList<OutputLanguageNamespace> outputLanguageNamespaces =
      ImmutableList.of(CppNamespace.INSTANCE,
                       JavaNamespace.INSTANCE);

  public static Iterable<OutputLanguageNamespace> getOutputLanguageNamespaces() {
    return outputLanguageNamespaces;
  }

  /**
   * Constructs an {@link MultiLanguageAttrValue} for the given {@code name}.
   */
  public MultiLanguageAttrValue getMultiLanguageAttrValue(String name) {
    Map<OutputLanguage, String> map = Maps.newHashMap();
    for (OutputLanguageNamespace ns : outputLanguageNamespaces) {
      String value = getOptional(ns, name, null);
      if (value != null) {
        map.put(ns.getOutputLanguage(), value);
      }
    }
    return new MultiLanguageAttrValue(map, getOptional(name, null));
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
