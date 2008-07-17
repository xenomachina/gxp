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
import com.google.gxp.compiler.base.FormalParameter;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.Parameter;

/**
 * {@code ErrorAlert} indicating that there is a type mismatch between
 * a template and interface parameter.
 */
public class ParamTypeMismatchError extends ErrorAlert {
  public ParamTypeMismatchError(SourcePosition pos, String paramName,
                                String interfaceType, String templateType) {
    super(pos, "Parameter type mismatch for " + paramName + ": "
          + interfaceType + " vs " + templateType);
  }

  public ParamTypeMismatchError(Node implDecl, FormalParameter interfaceParam,
                                Parameter templateParam) {
    this(implDecl.getSourcePosition(), interfaceParam.getPrimaryName(),
         interfaceParam.getType().toString(), templateParam.getType().toString());
  }
}
