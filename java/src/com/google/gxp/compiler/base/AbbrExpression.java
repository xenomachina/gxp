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
 * A {@code Expression} that declares a variable ("abbreviation"). This is the
 * internal representation of a {@code <gxp:abbr>}.
 */
public class AbbrExpression extends Expression {
  private final Type type;
  private final String name;
  private final Expression value;
  private final Expression content;

  public AbbrExpression(Node fromNode, Type type, String name,
                        Expression value, Expression content) {
    super(fromNode, content.getSchema());
    this.type = Objects.nonNull(type);
    this.name = Objects.nonNull(name);
    this.value = Objects.nonNull(value);
    this.content = Objects.nonNull(content);
  }

  public Type getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public Expression getValue() {
    return value;
  }

  public Expression getContent() {
    return content;
  }

  public AbbrExpression withValueAndContent(Expression newValue, Expression
                                            newContent) {
    return (value.equals(newValue) && content.equals(newContent))
        ? this
        : new AbbrExpression(this, type, name, newValue, newContent);
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitAbbrExpression(this);
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof AbbrExpression && equals((AbbrExpression) that));
  }

  public boolean equals(AbbrExpression that) {
    return equalsExpression(that)
        && Objects.equal(getType(), that.getType())
        && Objects.equal(getName(), that.getName())
        && Objects.equal(getValue(), that.getValue())
        && Objects.equal(getContent(), that.getContent());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        getType(),
        getName(),
        getValue(),
        getContent());
  }
}
