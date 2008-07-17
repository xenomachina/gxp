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

/**
 * An {@code Expression} that has been decorated with an example suitable
 * for use in a Placeholder
 */
public class ExampleExpression extends Expression {
  private final Expression subexpression;
  private final String example;

  public ExampleExpression(Expression subexpression, String example) {
    super(subexpression);
    this.subexpression = Objects.nonNull(subexpression);
    this.example = Objects.nonNull(example);
  }

  public Expression withSubexpression(Expression newSubexpression) {
    return newSubexpression.equals(subexpression)
        ? this
        : new ExampleExpression(newSubexpression, example);
  }

  public Expression getSubexpression() {
    return subexpression;
  }

  public String getExample() {
    return example;
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitExampleExpression(this);
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof ExampleExpression)
        && equals((ExampleExpression) that);
  }

  public boolean equals(ExampleExpression that) {
    return equalsExpression(that)
        && subexpression.equals(that.subexpression)
        && example.equals(that.example);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        subexpression,
        example);
  }
}
