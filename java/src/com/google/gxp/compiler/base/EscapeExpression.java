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
import com.google.gxp.compiler.schema.Schema;

/**
 * An expression which "escapes" a subexpression.
 */
public class EscapeExpression extends Expression {
  private final Expression subexpression;

  public EscapeExpression(Schema schema, Expression subexpression) {
    super(subexpression, Preconditions.checkNotNull(schema));
    this.subexpression = Preconditions.checkNotNull(subexpression);
  }

  public Expression getSubexpression() {
    return subexpression;
  }

  public Expression withSubexpression(Expression newSubexpression) {
    return newSubexpression.equals(subexpression)
        ? this
        : new EscapeExpression(getSchema(), newSubexpression);
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitEscapeExpression(this);
  }

  @Override
  public boolean alwaysEquals(Expression that) {
    return (that instanceof EscapeExpression)
        && alwaysEquals((EscapeExpression) that);
  }

  protected boolean alwaysEquals(EscapeExpression that) {
    return Objects.equal(getSchema(), that.getSchema())
        && getSubexpression().alwaysEquals(that.getSubexpression());
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof EscapeExpression)
        && equals((EscapeExpression) that);
  }

  public boolean equals(EscapeExpression that) {
    return equalsExpression(that)
        && getSubexpression().equals(that.getSubexpression());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        getSubexpression());
  }
}
