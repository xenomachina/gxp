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

package com.google.gxp.testing;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.CompilationSet;
import com.google.gxp.compiler.alerts.Alert;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.fs.FileRef;

import java.io.*;
import java.util.*;

/**
 * Base TestCase for gxp error testing. Typical protocol for a test is to call
 * compile(), and then assertAlert() for each Alert you expect, and finally
 * assertNoUnexpectedAlerts().
 */
public abstract class BaseErrorTestCase extends BaseBuildingTestCase {
  private AlertSet actualAlerts;
  private Set<Alert> expectedAlerts;
  private AlertSetBuilder alertSetBuilder;

  @Override
  protected CompilationSet compileFiles(Collection<FileRef> gxpFiles) {
    CompilationSet result = super.compileFiles(gxpFiles);

    // TODO(laurence): check alerts from DynamicImplJava separately from Java.
    // They should generate the same alerts.

    // collect actualAlerts and reset expectedAlerts
    actualAlerts = alertSetBuilder.buildAndClear();
    expectedAlerts = Sets.newLinkedHashSet();
    return result;
  }

  @Override
  protected void compileSchemas(FileRef... schemaFiles) {
    super.compileSchemas(schemaFiles);

    // collect actualAlerts and reset expectedAlerts
    actualAlerts = alertSetBuilder.buildAndClear();
    expectedAlerts = Sets.newLinkedHashSet();
  }

  //////////////////////////////////////////////////////////////////////
  // Functions for making assertations about the alerts returned
  // from the most recent compile
  //////////////////////////////////////////////////////////////////////

  protected final void fail(String msg, Alert alert, String suffix) {
    fail(formatAlert(msg, alert) + suffix);
  }

  /**
   * Assert that the given alert is contained within the set recorded.
   */
  protected final void assertAlert(Alert expectedAlert) {
    for (Alert actualAlert : actualAlerts) {
      if (actualAlert.equals(expectedAlert)) {
        expectedAlerts.add(actualAlert);
        return;
      }
    }
    String suffix = "";
    for (Alert unexpectedAlert : unexpectedAlerts()) {
      suffix += "\n" + formatAlert("Found", unexpectedAlert);
    }
    fail("Missing", expectedAlert, suffix);
  }

  /**
   * Asserts that there aren't any unexpected alerts in actualAlerts.
   */
  protected final void assertNoUnexpectedAlerts() {
    if (actualAlerts != null && expectedAlerts != null) {
      for (Alert alert : unexpectedAlerts()) {
        fail("Unexpected", alert, "");
      }
    }
  }

  protected AlertSink createAlertSink() {
    alertSetBuilder = new AlertSetBuilder();
    return alertSetBuilder;
  }

  //////////////////////////////////////////////////////////////////////
  // Functions to create SourcePositions
  //////////////////////////////////////////////////////////////////////

  /**
   * Creates a {@code SourcePosition} for the file that you most recently
   * compiled.
   */
  protected final SourcePosition pos() {
    return new SourcePosition(getSource());
  }

  /**
   * Creates a {@code SourcePosition} for the file that you most recently
   * compiled with the specified line and column.
   */
  protected final SourcePosition pos(int line, int column) {
    return new SourcePosition(getSource(), line, column);
  }

  //////////////////////////////////////////////////////////////////////
  // Private support functions
  //////////////////////////////////////////////////////////////////////

  private String formatAlert(String msg, Alert alert) {
    return String.format("%s: [%s] %s", msg, alert.getClass().getSimpleName(),
                         alert);
  }

  private List<Alert> unexpectedAlerts() {
    AlertPolicy alertPolicy = getAlertPolicy();
    List<Alert> result = Lists.newArrayList();
    for (Alert alert : actualAlerts) {
      if ((alertPolicy.getSeverity(alert) != Alert.Severity.INFO)
          && !expectedAlerts.contains(alert)) {
        result.add(alert);
      }
    }
    return result;
  }
}
