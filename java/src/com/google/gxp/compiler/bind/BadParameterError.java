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

package com.google.gxp.compiler.bind;

import com.google.gxp.compiler.alerts.ErrorAlert;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.TemplateName;

/**
 * An {@link com.google.gxp.compiler.alerts.Alert Alert} that indicates that
 * the callable does not take the specified parameter
 */
public class BadParameterError extends ErrorAlert {
  public BadParameterError(Node fromNode, Callable callee, String paramName) {
    this(fromNode.getSourcePosition(), callee.getName(), paramName);
  }

  public BadParameterError(SourcePosition sourcePosition,
                           TemplateName calleeName, String paramName) {
    super(sourcePosition,
          calleeName + " does not take parameter '" + paramName + "'");
  }
}
