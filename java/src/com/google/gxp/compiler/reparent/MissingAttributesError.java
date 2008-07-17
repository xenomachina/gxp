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

import com.google.common.base.Join;
import com.google.gxp.compiler.alerts.ErrorAlert;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Node;

/**
 * {@link com.google.gxp.compiler.alerts.Alert} which indicates that an
 * element requires at least one of the specified attributes, but none
 * were provided.
 */
public class MissingAttributesError extends ErrorAlert {
  public MissingAttributesError(SourcePosition pos, String displayName,
                                String... attributes) {
    super(pos, displayName + " must have one of the folllowing attributes: "
          + Join.join(", ", attributes) + ".");
  }

  public MissingAttributesError(Node node, String... attributes) {
    this(node.getSourcePosition(), node.getDisplayName(), attributes);
  }
}
