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

package com.google.gxp.compiler.bind;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.base.AttrBundleParam;
import com.google.gxp.compiler.base.BoundCall;
import com.google.gxp.compiler.base.BoundImplementsDeclaration;
import com.google.gxp.compiler.base.BundleType;
import com.google.gxp.compiler.base.Call;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.CallVisitor;
import com.google.gxp.compiler.base.CollapseExpression;
import com.google.gxp.compiler.base.ConstructedConstant;
import com.google.gxp.compiler.base.ConvertibleToContent;
import com.google.gxp.compiler.base.DefaultingTypeVisitor;
import com.google.gxp.compiler.base.ExhaustiveExpressionVisitor;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.FormalParameter;
import com.google.gxp.compiler.base.Implementable;
import com.google.gxp.compiler.base.ImplementsDeclaration;
import com.google.gxp.compiler.base.ImplementsVisitor;
import com.google.gxp.compiler.base.NativeImplementsDeclaration;
import com.google.gxp.compiler.base.ObjectConstant;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.base.Type;
import com.google.gxp.compiler.base.UnboundCall;
import com.google.gxp.compiler.base.UnboundImplementsDeclaration;
import com.google.gxp.compiler.base.ValidatedCall;
import com.google.gxp.compiler.reparent.Attribute;
import com.google.gxp.compiler.reparent.ReparentedTree;
import com.google.gxp.compiler.schema.AttributeValidator;
import com.google.gxp.compiler.schema.Schema;
import com.google.gxp.compiler.schema.SchemaFactory;
import com.google.gxp.compiler.servicedir.ScopedServiceDirectory;
import com.google.gxp.compiler.servicedir.ServiceDirectory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Binds references to resources, typically across compilation units, by
 * replacing {@code UnboundCall}s with {@code BoundCall}s.
 */
public class Binder implements Function<ReparentedTree, BoundTree> {
  private final SchemaFactory schemaFactory;
  private final ServiceDirectory baseServiceDirectory;

  public Binder(SchemaFactory schemaFactory, ServiceDirectory baseServiceDirectory) {
    this.schemaFactory = Objects.nonNull(schemaFactory);
    this.baseServiceDirectory = Objects.nonNull(baseServiceDirectory);
  }

  public BoundTree apply(ReparentedTree reparentedTree) {
    Set<Callable> requirements = Sets.newHashSet();
    AlertSetBuilder alertSetBuilder = new AlertSetBuilder(reparentedTree.getAlerts());

    Root oldRoot = reparentedTree.getRoot();
    ServiceDirectory serviceDirectory =
        new ScopedServiceDirectory(alertSetBuilder,
                                   baseServiceDirectory,
                                   oldRoot.getName().getPackageName(),
                                   oldRoot.getImports());

    Root newRoot = oldRoot.acceptVisitor(
        new Visitor(alertSetBuilder, schemaFactory, serviceDirectory, requirements));

    return new BoundTree(reparentedTree.getSourcePosition(), alertSetBuilder.buildAndClear(),
                         newRoot, requirements);
  }

  private static class Visitor extends ExhaustiveExpressionVisitor
      implements CallVisitor<Expression>, ImplementsVisitor<ImplementsDeclaration> {
    private final AlertSink alertSink;
    private final SchemaFactory schemaFactory;
    private final ServiceDirectory serviceDirectory;
    private final Set<Callable> requirements;

    Visitor(AlertSink alertSink, SchemaFactory schemaFactory, ServiceDirectory serviceDirectory,
            Set<Callable> requirements) {
      this.alertSink = Objects.nonNull(alertSink);
      this.schemaFactory = Objects.nonNull(schemaFactory);
      this.serviceDirectory = Objects.nonNull(serviceDirectory);
      this.requirements = Objects.nonNull(requirements);
    }

    public Template visitTemplate(final Template template) {
      List<ImplementsDeclaration> newImplDeclarations = Lists.newLinkedList();
      for (ImplementsDeclaration id : template.getImplementsDeclarations()) {
        ImplementsDeclaration transformedImplDec =
            id.acceptImplementsVisitor(this);
        if (transformedImplDec != null) {
          newImplDeclarations.add(transformedImplDec);
        }
      }
      return super.visitTemplate(template.withImplementsDeclarations(newImplDeclarations));
    }

    @Override
    public Expression visitCall(Call call) {
      return call.acceptCallVisitor(this);
    }

    public Expression visitBoundCall(BoundCall call) {
      // This shouldn't really happen, but it seems reasonable to leave an
      // already bound call alone.
      return call.transformParams(this);
    }

    public Expression visitValidatedCall(ValidatedCall call) {
      // Again, this shouldn't really happen, but it seems reasonable to leave
      // an already bound and validated call alone.
      return call.transformParams(this);
    }

    public Expression visitUnboundCall(UnboundCall call) {
      TemplateName calleeName = call.getCallee();
      Map<String, Attribute> params = call.getAttributes();
      Callable callee = params.containsKey(Implementable.INSTANCE_PARAM_NAME)
          ? serviceDirectory.getInstanceCallable(calleeName)
          : serviceDirectory.getCallable(calleeName);

      if (callee == null) {
        alertSink.add(new CallableNotFoundError(call, calleeName));
        return new StringConstant(call, null, "");
      } else {
        final ImmutableMap.Builder<String, Attribute> newAttrBuilder =
            ImmutableMap.builder();

        // construct a Map of attribute bundles with one entry for
        // each bundle parameter
        final Map<String, Map<AttributeValidator, Attribute>> attrBundles = Maps.newHashMap();
        for (FormalParameter parameter : callee.getParameters()) {
          if (parameter.getType() instanceof BundleType) {
            attrBundles.put(parameter.getPrimaryName(),
                            new HashMap<AttributeValidator, Attribute>());
          }
        }

        Expression content = apply(call.getContent());
        content = prepareExpressionAsParameterValue(callee.getContentConsumingParameter(),
                                                    content);

        for (final String param : params.keySet()) {
          final FormalParameter parameter = callee.getParameter(param);
          Attribute attr = params.get(param);
          if (parameter == null) {
            alertSink.add(new BadParameterError(attr.getValue(), callee, param));
            continue;
          }
          // TODO(harryh): maybe better to use a  DefaultingExpressionVisitor
          //               here?
          if (attr.getValue() instanceof ObjectConstant) {
            ObjectConstant oc = (ObjectConstant) attr.getValue();
            // TODO(harryh): maybe this should be in Validator?
            if (!parameter.regexMatches(oc)) {
              alertSink.add(new InvalidParameterFailedRegexError(
                                calleeName, param, parameter.getRegex(), oc));
            }
            if (parameter.hasConstructor()) {
              attr = attr.withValue(new ConstructedConstant(oc, oc.getValue(), callee, parameter));
            } else {
              attr = attr.withValue(parameter.getType().parseObjectConstant(param, oc, alertSink));
            }
          }

          attr = attr.withValue(prepareExpressionAsParameterValue(parameter, attr.getValue()));

          final Attribute updatedAttr = visitAttribute(attr);

          parameter.getType().acceptTypeVisitor(new DefaultingTypeVisitor<Void>() {
            protected Void defaultVisitType(Type type) {
              newAttrBuilder.put(param, updatedAttr);
              return null;
            }

            public Void visitBundleType(BundleType type) {
              final AttributeValidator validator = type.getValidator(param);
              String innerContentTypeString = validator.getContentType();
              if (innerContentTypeString != null) {
                Schema innerSchema = schemaFactory.fromContentTypeName(innerContentTypeString);
                attrBundles.get(parameter.getPrimaryName()).put(validator,
                                                         updatedAttr.withInnerSchema(innerSchema));
              } else {
                attrBundles.get(parameter.getPrimaryName()).put(validator, updatedAttr);
              }
              return null;
            }
          });
        }

        // go through the attrBundleMap and turn each entry into an
        // AttrBundleParam and put this into the builder map.
        for (Map.Entry<String, Map<AttributeValidator, Attribute>> attrBundle :
                attrBundles.entrySet()) {
          FormalParameter parameter = callee.getParameterByPrimary(attrBundle.getKey());
          BundleType bt = (BundleType) parameter.getType();

          // special case for the (common case) of a single bundle on the
          // callee side. In this case there is no mixing of attributes
          // between bundles so the GxpAttrBundleBuilder does not need to
          // include only some attributes from passed in bundles.  See the
          // empty constructor in j/c/g/gxp/base/GxpAttrBundleBuilder.java
          Set<String> includeAttrs = (attrBundles.size() == 1)
              ? Collections.<String>emptySet() : bt.getAttrMap().keySet();

          AttrBundleParam newBundle =
              new AttrBundleParam(call, callee.getSchema(), includeAttrs,
                                  attrBundle.getValue(), call.getAttrBundles());

          newAttrBuilder.put(attrBundle.getKey(),
                             new Attribute(call, attrBundle.getKey(),
                                           newBundle, null));
        }

        requirements.add(callee);
        return new BoundCall(call, callee, newAttrBuilder.build(), content);
      }
    }

    /**
     * Performs automatic adaptations of {@code Expression}s to {@code
     * FormalParameter}s. This currently consists of:
     * <ul>
     * <li>appying the space collapsing rules specified by the given {@code
     * FormalParameter}, if any, and applying them to the {@code Expression} if
     * appropriate.
     * <li>leaving ConvertibleToContent nodes around the {@code Expression}
     * only if the {@code FormalParameter} is for a content parameter.
     * </ul>
     * If {@code parameter} is {@code null} then {@code expr} is returned as-is.
     *
     * @param parameter the {@code FormalParameter} to adapt to
     * @param expr the {@code Expression} to be adapted
     * @return an adapted {@code Expression} (may be the original Expression)
     */
    private static Expression prepareExpressionAsParameterValue(FormalParameter parameter,
                                                                Expression expr) {
      if (parameter != null) {
        // TODO(laurence): don't use instanceof here
        if (!parameter.getType().isContent()
            && (expr instanceof ConvertibleToContent)) {
          expr = ((ConvertibleToContent) expr).getSubexpression();
        }

        // TODO(laurence): don't use instanceof here
        if ((expr instanceof CollapseExpression)
            || (expr instanceof ConvertibleToContent)) {
          return CollapseExpression.create(expr, parameter.getSpaceOperators());
        }
      }
      return expr;
    }

    // ImplementsVisitor<ImplementsDeclaration> methods:
    public ImplementsDeclaration
    visitUnboundImplementsDeclaration(UnboundImplementsDeclaration uid) {
      TemplateName templateName = uid.getTemplateName();
      Implementable theInterface = serviceDirectory.getImplementable(templateName);

      if (theInterface == null) {
        alertSink.add(new ImplementableNotFoundError(uid, templateName));
        // suppress any other ImplementsDeclaration alerts; return 'null',
        // indicating that this ImplementsDeclaration is invalid
        return null;
      }

      requirements.add(theInterface);
      return new BoundImplementsDeclaration(theInterface, uid.getSourcePosition(),
                                            uid.getDisplayName());
    }

    public ImplementsDeclaration visitBoundImplementsDeclaration(BoundImplementsDeclaration bid) {
      return bid;
    }

    public ImplementsDeclaration
    visitNativeImplementsDeclaration(NativeImplementsDeclaration nid) {
      return nid;
    }
  }
}
