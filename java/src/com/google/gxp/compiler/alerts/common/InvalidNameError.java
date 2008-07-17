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

import com.google.common.base.Objects;
import com.google.gxp.compiler.alerts.ErrorAlert;
import com.google.gxp.compiler.alerts.SourcePosition;

/**
 * An {@link com.google.gxp.compiler.alerts.Alert Alert} that indicates that
 * the specified name is invalid.
 */
public class InvalidNameError extends ErrorAlert {
  private final String name;

  public InvalidNameError(SourcePosition sourcePosition, String name) {
    super(sourcePosition, "invalid name \"" + name + "\"");
    this.name = Objects.nonNull(name);
  }

  public String getName() {
    return name;
  }
}
