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
 * {@link com.google.gxp.compiler.alerts.Alert} which indicates that a
 * node that requires static content was found, but it contained dynamic
 * content.
 */
public class RequiresStaticContentError extends ErrorAlert {
  public RequiresStaticContentError(SourcePosition pos, String displayName) {
    super(pos, displayName + " cannot contain any dynamic content.");
  }

  public RequiresStaticContentError(Node node) {
    this(node.getSourcePosition(), node.getDisplayName());
  }
}
