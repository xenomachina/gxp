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

/**
 * Abstract base class useful for creating implementations of
 * {@code ExpressionVisitor} that do the same thing for almost all types of
 * {@code Expression}.
 *
 * @param <T> return type of visitor
 */
public abstract class DefaultingExpressionVisitor<T>
    implements ExpressionVisitor<T> {

  /**
   * Subclasses should override this to perform the default visit operation,
   * and also override any other visit methods where the behavior should
   * deviate from the default.
   */
  protected abstract T defaultVisitExpression(Expression node);

  public T apply(Expression value) {
    return value.acceptVisitor(this);
  }

  public T visitAbbrExpression(AbbrExpression value) {
    return defaultVisitExpression(value);
  }

  public T visitAttrBundleParam(AttrBundleParam value) {
    return defaultVisitExpression(value);
  }

  public T visitBooleanConstant(BooleanConstant value) {
    return defaultVisitExpression(value);
  }

  public T visitCall(Call value) {
    return defaultVisitExpression(value);
  }

  public T visitCollapseExpression(CollapseExpression value) {
    return defaultVisitExpression(value);
  }

  public T visitConcatenation(Concatenation value) {
    return defaultVisitExpression(value);
  }

  public T visitConditional(Conditional value) {
    return defaultVisitExpression(value);
  }

  public T visitConstructedConstant(ConstructedConstant value) {
    return defaultVisitExpression(value);
  }

  public T visitConvertibleToContent(ConvertibleToContent value) {
    return defaultVisitExpression(value);
  }

  public T visitEscapeExpression(EscapeExpression value) {
    return defaultVisitExpression(value);
  }

  public T visitExampleExpression(ExampleExpression value) {
    return defaultVisitExpression(value);
  }

  public T visitExceptionExpression(ExceptionExpression value) {
    return defaultVisitExpression(value);
  }

  public T visitExtractedMessage(ExtractedMessage value) {
    return defaultVisitExpression(value);
  }

  public T visitIsXmlExpression(IsXmlExpression value) {
    return defaultVisitExpression(value);
  }

  public T visitLoopExpression(LoopExpression value) {
    return defaultVisitExpression(value);
  }

  public T visitNativeExpression(NativeExpression value) {
    return defaultVisitExpression(value);
  }

  public T visitNoMessage(NoMessage value) {
    return defaultVisitExpression(value);
  }

  public T visitObjectConstant(ObjectConstant value) {
    return defaultVisitExpression(value);
  }

  public T visitOutputElement(OutputElement value) {
    return defaultVisitExpression(value);
  }

  public T visitPlaceholderEnd(PlaceholderEnd value) {
    return defaultVisitExpression(value);
  }

  public T visitPlaceholderNode(PlaceholderNode value) {
    return defaultVisitExpression(value);
  }

  public T visitPlaceholderStart(PlaceholderStart value) {
    return defaultVisitExpression(value);
  }

  public T visitStringConstant(StringConstant value) {
    return defaultVisitExpression(value);
  }

  public T visitUnextractedMessage(UnextractedMessage value) {
    return defaultVisitExpression(value);
  }
}
