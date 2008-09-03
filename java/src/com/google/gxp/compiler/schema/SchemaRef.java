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

package com.google.gxp.compiler.schema;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.io.Serializable;

/**
 * A reference to a {@code Schema}
 */
@SuppressWarnings("serial")
public class SchemaRef implements Serializable {
  private final String contentType;

  public SchemaRef(String contentType) {
    this.contentType = Preconditions.checkNotNull(contentType);
  }

  public String getContentType() {
    return contentType;
  }

  public Schema resolve(SchemaFactory schemaFactory) {
    return schemaFactory.fromContentTypeName(contentType);
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || ((that instanceof SchemaRef) && equals((SchemaRef) that));
  }

  public boolean equals(SchemaRef that) {
    return Objects.equal(getContentType(), that.getContentType());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        getContentType());
  }
}
