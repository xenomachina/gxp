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
 * Internal representation of a {@code <gxp:nomsg>} element.
 */
public class NoMessage extends Expression {
  private final Expression subexpression;

  public NoMessage(Node fromNode, Expression subexpression) {
    super(fromNode, subexpression.getSchema());
    this.subexpression = Preconditions.checkNotNull(subexpression);
  }

  public Expression getSubexpression() {
    return subexpression;
  }

  public Expression withSubexpression(Expression newSubexpression) {
    return newSubexpression.equals(subexpression)
        ? this
        : new NoMessage(this, newSubexpression);
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
    return visitor.visitNoMessage(this);
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof NoMessage && equals((NoMessage) that));
  }

  public boolean equals(NoMessage that) {
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
