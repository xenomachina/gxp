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

package com.google.gxp.compiler.i18ncheck;

import com.google.common.base.Preconditions;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.base.AbbrExpression;
import com.google.gxp.compiler.base.AttrBundleParam;
import com.google.gxp.compiler.base.BoundCall;
import com.google.gxp.compiler.base.Call;
import com.google.gxp.compiler.base.CallVisitor;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.ContentType;
import com.google.gxp.compiler.base.DefaultingTypeVisitor;
import com.google.gxp.compiler.base.ExhaustiveExpressionVisitor;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.NoMessage;
import com.google.gxp.compiler.base.OutputElement;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.PlaceholderEnd;
import com.google.gxp.compiler.base.PlaceholderStart;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.Type;
import com.google.gxp.compiler.base.TypeVisitor;
import com.google.gxp.compiler.base.UnboundCall;
import com.google.gxp.compiler.base.UnexpectedNodeException;
import com.google.gxp.compiler.base.UnextractedMessage;
import com.google.gxp.compiler.base.ValidatedCall;
import com.google.gxp.compiler.collapse.SpaceCollapsedTree;
import com.google.gxp.compiler.phpivot.PlaceholderPivotedTree;
import com.google.gxp.compiler.reparent.Attribute;
import com.google.gxp.compiler.schema.AttributeValidator;
import com.google.gxp.compiler.schema.ContentFamily;
import com.google.gxp.compiler.schema.ElementValidator;
import com.google.gxp.compiler.schema.Schema;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Examines a {@code SpaceCollapsedTree} and reports {@code
 * UnextractedContentAlert}s as appropriate.
 */
public class I18nChecker {
  private I18nChecker() {}

  public static final I18nChecker INSTANCE = new I18nChecker();

  public I18nCheckedTree apply(SpaceCollapsedTree tree, PlaceholderPivotedTree pivotedTree) {
    AlertSetBuilder alertSetBuilder = new AlertSetBuilder(pivotedTree.getAlerts());
    tree.getRoot().acceptVisitor(new Visitor(alertSetBuilder));

    return new I18nCheckedTree(pivotedTree.getSourcePosition(),
                               alertSetBuilder.buildAndClear(),
                               pivotedTree.getRoot());
  }

  /**
   * Visits a space collapsed tree and generates
   * {@code UnextractedContentAlert} instances where appropriate.
   */
  private static class Visitor
      extends ExhaustiveExpressionVisitor implements CallVisitor<Expression> {
    private final AlertSink alertSink;
    private boolean visible = true;
    private boolean insideMsg = false;
    private boolean insidePh = false;
    private boolean insideNoMsg = false;

    Visitor(AlertSink alertSink) {
      this.alertSink = Preconditions.checkNotNull(alertSink);
    }

    @Override
    public Template visitTemplate(Template template) {
      boolean oldVisible = visible;

      // We want to examine default values, but we intentionally skip the
      // comments.
      for (Parameter parameter : template.getParameters()) {
        visible = isTypeVisible(parameter.getType());
        Expression defaultValue = parameter.getDefaultValue();
        if (defaultValue != null) {
          defaultValue.acceptVisitor(this);
        }
        visible = oldVisible;
      }

      // TODO(laurence): should probably move this check into
      // visitStringConstant instead, but we'll need access to the unescaped
      // form of the StringConstant.
      visible = isSchemaVisible(template.getSchema());
      template.getContent().acceptVisitor(this);
      visible = oldVisible;
      return template;
    }

    // \xa0 is non-breaking-space (nbsp)
    private static final Pattern LOCALE_INDEPENDENT_PATTERN =
        Pattern.compile("[\\s\\xa0]*");

    /**
     * Return true if and only if the specified string does not need to be
     * translated.
     */
    private static boolean isLocaleIndependent(String s) {
      return LOCALE_INDEPENDENT_PATTERN.matcher(s).matches();
    }

    @Override
    public Expression visitStringConstant(StringConstant node) {
      if (visible && (!insideMsg || insidePh) && !insideNoMsg
          && !isLocaleIndependent(node.evaluate())) {
        alertSink.add(new UnextractableContentAlert(node.getSourcePosition(),
                                                    node.getDisplayName()));
      }
      return node;
    }

    @Override
    public Expression visitAbbrExpression(AbbrExpression abbr) {
      boolean oldVisible = visible;
      visible = isTypeVisible(abbr.getType());
      apply(abbr.getValue());
      visible = oldVisible;

      apply(abbr.getContent());
      return abbr;
    }

    @Override
    public Expression visitCall(Call call) {
      return call.acceptCallVisitor(this);
    }

    @Override
    public Expression visitBoundCall(BoundCall call) {
      boolean oldVisible = visible;
      Callable callee = call.getCallee();
      for (Map.Entry<String, Attribute> param : call.getAttributes().entrySet()) {
        Type type = callee.getParameterByPrimary(param.getKey()).getType();
        visible = isTypeVisible(type);
        visitAttribute(param.getValue());
      }
      visible = oldVisible;
      return call;
    }

    @Override
    public Expression visitUnboundCall(UnboundCall call) {
      throw new UnexpectedNodeException(call);
    }

    @Override
    public Expression visitValidatedCall(ValidatedCall call) {
      throw new UnexpectedNodeException(call);
    }

    @Override
    public Expression visitOutputElement(OutputElement element) {
      boolean oldVisible = visible;
      ElementValidator elementValidator = element.getValidator();
      boolean oldInsideMsg = insideMsg;
      insideMsg = false;
      for (Attribute attr : element.getAttributes()) {
        AttributeValidator attrValidator =
            elementValidator.getAttributeValidator(attr.getName());
        visible = attrValidator.isFlagSet(AttributeValidator.Flag.VISIBLETEXT);
        visitAttribute(attr);
        visible = oldVisible;
      }
      insideMsg = oldInsideMsg;
      visible &= !elementValidator.isFlagSet(ElementValidator.Flag.INVISIBLEBODY);
      apply(element.getContent());
      visible = oldVisible;
      return element;
    }

    @Override
    public Expression visitAttrBundleParam(AttrBundleParam bundle) {
      boolean oldVisible = visible;
      boolean oldInsideMsg = insideMsg;
      insideMsg = false;
      for (Map.Entry<AttributeValidator, Attribute> entry : bundle.getAttrs().entrySet()) {
        AttributeValidator attrValidator = entry.getKey();
        visible = attrValidator.isFlagSet(AttributeValidator.Flag.VISIBLETEXT);
        visitAttribute(entry.getValue());
      }
      visible = oldVisible;
      insideMsg = oldInsideMsg;
      return bundle;
    }

    @Override
    public Expression visitUnextractedMessage(UnextractedMessage msg) {
      boolean oldInsideMsg = insideMsg;
      boolean oldInsidePh = insidePh;
      insideMsg = true;
      insidePh = false;
      super.visitUnextractedMessage(msg);
      insideMsg = oldInsideMsg;
      insidePh = oldInsidePh;
      return msg;
    }

    @Override
    public Expression visitNoMessage(NoMessage noMsg) {
      insideNoMsg = true;
      super.visitNoMessage(noMsg);
      insideNoMsg = false;
      return noMsg;
    }

    @Override
    public Expression visitPlaceholderStart(PlaceholderStart phStart) {
      insidePh = true;
      return super.visitPlaceholderStart(phStart);
    }

    @Override
    public Expression visitPlaceholderEnd(PlaceholderEnd phEnd) {
      insidePh = false;
      return super.visitPlaceholderEnd(phEnd);
    }

    private static boolean isSchemaVisible(Schema schema) {
      return (schema.getContentFamily() == ContentFamily.MARKUP);
    }

    private static boolean isTypeVisible(Type type) {
      return type.acceptTypeVisitor(TYPE_VISITOR);
    }

    /**
     * A {@code TypeVisitor} that indicates that i18n checking should be done
     * for all types except for non markup content-types. Used for call
     * attributes, and default values of {@code Parameter}s.
     */
    private static final TypeVisitor<Boolean> TYPE_VISITOR = new DefaultingTypeVisitor<Boolean>() {
      public Boolean defaultVisitType(Type type) {
        return true;
      }

      public Boolean visitContentType(ContentType type) {
        return isSchemaVisible(type.getSchema());
      }
    };
  }
}
