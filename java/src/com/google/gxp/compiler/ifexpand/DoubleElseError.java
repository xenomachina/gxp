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

package com.google.gxp.compiler.ifexpand;

import com.google.gxp.compiler.alerts.ErrorAlert;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Node;

/**
 * {@link com.google.gxp.compiler.alerts.Alert} which indicates that more
 * that one {@code gxp:else} was found with a {@code gxp:if} node.
 */
public class DoubleElseError extends ErrorAlert {
  public DoubleElseError(SourcePosition pos, String displayName) {
    super(pos, "Only one " + displayName + " allowed per if block.");
  }

  public DoubleElseError(Node node) {
    this(node.getSourcePosition(), node.getDisplayName());
  }
}
