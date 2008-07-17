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

package com.google.gxp.compiler.escape;

import com.google.gxp.compiler.alerts.ErrorAlert;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.schema.Schema;

/**
 * An {@link com.google.gxp.compiler.alerts.Alert Alert} that indicates that an
 * Expression is of the wrong type ({@code ContentType}).
 */
public class TypeError extends ErrorAlert {
  public TypeError(SourcePosition pos, String displayName, String schema,
                   String expectedSchema) {
    super(pos, displayName + " is " + schema + " but expected " + expectedSchema);
  }

  public TypeError(Schema schema, Expression expr) {
    this(expr.getSourcePosition(), expr.getDisplayName(),
         expr.getSchema().toString(), schema.toString());
  }
}
