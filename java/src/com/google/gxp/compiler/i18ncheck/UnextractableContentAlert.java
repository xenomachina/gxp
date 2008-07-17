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

package com.google.gxp.compiler.i18ncheck;

import com.google.gxp.compiler.alerts.InfoAlert;
import com.google.gxp.compiler.alerts.SourcePosition;

/**
 * {@code InfoAlert} which indicates that static visible static content which
 * was not marked as extractable for translation (ie: using a {@code <gxp:msg>}
 * element) has been found.
 */
public class UnextractableContentAlert extends InfoAlert {
  public UnextractableContentAlert(SourcePosition sourcePosition,
                                   String displayName) {
    super(sourcePosition, "unextractable " + displayName);
  }
}
