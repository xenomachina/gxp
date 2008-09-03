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
import com.google.gxp.compiler.schema.Schema;

/**
 * Internal representation of {@code <gxp:ph>}, prior to execution of
 * {@link com.google.gxp.compiler.phpivot.PlaceholderPivoter
 * PlaceholderPivoter}.
 */
public class PlaceholderStart extends Expression {
  private final String name;
  private final String example;

  public PlaceholderStart(Node fromNode, Schema schema,
                          String name, String example) {
    super(fromNode, schema);
    this.name = Preconditions.checkNotNull(name);
    this.example = example;
  }

  public String getName() {
    return name;
  }

  public String getExample() {
    return example;
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitPlaceholderStart(this);
  }

  public PlaceholderStart withSchema(Schema newSchema) {
    return Objects.equal(getSchema(), newSchema)
        ? this
        : new PlaceholderStart(this, newSchema, name, example);
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof PlaceholderStart
            && equals((PlaceholderStart) that));
  }

  public boolean equals(PlaceholderStart that) {
    return equalsExpression(that)
        && Objects.equal(getName(), that.getName())
        && Objects.equal(getExample(), that.getExample());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        getName(),
        getExample());
  }
}
