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

package com.google.gxp.compiler.functests.bundle;

import com.google.gxp.testing.BaseFunctionalTestCase;

/**
 * Functional tests for attribute bundles
 */
public class JavaCodeTest extends BaseFunctionalTestCase {
  public void testAttrVariations() throws Exception {
    AttrVariationsGxp.write(out, gxpContext);
    assertOutputEquals("<option class=\"theclass\">body</option>\n"
                       + "<option class=\"theclass\">body</option>\n"
                       + "<option class=\"theclass\">body</option>\n"
                       + "<option class=\"theclass\">body</option>\n"
                       + "<option class=\"&lt;&gt;\">body</option>\n"
                       + "<option class=\"&lt;&gt;\">body</option>\n"
                       + "<option class=\"&lt;&gt;\">body</option>\n"
                       + "<option class=\"&lt;&gt;\">body</option>\n"
                       + "<option class=\"&lt;&gt;\">body</option>");
  }

  public void testCaller() throws Exception {
    CallerGxp.write(out, gxpContext, 1);
    assertOutputEquals("<div id=\"1\" class=\"theclass\">body</div>\n"
                       + "<div id=\"2\" class=\"theclass\">body</div>\n"
                       + "<div id=\"3\" onclick=\"alert(&quot;foo&quot;); foo();\">body</div>\n"
                       + "<option selected>body</option>\n"
                       + "<option selected>body</option>\n"
                       + "<option>body</option>\n"
                       + "<div id=\"1\" class=\"theclass\" onclick=\"foo()\">body</div>\n"
                       + "<img alt=\"fluffy the cat\" src=\"fluffy.gif\">");

    // XML Mode
    CallerGxp.write(out, xmlGxpContext, 1);
    assertOutputEquals("<div id=\"1\" class=\"theclass\">body</div>\n"
                       + "<div id=\"2\" class=\"theclass\">body</div>\n"
                       + "<div id=\"3\" onclick=\"alert(&quot;foo&quot;); foo();\">body</div>\n"
                       + "<option selected=\"selected\">body</option>\n"
                       + "<option selected=\"selected\">body</option>\n"
                       + "<option>body</option>\n"
                       + "<div id=\"1\" class=\"theclass\" onclick=\"foo()\">body</div>\n"
                       + "<img alt=\"fluffy the cat\" src=\"fluffy.gif\" />");
  }
}
