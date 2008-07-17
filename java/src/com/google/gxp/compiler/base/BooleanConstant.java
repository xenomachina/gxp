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
 * An Expression representing a constant boolean value (true or false)
 */
public class BooleanConstant extends Expression {
  private final Boolean value;

  public BooleanConstant(Node fromNode, Boolean value) {
    super(fromNode, null);
    this.value = value;
  }

  public Boolean getValue() {
    return value;
  }

  @Override
  public boolean alwaysEquals(Expression that) {
    return (that instanceof BooleanConstant)
        && ((BooleanConstant) that).getValue() == getValue();
  }

  @Override
  public boolean alwaysEquals(boolean thatValue) {
    return this.value == thatValue;
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitBooleanConstant(this);
  }

  @Override
  public String toString() {
    return "BooleanConstant(" + value.toString() + ")@" + getSourcePosition();
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof BooleanConstant) && equals((BooleanConstant) that);
  }

  public boolean equals(BooleanConstant that) {
    return equalsExpression(that)
        && getValue().equals(that.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(expressionHashCode(),
                            getValue());
  }
}
