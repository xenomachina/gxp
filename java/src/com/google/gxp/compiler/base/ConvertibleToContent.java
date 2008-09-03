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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Prior to the binding phase, indicates that an {@code Expression} can be
 * converted to content. After the binding phase, indicates that the Expression
 * <em>should</em> be converted. The canonical example of where this is used is
 * when passing a parameter to a called template either via a {@code
 * <gxp:attr>} or as the body of the call. If the parameter is a {@code
 * <gxp:eval>} then the ConvertibleToContent lets to code generator know that
 * the expression should be coerced if the parameter is a content parameter.
 */
public class ConvertibleToContent extends Expression {
  private final Expression subexpression;

  public ConvertibleToContent(Expression subexpression) {
    super(subexpression);
    this.subexpression = Preconditions.checkNotNull(subexpression);
  }

  public Expression getSubexpression() {
    return subexpression;
  }

  public Expression withSubexpression(Expression newSubexpression) {
    return newSubexpression.equals(subexpression)
        ? this
        : new ConvertibleToContent(newSubexpression);
  }

  @Override
  public boolean alwaysEmpty() {
    return subexpression.alwaysEmpty();
  }

  @Override
  public boolean alwaysOnlyWhitespace() {
    return subexpression.alwaysOnlyWhitespace();
  }

  @Override
  public boolean alwaysEquals(boolean value) {
    return subexpression.alwaysEquals(value);
  }

  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitConvertibleToContent(this);
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof ConvertibleToContent && equals((ConvertibleToContent) that));
  }

  public boolean equals(ConvertibleToContent that) {
    return equalsExpression(that)
        && Objects.equal(getSubexpression(), that.getSubexpression());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        getSubexpression());
  }
}
