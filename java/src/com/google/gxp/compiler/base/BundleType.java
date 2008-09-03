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

import com.google.common.base.Join;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.InvalidAttributeValueError;
import com.google.gxp.compiler.schema.AttributeValidator;
import com.google.gxp.compiler.schema.Schema;

import java.util.Map;

/**
 * The bundle type. This is the type of a {@code gxp:param} with {@code
 * gxp:type='bundle'}.
 */
@SuppressWarnings("serial") // let java pick the serialVersionUID
public class BundleType extends Type {
  private final Schema schema;
  private final ImmutableMap<String, AttributeValidator> attrMap;

  public BundleType(SourcePosition pos, String displayName,
                    Schema schema, Map<String, AttributeValidator> attrMap) {
    super(pos, displayName);
    this.schema = Preconditions.checkNotNull(schema);
    this.attrMap = ImmutableMap.copyOf(attrMap);
  }

  public BundleType(Node fromNode, Schema schema, Map<String, AttributeValidator> attrMap) {
    super(fromNode);
    this.schema = Preconditions.checkNotNull(schema);
    this.attrMap = ImmutableMap.copyOf(attrMap);
  }

  public Schema getSchema() {
    return schema;
  }

  public Map<String, AttributeValidator> getAttrMap() {
    return attrMap;
  }

  public AttributeValidator getValidator(String attr) {
    return attrMap.get(attr);
  }

  @Override
  public boolean takesDefaultParam() {
    return false;
  }

  @Override
  public boolean onlyAllowedInParam() {
    return true;
  }

  @Override
  public Expression parseObjectConstant(String paramName,
                                        ObjectConstant oc,
                                        AlertSink alertSink) {

    AttributeValidator validator = getValidator(paramName);
    if (!validator.isValidValue(oc.getValue())) {
      alertSink.add(new InvalidAttributeValueError(oc));
    }

    return (validator.isFlagSet(AttributeValidator.Flag.BOOLEAN))
        ? new BooleanConstant(oc, true)
        : new StringConstant(oc, null, oc.getValue());
  }

  @Override
  public <T> T acceptTypeVisitor(TypeVisitor<T> visitor) {
    return visitor.visitBundleType(this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("AttributeBundle(");
    Join.join(sb, ", ", attrMap.keySet());
    sb.append(")");
    return sb.toString();
  }

  @Override
  public boolean matches(Type that) {
    if (that instanceof BundleType) {
      BundleType thatBundle = (BundleType) that;
      return Objects.equal(getSchema(), thatBundle.getSchema())
          && Objects.equal(getAttrMap(), thatBundle.getAttrMap());
    }
    return false;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof BundleType && equals((BundleType) that));
  }

  public boolean equals(BundleType that) {
    return equalsType(that)
        && Objects.equal(getSchema(), that.getSchema())
        && Objects.equal(getAttrMap(), that.getAttrMap());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        typeHashCode(),
        getSchema(),
        getAttrMap());
  }
}
