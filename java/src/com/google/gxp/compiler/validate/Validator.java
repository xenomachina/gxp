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

package com.google.gxp.compiler.validate;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.MissingAttributeError;
import com.google.gxp.compiler.alerts.common.RequiredAttributeHasCondError;
import com.google.gxp.compiler.alerts.common.UnknownAttributeError;
import com.google.gxp.compiler.base.AbbrExpression;
import com.google.gxp.compiler.base.AttrBundleParam;
import com.google.gxp.compiler.base.BoundCall;
import com.google.gxp.compiler.base.BoundImplementsDeclaration;
import com.google.gxp.compiler.base.BundleType;
import com.google.gxp.compiler.base.Call;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.CallVisitor;
import com.google.gxp.compiler.base.ExhaustiveExpressionVisitor;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.FormalParameter;
import com.google.gxp.compiler.base.Implementable;
import com.google.gxp.compiler.base.ImplementsDeclaration;
import com.google.gxp.compiler.base.ImplementsVisitor;
import com.google.gxp.compiler.base.Interface;
import com.google.gxp.compiler.base.NativeImplementsDeclaration;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.ObjectConstant;
import com.google.gxp.compiler.base.OutputElement;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.UnboundCall;
import com.google.gxp.compiler.base.UnboundImplementsDeclaration;
import com.google.gxp.compiler.base.UnexpectedNodeException;
import com.google.gxp.compiler.base.ValidatedCall;
import com.google.gxp.compiler.escape.EscapedTree;
import com.google.gxp.compiler.reparent.Attribute;
import com.google.gxp.compiler.schema.AttributeValidator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates the tree. This validation primarily deals with {@code Call}s and
 * {@code OutputElement}s. Essentially, the attributes and content of these
 * nodes are checked, and any issues are reported as {@code Alert}s in the
 * resulting {@code ValidatedTree}. Also, {@code BoundCall}s will be converted
 * into {@code ValidatedCall}s. ({@code OutputElement}s aren't modified.)
 */
public class Validator implements Function<EscapedTree, ValidatedTree> {

  public ValidatedTree apply(EscapedTree tree) {
    AlertSetBuilder alertSetBuilder = new AlertSetBuilder(tree.getAlerts());
    Root root = tree.getRoot().acceptVisitor(new Visitor(alertSetBuilder));

    return new ValidatedTree(tree.getSourcePosition(), alertSetBuilder.buildAndClear(), root);
  }

  private static class Visitor extends ExhaustiveExpressionVisitor
      implements CallVisitor<Call>, ImplementsVisitor<Void> {
    private final AlertSink alertSink;
    private Template template = null;

    Visitor(AlertSink alertSink) {
      this.alertSink = Objects.nonNull(alertSink);
    }

    /**
     * Check for duplicate parameter names, or more than 1 content parameter
     *
     * TODO(harryh): this isn't examining the primary name of bundle
     *               parameters.  need to do that.
     */
    private void checkForDuplicateParams(Iterable<Parameter> params) {
      Set<String> names = Sets.newHashSet();
      boolean foundContentConsumer = false;

      for (Parameter parameter : params) {
        for (String name : parameter.getNames()) {
          if (names.contains(name)) {
            alertSink.add(new DuplicateParameterNameError(parameter, name));
          } else {
            names.add(name);
          }
        }
        if (parameter.consumesContent()) {
          if (foundContentConsumer) {
            alertSink.add(new TooManyContentParametersError(parameter));
          } else {
            foundContentConsumer = true;
          }
        }
      }
    }

    @Override
    public Interface visitInterface(Interface iface) {
      checkForDuplicateParams(iface.getParameters());
      for (Parameter param : iface.getParameters()) {
        if (param.getDefaultValue() != null) {
          alertSink.add(new InterfaceParamHasDefaultValueError(param));
        }
        if (param.getConstructor() != null) {
          alertSink.add(new InterfaceParamHasConstructorError(param));
        }
      }
      return super.visitInterface(iface);
    }

    private final Deque<String> varNames = new ArrayDeque<String>();

    @Override
    public Template visitTemplate(Template template) {
      // save template so that children have it avaliable
      this.template = template;

      @SuppressWarnings("unchecked")
      Iterable<Parameter> params =
          Iterables.concat(template.getConstructor().getParameters(),
                           template.getParameters());

      checkForDuplicateParams(params);

      for (Parameter param : params) {
        if (param.hasDefaultFlag() == true) {
          alertSink.add(new TemplateParamWithHasDefaultError(param));
        }
        if (param.hasConstructorFlag() == true) {
          alertSink.add(new TemplateParamWithHasConstructorError(param));
        }
        varNames.push(param.getPrimaryName());
      }

      for (ImplementsDeclaration id : template.getImplementsDeclarations()) {
        id.acceptImplementsVisitor(this);
      }

      return super.visitTemplate(template);
    }

    @Override
    public Expression visitAbbrExpression(AbbrExpression abbr) {
      if (varNames.contains(abbr.getName())) {
        alertSink.add(new ConflictingVarNameError(abbr));
      }

      varNames.push(abbr.getName());
      Expression ret = super.visitAbbrExpression(abbr);
      varNames.pop();
      return ret;
    }

    /**
     * Validates the attribute bundles being expanded into this {@code Node}
     * that were specified in a {@code gxp:bundles} attribute.
     *
     * 1. make sure all bundles correspond to bundle parameters
     * 2. make sure all bundle attributes are allowed for this
     *    node, and that the {@code AttributeValidator}s match
     * 3. make sure there are no conflicting attributes either between
     *    bundles or between a bundle and another attribue.
     *
     * @param node the {@code Node} being validated
     * @param attributeBundles the {@code List} of attribute bundles being
     *        expanded into this {@code Node}
     * @param validatorMap a {@code Map} from attribute names to
     *        {@code AttributeValidator}s for the attributes that can be
     *        supplied by the attribute bundles.
     * @param allowedAttributes the {@code Set} of attributes that this node
     *        can accept.
     */
    private void validateAttributeBundles(
        Node node, List<String> attributeBundles,
        Map<String, AttributeValidator> validatorMap,
        Set<String> allowedAttributes) {

      Set<String> foundAttributes = Sets.newHashSet();

      for (String attrBundle : attributeBundles) {
        Parameter parameter = template.getParameterByPrimary(attrBundle);
        if (parameter == null || !(parameter.getType() instanceof BundleType)) {
          alertSink.add(new InvalidAttrBundleError(node, attrBundle));
          continue;
        }

        for (Map.Entry<String, AttributeValidator> attr :
                 ((BundleType) parameter.getType()).getAttrMap().entrySet()) {

          AttributeValidator validator = validatorMap.get(attr.getKey());
          if (validator == null) {
            // no validator match, so we either already have seen this
            // attribute or this attribute isn't allowed at all
            if (allowedAttributes.contains(attr.getKey())) {
              alertSink.add(new DuplicateAttributeError(node, attrBundle,
                                                        attr.getKey()));
            } else {
              alertSink.add(new UnknownAttributeError(node, attr.getKey()));
            }
          } else if (!validator.equals(attr.getValue())) {
            alertSink.add(new MismatchedAttributeValidatorsError(
                              node, attr.getKey(), parameter.getPrimaryName()));
          } else {
            foundAttributes.add(attr.getKey());
          }
        }
      }

      // check to make sure that there are no remaining allowed attributes
      // that are required.
      for (Map.Entry<String, AttributeValidator> entry : validatorMap.entrySet()) {
        if (entry.getValue().isFlagSet(AttributeValidator.Flag.REQUIRED)
            && !foundAttributes.contains(entry.getKey())) {
          alertSink.add(new MissingAttributeError(node, entry.getKey()));
        }
      }
    }

    @Override
    public Expression visitOutputElement(OutputElement element) {

      // this is a map of all Name -> AttributeValidators for the element
      // we will remove items from the map as they are found.
      Map<String, AttributeValidator> validatorMap = Maps.newHashMap(
          element.getValidator().getAttributeValidatorMap());

      // this is a set of all allowed attributes for the element
      // it remains unchanged for the entire execution of this function
      Set<String> allowedAttributes = ImmutableSet.copyOf(validatorMap.keySet());

      for (final Attribute attr : element.getAttributes()) {
        AttributeValidator validator = validatorMap.remove(attr.getName());
        if (attr.getCondition() != null && validator.isFlagSet(AttributeValidator.Flag.REQUIRED)) {
          alertSink.add(new RequiredAttributeHasCondError(element, attr));
        }
      }

      if (template == null) {
        throw new AssertionError("found output element without a template");
      }

      validateAttributeBundles(element, element.getAttrBundles(), validatorMap, allowedAttributes);

      return super.visitOutputElement(element);
    }

    // TODO(harryh): validate Conditional Attributes.  Required attributes
    //               should never be conditional

    @Override
    public Expression visitCall(Call call) {
      return call.acceptCallVisitor(this);
    }

    public Call visitValidatedCall(ValidatedCall call) {
      // This shouldn't really happen, but it seems reasonable to leave
      // an already bound and validated call alone.
      return call.transformParams(this);
    }

    public Call visitUnboundCall(UnboundCall call) {
      throw new UnexpectedNodeException(call);
    }

    public Call visitBoundCall(final BoundCall call) {
      Callable callee = call.getCallee();

      final ImmutableMap.Builder<String, Attribute> newAttrBuilder = ImmutableMap.builder();

      // this is a map of all Name->AttributeValidators for the bundles
      // parameters of the callee.  We will remove items from the map
      // as they are found.
      Map<String, AttributeValidator> validatorMap = Maps.newHashMap();

      for (FormalParameter parameter : callee.getParameters()) {
        if (parameter.getType() instanceof BundleType) {
          // build up validatorMap
          BundleType calleeBundle = (BundleType) parameter.getType();
          validatorMap.putAll(calleeBundle.getAttrMap());
        }
      }

      // this is a set of all allowed attributes for the call attr bundles
      // it remains unchanged for the entire execution of this function
      Set<String> allowedAttributes = ImmutableSet.copyOf(validatorMap.keySet());

      for (final Map.Entry<String, Attribute> param : call.getAttributes().entrySet()) {
        Expression actualArgument = param.getValue().getValue();
        if (actualArgument instanceof AttrBundleParam) {
          AttrBundleParam bundle = (AttrBundleParam) actualArgument;
          for (Map.Entry<AttributeValidator, Attribute> attr : bundle.getAttrs().entrySet()) {
            validatorMap.remove(attr.getKey().getName());
            if (attr.getValue().getCondition() != null
                && attr.getKey().isFlagSet(AttributeValidator.Flag.REQUIRED)) {
              alertSink.add(new RequiredAttributeHasCondError(call, attr.getValue()));
            }
          }
        }
        newAttrBuilder.put(param.getKey(), visitAttribute(param.getValue()));
      }

      validateAttributeBundles(call, call.getAttrBundles(), validatorMap, allowedAttributes);
      Map<String, Attribute> newAttrParams = newAttrBuilder.build();

      // check for missing required attributes
      for (FormalParameter parameter : callee.getParameters()) {
        if (!parameter.hasDefault()) {
          if (!newAttrParams.containsKey(parameter.getPrimaryName())) {
            alertSink.add(new MissingAttributeError(call, parameter.getPrimaryName()));
          } else {
            Attribute fpAttribute = call.getAttributes().get(parameter.getPrimaryName());
            if (fpAttribute != null && fpAttribute.getCondition() != null) {
              alertSink.add(new RequiredAttributeHasCondError(call, fpAttribute));
            }
          }
        }
      }

      return new ValidatedCall(call, callee, newAttrParams);
    }

    @Override
    public Expression visitObjectConstant(ObjectConstant value) {
      if (value.getType() == null) {
        // TODO(harryh): make this a better error
        throw new UnexpectedNodeException(value);
      }
      return value;
    }

    public Void visitUnboundImplementsDeclaration(UnboundImplementsDeclaration uid) {
      throw new UnexpectedNodeException(uid);
    }

    public Void visitBoundImplementsDeclaration(BoundImplementsDeclaration bid) {
      // check for a schema match
      if (!template.getSchema().equals(bid.getImplementable().getSchema())) {
        alertSink.add(new SchemaMismatchError(bid, template.getName().toString()));
      }
      Implementable implementable = bid.getImplementable();

      // check that the number of params match
      // the interface should have one extra param ("this") that the template does not
      if (template.getParameters().size() !=
          implementable.getParameters().size() - 1) {
        alertSink.add(new NumParamsMismatchError(bid,
                                                 implementable.getParameters().size() - 1,
                                                 template.getParameters().size()));
      } else {
        Iterator<FormalParameter> interfaceParams = implementable.getParameters().iterator();
        Iterator<Parameter> templateParams = template.getParameters().iterator();
        while (interfaceParams.hasNext()) {
          FormalParameter interfaceParam = interfaceParams.next();
          if (!Implementable.INSTANCE_PARAM_NAME.equals(interfaceParam.getPrimaryName())) {
            Parameter templateParam = templateParams.next();

            if (!interfaceParam.getPrimaryName().equals(templateParam.getPrimaryName())) {
              alertSink.add(new ParamNameMismatchError(bid, interfaceParam, templateParam));
              continue;
            }

            if (!interfaceParam.getType().matches(templateParam.getType())) {
              alertSink.add(new ParamTypeMismatchError(bid, interfaceParam, templateParam));
              continue;
            }

            if (interfaceParam.hasDefault() && templateParam.getDefaultValue() == null) {
              alertSink.add(new ParamDefaultMismatchError(templateParam));
            }

            if (interfaceParam.hasConstructor() && templateParam.getConstructor() == null) {
              alertSink.add(new ParamConstructorMismatchError(templateParam));
            }
          }
        }
      }

      return null;
    }

    public Void visitNativeImplementsDeclaration(NativeImplementsDeclaration nid) {
      return null;
    }
  }
}
