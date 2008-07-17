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

import com.google.gxp.compiler.alerts.Alert.Severity;
import static com.google.gxp.compiler.alerts.Alert.Severity.*;

/**
 * Decorator for AlertSinks that count alerts (by severity).
 *
 * Alerts are counted and passed to the delgate alert.
 */
public class AlertCounter implements AlertSink {
  private final int[] counts;
  private final AlertSink delegate;
  private final AlertPolicy policy;

  public AlertCounter(AlertSink delegate, AlertPolicy policy) {
    this.counts = new int[Severity.values().length];
    this.delegate = delegate;
    this.policy = policy;
  }

  /**
   * Counts alerts and passes it to the delegate.
   */
  public void add(Alert alert) {
    countAlert(alert);
    delegate.add(alert);
  }

  /**
   * Counts all alerts and then passes them to the delegate.
   */
  public void addAll(AlertSet alertSet) {
    for (Alert alert : alertSet) {
      countAlert(alert);
    }
    delegate.addAll(alertSet);
  }

  /**
   * Return the number of alerts received that were informational.
   */
  public int getInfoCount() {
    return counts[INFO.ordinal()];
  }

  /**
   * Return the number of alerts received that were warnings.
   */
  public int getWarningCount() {
    return counts[WARNING.ordinal()];
  }

  /**
   * Return the number of alerts that were errors.
   */
  public int getErrorCount() {
    return counts[ERROR.ordinal()];
  }

  private void countAlert(Alert alert) {
    Severity severity = policy.getSeverity(alert);
    counts[severity.ordinal()]++;
  }
}
