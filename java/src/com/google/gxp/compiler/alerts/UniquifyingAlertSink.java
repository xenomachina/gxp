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

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * AlertSink decorator that only alows any given alert to pass
 * through to the delegate once.
 */
public class UniquifyingAlertSink implements AlertSink {
  private final Set<Alert> seenAlerts = Sets.newHashSet();
  private final AlertSink delegate;

  public UniquifyingAlertSink(AlertSink delegate) {
    this.delegate = delegate;
  }

  /**
   * Filters the alert and passes it to the delegate if we haven't
   * seen it before.
   */
  public void add(Alert alert) {
    if (!seenAlerts.contains(alert)) {
      seenAlerts.add(alert);
      delegate.add(alert);
    }
  }

  /**
   * Filters all the alerts and passes remaining ones to the delegate.
   */
  public void addAll(AlertSet alertSet) {
    for (Alert alert : alertSet) {
      add(alert);
    }
  }
}
