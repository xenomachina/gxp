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

package com.google.gxp.compiler.functests.call;

import com.google.gxp.testing.BaseFunctionalTestCase;

/**
 * Functional tests related to calling of one gxp from another
 */
public class JavaCodeTest extends BaseFunctionalTestCase {
  public void testBasicCall() throws Exception {
    BasicCallerGxp.write(out, gxpContext, 0);
    assertOutputEquals("<i><b>0</b></i>");

    BasicCallerGxp.write(out, gxpContext, 12345);
    assertOutputEquals("<i><b>12345</b></i>");
  }

  public void testBooleanParam() throws Exception {
    BooleanParamCallerGxp.write(out, gxpContext);
    assertOutputEquals("yes\n" + "no\n" + "yes\n" + "no");
  }

  public void testCallAsExpression() throws Exception {
    CallAsExpressionGxp.write(out, gxpContext, "x val");
    assertOutputEquals(" X: x val\nY: default y \n X: x val\nY: default y ");
  }

  public void testCallAsEscapableExpression() throws Exception {
    CallAsEscapableExpressionGxp.write(out, gxpContext, "x val");
    assertOutputEquals(
        "<div onclick=\"alert(&quot;x val&quot;); alert(&quot;default y&quot;);\"></div>\n"
        + "<div onclick=\"alert(&quot;x val&quot;); alert(&quot;default y&quot;);\"></div>");
  }

  public void testCallingContentParamsWithDefaults() throws Exception {
    DefaultContentCallerGxp.write(out, gxpContext);
    assertOutputEquals("<h1>Hello, World!</h1>\n"
                       + "<h1><i>Untitled</i></h1>\n"
                       + "<h1><i>Untitled</i></h1>\n"
                       + "<h1>Goodbye, World?</h1>");
  }

  public void testCtorParamMethods() throws Exception {
    CtorParamCalleeGxp.Interface iface = new CtorParamCalleeGxp.Instance();

    assertEquals(CtorParamCalleeGxp.constructS("foo"), "FOO");
    assertEquals(iface.constructS("bar"), "BAR");
  }

  public void testCtorParam() throws Exception {
    CtorParamCallerGxp.write(out, gxpContext);
    assertOutputEquals("foo\n"
                       + "bar\n"
                       + "BAZ\n"
                       + "BUZ");
  }

  public void testCondAttrCaller() throws Exception {
    CondAttrCallerGxp.write(out, gxpContext, true);
    assertOutputEquals("<div id=\"optionalId\">ImaDiv</div>");

    CondAttrCallerGxp.write(out, gxpContext, false);
    assertOutputEquals("<div>ImaDiv</div>");
  }

  public void testCondAttrDefaultParamCaller() throws Exception {
    CondAttrDefaultParamCallerGxp.write(out, gxpContext, 3);
    assertOutputEquals("7");

    CondAttrDefaultParamCallerGxp.write(out, gxpContext, 20);
    assertOutputEquals("20");
  }

  public void testDefaultParamCaller() throws Exception {
    DefaultParamCallerGxp.write(out, gxpContext);
    assertOutputEquals("7\n8\n7\n7\n8\n7");
  }

  public void testDefaultParamMethods() throws Exception {
    DefaultParamCalleeGxp.Interface iface = new DefaultParamCalleeGxp.Instance();

    Integer expected = 7;
    assertEquals(DefaultParamCalleeGxp.getDefaultI(), expected);
    assertEquals(iface.getDefaultI(), expected);
  }

  public void testNewStyleCall() throws Exception {
    NewStyleCallerGxp.write(out, gxpContext, 0);
    assertOutputEquals("<i><b>0</b></i>");

    NewStyleCallerGxp.write(out, gxpContext, 12345);
    assertOutputEquals("<i><b>24690</b></i>");
  }

  public void testNewStyleStringCall() throws Exception {
    NewStyleStringCallerGxp.write(out, gxpContext);
    assertOutputEquals("<i>foo</i>");
  }

  public void testRegex() throws Exception {
    RegexCallerGxp.write(out, gxpContext);
    assertOutputEquals("foo");
  }
}
