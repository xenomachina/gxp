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

package com.google.gxp.compiler.dynamictests;

import com.google.gxp.testing.BaseFunctionalTestCase;

/**
 * Functional tests for {@link com.google.gxp.compiler.java.JavaCodeGenerator
 * JavaCodeGenerator}.  Compiles gxps at runtime.
 */
public class JavaCodeTest extends BaseFunctionalTestCase {

  //////////////////////////////////////////////////////////////////////
  // Hello World Test
  //////////////////////////////////////////////////////////////////////

  public void testHello() throws Exception {
    HelloGxp.write(out, gxpContext);
    assertOutputEquals("hello, world!");
  }

  //////////////////////////////////////////////////////////////////////
  // gxp:msg Tests
  //////////////////////////////////////////////////////////////////////

  public void testOneMsg() throws Exception {
    OneMsgGxp.write(out, gxpContext);
    assertOutputEquals("<b>hello, world!</b>");
  }

  public void testNestedMsgs() throws Exception {
    NestedMsgsGxp.write(out, gxpContext);
    assertOutputEquals("baz &lt; bot");
  }

  public void testSimplePlaceholder() throws Exception {
    SimplePlaceholderGxp.write(out, gxpContext, "cruel");
    assertOutputEquals("hello <b>cruel</b> world!");

    SimplePlaceholderGxp.write(out, gxpContext, "kind");
    assertOutputEquals("hello <b>kind</b> world!");

    SimplePlaceholderGxp.write(out, gxpContext, "<&>");
    assertOutputEquals("hello <b>&lt;&amp;&gt;</b> world!");
  }

  public void testMsgInScript() throws Exception {
    MsgInScriptGxp.write(out, gxpContext, "harryh");
    assertOutputEquals("<script type=\"text/javascript\">\n"
                       + "  var p = \"%\";\n"
                       + "  var n = \"your name is harryh\";\n"
                       + "  var y = \"I can\\x26#39;t believe it\\x26#39;s not butter!"
                       + "\\x3csup\\x3eTM\\x3c/sup\\x3e\";\n"
                       + "</script>");
  }

  public void testJsMsgEscape() throws Exception {
    JsMsgEscapeGxp.write(out, gxpContext);
    assertOutputEquals("<script type=\"text/javascript\">\n"
                       + "  var foo = \"foo\";\n"
                       + "  var bar = \"bar\";\n"
                       + "</script>\n"
                       + "<div onclick=\"alert(&quot;foo \\x22 \\x27&quot;);\"></div>");
  }

  //////////////////////////////////////////////////////////////////////
  // Call Tests
  //////////////////////////////////////////////////////////////////////

  public void testBasicCall() throws Exception {
    BasicCallerGxp.write(out, gxpContext, 0);
    assertOutputEquals("<i><b>0</b></i>");

    BasicCallerGxp.write(out, gxpContext, 12345);
    assertOutputEquals("<i><b>12345</b></i>");
  }

  //////////////////////////////////////////////////////////////////////
  // Exception Tests
  //////////////////////////////////////////////////////////////////////

  public void testThrow() throws Exception {
    try {
      ThrowsGxp.write(out, gxpContext);
      fail("Didn't throw BarException");
    } catch(BarException e) {
      // this should happen
    }
  }

  //////////////////////////////////////////////////////////////////////
  // Instantiable Template Tests
  //////////////////////////////////////////////////////////////////////

  public void testConstructor() throws Exception {
    ConstructorGxp.write(out, gxpContext, "foo", "bar");
    assertOutputEquals("foo\nbar");

    ConstructorGxp.Interface i = new ConstructorGxp.Instance("foo");
    i.write(out, gxpContext, "bar");
    assertOutputEquals("foo\nbar");
  }

  public void testCallGxpInterface() throws Exception {
    StringParamInterfaceCallerGxp.write(out, gxpContext, new StringParamImplGxp.Instance());
    assertOutputEquals("foo=42\nbar=84");
  }

  //////////////////////////////////////////////////////////////////////
  // Permissions Tests
  //////////////////////////////////////////////////////////////////////

  public void testAccess() throws Exception {
    AccessGxp.write(out, gxpContext);
    assertOutputEquals("private data");
  }
}
