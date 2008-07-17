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
 * {@link com.google.gxp.compiler.alerts.Alert} which indicates that when
 * an attribute bundle was expanded for an output element there was a mismatch
 * between the validators for a given element.
 *
 * This could happen (for exmaple) if a bundle created from TD was expanded for
 * a DIV.  Both elements have an align attribute, but they accept different
 * values.
 */
public class MismatchedAttributeValidatorsError extends ErrorAlert {
  public MismatchedAttributeValidatorsError(SourcePosition pos,
                                            String displayName,
                                            String attributeName,
                                            String bundleName) {
    super(pos, displayName + " " + attributeName + " is different from the "
          + attributeName + " attribute contained within " + bundleName);
  }

  public MismatchedAttributeValidatorsError(Node node,
                                            String attributeName,
                                            String bundleName) {
    this(node.getSourcePosition(), node.getDisplayName(), attributeName,
         bundleName);
  }
}
