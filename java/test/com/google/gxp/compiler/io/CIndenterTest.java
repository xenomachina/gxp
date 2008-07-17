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

package com.google.gxp.compiler.io;

import junit.framework.TestCase;

import java.util.*;

/**
 * Tests {@link CIndenter}
 */
public class CIndenterTest extends TestCase {
  private StringBuilder sb = new StringBuilder();
  private CIndenter ci = new CIndenter(sb, "public:", "private:");

  public void testBase() throws Exception {
    ci.appendLine("foo();");
    assertEquals("foo();\n",
                 sb.toString());
  }

  public void testIndent() throws Exception {
    ci.appendLine("if (something) {");
    ci.appendLine("doIt();");
    assertEquals("if (something) {\n" +
                 "  doIt();\n",
                 sb.toString());
  }

  public void testIndentOutdent() throws Exception {
    ci.appendLine("if (something) {");
    ci.appendLine("doIt();");
    ci.appendLine("}");
    ci.appendLine("return 5;");
    assertEquals("if (something) {\n" +
                 "  doIt();\n" +
                 "}\n" +
                 "return 5;\n",
                 sb.toString());
  }

  public void testBlankLineInIndentNotIndented() throws Exception {
    ci.appendLine("if (x) {");
    ci.appendLine("foo();");
    ci.appendLine("");
    ci.appendLine("bar();");
    ci.appendLine("}");
    assertEquals("if (x) {\n"
                 + "  foo();\n"
                 + "\n"
                 + "  bar();\n"
                 + "}\n",
                 sb.toString());
  }

  public void testSameLineOutdentIndent() throws Exception {
    ci.appendLine("if (something) {");
    ci.appendLine("doIt();");
    ci.appendLine("} else {");
    ci.appendLine("undoIt();");
    ci.appendLine("}");
    ci.appendLine("return 5;");
    assertEquals("if (something) {\n" +
                 "  doIt();\n" +
                 "} else {\n" +
                 "  undoIt();\n" +
                 "}\n" +
                 "return 5;\n",
                 sb.toString());
  }

  public void testNesting() throws Exception {
    ci.appendLine("if (condition1) {");
    ci.appendLine("if (condition2) {");
    ci.appendLine("doIt();");
    ci.appendLine("}");
    ci.appendLine("}");
    assertEquals("if (condition1) {\n" +
                 "  if (condition2) {\n" +
                 "    doIt();\n" +
                 "  }\n" +
                 "}\n",
                 sb.toString());
  }

  public void testMultiLine() throws Exception {
    ci.appendLine("{");
    ci.appendLine("List<Foo> foos =\n" +
                  "    new ArrayList<Foo>();");
    ci.appendLine("}");
    assertEquals("{\n" +
                 "  List<Foo> foos =\n" +
                 "      new ArrayList<Foo>();\n" +
                 "}\n",
                 sb.toString());
  }

  public void testHalfIndent() throws Exception {
    ci.appendLine("{");
    ci.appendLine("public:");
    ci.appendLine("int i = 1;");
    ci.appendLine("private:");
    ci.appendLine("int j = 2;");
    ci.appendLine("}");
    assertEquals("{\n" +
                 " public:\n" +
                 "  int i = 1;\n" +
                 " private:\n" +
                 "  int j = 2;\n" +
                 "}\n",
                 sb.toString());
  }

  public void testKitchenSink() throws Exception {
    ci.appendLine("{");
    ci.appendLine("if (condition1) {");
    ci.appendLine("foo();");
    ci.appendLine("} else if (condition1\n" +
                  "        || condition2\n" +
                  "        || condition3) {");
    ci.appendLine("bar();");
    ci.appendLine("} else {");
    ci.appendLine("baz();");
    ci.appendLine("}");
    ci.appendLine("}");
    assertEquals("{\n" +
                 "  if (condition1) {\n" +
                 "    foo();\n" +
                 "  } else if (condition1\n" +
                 "          || condition2\n" +
                 "          || condition3) {\n" +
                 "    bar();\n" +
                 "  } else {\n" +
                 "    baz();\n" +
                 "  }\n" +
                 "}\n",
                 sb.toString());
  }
}
