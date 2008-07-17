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

package com.google.gxp.compiler.functests.i18n;

import com.google.gxp.testing.BaseFunctionalTestCase;

/**
 * Functional tests related to i18n functionallity in gxp
 */
public class JavaCodeTest extends BaseFunctionalTestCase {
  public void testOneMsg() throws Exception {
    OneMsgGxp.write(out, gxpContext);
    assertOutputEquals("<b>hello, world!</b>");
  }

  public void testNestedMsgs() throws Exception {
    NestedMsgsGxp.write(out, gxpContext);
    assertOutputEquals("baz &lt; bot");
  }

  public void testMessageMetaChars() throws Exception {
    // Tests percent signs in various parts of messages. They're meta-chars in
    // messages, so they're error-prone.
    MessageMetaCharsGxp.write(out, gxpContext, 98.5);
    assertOutputEquals("110% was required but you only gave 98.5%!");
  }

  public void testMsgInCss() throws Exception {
    MsgInCssGxp.write(out, gxpContext);
    assertOutputEquals("<style type=\"text/css\">\n"
                       + "  body {\n"
                       + "    font-family: \"arial\";\n"
                       + "  }\n"
                       + "</style>");
  }

  public void testMsgInPlaintext() throws Exception {
    MsgInPlaintextGxp.write(out, gxpContext);
    assertOutputEquals("hello world\n"
                       + "funny chars: < > ' \"\n"
                       + "funny chars in eval: < > ' \"");
  }

  public void testMsgInScript() throws Exception {
    MsgInScriptGxp.write(out, gxpContext);
    assertOutputEquals("<script type=\"text/javascript\">\n"
                       + "  var x = \"hello world\";\n"
                       + "  var y = \"\\x3cb\\x3ebold\\x3c/b\\x3e\";\n"
                       + "</script>");
  }

  public void testSimplePlaceholder() throws Exception {
    SimplePlaceholderGxp.write(out, gxpContext, "cruel");
    assertOutputEquals("hello <b>cruel</b> world!");

    SimplePlaceholderGxp.write(out, gxpContext, "kind");
    assertOutputEquals("hello <b>kind</b> world!");

    SimplePlaceholderGxp.write(out, gxpContext, "<&>");
    assertOutputEquals("hello <b>&lt;&amp;&gt;</b> world!");
  }

  public void testPlaceholder() throws Exception {
    PlaceholderGxp.write(out, gxpContext, "bob");
    assertOutputEquals("bob");
  }

  public void testPhAttribute() throws Exception {
    PhAttributeGxp.write(out, gxpContext, "http://www.google.com");
    assertOutputEquals("click "
                       + "<a href=\"http://www.google.com\" class=\"foo\" name=\"bar\">"
                       + "here</a>\n"
                       + "foo <br> bar\n"
                       + "<a name=\"foo\"></a>\n"
                       + "bob");
  }

  public void testMsgNamespace() throws Exception {
    MsgNamespaceGxp.write(out, gxpContext);
    assertOutputEquals("<div id=\"foo\"></div>\n<b>bar</b>\n<b>baz</b>");
  }

  public void testNoMsgNamespace() throws Exception {
    NoMsgNamespaceGxp.write(out, gxpContext);
    assertOutputEquals("<div id=\"foo\"></div>\n<b>bar</b>\n<b>baz</b>");
  }

  public void testAttrInsideMsg() throws Exception {
    AttrInsideMsgGxp.write(out, gxpContext);
    assertOutputEquals("<div class=\"foo\"></div>");
  }
}
