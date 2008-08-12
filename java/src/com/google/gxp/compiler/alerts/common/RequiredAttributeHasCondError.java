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

package com.google.gxp.compiler.alerts.common;

import com.google.gxp.compiler.alerts.ErrorAlert;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.reparent.Attribute;

/**
 * {@link com.google.gxp.compiler.alerts.Alert} which indicates that
 * the template uses <gxp:attr ... cond='...'> for a required parameter
 */
public class RequiredAttributeHasCondError extends ErrorAlert {
  public RequiredAttributeHasCondError(SourcePosition pos, String displayName, String attrName) {
    super(pos, "The '" + attrName + "' attribute is required for " + displayName +
          "; it cannot be conditional.");
  }

  public RequiredAttributeHasCondError(Node node, Attribute attribute) {
    this(node.getSourcePosition(), node.getDisplayName(), attribute.getName());
  }
}
