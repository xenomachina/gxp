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

import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.Alert;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.ConfigurableAlertPolicy;
import com.google.gxp.compiler.codegen.CodeGeneratorFactory;
import com.google.gxp.compiler.codegen.DefaultCodeGeneratorFactory;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.i18ncheck.UnextractableContentAlert;

import java.util.*;

/**
 * Tests that {@link UnextractableContentAlert}s are generated when they should
 * be.
 */
public class UnextractableContentAlertTest extends BaseTestCase {
  private final DefaultCodeGeneratorFactory codeGeneratorFactory;
  private final List<String> extraHeaders = Lists.newArrayList();

  public UnextractableContentAlertTest() {
    codeGeneratorFactory = new DefaultCodeGeneratorFactory();
    codeGeneratorFactory.setRuntimeMessageSource("com.google.foo.bar");
  }

  @Override
  protected List<String> extraHeaders() {
    return extraHeaders;
  }

  @Override
  protected CodeGeneratorFactory getCodeGeneratorFactory() {
    return codeGeneratorFactory;
  }

  @Override
  protected AlertPolicy getAlertPolicy() {
    ConfigurableAlertPolicy result = new ConfigurableAlertPolicy();
    result.setSeverity(UnextractableContentAlert.class, Alert.Severity.WARNING);
    return result;
  }

  public void testBase_text() throws Exception {
    compile("hello, world!");
    // position is fragile because text starts right after gxp:template. :-(
    assertAlert(new UnextractableContentAlert(pos(1, 564), "text"));

    assertNoUnexpectedAlerts();
  }

  public void testBase_outputElement() throws Exception {
    compile("<div/>");
    assertNoUnexpectedAlerts();
  }

  public void testBase_whitespace() throws Exception {
    compile(" &nbsp;\n&nbsp; &nbsp; &#160; ");
    assertNoUnexpectedAlerts();
  }

  public void testBase_paramComment() throws Exception {
    compile("<gxp:param name='foo' type='bar'>some comment</gxp:param>");
    assertNoUnexpectedAlerts();
  }

  public void testBase_paramDefault() throws Exception {
    compile("<gxp:param name='foo' type='bar'>",
            "<gxp:attr name='default'>unextractable default value</gxp:attr>",
            "</gxp:param>");
    assertAlert(new UnextractableContentAlert(pos(3, 26), "text"));
    assertNoUnexpectedAlerts();
  }

  public void testBase_outputElementInvisibleAttr() throws Exception {
    compile("<div id='invisible-attribute-value'/>");
    assertNoUnexpectedAlerts();
  }

  public void testBase_outputElementVisibleAttr() throws Exception {
    compile("<td abbr='visible-attribute-value'/>");
    assertAlert(new UnextractableContentAlert(pos(2, 1), "'abbr' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testBase_outputElementVisibleExprAttr() throws Exception {
    compile("<td expr:abbr='\"visible-attribute-value\"'/>");
    assertNoUnexpectedAlerts();
  }

  public void testBase_outputElementVisibleAttrWithGxpAttr() throws Exception {
    compile("<td>",
            "<gxp:attr name='abbr'>",
            "visible-attribute-value",
            "</gxp:attr>",
            "</td>");
    assertAlert(new UnextractableContentAlert(pos(3, 23), "text"));
    assertNoUnexpectedAlerts();
  }

  public void testBase_outputElementVisibleAttrWithMsg() throws Exception {
    compile("<td>",
            "<gxp:attr name='abbr'>",
            "<gxp:msg>",
            "visible-attribute-value",
            "</gxp:msg>",
            "</gxp:attr>",
            "</td>");
    assertNoUnexpectedAlerts();
  }

  public void testBase_outputElementContent() throws Exception {
    compile("<div>foo</div>");
    assertAlert(new UnextractableContentAlert(pos(2, 6), "text"));
    assertNoUnexpectedAlerts();
  }

  public void testBase_outputElementInvisibleContent() throws Exception {
    compile("<script type='text/javascript'>alert('hello');</script>");
    assertNoUnexpectedAlerts();
  }

  public void testBase_callInvisibleBundleAttr() throws Exception {
    compile(
        "<gxp:param name='bundle1' gxp:type='bundle' from-element='div'/>",
        "<" + getMyTagName() + " id='invisible-attribute-value'/>");
    assertNoUnexpectedAlerts();
  }

  public void testBase_callVisibleBundleAttr() throws Exception {
    compile(
        "<gxp:param name='bundle1' gxp:type='bundle' from-element='div'/>",
        "<" + getMyTagName() + " title='visible-attribute-value'/>");
    assertAlert(new UnextractableContentAlert(pos(3, 1), "'title' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testBase_callVisibleExprBundleAttr() throws Exception {
    compile(
        "<gxp:param name='bundle1' gxp:type='bundle' from-element='div'/>",
        "<" + getMyTagName() + " expr:title='\"visible-attribute-value\"'/>");
    assertNoUnexpectedAlerts();
  }

  public void testBase_callVisibleBundleAttrWithGxpAttr() throws Exception {
    compile(
        "<gxp:param name='bundle1' gxp:type='bundle' from-element='div'/>",
        "<" + getMyTagName() + ">",
        "<gxp:attr name='title'>",
        "visible-attribute-value",
        "</gxp:attr>",
        "</" + getMyTagName() + ">");
    assertAlert(new UnextractableContentAlert(pos(4, 24), "text"));
    assertNoUnexpectedAlerts();
  }

  public void testBase_callVisibleBundleAttrWithMsg() throws Exception {
    compile(
        "<gxp:param name='bundle1' gxp:type='bundle' from-element='div'/>",
        "<" + getMyTagName() + ">",
        "<gxp:attr name='title'>",
        "<gxp:msg>",
        "visible-attribute-value",
        "</gxp:msg>",
        "</gxp:attr>",
        "</" + getMyTagName() + ">");
    assertNoUnexpectedAlerts();
  }

  public void testInAbbrExprCss_text() throws Exception {
    compile("<gxp:abbr name='x' content-type='text/css'>",
            "  <gxp:attr name='expr'>",
            "    color: blue;",
            "  </gxp:attr>",
            "</gxp:abbr>");
    assertNoUnexpectedAlerts();
  }

  public void testInCallAttrCss_text() throws Exception {
    FileRef callee = createFile("callee", "<gxp:param name='x' content-type='text/css' />");
    FileRef caller = createFile("caller",
                                "<call:callee>",
                                "  <gxp:attr name='x'>",
                                "    color: blue;",
                                "  </gxp:attr>",
                                "</call:callee>");
    compileFiles(callee, caller);
    assertNoUnexpectedAlerts();
  }

  public void testInParameterDefaultAttrJs_text() throws Exception {
    compile("<gxp:param name='x' content-type='text/javascript'>",
            "  <gxp:attr name='default'>",
            "    someFunction();",
            "  </gxp:attr>",
            "</gxp:param>");
    assertNoUnexpectedAlerts();
  }

  public void testInMsg_text() throws Exception {
    compile("<gxp:msg>hello, world!</gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testInMsg_outputElement() throws Exception {
    compile("<gxp:msg><div/></gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testInMsg_outputElementInvisibleAttr() throws Exception {
    compile("<gxp:msg><div id='invisible-attribute-value'/></gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testInMsg_outputElementVisibleAttr() throws Exception {
    compile("<gxp:msg>",
            "<td abbr='visible-attribute-value'/>",
            "</gxp:msg>");
    assertAlert(new UnextractableContentAlert(pos(3, 1), "'abbr' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testInMsg_outputElementContent() throws Exception {
    compile("<gxp:msg><div>foo</div></gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testInMsg_outputElementInvisibleContent() throws Exception {
    compile("<gxp:msg>",
            "<script type='text/javascript'>alert('hello');</script>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testInNoMsg_text() throws Exception {
    compile("<gxp:nomsg>hello, world!</gxp:nomsg>");
    assertNoUnexpectedAlerts();
  }

  public void testInNoMsg_outputElement() throws Exception {
    compile("<gxp:nomsg><div/></gxp:nomsg>");
    assertNoUnexpectedAlerts();
  }

  public void testInNoMsg_outputElementInvisibleAttr() throws Exception {
    compile("<gxp:nomsg><div id='invisible-attribute-value'/></gxp:nomsg>");
    assertNoUnexpectedAlerts();
  }

  public void testInNoMsg_outputElementVisibleAttr() throws Exception {
    compile("<gxp:nomsg>",
            "<td abbr='visible-attribute-value'/>",
            "</gxp:nomsg>");
    assertNoUnexpectedAlerts();
  }

  public void testInNoMsg_outputElementContent() throws Exception {
    compile("<gxp:nomsg><div>foo</div></gxp:nomsg>");
    assertNoUnexpectedAlerts();
  }

  public void testInNoMsg_outputElementInvisibleContent() throws Exception {
    compile("<gxp:nomsg>",
            "<script type='text/javascript'>alert('hello');</script>",
            "</gxp:nomsg>");
    assertNoUnexpectedAlerts();
  }

  public void testInPlaceholder_text() throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='p'/>hello, world!<gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new UnextractableContentAlert(pos(3, 19), "text"));
    assertNoUnexpectedAlerts();
  }

  public void testInPlaceholder_outputElement() throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='p'/><div/><gxp:eph/>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testInPlaceholder_outputElementInvisibleAttr() throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='p'/>"
            + "<div id='invisible-attribute-value'/><gxp:eph/>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testInPlaceholder_outputElementVisibleAttr() throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='p'/><td abbr='visible-attribute-value'/><gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new UnextractableContentAlert(pos(3, 19), "'abbr' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testInPlaceholder_outputElementContent() throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='p'/><div>foo</div><gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new UnextractableContentAlert(pos(3, 24), "text"));
    assertNoUnexpectedAlerts();
  }

  public void testInPlaceholder_outputElementInvisibleContent()
      throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='p'/><script type='text/javascript'>"
            + "alert('hello');</script><gxp:eph/>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testInMsgInCall_text() throws Exception {
    compile("<gxp:param name='body' content='*' />",
            "",
            "<call:TestInMsgInCall_text>",
            "  text",
            "</call:TestInMsgInCall_text>");
    assertAlert(new UnextractableContentAlert(pos(4, 28), "text"));
    assertNoUnexpectedAlerts();
  }

  public void testInMsgInPlaceholder_text() throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='p' example='x'/>"
            + "<gxp:msg>hello, world!</gxp:msg><gxp:eph/>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testInMsgInPlaceholder_outputElement() throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='p' example='x'/><gxp:msg><div/></gxp:msg><gxp:eph/>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testInMsgInPlaceholder_outputElementInvisibleAttr()
      throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='p' example='x'/>"
            + "<gxp:msg><div id='invisible-attribute-value'/></gxp:msg>"
            + "<gxp:eph/>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testInMsgInPlaceholder_outputElementVisibleAttr()
      throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='p' example='x'/>"
            + "<gxp:msg><td abbr='visible-attribute-value'/></gxp:msg>"
            + "<gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new UnextractableContentAlert(pos(3, 40), "'abbr' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testInMsgInPlaceholder_outputElementContent() throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='p' example='x'/>"
            + "<gxp:msg><div>foo</div></gxp:msg><gxp:eph/>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testInMsgInPlaceholder_outputElementInvisibleContent()
      throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='p' example='x'/>"
            + "<gxp:msg><script type='text/javascript'>"
            + "alert('hello');</script></gxp:msg><gxp:eph/>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testNonMarkup_text() throws Exception {
    extraHeaders.add("content-type='text/javascript'");
    compile("alert('hello, world!');");
    assertNoUnexpectedAlerts();
  }

  public void testNonMarkup_msg() throws Exception {
    extraHeaders.add("content-type='text/javascript'");
    compile("alert(<gxp:msg>Hello, World!</gxp:msg>);",
            "alert(<gxp:msg>Goodbye, World!</gxp:msg>);");
    assertNoUnexpectedAlerts();
  }
}
