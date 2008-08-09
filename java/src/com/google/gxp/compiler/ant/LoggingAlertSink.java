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

package com.google.gxp.compiler.ant;

import com.google.common.base.Objects;
import com.google.gxp.compiler.alerts.Alert;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.alerts.AlertSink;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * An {@link AlertSink} that logs {@code Alert}s to the given {@code Task}.
 */
public class LoggingAlertSink implements AlertSink {
  private final AlertPolicy alertPolicy;
  private final Task task;

  public LoggingAlertSink(AlertPolicy alertPolicy, Task task) {
    this.alertPolicy = Objects.nonNull(alertPolicy);
    this.task = Objects.nonNull(task);
  }

  public void add(Alert alert) {
    switch (alertPolicy.getSeverity(alert)) {
      case INFO:
        task.log(alert.toString(), Project.MSG_VERBOSE);
        break;
      case WARNING:
        task.log(alert.toString(), Project.MSG_WARN);;
        break;
      case ERROR:
        task.log(alert.toString(), Project.MSG_ERR);
        break;
    }
  }

  public void addAll(AlertSet alertSet) {
    for (Alert alert : alertSet) {
      add(alert);
    }
  }
}
