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
 * An {@code Expression} representing a reference to an attribute bundle. The
 * internal representation of a bundle referenced by {@code gxp:bundles}.
 */
public class AttrBundleReference extends Expression {
  private final String name;

  public AttrBundleReference(Node fromNode, String name) {
    super(fromNode, null);
    this.name = Objects.nonNull(name);
  }

  public String getName() {
    return name;
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitAttrBundleReference(this);
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof AttrBundleReference) && equals((AttrBundleReference) that);
  }

  public boolean equals(AttrBundleReference that) {
    return equalsExpression(that)
        && Objects.equal(getName(), that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        getName());
  }
}
