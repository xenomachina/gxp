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

import com.google.gxp.compiler.schema.Schema;

/**
 * Expression used to distinguish SGML vs XML mode
 */
public class IsXmlExpression extends Expression {

  public IsXmlExpression(Node fromNode, Schema schema) {
    super(fromNode, schema);
  }

  @Override
  public boolean alwaysEquals(Expression that) {
    return that instanceof IsXmlExpression;
  }

  @Override
  public boolean alwaysEqualToXmlEnabled() {
    return true;
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitIsXmlExpression(this);
  }

  @Override
  public String toString() {
    return "IsXmlExpression@" + getSourcePosition();
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof IsXmlExpression) && equals((IsXmlExpression) that);
  }

  public boolean equals(IsXmlExpression that) {
    return equalsExpression(that);
  }

  @Override
  public int hashCode() {
    return expressionHashCode();
  }
}
