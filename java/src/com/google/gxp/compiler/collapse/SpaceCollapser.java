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

package com.google.gxp.compiler.collapse;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.AttrBundleParam;
import com.google.gxp.compiler.base.Call;
import com.google.gxp.compiler.base.CollapseExpression;
import com.google.gxp.compiler.base.Concatenation;
import com.google.gxp.compiler.base.DefaultingExpressionVisitor;
import com.google.gxp.compiler.base.ExhaustiveExpressionVisitor;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.ExtractedMessage;
import com.google.gxp.compiler.base.NoMessage;
import com.google.gxp.compiler.base.OutputElement;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.SpaceOperator;
import com.google.gxp.compiler.base.SpaceOperatorSet;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.base.UnexpectedNodeException;
import com.google.gxp.compiler.base.UnextractedMessage;
import com.google.gxp.compiler.base.Util;
import com.google.gxp.compiler.bind.BoundTree;
import com.google.gxp.compiler.reparent.Attribute;
import com.google.gxp.compiler.schema.ElementValidator;
import com.google.gxp.compiler.schema.Schema;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs <a
 * href="https://www.corp.google.com/eng/designdocs/gxp/java-rewrite/#new-style-space-collapsing">new-style
 * space collapsing</a>.
 */
public class SpaceCollapser implements Function<BoundTree,SpaceCollapsedTree> {
  public SpaceCollapsedTree apply(BoundTree tree) {
    Root root = tree.getRoot().acceptVisitor(new SearchingVisitor());
    return new SpaceCollapsedTree(tree.getSourcePosition(), tree.getAlerts(), root);
  }

  private static final Pattern LEADING_SPACES =
      Pattern.compile("^(\\s+)(.*?)$", Pattern.DOTALL);
  private static final Pattern TRAILING_SPACES =
      Pattern.compile("^(.*?)(\\s+)$", Pattern.DOTALL);
  private static final Pattern ONLY_SPACES =
      Pattern.compile("^\\s*$", Pattern.DOTALL);
  private static final Pattern SPACES =
      Pattern.compile("\\s+", Pattern.DOTALL);

  // TODO(laurence): add concept of a default SpaceOperatorSet for each
  // Schema, and use that instead of these constants.
  private static final SpaceOperatorSet DEFAULT_SPACE_OPERATORS =
      new SpaceOperatorSet(SpaceOperator.COLLAPSE, SpaceOperator.REMOVE);
  private static final SpaceOperatorSet PRESERVING_SPACE_OPERATORS =
      new SpaceOperatorSet(SpaceOperator.PRESERVE, SpaceOperator.PRESERVE);

  private static final SpaceOperatorSet ATTR_SPACE_OPERATORS =
      new SpaceOperatorSet(SpaceOperator.NORMALIZE, SpaceOperator.REMOVE);

  private static final SpaceOperatorSet MESSAGE_SPACE_OPERATORS =
      new SpaceOperatorSet(SpaceOperator.NORMALIZE, SpaceOperator.REMOVE);

  /**
   * Visitor which does not collapse the current value, but recursively
   * searches for nested values that need collapsing and collapses them.
   */
  private static class SearchingVisitor extends ExhaustiveExpressionVisitor {
    private final SpaceOperatorSet spaceOperators;
    private boolean useSpecialAttrCollapsing = true;

    SearchingVisitor() {
      this(DEFAULT_SPACE_OPERATORS);
    }

    private SearchingVisitor(SpaceOperatorSet spaceOperators) {
      this.spaceOperators = Objects.nonNull(spaceOperators);
    }

    public SearchingVisitor with(SpaceOperatorSet newSpaceOperators) {
      return spaceOperators.equals(newSpaceOperators)
          ? this
          : new SearchingVisitor(newSpaceOperators);
    }

    @Override
    public Expression visitCollapseExpression(CollapseExpression value) {
      SpaceOperatorSet newSpaceOperators =
          value.getSpaceOperators().inheritFrom(spaceOperators);
      SearchingVisitor subVisitor = this.with(newSpaceOperators);
      Expression subExpression =
          value.getSubexpression().acceptVisitor(subVisitor);
      CollapsingVisitor collapsingVisitor =
          new CollapsingVisitor(newSpaceOperators);
      return subExpression.acceptVisitor(collapsingVisitor);
    }

    public Expression visitExtractedMessage(ExtractedMessage msg) {
      throw new UnexpectedNodeException(msg);
    }

    public Expression visitUnextractedMessage(UnextractedMessage msg) {
      SearchingVisitor contentVisitor = this.with(MESSAGE_SPACE_OPERATORS);
      return msg.withContent(
          msg.getContent().acceptVisitor(contentVisitor));
    }

    public Expression visitNoMessage(NoMessage nomsg) {
      SearchingVisitor contentVisitor = this.with(MESSAGE_SPACE_OPERATORS);
      return nomsg.withSubexpression(
          nomsg.getSubexpression().acceptVisitor(contentVisitor));
    }

    @Override
    public Attribute visitAttribute(Attribute attr) {
      // useSpecialAttrCollapsing is used here to decide if ispace should
      // (by default) be normalized.  We do this for Attributes of OutputElements
      // and Attribute Bundles (which are essentially for OutputElements) because
      // we generally don't want \ns in these cases.  We do NOT use alternate
      // collapsing rules for Attributes that belong to calls.
      Expression newValue = useSpecialAttrCollapsing
          ? this.with(ATTR_SPACE_OPERATORS).apply(attr.getValue())
          : apply(attr.getValue());
      Expression newCondition = attr.getCondition();
      if (newCondition != null) {
        newCondition = apply(newCondition);
      }

      return attr.withValue(newValue).withCondition(newCondition);
    }

    @Override
    public Expression visitOutputElement(OutputElement element) {
      boolean oldUseSpecialAttrCollapsing = useSpecialAttrCollapsing;
      useSpecialAttrCollapsing = true;
      ElementValidator validator = element.getValidator();
      SearchingVisitor contentVisitor =
          validator.isFlagSet(ElementValidator.Flag.PRESERVESPACES)
            ? this.with(PRESERVING_SPACE_OPERATORS)
            : this;
      Expression result = element.withAttributesAndContent(
          Util.map(element.getAttributes(), getAttributeFunction()),
          element.getContent().acceptVisitor(contentVisitor));
      useSpecialAttrCollapsing = oldUseSpecialAttrCollapsing;
      return result;
    }

    @Override
    public Expression visitAttrBundleParam(AttrBundleParam bundle) {
      boolean oldUseSpecialAttrCollapsing = useSpecialAttrCollapsing;
      useSpecialAttrCollapsing = true;
      Expression result = super.visitAttrBundleParam(bundle);
      useSpecialAttrCollapsing = oldUseSpecialAttrCollapsing;
      return result;
    }

    @Override
    public Expression visitCall(Call call) {
      boolean oldUseSpecialAttrCollapsing = useSpecialAttrCollapsing;
      useSpecialAttrCollapsing = false;
      Expression result = super.visitCall(call);
      useSpecialAttrCollapsing = oldUseSpecialAttrCollapsing;
      return result;
    }
  }

  /**
   * Visitor which space-collapses an Expression. Should only be invoked on
   * the subexpression of a CollapseExpression, and only collapses the
   * top-level.
   */
  private static class CollapsingVisitor
      extends DefaultingExpressionVisitor<Expression> {
    private final SpaceOperatorSet spaceOperators;

    CollapsingVisitor(SpaceOperatorSet spaceOperators) {
      this.spaceOperators = Objects.nonNull(spaceOperators);
    }

    @Override
    public Expression defaultVisitExpression(Expression value) {
      return value;
    }

    @Override
    public Expression visitCollapseExpression(CollapseExpression value) {
      throw new UnexpectedNodeException(value);
    }

    @Override
    public Expression visitStringConstant(StringConstant value) {
      return Concatenation.create(value.getSourcePosition(),
                                  null,
                                  processConcatenation(
                                      value.getSchema(),
                                      Collections.singletonList(value)));
    }

    @Override
    public Expression visitConcatenation(Concatenation value) {
      return value.withValues(processConcatenation(value.getSchema(),
                                                   value.getValues()));
    }

    private List<Expression> processConcatenation(
        Schema schema, List<? extends Expression> values) {
      StringBuilder sb = new StringBuilder();

      // Create a pair of lists that describe the alternating text and
      // non-text regions. Text segments go at even-numbered positions (0, 2,
      // 4, etc.) while non-text values go at odd numbered positions. There
      // will always be one more text segment than non-text value.
      List<String> textSegments = Lists.newArrayList();
      List<SourcePosition> textPositions = Lists.newArrayList();
      List<Expression> nonTextValues = Lists.newArrayList();
      {
        SourcePosition sbPos = null;
        for (Expression value : values) {
          if (value instanceof StringConstant) {
            StringConstant stringConstant = (StringConstant) value;
            sb.append(stringConstant.evaluate());
            if (sbPos == null) {
              sbPos = stringConstant.getSourcePosition();
            }
          } else {
            textSegments.add(sb.toString());
            sb.setLength(0);
            textPositions.add(sbPos);
            sbPos = null;
            nonTextValues.add(value);
          }
        }
        textSegments.add(sb.toString());
        textPositions.add(sbPos);
      }

      List<Expression> result = Lists.newArrayList();

      SpaceOperator interiorSpaceOperator =
          spaceOperators.getInteriorSpaceOperator();
      SpaceOperator exteriorSpaceOperator =
          spaceOperators.getExteriorSpaceOperator();
      int textSegmentCount = textSegments.size();
      if (textSegmentCount > 0) {
        // pull off leading spaces
        Matcher m = LEADING_SPACES.matcher(textSegments.get(0));
        String leadingSpaces = "";
        if (m.matches()) {
          leadingSpaces = exteriorSpaceOperator.apply(m.group(1));
          textSegments.set(0, m.group(2));
        }

        // pull off trailing spaces
        m = TRAILING_SPACES.matcher(textSegments.get(textSegmentCount - 1));
        String trailingSpaces = "";
        if (m.matches()) {
          trailingSpaces = exteriorSpaceOperator.apply(m.group(2));
          textSegments.set(textSegmentCount - 1, m.group(1));
        }

        // process interior spaces
        for (int i = 0; i < textSegmentCount; i++) {
          String text = textSegments.get(i);
          if (text.length() > 0) {
            sb.setLength(0);
            m = SPACES.matcher(text);
            int start = 0;
            while (true) {
              if (m.find(start)) {
                sb.append(text, start, m.start());
                start = m.end();
                sb.append(interiorSpaceOperator.apply(m.group()));
              } else {
                sb.append(text, start, text.length());
                break;
              }
            }
            textSegments.set(i, sb.toString());
          }
        }

        // re-attach leading and trailing spaces
        textSegments.set(0, leadingSpaces + textSegments.get(0));
        textSegments.set(textSegmentCount - 1,
                         textSegments.get(textSegmentCount - 1)
                         + trailingSpaces);

        for (int i = 0; i < textSegmentCount; i++) {
          String text = textSegments.get(i);
          if (text.length() > 0) {
            result.add(new StringConstant(textPositions.get(i), schema, text));
          }
          if (i < (textSegmentCount - 1)) {
            result.add(nonTextValues.get(i));
          }
        }
      }

      return result;
    }
  }
}
