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
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.schema.Schema;

/**
 * Types that correspond to MIME content types. eg: text/html, text/javascript,
 * etc.
 */
@SuppressWarnings("serial") // let java pick the serialVersionUID
public class ContentType extends Type {
  private final Schema schema;

  public ContentType(SourcePosition sourcePosition,
                     String displayName,
                     Schema schema) {
    super(sourcePosition, displayName);
    this.schema = Objects.nonNull(schema);
  }

  public ContentType(Schema schema) {
    this(schema.getSourcePosition(), schema.getDisplayName(), schema);
  }

  public Schema getSchema() {
    return schema;
  }

  @Override
  public boolean onlyAllowedInParam() {
    return false;
  }

  @Override
  public boolean isContent() {
    return true;
  }

  @Override
  public boolean takesDefaultParam() {
    return true;
  }

  @Override
  public Expression parseObjectConstant(String paramName,
                                        ObjectConstant objectConstant,
                                        AlertSink alertSink) {
    return new StringConstant(objectConstant, null, objectConstant.getValue());
  }

  @Override
  public <T> T acceptTypeVisitor(TypeVisitor<T> visitor) {
    return visitor.visitContentType(this);
  }

  @Override
  public String toString() {
    return schema.getCanonicalContentType();
  }

  @Override
  public boolean matches(Type that) {
    if (that instanceof ContentType) {
      ContentType thatContent = (ContentType)that;
      return Objects.equal(getSchema(), thatContent.getSchema());
    }
    return false;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof ContentType && equals((ContentType) that));
  }

  public boolean equals(ContentType that) {
    return equalsType(that)
        && Objects.equal(getSchema(), that.getSchema());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        typeHashCode(),
        getSchema());
  }
}
