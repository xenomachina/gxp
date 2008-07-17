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

package com.google.gxp.compiler.alerts;

import com.google.common.base.Objects;
import com.google.gxp.compiler.util.ClassMap;

/**
 * An {@code AlertPolicy} that can be configured to adjust the severity of
 * {@code Alert}s in various ways.
 */
public class ConfigurableAlertPolicy implements AlertPolicy {
  ClassMap<Alert, Alert.Severity> map = ClassMap.create();
  private boolean treatWarningsAsErrors = false;

  public void setSeverity(Class<? extends Alert> cls, Alert.Severity severity) {
    map.put(cls, Objects.nonNull(severity));
  }

  /**
   * When enabled, all warnings will be promoted to errors. Note that the
   * effects of that are applied after the per-class severity, so an {@code
   * Alert} that's made into a warning according to setSeverity will actually
   * be treated as an error if treatWarningsAsErrors is enabled.
   */
  public boolean getTreatWarningsAsErrors() {
    return treatWarningsAsErrors;
  }

  public void setTreatWarningsAsErrors(boolean value) {
    treatWarningsAsErrors = value;
  }

  public Alert.Severity getSeverity(Alert alert) {
    Alert.Severity result = map.get(alert.getClass());
    if (result == null) {
      result = alert.getDefaultSeverity();
    }
    if (treatWarningsAsErrors && result == Alert.Severity.WARNING) {
      result = Alert.Severity.ERROR;
    }
    return result;
  }

  private static final long serialVersionUID = 1L;
}
