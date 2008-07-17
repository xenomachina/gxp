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

package com.google.gxp.compiler.errortests;

import com.google.gxp.compiler.alerts.common.InvalidAttributeValueError;
import com.google.gxp.compiler.alerts.common.MissingAttributeError;
import com.google.gxp.compiler.reparent.MisplacedJavaAnnotationError;

/**
 * Collection of tests of proper error reporting by the GXP compiler relating
 * to annotations.
 */
public class AnnotateErrorTest extends BaseTestCase {
  public void testInvalidElement() throws Exception {
    compile("<java:annotate element='bad' with='@Foo' />");
    assertAlert(new InvalidAttributeValueError(pos(2,1), "'element' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testMisplacedElement() throws Exception {
    compile("<java:annotate element='param' with='@Foo' />");
    assertAlert(new MisplacedJavaAnnotationError(pos(2,1), "param"));
    assertNoUnexpectedAlerts();
  }

  public void testMissingWith() throws Exception {
    compile("<java:annotate />");
    assertAlert(new MissingAttributeError(pos(2,1), "<java:annotate>", "with"));
    assertNoUnexpectedAlerts();
  }

  // TODO(harryh): add code to JavaUtil to test for well formed annotations.
  //               add some tests for bad annotations
}
