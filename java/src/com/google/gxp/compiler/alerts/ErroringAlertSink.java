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

import com.google.common.base.Preconditions;

/**
 * An {@code AlertSink} that throws an {@code Error} if it recieves any
 * {code Alert}s.
 */
public class ErroringAlertSink implements AlertSink {
  private final AlertPolicy alertPolicy;

  public ErroringAlertSink(AlertPolicy alertPolicy) {
    this.alertPolicy = Preconditions.checkNotNull(alertPolicy);
  }

  public void add(Alert alert) {
    if (alertPolicy.getSeverity(alert) != Alert.Severity.INFO) {
      throw new Error(alert.toString());
    }
  }

  public void addAll(AlertSet alertSet) {
    for (Alert alert : alertSet) {
      add(alert);
    }
  }
}
