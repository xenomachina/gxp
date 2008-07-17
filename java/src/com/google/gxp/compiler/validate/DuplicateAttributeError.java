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

package com.google.gxp.compiler.validate;

import com.google.gxp.compiler.alerts.ErrorAlert;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Node;

/**
 * An {@link com.google.gxp.compiler.alerts.Alert Alert} that indicates that
 * an {@code OutputElement} was specified with a duplicate attribute.  This
 * could be the result of a {@code gxp:bundle} declaration.
 */
public class DuplicateAttributeError extends ErrorAlert {
  public DuplicateAttributeError(SourcePosition pos, String displayName,
                                 String bundleName, String attrName) {
    super(pos, displayName + " has duplicate attribute " + attrName
          + " from bundle: " + bundleName);
  }

  public DuplicateAttributeError(Node node, String bundleName,
                                 String attrName) {
    this(node.getSourcePosition(), node.getDisplayName(),
         bundleName, attrName);
  }
}
