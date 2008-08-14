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

package com.google.gxp.compiler.base;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.reparent.Attribute;

import java.util.*;

/**
 * An abstract base class useful for implementing the typical type of {@link
 * RootVisitor}/{@link ExpressionVisitor} that transforms certain kinds of
 * {@link Node}s, while leaving other types of {@code Node}s (mostly) alone.
 * Subclasses can override the visit methods for the types of nodes they care
 * about, and the rest will "do the right thing".  That is, they'll
 * exhaustively recurse into subcomponents looking for nodes that need
 * changing, and then rebuild the nodes back up to the root.
 */
public abstract class ExhaustiveExpressionVisitor
    extends ExhaustiveRootVisitor {

  public Expression apply(Expression value) {
    return value.acceptVisitor(this);
  }

  /**
   * Allows subclasses to have a global "post-processor" for all Expressions
   * returned by visit methods. The default post-processor behavior is to leave
   * the Expression alone.
   *
   * <p>Note that when overriding visit methods you are responsible for calling
   * postProcess yourself (if it actually needs to be called in your subclass).
   */
  protected Expression postProcess(Expression value) {
    return value;
  }

  public Expression visitAttrBundleParam(AttrBundleParam bundle) {
    return postProcess(bundle.transform(this));
  }

  public Expression visitAttrBundleReference(AttrBundleReference value) {
    return postProcess(value);
  }

  public Expression visitExceptionExpression(ExceptionExpression value) {
    return postProcess(value);
  }

  public Expression visitStringConstant(StringConstant value) {
    return postProcess(value);
  }

  public Expression visitBooleanConstant(BooleanConstant value) {
    return postProcess(value);
  }

  public Expression visitObjectConstant(ObjectConstant value) {
    return postProcess(value);
  }

  public Expression visitConstructedConstant(ConstructedConstant value) {
    return postProcess(value);
  }

  public Expression visitEscapeExpression(EscapeExpression value) {
    return postProcess(
        value.withSubexpression(apply(value.getSubexpression())));
  }

  public Expression visitExampleExpression(ExampleExpression value) {
    return postProcess(
        value.withSubexpression(apply(value.getSubexpression())));
  }

  public Expression visitCollapseExpression(CollapseExpression value) {
    return postProcess(
        value.withSubexpression(apply(value.getSubexpression())));
  }

  public Expression visitNativeExpression(NativeExpression value) {
    return postProcess(value);
  }

  public Expression visitNoMessage(NoMessage value) {
    return postProcess(
        value.withSubexpression(apply(value.getSubexpression())));
  }

  public Expression visitConvertibleToContent(ConvertibleToContent value) {
    return postProcess(
        value.withSubexpression(apply(value.getSubexpression())));
  }

  public Expression visitConcatenation(Concatenation value) {
    return postProcess(value.withValues(Util.map(value.getValues(), this)));
  }

  protected Attribute visitAttribute(Attribute attr) {
    Expression newValue = apply(attr.getValue());
    Expression newCondition = attr.getCondition();
    if (newCondition != null) {
      newCondition = apply(newCondition);
    }

    return attr.withValue(newValue).withCondition(newCondition);
  }

  private final Function<Attribute, Attribute> attrFunction =
      new Function<Attribute, Attribute>() {
        public Attribute apply(Attribute attr) {
          return visitAttribute(attr);
        }
      };

  protected final Function<Attribute, Attribute> getAttributeFunction() {
    return attrFunction;
  }

  public Expression visitOutputElement(OutputElement element) {
    return postProcess(element.withAttributesAndContent(
        Util.map(element.getAttributes(), getAttributeFunction()),
        apply(element.getContent())));
  }

  public Conditional.Clause visitClause(Conditional.Clause clause) {
    return clause.withExpression(apply(clause.getExpression()));
  }

  public Expression visitConditional(Conditional conditional) {
    List<Conditional.Clause> clauses = Lists.newArrayList();
    for (Conditional.Clause clause : conditional.getClauses()) {
      clauses.add(visitClause(clause));
    }
    Expression elseExpression = apply(conditional.getElseExpression());
    return postProcess(conditional.withClauses(clauses, elseExpression));
  }

  public Expression visitLoopExpression(LoopExpression loop) {
    return postProcess(
        loop.withSubexpressionAndDelimiter(apply(loop.getSubexpression()),
                                           apply(loop.getDelimiter())));
  }

  public Expression visitAbbrExpression(AbbrExpression abbr) {
    return postProcess(
        abbr.withValueAndContent(apply(abbr.getValue()),
                                 apply(abbr.getContent())));
  }

  public Expression visitCall(Call call) {
    return postProcess(call.transformParams(this));
  }

  public Expression visitUnextractedMessage(UnextractedMessage msg) {
    return postProcess(
        msg.withContent(apply(msg.getContent())));
  }

  public Expression visitExtractedMessage(ExtractedMessage msg) {
    return postProcess(msg.transformParams(this));
  }

  public Expression visitPlaceholderStart(PlaceholderStart phStart) {
    return postProcess(phStart);
  }

  public Expression visitPlaceholderEnd(PlaceholderEnd phEnd) {
    return postProcess(phEnd);
  }

  public Expression visitPlaceholderNode(PlaceholderNode ph) {
    return postProcess(
        ph.withContent(apply(ph.getContent())));
  }

  public Expression visitIsXmlExpression(IsXmlExpression value) {
    return value;
  }
}
