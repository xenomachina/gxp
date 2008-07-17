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
 * Internal representation of {@code <gxp:ph>}/{@code <gxp:eph>} pair. One is
 * created from each {@link PlaceholderStart}/{@link PlaceholderEnd} pair by
 * {@link com.google.gxp.compiler.phpivot.PlaceholderPivoter
 * PlaceholderPivoter}.
 */
public class PlaceholderNode extends Expression {
  private final String name;
  private final String example;
  private final Expression content;

  public PlaceholderNode(Node fromNode, String name, String example,
                         Expression content) {
    super(fromNode, content.getSchema());
    this.name = Objects.nonNull(name);
    this.example = Objects.nonNull(example);
    this.content = Objects.nonNull(content);
  }

  public String getName() {
    return name;
  }

  public String getExample() {
    return example;
  }

  public Expression getContent() {
    return content;
  }

  public PlaceholderNode withContent(Expression newContent) {
    return newContent.equals(content)
        ? this
        : new PlaceholderNode(this, name, example, newContent);
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitPlaceholderNode(this);
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof PlaceholderNode && equals((PlaceholderNode) that));
  }

  public boolean equals(PlaceholderNode that) {
    return equalsExpression(that)
        && Objects.equal(getName(), that.getName())
        && Objects.equal(getExample(), that.getExample())
        && Objects.equal(getContent(), that.getContent());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        getName(),
        getExample(),
        getContent());
  }
}
