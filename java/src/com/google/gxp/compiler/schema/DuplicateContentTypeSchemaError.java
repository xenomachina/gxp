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

import com.google.gxp.compiler.alerts.ErrorAlert;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Node;

/**
 * An {@code Alert} caused when two different schemas with the same
 * content-type are passed to gxpc.
 */
public class DuplicateContentTypeSchemaError extends ErrorAlert {
  public DuplicateContentTypeSchemaError(SourcePosition pos, String contentTypeName) {
    super(pos, "Duplicate definition for content-type: " + contentTypeName);
  }

  public DuplicateContentTypeSchemaError(Node node, String contentTypeName) {
    this(node.getSourcePosition(), contentTypeName);
  }
}
