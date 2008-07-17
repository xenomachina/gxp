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

import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;

import java.util.regex.Pattern;

/**
 * The boolean type. This is the type of a {@code gxp:param} with {@code
 * gxp:type='boolean'}.
 */
@SuppressWarnings("serial") // let java pick the serialVersionUID
public class BooleanType extends Type {
  public BooleanType(Node fromNode) {
    super(fromNode);
  }

  public BooleanType(SourcePosition sourcePosition, String displayName) {
    super(sourcePosition, displayName);
  }

  @Override
  public Expression getDefaultValue() {
    return new BooleanConstant(this, false);
  }

  @Override
  public Pattern getPattern(String attrName) {
    return Pattern.compile(Pattern.quote(attrName));
  }

  @Override
  public boolean takesDefaultParam() {
    return false;
  }

  @Override
  public boolean onlyAllowedInParam() {
    return false;
  }

  @Override
  public Expression parseObjectConstant(String paramName,
                                        ObjectConstant objectConstant,
                                        AlertSink alertSink) {
    return new BooleanConstant(objectConstant, true);
  }

  @Override
  public <T> T acceptTypeVisitor(TypeVisitor<T> visitor) {
    return visitor.visitBooleanType(this);
  }

  @Override
  public String toString() {
    return "BooleanType";
  }

  @Override
  public boolean matches(Type that) {
    return (that instanceof BooleanType);
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof BooleanType && equals((BooleanType) that));
  }

  public boolean equals(BooleanType that) {
    return equalsType(that);
  }

  @Override
  public int hashCode() {
    return typeHashCode();
  }
}
