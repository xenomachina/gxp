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

import java.util.*;

/**
 * Builder for AlertSets. Note that there are very few "read" methods by
 * design. An AlertSetBuilder is intended to be write-only, except for the
 * {@link #buildAndClear()} method which returns a (by contrast, read-only)
 * AlertSet. This is intended to discourage passing AlertSetBuilders between
 * modules. Instead, pass AlertSets around between modules.
 */
public final class AlertSetBuilder implements AlertSink {
  private LinkedHashSet<Alert> alerts;

  /**
   * Create a clear AlertSetBuilder.
   */
  public AlertSetBuilder() {
    clear();
  }

  /**
   * Create a clear AlertSetBuilder initialized with the {@code Alert}s
   * in {@code alertSet}.
   */
  public AlertSetBuilder(AlertSet alertSet) {
    clear();
    addAll(alertSet);
  }

  /**
   * Clears this AlertSetBuilder. If {@link #buildAndClear()} is called
   * immediately afterwards, an empty AlertSet will be created.
   */
  public void clear() {
    alerts = Sets.newLinkedHashSet();
  }

  /**
   * Builds an AlertSet containing all of the Alerts that were added to this
   * AlertSetBuilder (since it was last cleared), and then clears this
   * AlertSetBuilder.
   */
  public AlertSet buildAndClear() {
    AlertSet result = new AlertSet(alerts);
    clear();
    return result;
  }

  public void addAll(AlertSet alertSet) {
    for (Alert alert : alertSet) {
      add(alert);
    }
  }

  public void add(Alert alert) {
    alerts.add(alert);
  }
}
