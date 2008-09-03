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
import com.google.common.collect.ImmutableMap;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.reparent.Attribute;

import java.util.*;

/**
 * Represents a call that has been "validated".  {@code ValidatedCall}s are
 * converted from {@link BoundCall}s by the {@link
 * com.google.gxp.compiler.validate.Validator Validator}.
 */
public class ValidatedCall extends Call {
  private final Callable callee;

  public ValidatedCall(SourcePosition sourcePosition,
                       String displayName,
                       Callable callee,
                       Map<String, Attribute> attributes,
                       List<String> attrBundles) {
    super(sourcePosition, displayName, Preconditions.checkNotNull(callee.getSchema()),
          attributes, attrBundles);
    this.callee = Preconditions.checkNotNull(callee);
  }

  public ValidatedCall(Call fromCall,
                       Callable callee,
                       Map<String, Attribute> attributes) {
    super(fromCall.getSourcePosition(), fromCall.getDisplayName(),
          Preconditions.checkNotNull(callee.getSchema()), attributes,
          fromCall.getAttrBundles());
    this.callee = Preconditions.checkNotNull(callee);
  }

  public ValidatedCall withParams(Map<String, Attribute> attributes) {
    return Objects.equal(getAttributes(), attributes)
        ? this
        : new ValidatedCall(getSourcePosition(), getDisplayName(), callee,
                            attributes, getAttrBundles());
  }

  @Override
  public Call transformParams(ExhaustiveExpressionVisitor visitor) {
    ImmutableMap.Builder<String, Attribute> mapBuilder = ImmutableMap.builder();
    for (Map.Entry<String, Attribute> param : getAttributes().entrySet()) {
      mapBuilder.put(param.getKey(), visitor.visitAttribute(param.getValue()));
    }
    return withParams(mapBuilder.build());
  }

  @Override
  public final <T> T acceptCallVisitor(CallVisitor<T> visitor) {
    return visitor.visitValidatedCall(this);
  }

  public Callable getCallee() {
    return callee;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof ValidatedCall && equals((ValidatedCall) that));
  }

  public boolean equals(ValidatedCall that) {
    return equalsCall(that)
        && Objects.equal(getCallee(), that.getCallee());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        callHashCode(),
        getCallee());
  }
}
