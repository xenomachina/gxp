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
 * A looping {@code Expression}. This is the internal representation of a
 * {@code <gxp:loop>}.
 */
public class LoopExpression extends Expression {
  private final Type type;
  private final String var;
  private final Expression iterable;
  private final Expression iterator;
  private final Expression subexpression;
  private final Expression delimiter;

  private LoopExpression(Node fromNode, Type type, String var,
                         Expression iterable, Expression iterator,
                         Expression subexpression, Expression delimiter) {
    super(fromNode, subexpression.getSchema());
    this.type = Objects.nonNull(type);
    this.var = Objects.nonNull(var);
    this.iterable = iterable;
    this.iterator = iterator;
    this.subexpression = Objects.nonNull(subexpression);
    this.delimiter = Objects.nonNull(delimiter);
  }

  public static LoopExpression createWithIterable(Node fromNode, Type type,
                                                  String var,
                                                  Expression iterable,
                                                  Expression subexpression,
                                                  Expression delimiter) {
    return new LoopExpression(fromNode, type, var,
                              Objects.nonNull(iterable), null, subexpression,
                              delimiter);
  }

  public static LoopExpression createWithIterator(Node fromNode, Type type,
                                                  String var,
                                                  Expression iterator,
                                                  Expression subexpression,
                                                  Expression delimiter) {
    return new LoopExpression(fromNode, type, var,
                              null, Objects.nonNull(iterator), subexpression,
                              delimiter);
  }

  public Type getType() {
    return type;
  }

  public String getVar() {
    return var;
  }

  public Expression getIterable() {
    return iterable;
  }

  public Expression getIterator() {
    return iterator;
  }

  public Expression getSubexpression() {
    return subexpression;
  }

  public Expression getDelimiter() {
    return delimiter;
  }

  public LoopExpression withSubexpressionAndDelimiter(
      Expression newSubexpression, Expression newDelimiter) {
    return (Objects.equal(subexpression, newSubexpression)
            && Objects.equal(delimiter, newDelimiter))
        ? this
        : new LoopExpression(this, type, var, iterable, iterator,
                             newSubexpression, newDelimiter);
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitLoopExpression(this);
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof LoopExpression) && equals((LoopExpression) that);
  }

  public boolean equals(LoopExpression that) {
    return equalsExpression(that)
        && Objects.equal(type, that.type)
        && Objects.equal(var, that.var)
        && Objects.equal(iterable, that.iterable)
        && Objects.equal(iterator, that.iterator)
        && Objects.equal(subexpression, that.subexpression)
        && Objects.equal(delimiter, that.delimiter);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        type,
        var,
        iterable,
        iterator,
        subexpression,
        delimiter);
  }
}
