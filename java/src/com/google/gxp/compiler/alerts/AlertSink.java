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

/**
 * A "sink" for {@code Alert}s. That is, something you can put {@code Alert}s
 * into.
 */
public interface AlertSink {

  /**
   * Add the Alert to this AlertSink.
   */
  public void add(Alert alert);

  /**
   * Add the Alerts from the specified AlertSet to this AlertSink.
   */
  public void addAll(AlertSet alertSet);
}
