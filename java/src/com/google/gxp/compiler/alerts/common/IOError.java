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
import com.google.gxp.compiler.fs.FileRef;

import java.io.*;

/**
 * An {@code Alert} caused by an {@code IOException}.
 */
public class IOError extends ErrorAlert {
  private final IOException exception;

  public IOError(FileRef source, IOException exception) {
    this(new SourcePosition(source), exception);
  }

  public IOError(SourcePosition sourcePosition, IOException exception) {
    super(sourcePosition,
          (exception.getMessage() == null)
            ? ("io error: " + exception.getClass().getSimpleName())
            : exception.getMessage());
    this.exception = exception;
  }

  public IOException getException() {
    return exception;
  }
}
