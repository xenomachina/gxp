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

/**
 * Visitor for {@link Expression}s.
 *
 * @param <T> return type of visitor
 */
public interface ExpressionVisitor<T> extends Function<Expression, T> {
  T visitAbbrExpression(AbbrExpression value);
  T visitAttrBundleParam(AttrBundleParam value);
  T visitBooleanConstant(BooleanConstant value);
  T visitCall(Call value);
  T visitCollapseExpression(CollapseExpression value);
  T visitConcatenation(Concatenation value);
  T visitConditional(Conditional value);
  T visitConstructedConstant(ConstructedConstant value);
  T visitConvertibleToContent(ConvertibleToContent value);
  T visitEscapeExpression(EscapeExpression value);
  T visitExampleExpression(ExampleExpression value);
  T visitExceptionExpression(ExceptionExpression value);
  T visitExtractedMessage(ExtractedMessage value);
  T visitIsXmlExpression(IsXmlExpression value);
  T visitLoopExpression(LoopExpression value);
  T visitNativeExpression(NativeExpression value);
  T visitNoMessage(NoMessage value);
  T visitObjectConstant(ObjectConstant value);
  T visitOutputElement(OutputElement value);
  T visitPlaceholderEnd(PlaceholderEnd value);
  T visitPlaceholderNode(PlaceholderNode value);
  T visitPlaceholderStart(PlaceholderStart value);
  T visitStringConstant(StringConstant value);
  T visitUnextractedMessage(UnextractedMessage value);
}
