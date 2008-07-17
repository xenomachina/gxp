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

package com.google.gxp.compiler.servicedir;

import com.google.gxp.compiler.alerts.ErrorAlert;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.TemplateName;

/**
 * An {@link com.google.gxp.compiler.alerts.Alert Alert} that indicates that
 * more than one class import with the same base name was found.
 */
public class AmbiguousImportError extends ErrorAlert {
  public AmbiguousImportError(Node node, String baseName,
                              TemplateName import1, TemplateName import2) {
    this(node.getSourcePosition(), baseName, import1.toString(), import2.toString());
  }

  public AmbiguousImportError(SourcePosition pos, String baseName,
                              String import1, String import2) {
    super(pos, "Multiple imports for " + baseName + ": "
          + import1 + " and " + import2);
  }
}
