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

import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.BadNodePlacementError;
import com.google.gxp.compiler.alerts.common.InvalidNameError;
import com.google.gxp.compiler.alerts.common.MissingAttributeError;
import com.google.gxp.compiler.alerts.common.MultiValueAttributeError;
import com.google.gxp.compiler.alerts.common.RequiredAttributeHasCondError;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.bind.BadParameterError;
import com.google.gxp.compiler.bind.CallableNotFoundError;
import com.google.gxp.compiler.bind.InvalidParameterFailedRegexError;
import com.google.gxp.compiler.escape.TypeError;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.java.IllegalJavaPrimitiveError;
import com.google.gxp.compiler.scala.IllegalScalaPrimitiveError;

/**
 * Tests of proper error reporting by the GXP compiler relating to calls.
 */
public class CallErrorTest extends BaseTestCase {
  public void testCall_invalidCallee() throws Exception {
    compile("<call:ham-and-eggs/>");
    assertAlert(new InvalidNameError(pos(2, 1), "ham-and-eggs"));
    assertNoUnexpectedAlerts();

    compile("<call:dots..dots/>");
    assertAlert(new InvalidNameError(pos(2, 1), "dots..dots"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_invalidExprAttributes() throws Exception {
    // old style with expr:
    assertIllegalExpressionDetected(
        "<gxp:param type='String' name='x'/><call:" + getTemplateBaseName()
        + " expr:x='", "'/>", 2, 36);

    // new style
    assertIllegalExpressionDetected(
        "<gxp:param type='String' name='x'/><my:" + getTemplateBaseName()
        + " expr:x='", "'/>", 2, 36);
  }

  public void testCall_invalidRegex() throws Exception {
    FileRef callee = createFile(
        "callee", "<gxp:param name='s' type='String' regex='foo' />");
    FileRef caller = createFile("caller", "<my:callee s='bar' />");
    compileFiles(caller, callee);

    assertAlert(new InvalidParameterFailedRegexError(
                    pos(2, 1), "callee", "s", "foo", "bar"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_nonEmptyNonContainer() throws Exception {
    String startCall = "<my:" + getTemplateBaseName() + ">";
    String endCall = "</my:" + getTemplateBaseName() + ">";
    compile(startCall + "stuff inside" + endCall);
    SourcePosition errorPos = pos(2, endCall.length());
    assertAlert(new BadNodePlacementError(errorPos, "text",
                                          "inside " + startCall));
    assertNoUnexpectedAlerts();
  }

  public void testCall_paramHasMultipleValues() throws Exception {
    FileRef callee = createFile("callee", "<gxp:param type='int' name='y'/>");
    FileRef caller = createFile("caller",
                                "<gxp:param type='int' name='x'/>",
                                "<call:callee y='1'>",
                                "  <gxp:attr name='y'>",
                                "    5",
                                "  </gxp:attr>",
                                "</call:callee>",
                                "",
                                "<call:callee expr:y='x'>",
                                "  <gxp:attr name='y'>",
                                "    5",
                                "  </gxp:attr>",
                                "</call:callee>");
    compileFiles(callee, caller);

    assertAlert(new MultiValueAttributeError(pos(4, 3), "<call:callee>", "'y' attribute"));
    assertAlert(new MultiValueAttributeError(pos(10, 3), "<call:callee>", "'y' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_paramIsContentButNotContainer() throws Exception {
    FileRef callee = createFile(
        "callee", "<gxp:param content-type='text/html' name='x'/>");
    FileRef caller = createFile("caller",
                                "<call:callee>foo</call:callee>",
                                "<my:callee>foo</my:callee>");
    compileFiles(callee, caller);
    assertAlert(new BadNodePlacementError(pos(2, 14), "text",
                                          "inside <call:callee>"));
    assertAlert(new MissingAttributeError(pos(2, 1), "<call:callee>", "x"));
    assertAlert(new BadNodePlacementError(pos(3, 12), "text",
                                          "inside <my:callee>"));
    assertAlert(new MissingAttributeError(pos(3, 1), "<my:callee>", "x"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_requiredParamMissing() throws Exception {
    String endCall = "</my:" + getTemplateBaseName() + ">";
    compile("<gxp:param type='String' name='s'/>",
            "<my:" + getTemplateBaseName() + "/>");
    assertAlert(new MissingAttributeError(pos(3, 1),
                                          "<my:" + getTemplateBaseName() + ">",
                                          "s"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_requiredAttributeHasConditional() throws Exception {
    String callee = "<gxp:param type='int' name='i'/>";
    String caller = "<my:" + getTemplateBaseName() + ">" +
        "<gxp:attr name='i' cond='1 > 0'><gxp:eval expr='42' /></gxp:attr>" +
        "</my:" + getTemplateBaseName() + ">";
    compile(callee, caller);
    assertAlert(new RequiredAttributeHasCondError(pos(3, 1),
                                                  "<my:" + getTemplateBaseName() + ">", "i"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_unknownCallee() throws Exception {
    compile("<call:slartibartfast/>");
    assertAlert(new CallableNotFoundError(
        pos(2, 1), TemplateName.create(null, "slartibartfast")));
    assertNoUnexpectedAlerts();
  }

  public void testCall_unknownParam() throws Exception {
    String endCall = "</my:" + getTemplateBaseName() + ">";
    compile("<my:" + getTemplateBaseName() + " s='hello'/>");
    assertAlert(new BadParameterError(pos(2, 1), getTemplateName(), "s"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_nonExprAttributes() throws Exception {
    // good int (2nd one tests with whitespace)
    compile("<gxp:param name='x' type='int' />",
            "<my:TestCall_nonExprAttributes x='1' />",
            "<my:TestCall_nonExprAttributes x=' 1 ' />");
    assertNoUnexpectedAlerts();

    // bad int
    compile("<gxp:param name='x' type='int' />",
            "<my:TestCall_nonExprAttributes x='bad' />");
    assertAlert(new IllegalJavaPrimitiveError(pos(3, 1), "bad", "int"));
    assertAlert(new IllegalScalaPrimitiveError(pos(3, 1), "bad", "int"));
    assertNoUnexpectedAlerts();

    // good char
    compile("<gxp:param name='x' type='char' />",
            "<my:TestCall_nonExprAttributes x='c' />",
            "<my:TestCall_nonExprAttributes x=' ' />");
    assertNoUnexpectedAlerts();

    // bad char
    compile("<gxp:param name='x' type='char' />",
            "<my:TestCall_nonExprAttributes x='bad' />");
    assertAlert(new IllegalJavaPrimitiveError(pos(3, 1), "bad", "char"));
    assertAlert(new IllegalScalaPrimitiveError(pos(3, 1), "bad", "char"));
    assertNoUnexpectedAlerts();

    // good boolean
    compile("<gxp:param name='x' type='boolean' />",
            "<my:TestCall_nonExprAttributes x='true' />",
            "<my:TestCall_nonExprAttributes x='false' />");
    assertNoUnexpectedAlerts();

    // bad boolean
    compile("<gxp:param name='x' type='boolean' />",
            "<my:TestCall_nonExprAttributes x='True' />",
            "<my:TestCall_nonExprAttributes x='TRUE' />",
            "<my:TestCall_nonExprAttributes x='bad' />");
    assertAlert(new IllegalJavaPrimitiveError(pos(3, 1), "True", "boolean"));
    assertAlert(new IllegalJavaPrimitiveError(pos(4, 1), "TRUE", "boolean"));
    assertAlert(new IllegalJavaPrimitiveError(pos(5, 1), "bad", "boolean"));
    assertAlert(new IllegalScalaPrimitiveError(pos(3, 1), "True", "boolean"));
    assertAlert(new IllegalScalaPrimitiveError(pos(4, 1), "TRUE", "boolean"));
    assertAlert(new IllegalScalaPrimitiveError(pos(5, 1), "bad", "boolean"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_badContentType() throws Exception {
    FileRef callee = createFileNoHeader(
        "callee",
        "<!DOCTYPE gxp:template SYSTEM \"http://gxp.googlecode.com/svn/trunk/resources/xhtml.ent\">",
        "<gxp:template name='com.google.gxp.compiler.errortests.callee'",
        "              xmlns:gxp='http://google.com/2001/gxp'",
        "              content-type='text/javascript'>",
        "</gxp:template>");

    // bad call (not on javascript context)
    FileRef caller = createFile("caller", "<call:callee />");
    compileFiles(callee, caller);
    assertAlert(new TypeError(pos(2,1), "<call:callee>", "text/javascript", "text/html"));
    assertNoUnexpectedAlerts();

    // bad call (in an attribute that isn't javascript)
    caller = createFile("caller",
                        "<div>",
                        "  <gxp:attr name='title'>",
                        "    <call:callee />",
                        "  </gxp:attr>",
                        "</div>");
    compileFiles(callee, caller);
    assertAlert(new TypeError(pos(4,5), "<call:callee>", "text/javascript", "text/html"));
    assertNoUnexpectedAlerts();

    // good call (in javascript attribute)
    caller = createFile("caller",
                        "<div>",
                        "  <gxp:attr name='onclick'>",
                        "    <call:callee />",
                        "  </gxp:attr>",
                        "</div>");
    compileFiles(callee, caller);
    assertNoUnexpectedAlerts();
  }

  public void testCall_badContentTypeForCallInParam() throws Exception {
    FileRef innerCallee = createFileNoHeader(
        "innerCallee",
        "<!DOCTYPE gxp:template SYSTEM \"http://gxp.googlecode.com/svn/trunk/resources/xhtml.ent\">",
        "<gxp:template name='com.google.gxp.compiler.errortests.innerCallee'",
        "              xmlns:gxp='http://google.com/2001/gxp'",
        "              content-type='text/javascript'>",
        "</gxp:template>");

    FileRef outerCallee = createFile("outerCallee",
                                     "<gxp:param name='x' content-type='text/html'/>");

    FileRef caller = createFile("caller",
                                "<call:outerCallee>",
                                "  <gxp:attr name='x'>",
                                "    <call:innerCallee/>",
                                "  </gxp:attr>",
                                "</call:outerCallee>");

    compileFiles(innerCallee, outerCallee, caller);
    assertAlert(new TypeError(pos(4,5), "<call:innerCallee>", "text/javascript", "text/html"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_badContentTypeForParam() throws Exception {
    FileRef callee = createFile("callee", "<gxp:param name='x' content-type='text/plain' />");
    FileRef caller = createFile("caller",
                                "<call:callee>",
                                "  <gxp:attr name='x'>",
                                "    <b>foo</b>",
                                "  </gxp:attr>",
                                "</call:callee>");
    compileFiles(callee, caller);

    assertAlert(new TypeError(pos(4,5), "<b>", "text/html", "text/plain"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_multipleBodyAttributes() throws Exception {
    FileRef callee = createFile("callee", "<gxp:param name='body' content='*' />");
    FileRef caller = createFile("caller",
                                "<call:callee expr:body='HtmlClosures.EMPTY' />");
    compileFiles(callee, caller);
    assertNoUnexpectedAlerts();

    caller = createFile("caller",
                        "<call:callee>",
                        "  <gxp:attr name='body'>",
                        "    foo",
                        "  </gxp:attr>",
                        "</call:callee>");
    compileFiles(callee, caller);
    assertNoUnexpectedAlerts();

    caller = createFile("caller",
                        "<call:callee expr:body='HtmlClosures.EMPTY'>",
                        "  foo",
                        "</call:callee>");
    compileFiles(callee, caller);
    assertAlert(new MultiValueAttributeError(pos(2,1), "<call:callee>", "'body' attribute"));
    assertNoUnexpectedAlerts();
  }
}
