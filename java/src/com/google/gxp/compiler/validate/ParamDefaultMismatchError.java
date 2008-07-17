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
import com.google.gxp.compiler.base.Parameter;

/**
 * {@code ErrorAlert} indicating that an interface param required a default
 * and the template param didn't provide it.
 */
public class ParamDefaultMismatchError extends ErrorAlert {
  public ParamDefaultMismatchError(SourcePosition pos, String displayName, String paramName) {
    super(pos, displayName + " " + paramName +
          " requires a default and none was set in the template.");
  }

  public ParamDefaultMismatchError(Parameter templateParam) {
    this(templateParam.getSourcePosition(), templateParam.getDisplayName(),
         templateParam.getPrimaryName());
  }
}
