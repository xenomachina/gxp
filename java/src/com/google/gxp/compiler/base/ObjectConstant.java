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
 * ObjectConstant is a constant String that will be converted into an object
 * when used as a parameter for a call
 */
public class ObjectConstant extends Expression {
  private final Type type;
  private final String value;

  public ObjectConstant(StringConstant node) {
    this(node, node.evaluate());
  }

  public ObjectConstant(Node fromNode, String value) {
    this(fromNode, value, null);
  }

  public ObjectConstant(Node fromNode, String value, Type type) {
    super(fromNode, null);
    this.value = Preconditions.checkNotNull(value);
    this.type = type;
  }

  public ObjectConstant withType(Type newType) {
    return new ObjectConstant(this, getValue(), newType);
  }

  public Type getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitObjectConstant(this);
  }

  @Override
  public String toString() {
    return getDisplayName() + "=StringConstant@" + getSourcePosition()
        + "(\"" + CharEscapers.JAVA_STRING_ESCAPE.escape(getValue()) + "\")";
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof ObjectConstant) && equals((ObjectConstant) that);
  }

  public boolean equals(ObjectConstant that) {
    return equalsExpression(that)
        && value.equals(that.value)
        && Objects.equal(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        type,
        value);
  }
}
