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

package com.google.gxp.compiler.reparent;

import com.google.gxp.compiler.alerts.ErrorAlert;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Node;

/**
 * An {@link ErrorAlert} which indicates that a {@code gxp:cond} with
 * no clauses was encountered.
 */
public class NoClausesInCondError extends ErrorAlert {
  /**
   * @param pos the {@code SourcePosition} of the error.
   * @param displayName the display name of the {@code Node} that has an error.
   */
  public NoClausesInCondError(SourcePosition pos, String displayName) {
    super(pos, displayName + " must contain at least one clause.");
  }

  public NoClausesInCondError(Node node) {
    this(node.getSourcePosition(), node.getDisplayName());
  }
}
