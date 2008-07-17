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
import com.google.common.collect.ImmutableMap;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.reparent.Attribute;

import java.util.*;

/**
 * Represents a call that has been bound to the {@code Callable} that it calls.
 * {@code BoundCall}s are converted from {@link UnboundCall}s by the {@link
 * com.google.gxp.compiler.bind.Binder Binder}.
 */
public class BoundCall extends Call {
  private final Callable callee;
  private final Expression content;

  public BoundCall(SourcePosition sourcePosition,
                   String displayName,
                   Callable callee,
                   Map<String, Attribute> attributes,
                   List<String> attrBundles,
                   Expression content) {
    super(sourcePosition, displayName, Objects.nonNull(callee.getSchema()),
          attributes, attrBundles);
    this.callee = Objects.nonNull(callee);
    this.content = Objects.nonNull(content);
  }

  public BoundCall(Call fromCall,
                   Callable callee,
                   Map<String, Attribute> attributes,
                   Expression content) {
    super(fromCall.getSourcePosition(), fromCall.getDisplayName(),
          Objects.nonNull(callee.getSchema()), attributes,
          fromCall.getAttrBundles());
    this.callee = Objects.nonNull(callee);
    this.content = Objects.nonNull(content);
  }

  public Callable getCallee() {
    return callee;
  }

  public Expression getContent() {
    return content;
  }

  public BoundCall withParamsAndContent(Map<String, Attribute> newAttributes,
                                        Expression newContent) {
    return (Objects.equal(getAttributes(), newAttributes)
            && Objects.equal(getContent(), newContent))
        ? this
        : new BoundCall(getSourcePosition(), getDisplayName(), callee,
                        newAttributes, getAttrBundles(), newContent);
  }

  @Override
  public Call transformParams(ExhaustiveExpressionVisitor visitor) {
    ImmutableMap.Builder<String, Attribute> mapBuilder = ImmutableMap.builder();
    for (Map.Entry<String, Attribute> param : getAttributes().entrySet()) {
      mapBuilder.put(param.getKey(), visitor.visitAttribute(param.getValue()));
    }
    Expression newContent = getContent().acceptVisitor(visitor);
    return withParamsAndContent(mapBuilder.build(), newContent);
  }

  @Override
  public final <T> T acceptCallVisitor(CallVisitor<T> visitor) {
    return visitor.visitBoundCall(this);
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof BoundCall && equals((BoundCall) that));
  }

  public boolean equals(BoundCall that) {
    return equalsCall(that)
        && Objects.equal(callee, that.callee)
        && Objects.equal(content, that.content);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        callHashCode(),
        callee,
        content);
  }
}
