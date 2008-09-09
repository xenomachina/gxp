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

package com.google.gxp.compiler.js;

import com.google.common.base.CharEscapers;
import com.google.common.collect.ImmutableSet;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.codegen.MissingExpressionError;

/**
 * Contains static functions for validating javascript expressions,
 * and a couple additional javascript utility functions.
 */
public class JavaScriptUtil {

  public static String validateExpression(AlertSink alertSink, NativeExpression expr) {
    String result = expr.getNativeCode(OutputLanguage.JAVASCRIPT);
    if (result == null) {
      alertSink.add(new MissingExpressionError(expr, OutputLanguage.JAVASCRIPT));
      return "";
    }

    // TODO: do some actual validation

    // TODO(harryh): is javaStringUnicodeEscaper() really the right thing here?
    return CharEscapers.javaStringUnicodeEscaper().escape(result);
  }

  // TODO(harryh): I found this list on a random internet site.  It is no doubt
  //               somewhat wrong.  Find a definitive source.
  private static final ImmutableSet<String> RESERVED_WORDS = ImmutableSet.of(
      // JavaScript Reserved Words
      "break",
      "case",
      "comment",
      "continue",
      "default",
      "delete",
      "do",
      "else",
      "export",
      "for",
      "function",
      "if",
      "import",
      "in",
      "label",
      "new",
      "return",
      "switch",
      "this",
      "typeof",
      "var",
      "void",
      "while",
      "with",
      // ECMAScipt Reserved Words
      "catch",
      "class",
      "const",
      "debugger",
      "enum",
      "extends",
      "finally",
      "super",
      "throw",
      "try");

  /**
   * Validate that the given name is a valid javascript variable name. Add an
   * {@code Alert} to the {@code AlertSink} if it isn't.
   *
   * @return the name
   */
  public static String validateName(AlertSink alertSink, Node node, String name) {
    if (RESERVED_WORDS.contains(name)) {
      alertSink.add(new IllegalJavaScriptNameError(node, name));
    }
    return name;
  }

  //////////////////////////////////////////////////////////////////////
  // String manipulation
  //////////////////////////////////////////////////////////////////////

  public static String toJavaScriptStringLiteral(String s) {
    return "'" + CharEscapers.javascriptEscaper().escape(s) + "'";
  }
}
