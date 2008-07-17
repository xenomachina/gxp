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
 * {@code ErrorAlert} indicating that the Nth parameter of a template does not
 * match the Nth parameter of an interface that the template implements.
 */
public class ParamNameMismatchError extends ErrorAlert {
  public ParamNameMismatchError(SourcePosition pos, String interfaceParam,
                                String templateParam) {
    super(pos, "Interface parameter " + interfaceParam + " does not match " +
               "the corresponding template parameter " + templateParam);
  }

  public ParamNameMismatchError(Node implDecl, FormalParameter interfaceParam,
                                Parameter templateParam) {
    this(implDecl.getSourcePosition(), interfaceParam.getPrimaryName(),
         templateParam.getPrimaryName());
  }
}
