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

package com.google.gxp.compiler;

import com.google.gxp.compiler.alerts.Alert;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.alerts.Alert.Severity;
import com.google.gxp.compiler.alerts.AlertSink;

import java.util.EnumSet;
import java.util.Set;

/**
 * Alert sink that filters out all alerts of supplied severities.
 *
 * All alerts whose severities are not excluded are passed through to the
 * delegate AlertSink.
 */
public class FilteredAlertSink implements AlertSink {
  private final AlertSink delegate;
  private final AlertPolicy policy;
  private final Set<Severity> excludedSeverities;

  /**
   * Create a new instance.
   *
   * @param delegate the delegate to receive approved alerts.
   * @param first The first excluded severity
   * @param rest The remainin excluded severities
   */
  public FilteredAlertSink(AlertSink delegate, AlertPolicy policy,
      Severity first, Severity... rest) {
    this.delegate = delegate;
    this.policy = policy;
    this.excludedSeverities = EnumSet.of(first, rest);
  }

  /**
   * Add an alert, unless its severity is excluded.
   */
  public void add(Alert alert) {
    if (!excludedSeverities.contains(policy.getSeverity(alert))) {
      delegate.add(alert);
    }
  }

  /**
   * Add all alerts from the {@link AlertSet} whose severity is not excluded.
   */
  public void addAll(AlertSet alertSet) {
    for (Alert alert : alertSet) {
      add(alert);
    }
  }
}
