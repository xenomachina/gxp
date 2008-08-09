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
import com.google.gxp.compiler.alerts.common.NothingToCompileError;
import com.google.gxp.compiler.alerts.common.StaticValueExpectedError;
import com.google.gxp.compiler.alerts.common.UnknownAttributeError;
import com.google.gxp.compiler.escape.TypeError;
import com.google.gxp.compiler.reparent.InvalidRootError;
import com.google.gxp.compiler.validate.ConflictingVarNameError;

/**
 * Basic tests of proper error reporting by the GXP compiler.
 */
public class BasicErrorTest extends BaseTestCase {
  public void testElement_nonTemplateRoot() throws Exception {
    compileNoHeader(
        "<!DOCTYPE gxp:template SYSTEM \"http://gxp.googlecode.com/svn/trunk/resources/xhtml.ent\">",
        "",
        "<html xmlns='http://www.w3.org/1999/xhtml'></html>");
    assertAlert(new InvalidRootError(pos(3,44), "<html>"));
    assertAlert(new NothingToCompileError(pos()));
    assertNoUnexpectedAlerts();

    compileNoHeader(
        "<!DOCTYPE gxp:template SYSTEM \"http://gxp.googlecode.com/svn/trunk/resources/xhtml.ent\">",
        "",
        "<gxp:if xmlns:gxp='http://google.com/2001/gxp' cond='false'></gxp:if>");
    assertAlert(new InvalidRootError(pos(3,61), "<gxp:if>"));
    assertAlert(new NothingToCompileError(pos()));
    assertNoUnexpectedAlerts();
  }

  public void testElement_requiredAttributeMissing() throws Exception {
    compile("<img src='foo.gif'/>");
    assertAlert(new MissingAttributeError(pos(2,1), "<img>", "alt"));
    assertNoUnexpectedAlerts();
  }

  public void testElement_unknownAttribute() throws Exception {
    compile("<div zaphod='hoopy-frood'/>");
    assertAlert(new UnknownAttributeError("<div>", pos(2, 1),
                                          "'zaphod' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testEvalElement_invalidExpr() throws Exception {
    assertIllegalExpressionDetected("<gxp:eval expr='", "'/>");
  }

  public void testEvalElement_nonEmpty() throws Exception {
    compile("<gxp:eval expr='5'>hello</gxp:eval>");
    assertAlert(new BadNodePlacementError(pos(2, 20), "text",
                                          "inside <gxp:eval>"));
    assertNoUnexpectedAlerts();
  }

  public void testAbbr_badContentInExpr() throws Exception {
    compile("<gxp:abbr name='a' content-type='text/plain'>",
            "  <gxp:attr name='expr'>",
            "    <b>foo</b>",
            "  </gxp:attr>",
            "</gxp:abbr>");
    assertAlert(new TypeError(pos(4,5), "<b>", "text/html", "text/plain"));
    assertNoUnexpectedAlerts();
  }

  public void testAbbr_invalidExpr() throws Exception {
    assertIllegalExpressionDetected("<gxp:abbr name='x' type='String' expr='",
                                    "'/>");
  }

  public void testAbbr_invalidName() throws Exception {
    assertIllegalVariableNameDetected(
        "name", "<gxp:abbr name='", "' type='int' expr='1'></gxp:abbr>");
  }

  public void testAbbr_invalidType() throws Exception {
    assertIllegalTypeDetected(
        "<gxp:abbr name='foo' type='", "' expr='1'></gxp:abbr>");
  }

  public void testAbbr_missingExpr() throws Exception {
    compile("<gxp:abbr name='fred' type='Flintstone'/>");
    assertAlert(new MissingAttributeError(pos(2, 1), "<gxp:abbr>", "expr"));
    assertNoUnexpectedAlerts();
  }

  public void testAbbr_conflictingName() throws Exception {
    // test nested <gxp:abbr>s
    compile("<gxp:abbr name='foo' type='int' expr='1'>",
            "  <gxp:abbr name='foo' type='int' expr='2'>",
            "  </gxp:abbr>",
            "</gxp:abbr>");
    assertAlert(new ConflictingVarNameError(pos(3,3), "<gxp:abbr>", "foo"));
    assertNoUnexpectedAlerts();

    // test nested <gxp:abbr>s with different types
    compile("<gxp:abbr name='foo' type='int' expr='1'>",
            "  <gxp:abbr name='foo' type='String' expr='\"whatever\"'>",
            "  </gxp:abbr>",
            "</gxp:abbr>");
    assertAlert(new ConflictingVarNameError(pos(3,3), "<gxp:abbr>", "foo"));
    assertNoUnexpectedAlerts();

    // test triply tested <gxp:abbr>s
    compile("<gxp:abbr name='foo' type='int' expr='1'>",
            "  <gxp:abbr name='bar' type='int' expr='2'>",
            "    <gxp:abbr name='foo' type='int' expr='3'>",
            "    </gxp:abbr>",
            "  </gxp:abbr>",
            "</gxp:abbr>");
    assertAlert(new ConflictingVarNameError(pos(4,5), "<gxp:abbr>", "foo"));
    assertNoUnexpectedAlerts();

    // test <gxp:abbr> that conflicts with <gxp:param>
    compile("<gxp:param name='foo' type='int' />",
            "",
            "<gxp:abbr name='foo' type='int' expr='1'>",
            "</gxp:abbr>");
    assertAlert(new ConflictingVarNameError(pos(4,1), "<gxp:abbr>", "foo"));
    assertNoUnexpectedAlerts();

    // make sure sequential abbrs don't cause an alert
    compile("<gxp:abbr name='foo' type='int' expr='1'>",
            "</gxp:abbr>",
            "<gxp:abbr name='foo' type='int' expr='2'>",
            "</gxp:abbr>");
    assertNoUnexpectedAlerts();
  }

  public void testAttr_condWhenInappropriate() throws Exception {
    // TODO(laurence):
    // self.error("cond is only allowed in an attr that's the immediate child of a html element")
  }

  public void testAttr_insideIf() throws Exception {
    compile("<div>",
            "  <gxp:if cond='true'>",
            "    <gxp:attr name='style'>",
            "      font-weight:bold",
            "    </gxp:attr>",
            "  </gxp:if>",
            "</div>");
    assertAlert(new UnknownAttributeError("<gxp:if>", pos(4,5), "'style' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testElement_misplacedAttrElement() throws Exception {
    compile("<gxp:abbr type='Flintstone' expr='5'>",
            "<gxp:attr name='name'>",
            "int",
            "</gxp:attr>",
            "</gxp:abbr>");
    // TODO(laurence): generate nicer error message. This error has two
    // problems: first, an attr containing only static text sure looks
    // "static". (if we change the code to allow this we should replace the
    // test with one that uses a <gxp:if> inside the attr) Second, the gxp:attr
    // is referred to as "text". Naming the attribute would be better.
    assertAlert(new StaticValueExpectedError(pos(3, 23), "text"));
    assertNoUnexpectedAlerts();
  }

  public void testAttr_invalidCond() throws Exception {
    assertIllegalExpressionDetected("<img><gxp:attr name='alt' cond='",
                                    "'/></img>", 2, 6);
  }
}
