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

import com.google.common.base.CharEscapers;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * A ConstructedConstant is a string that will be converted into an object
 * by a function implemented by the callee.
 */
public class ConstructedConstant extends Expression {
  private final String value;
  private final Callable callee;
  private final FormalParameter param;

  public ConstructedConstant(Node fromNode, String value, Callable callee, FormalParameter param) {
    super(fromNode, null);
    this.value = Preconditions.checkNotNull(value);
    this.callee = Preconditions.checkNotNull(callee);
    this.param = Preconditions.checkNotNull(param);
  }

  public String getValue() {
    return value;
  }

  public Callable getCallee() {
    return callee;
  }

  public FormalParameter getParam() {
    return param;
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitConstructedConstant(this);
  }

  @Override
  public String toString() {
    return getDisplayName() + "=ConstructedConstant@" + getSourcePosition()
        + "(\"" + CharEscapers.javaStringEscaper().escape(getValue()) + "\")";
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof ConstructedConstant)
        && equals((ConstructedConstant) that);
  }

  public boolean equals(ConstructedConstant that) {
    return equalsExpression(that)
        && value.equals(that.value)
        && callee.equals(that.callee)
        && param.equals(that.param);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        value,
        callee,
        param);
  }
}
