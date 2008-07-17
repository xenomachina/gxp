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
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.reparent.Attribute;
import com.google.gxp.compiler.schema.Schema;

import java.util.*;

/**
 * A call. This is the internal representation of an element in the "call"
 * namespace (eg: {@code <call:Foo>}).
 */
public abstract class Call extends Expression {
  private final ImmutableMap<String, Attribute> attributes;
  private final ImmutableList<String> attrBundles;

  public Call(SourcePosition sourcePosition,
              String displayName,
              Schema schema,
              Map<String, Attribute> attributes,
              List<String> attrBundles) {
    super(sourcePosition, displayName, schema);
    this.attributes = ImmutableMap.copyOf(attributes);
    this.attrBundles = ImmutableList.copyOf(attrBundles);
  }

  public Map<String, Attribute> getAttributes() {
    return attributes;
  }

  public List<String> getAttrBundles() {
    return attrBundles;
  }

  public abstract Call transformParams(ExhaustiveExpressionVisitor visitor);

  @Override
  public final <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitCall(this);
  }

  public abstract <T> T acceptCallVisitor(CallVisitor<T> visitor);

  protected boolean equalsCall(Call that) {
    return equalsExpression(that)
        && Objects.equal(getAttributes(), that.getAttributes())
        && Objects.equal(getAttrBundles(), that.getAttrBundles());
  }

  protected int callHashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        getAttributes(),
        getAttrBundles());
  }
}
