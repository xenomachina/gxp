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
 * An abstract base class useful for implementing the typical type of {@link
 * RootVisitor} that transforms certain kinds of {@link Node}s, while leaving
 * other types of {@code Node}s (mostly) alone. Subclasses can override the
 * visit methods for the types of nodes they care about, and the rest will "do
 * the right thing".  That is, they'll exhaustively recurse into subcomponents
 * looking for nodes that need changing, and then rebuild the nodes back up to
 * the root.
 *
 * <p>Note that in most cases you probably want to extend {@link
 * ExhaustiveExpressionVisitor} rather than this class if it is at all possible
 * for {@code Expression}s to show up in your tree of {@code Node}s.
 */
public abstract class ExhaustiveRootVisitor
    implements RootVisitor<Root>, ExpressionVisitor<Expression>{

  public Interface visitInterface(Interface iface) {
    return iface.transformParameters(getParameterTransformer());
  }

  public NullRoot visitNullRoot(NullRoot nullRoot) {
    return nullRoot;
  }

  public Template visitTemplate(Template template) {
    template = template.withContent(template.getContent().acceptVisitor(this));
    template = template.withConstructor(
        template.getConstructor().transformParameters(getParameterTransformer()));
    return template.transformParameters(getParameterTransformer());
  }

  private final Function<Parameter, Parameter> parameterTransformer =
    new Function<Parameter, Parameter>() {
      public Parameter apply(Parameter param) {
          Expression defaultValue = param.getDefaultValue();
          if (defaultValue != null) {
            param = param.withDefaultValue(defaultValue.acceptVisitor(ExhaustiveRootVisitor.this));
          }
          return param.withComment(param.getComment().acceptVisitor(ExhaustiveRootVisitor.this));
        }
    };

  protected Function<Parameter, Parameter> getParameterTransformer() {
    return parameterTransformer;
  }
}
