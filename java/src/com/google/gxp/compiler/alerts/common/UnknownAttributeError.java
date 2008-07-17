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

/**
 * {@link com.google.gxp.compiler.alerts.Alert} which indicates that an
 * unknown attribute has been encountered.
 */
public class UnknownAttributeError extends ErrorAlert {
  /**
   * @param parentNode the attribute's parent {@code Node}.
   * @param attrNode the attribute's {@code Node}.
   */
  public UnknownAttributeError(Node parentNode, Node attrNode) {
    this(parentNode.getDisplayName(), attrNode.getSourcePosition(),
         attrNode.getDisplayName());
  }

  /**
   * @param parentNode the attribute's parent {@code Node}.
   * @param attrName the attribute's display name.
   */
  public UnknownAttributeError(Node parentNode, String attrName) {
    this(parentNode.getDisplayName(), parentNode.getSourcePosition(),
         attrName);
  }

  /**
   * @param parentName the attribute's parent's display name.
   * @param attrPosition the attribute's {@code SourcePositionTest}.
   * @param attrName the attribute's display name.
   */
  public UnknownAttributeError(String parentName, SourcePosition attrPosition,
                               String attrName) {
    super(attrPosition, attrName + " is unknown in " + parentName + ".");
  }
}
