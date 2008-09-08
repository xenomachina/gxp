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

import com.google.gxp.compiler.alerts.common.MissingAttributeError;
import com.google.gxp.compiler.alerts.common.MultiValueAttributeError;
import com.google.gxp.compiler.codegen.MissingExpressionError;
import com.google.gxp.compiler.fs.FileRef;

/**
 * Tests of proper error reporting by the GXP compiler relating to
 * it's multi-lingual features.
 */
public class MultiLingualErrorTest extends BaseTestCase {

  private void assertInvalidExpressionAttributeDetected(String prefix,
                                                        String attrName,
                                                        String suffix,
                                                        String element,
                                                        String displyName,
                                                        int errorLine,
                                                        int errorColumn)
      throws Exception {

    // missing attribute
    compile(String.format("%s %s", prefix, suffix));
    assertAlert(new MissingAttributeError(pos(errorLine, errorColumn), element, attrName));
    assertNoUnexpectedAlerts();

    // basic attribute
    compile(String.format("%s %s='e' %s", prefix, attrName, suffix));
    assertNoUnexpectedAlerts();

    // attribute with expr: prefix
    compile(String.format("%s expr:%s='e' %s", prefix, attrName, suffix));
    assertNoUnexpectedAlerts();

    // attribute with java: prefix
    compile(String.format("%s java:%s='e' %s", prefix, attrName, suffix));
    assertAlert(new MissingExpressionError(pos(errorLine, errorColumn), displyName, "JavaScript"));
    assertNoUnexpectedAlerts();

    // basic attribute and with java: prefix
    compile(String.format("%s %s='e' java:%s='e' %s", prefix, attrName, attrName, suffix));
    assertNoUnexpectedAlerts();
  }

  public void testAbbr() throws Exception {
    assertInvalidExpressionAttributeDetected("<gxp:abbr name='x' type='t'", "expr", "/>",
                                             "<gxp:abbr>", "'expr' attribute", 2, 1);
  }

  public void testCond() throws Exception {
    assertInvalidExpressionAttributeDetected("<gxp:cond><gxp:clause", "cond", "/></gxp:cond>",
                                             "<gxp:clause>", "'cond' attribute", 2, 11);
  }

  public void testEval() throws Exception {
    assertInvalidExpressionAttributeDetected("<gxp:eval", "expr", "/>",
                                             "<gxp:eval>", "<gxp:eval>", 2, 1);
  }

  public void testIf() throws Exception {
    assertInvalidExpressionAttributeDetected("<gxp:if", "cond", "/>",
                                             "<gxp:if>", "'cond' attribute", 2, 1);
  }

  public void testCall_NullAndLanguageAttribute() throws Exception {
    FileRef callee = createFile("callee", "<gxp:param name='s' type='String' />");
    FileRef caller = createFile("caller", "<call:callee s='foo' java:s='bar' />");
    compileFiles(callee, caller);
    assertAlert(new MultiValueAttributeError(pos(2,1), "<call:callee>", "'java:s' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_JavaAndCppAttribute() throws Exception {
    FileRef callee = createFile("callee", "<gxp:param name='s' type='String' />");
    FileRef caller = createFile("caller", "<call:callee cpp:s='foo' java:s='bar' />");
    compileFiles(callee, caller);
    assertAlert(new MissingExpressionError(pos(2,1), "'s' attribute", "JavaScript"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_AttrAndLanguageAttribute() throws Exception {
    compile("<div java:class='bar'>",
            "  <gxp:attr name='class'>",
            "    foo",
            "  </gxp:attr>",
            "</div>");
    assertAlert(new MultiValueAttributeError(pos(2,1), "<div>", "'java:class' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_NullAndLanguageAttribute() throws Exception {
    compile("<div class='foo' java:class='bar'/>");
    assertAlert(new MultiValueAttributeError(pos(2,1), "<div>", "'java:class' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_NullExprAndLanguageAttribute() throws Exception {
    compile("<div class='foo' expr:class='bar' java:class='baz'/>");
    assertAlert(new MultiValueAttributeError(pos(2,1), "<div>", "'expr:class' attribute"));
    assertAlert(new MultiValueAttributeError(pos(2,1), "<div>", "'java:class' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_OnlyJavaAttribute() throws Exception {
    compile("<div java:class='baz'/>");
    assertAlert(new MissingExpressionError(pos(2,1), "'class' attribute", "JavaScript"));
    assertNoUnexpectedAlerts();
  }
}
