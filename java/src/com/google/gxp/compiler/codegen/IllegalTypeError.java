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

package com.google.gxp.compiler.codegen;

import com.google.gxp.compiler.alerts.ErrorAlert;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.NativeType;
import com.google.gxp.compiler.base.OutputLanguage;

/**
 * {@code ErrorAlert} which indicates that an illegal native type
 * was encountered.
 */
public class IllegalTypeError extends ErrorAlert {
  public IllegalTypeError(SourcePosition pos, String outputLanguage, String type) {
    super(pos, "Illegal " + outputLanguage + " type: " + type);
  }

  public IllegalTypeError(NativeType type, OutputLanguage outputLanguage) {
    this(type.getSourcePosition(), outputLanguage.getDisplay(), type.getNativeType(outputLanguage));
  }
}
