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
import com.google.gxp.compiler.io.RuntimeIOException;

import java.io.IOException;

/**
 * An {@link AlertSink} that prints errors to an {@link Appendable}.
 */
public class PrintingAlertSink implements AlertSink {
  private final AlertPolicy alertPolicy;
  private final boolean verboseEnabled;
  private final Appendable out;

  public PrintingAlertSink(AlertPolicy alertPolicy, boolean verboseEnabled, Appendable out) {
    this.alertPolicy = Objects.nonNull(alertPolicy);
    this.verboseEnabled = verboseEnabled;
    this.out = Objects.nonNull(out);
  }

  public void add(Alert alert) {
    try {
      if (verboseEnabled || alertPolicy.getSeverity(alert) != Alert.Severity.INFO) {
        out.append(alert.toString()).append("\n");
      }
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }

  public void addAll(AlertSet alertSet) {
    for (Alert alert : alertSet) {
      add(alert);
    }
  }
}
