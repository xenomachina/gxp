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
import com.google.gxp.compiler.alerts.common.RequiredAttributeHasCondError;
import com.google.gxp.compiler.js.LoopRequiresIterableInJavaScriptError;
import com.google.gxp.compiler.reparent.ConflictingAttributesError;
import com.google.gxp.compiler.reparent.MissingAttributesError;

/**
 * Tests of proper error reporting by the GXP compiler relating to loops.
 */
public class LoopErrorTest extends BaseTestCase {
  public void testLoop_bothIteratorAndIterable() throws Exception {
    compile("<gxp:loop var='x' type='int' iterable='foo' iterator='bar'>content</gxp:loop>");
    assertAlert(new ConflictingAttributesError(pos(2,1), "<gxp:loop>",
                                               "'iterable' attribute",
                                               "'iterator' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testLoop_invalidIterable() throws Exception {
    assertIllegalExpressionDetected(
        "<gxp:loop var='x' type='int' iterable='", "'>content</gxp:loop>");
  }

  public void testLoop_invalidIterator() throws Exception {
    testingInvalidIterator = true;
    assertIllegalExpressionDetected(
        "<gxp:loop var='x' type='int' iterator='", "'>content</gxp:loop>");
  }

  private boolean testingInvalidIterator = false;

  @Override
  protected void assertAdditionalAlerts() {
    if (testingInvalidIterator) {
      assertAlert(new LoopRequiresIterableInJavaScriptError(pos(2,1), "<gxp:loop>"));
    }
  }

  public void testLoop_invalidType() throws Exception {
    assertIllegalTypeDetected(
        "<gxp:loop var='x' type='", "' iterable='list' >content</gxp:loop>");
  }

  public void testLoop_invalidGxpType() throws Exception {
    compile("<gxp:loop var='x' gxp:type='bad' iterable='list'>content</gxp:loop>");
    assertAlert(new InvalidAttributeValueError(pos(2,1), "'gxp:type' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testLoop_invalidKey() throws Exception {
    assertIllegalVariableNameDetected(
        "key", "<gxp:loop key='", "' var='v' type='int' iterable='list'>content</gxp:loop>");
  }

  public void testLoop_invalidVar() throws Exception {
    assertIllegalVariableNameDetected(
        "var", "<gxp:loop var='", "' type='int' iterable='list'>content</gxp:loop>");
  }

  public void testLoop_missingIteratorAndIterable() throws Exception {
    compile("<gxp:loop var='x' type='int' />");
    assertAlert(new MissingAttributesError(pos(2,1), "<gxp:loop>", "iterator", "iterable"));
    assertNoUnexpectedAlerts();
  }

  public void testLoop_conditionalDelimiter() throws Exception {
    compile("<gxp:loop var='x' type='int' iterable='list'>",
            "  <gxp:attr name='delimiter' cond='false'>",
            "  </gxp:attr>",
            "</gxp:loop>");
    assertAlert(new RequiredAttributeHasCondError(pos(2,1), "<gxp:loop>", "delimiter"));
    assertNoUnexpectedAlerts();
  }
}
