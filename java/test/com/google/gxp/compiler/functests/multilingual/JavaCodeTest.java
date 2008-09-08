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

package com.google.gxp.compiler.functests.multilingual;

import com.google.gxp.testing.BaseFunctionalTestCase;

/**
 * Functional tests related to GXPs multi-lingual features
 */
public class JavaCodeTest extends BaseFunctionalTestCase {
  public void testEval() throws Exception {
    EvalGxp.write(out, gxpContext);
    assertOutputEquals("You are running: Java\n"
                       + "Is this C++? No\n"
                       + "Is this Java? Yes\n"
                       + "Is this JavaScript? No");
  }

  public void testAbbr() throws Exception {
    AbbrGxp.write(out, gxpContext);
    assertOutputEquals("You are running: Java");
  }

  public void testCond() throws Exception {
    CondGxp.write(out, gxpContext);
    assertOutputEquals("You are running:\nJava");
  }

  public void testIfElse() throws Exception {
    IfElseGxp.write(out, gxpContext);
    assertOutputEquals("You are running:\nJava");
  }

  public void testOutputElement() throws Exception {
    OutputElementGxp.write(out, gxpContext);
    assertOutputEquals("<div class=\"Java\"></div>");
  }

  public void testCaller() throws Exception {
    CallerGxp.write(out, gxpContext);
    assertOutputEquals("You are running: Java\n"
                       + "Is this C++? No\n"
                       + "Is this Java? Yes\n"
                       + "Is this JavaScript? No\n"
                       + "You are running: Java");
  }
}
