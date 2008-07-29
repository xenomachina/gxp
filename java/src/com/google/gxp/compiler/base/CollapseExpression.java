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
 * A value that indicates that its subexpression is eligible for space
 * collapsing. Intuitively, all content immediately inside of an XML element
 * will be placed inside of a {@code CollapseExpression} by {@link
 * com.google.gxp.compiler.reparent.Reparenter Reparenter}. Note that space
 * collapsing is purely a compile-time operation -- only the static parts of
 * the subexpression will be altered by this.
 */
public class CollapseExpression extends Expression {
  private final Expression subexpression;
  private final SpaceOperatorSet spaceOperators;

  private CollapseExpression(Expression subexpression,
                             SpaceOperatorSet spaceOperators) {
    super(subexpression);
    this.subexpression = Objects.nonNull(subexpression);
    this.spaceOperators = Objects.nonNull(spaceOperators);
    if (subexpression instanceof CollapseExpression) {
      throw new IllegalArgumentException(
          "CollapseExpressions can't be directly nested");
    }
  }

  public static CollapseExpression create(Expression subexpression,
                                          SpaceOperatorSet spaceOperators) {
    if (subexpression instanceof CollapseExpression) {
      CollapseExpression subCollapse = (CollapseExpression) subexpression;
      spaceOperators =
          subCollapse.getSpaceOperators().inheritFrom(spaceOperators);
      if (spaceOperators.equals(subCollapse.getSpaceOperators())) {
        return subCollapse;
      } else {
        subexpression = subCollapse.getSubexpression();
      }
    }
    return new CollapseExpression(subexpression, spaceOperators);
  }

  public Expression getSubexpression() {
    return subexpression;
  }

  public Expression withSubexpression(Expression newSubexpression) {
    return newSubexpression.equals(subexpression)
        ? this
        : new CollapseExpression(newSubexpression, spaceOperators);
  }

  public SpaceOperatorSet getSpaceOperators() {
    return spaceOperators;
  }

  @Override
  public boolean alwaysOnlyWhitespace() {
    return subexpression.alwaysOnlyWhitespace();
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitCollapseExpression(this);
  }

  @Override
  public boolean hasStaticString() {
    return getSubexpression().hasStaticString();
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof CollapseExpression
            && equals((CollapseExpression) that));
  }

  public boolean equals(CollapseExpression that) {
    return equalsExpression(that)
        && Objects.equal(getSubexpression(), that.getSubexpression())
        && Objects.equal(spaceOperators, that.getSpaceOperators());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        getSubexpression(),
        getSpaceOperators());
  }
}
