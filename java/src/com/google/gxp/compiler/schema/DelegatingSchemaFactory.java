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

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * A {@code SchemaFactory} that can returns {@code Schema}s based on a
 * collection of SchemaFactory delegates.  The delegates are queried
 * in order to see if they can supply the requested {@code Schema}.
 */
public class DelegatingSchemaFactory implements SchemaFactory {
  private final List<SchemaFactory> delegates;

  public DelegatingSchemaFactory(SchemaFactory... delegates) {
    this.delegates = ImmutableList.copyOf(delegates);
  }

  public Schema fromNamespaceUri(String nsUri) {
    Schema result = null;
    for (int i = 0; i < delegates.size() && result == null; i++) {
      result = delegates.get(i).fromNamespaceUri(nsUri);
    }
    return result;
  }

  public Schema fromContentTypeName(String contentTypeName) {
    Schema result = null;
    for (int i = 0; i < delegates.size() && result == null; i++) {
      result = delegates.get(i).fromContentTypeName(contentTypeName);
    }
    return result;
  }
}
