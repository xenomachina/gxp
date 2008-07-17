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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gxp.compiler.reparent.Attribute;
import com.google.gxp.compiler.schema.AttributeValidator;
import com.google.gxp.compiler.schema.Schema;

import java.util.*;

/**
 * An Expression representing an Attribute Bundle as a parameter being
 * passed in to a call.
 */
public class AttrBundleParam extends Expression {
  private final ImmutableSet<String> includeAttrs;
  private final ImmutableMap<AttributeValidator, Attribute> attrs;

  // These are attr bundles that are owned by the calling template
  // that are being passed through to the callee template
  private final ImmutableList<String> subBundles;

  public AttrBundleParam(Node fromNode,
                         Schema schema,
                         Set<String> includeAttrs,
                         Map<AttributeValidator, Attribute> attrs,
                         List<String> subBundles) {
    super(fromNode, schema);
    this.includeAttrs = ImmutableSet.copyOf(includeAttrs);
    this.attrs = ImmutableMap.copyOf(attrs);
    this.subBundles = ImmutableList.copyOf(subBundles);
  }

  public AttrBundleParam withAttrs(
      Map<AttributeValidator, Attribute> newAttrs) {
    return new AttrBundleParam(this, this.getSchema(),
                               includeAttrs, newAttrs, subBundles);
  }

  public AttrBundleParam transform(ExhaustiveExpressionVisitor visitor) {
    ImmutableMap.Builder<AttributeValidator, Attribute> builder =
        ImmutableMap.builder();
    for (Map.Entry<AttributeValidator, Attribute> entry : attrs.entrySet()) {
      builder.put(entry.getKey(), visitor.visitAttribute(entry.getValue()));
    }
    return withAttrs(builder.build());
  }

  public Map<AttributeValidator, Attribute> getAttrs() {
    return attrs;
  }

  public Set<String> getIncludeAttrs() {
    return includeAttrs;
  }

  public List<String> getSubBundles() {
    return subBundles;
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitAttrBundleParam(this);
  }

  @Override
  public String toString() {
    return "AttrBundleParam()@" + getSourcePosition();
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof AttrBundleParam)
        && equals((AttrBundleParam) that);
  }

  public boolean equals(AttrBundleParam that) {
    return equalsExpression(that)
        && Objects.equal(getIncludeAttrs(), that.getIncludeAttrs())
        && Objects.equal(getAttrs(), that.getAttrs())
        && Objects.equal(getSubBundles(), that.getSubBundles());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(expressionHashCode(),
                            getIncludeAttrs(),
                            getAttrs(),
                            getSubBundles());
  }
}
