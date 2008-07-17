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

package com.google.gxp.compiler.functests.closures;

import com.google.gxp.testing.BaseFunctionalTestCase;

/**
 * Functional tests for closures
 */
public class JavaCodeTest extends BaseFunctionalTestCase {
  public void testCss() throws Exception {
    CssGxp.write(out, gxpContext);
    assertOutputEquals("<style type=\"text/css\">"
                       + "foo { font-size:10 }\n"
                       + "</style>\n"
                       + "<style type=\"text/css\">\n"
                       + "foo { font-size:10 }\n"
                       + "\n</style>");
  }

  public void testJavascript() throws Exception {
    JavascriptGxp.write(out, gxpContext);
    assertOutputEquals("<script type=\"text/javascript\">"
                       + "var foo = \"bar\";\n"
                       + "</script>\n"
                       + "<script type=\"text/javascript\">\n"
                       + "var foo = \"bar\";\n"
                       + "\n</script>");
  }

  public void testPlaintext() throws Exception {
    PlaintextGxp.write(out, gxpContext);
    assertOutputEquals("foo &lt; &amp; &gt;");
  }

  public void testEvalInPlaintext() throws Exception {
    EvalInPlaintextGxp.write(out, gxpContext);
    assertOutputEquals("text\n"
                       + "\u00bb\n"
                       + "string\n"
                       + "1\n"
                       + "2\n"
                       + "3\n"
                       + "4\n"
                       + "5.5\n"
                       + "6.5\n"
                       + "c\n"
                       + "[toString(en_US)]\n"
                       + "foo < & >");
  }
}
