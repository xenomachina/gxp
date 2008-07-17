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

import com.google.gxp.compiler.alerts.common.BadNodePlacementError;
import com.google.gxp.compiler.alerts.common.MissingAttributeError;
import com.google.gxp.compiler.ifexpand.DoubleElseError;
import com.google.gxp.compiler.ifexpand.ElifAfterElseError;
import com.google.gxp.compiler.reparent.NoClausesInCondError;

/**
 * Tests of proper error reporting by the GXP compiler relating to
 * conditionals.
 */
public class ConditionalErrorTest extends BaseTestCase {
  public void testCond_noClauses() throws Exception {
    compile("<gxp:cond></gxp:cond>");
    assertAlert(new NoClausesInCondError(pos(2,1), "<gxp:cond>"));
    assertNoUnexpectedAlerts();
  }

  public void testCond_oneClauseWithNoCond() throws Exception {
    compile("<gxp:cond>",
            "  <gxp:clause>",
            "  </gxp:clause>",
            "</gxp:cond>");
    assertAlert(new MissingAttributeError(pos(3,3), "<gxp:clause>", "cond"));
    assertNoUnexpectedAlerts();
  }

  public void testCond_multipleClausesWithNoCond() throws Exception {
    compile("<gxp:cond>",
            "  <gxp:clause>",
            "  </gxp:clause>",
            "  <gxp:clause>",
            "  </gxp:clause>",
            "  <gxp:clause>",
            "  </gxp:clause>",
            "</gxp:cond>");
    assertAlert(new MissingAttributeError(pos(3,3), "<gxp:clause>", "cond"));
    assertAlert(new MissingAttributeError(pos(5,3), "<gxp:clause>", "cond"));
    // last clause should not generate an error as it does not require a condition
    // (it's the default clause)
    assertNoUnexpectedAlerts();
  }

  public void testIf_invalidCond() throws Exception {
    assertIllegalExpressionDetected("<gxp:if cond='", "'/>");
  }

  public void testIf_missingCond() throws Exception {
    compile("<gxp:if></gxp:if>");
    assertAlert(new MissingAttributeError(pos(2,1), "<gxp:if>", "cond"));
    assertNoUnexpectedAlerts();
  }

  public void testElif_afterElse() throws Exception {
    compile("<gxp:if cond='false'>",
            "<gxp:else/>",
            "<gxp:elif cond='false' />",
            "</gxp:if>");
    assertAlert(new ElifAfterElseError(pos(4,1), "<gxp:elif>"));
    assertNoUnexpectedAlerts();
  }

  public void testElif_invalidCond() throws Exception {
    assertIllegalExpressionDetected("<gxp:if cond='false'><gxp:elif cond='",
                                    "'/></gxp:if>", 2, 22);
  }

  public void testElif_nonEmpty() throws Exception {
    compile("<gxp:if cond='false'>",
            "<gxp:elif cond='true'>",
            "  some text",
            "</gxp:elif>",
            "</gxp:if>");
    assertAlert(new BadNodePlacementError(pos(3,23), "text",
                                          "inside <gxp:elif>"));
    assertNoUnexpectedAlerts();
  }

  public void testElif_notInIf() throws Exception {
    compile("<gxp:elif cond='false' />");
    assertAlert(new BadNodePlacementError(pos(2,1), "<gxp:elif>", "here"));
    assertNoUnexpectedAlerts();
  }

  public void testElse_multiple() throws Exception {
    compile("<gxp:if cond='false'>",
            "<gxp:else/>",
            "<gxp:else/>",
            "</gxp:if>");
    assertAlert(new DoubleElseError(pos(4,1), "<gxp:else>"));
    assertNoUnexpectedAlerts();
  }

  public void testElse_nonEmpty() throws Exception {
    compile("<gxp:if cond='false'>",
            "<gxp:else>",
            "  some text",
            "</gxp:else>",
            "</gxp:if>");
    assertAlert(new BadNodePlacementError(pos(3,11), "text",
                                          "inside <gxp:else>"));
    assertNoUnexpectedAlerts();
  }

  public void testElse_notInIf() throws Exception {
    compile("<gxp:else/>");
    assertAlert(new BadNodePlacementError(pos(2,1), "<gxp:else>", "here"));
    assertNoUnexpectedAlerts();
  }

  // TODO(laurence): Test <gxp:cond> errors
}
