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
import com.google.common.base.Join;
import com.google.common.collect.ImmutableSet;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.codegen.IllegalNameError;
import com.google.gxp.compiler.codegen.MissingExpressionError;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Contains static functions for validating javascript expressions,
 * and a couple additional javascript utility functions.
 */
public class JavaScriptUtil {

  // READ THIS BEFORE YOU CHANGE THE LIST BELOW!
  //
  // The list of disabled JavaScript operators was originally based on the list
  // of disabled Java Operators. If you want to enable something here, see
  // about getting it enabled for Java as well.

  private static final Set<String> FORBIDDEN_OPS = ImmutableSet.of(
      // simple boolean
      // "!",
      // "!=",
      // "==",
      // "===",

      // boolean connectives
      //  "&&",
      //  "||",

      // boolean comparators
      // ">",
      // ">=",
      // "<",
      // "<=",

      // arithmetic
      // "*",
      // "+",
      // "-",
      // "/",
      // "%",

      // conditional operator (really ?:)
      // "?",

      // type inspection
      "in",
      "instanceof",
      "typeof",

      // object instantiation/deletion
      // "new",
      "delete",
      "void",

      // bitwise
      "^",
      "~",
      "&",
      "<<",
      ">>",
      ">>>",
      "|",

      // assignment -- I can't imagine any reason why it would ever be a good
      //               idea to re-enable these,
      "--",
      "-=",
      "/=",
      "*=",
      "&=",
      "%=",
      "++",
      "+=",
      "<<=",
      "=",
      ">>=",
      ">>>=",
      "|=",
      "^=");

  // compile all the patterns into a giant or Expression;
  private static Pattern compileUnionPattern(String... patterns) {
    return Pattern.compile(Join.join("|", patterns));
  }

  // the order is important! The '|' operator  is non-greedy in
  // regexes. Sorting in order of descending length works.
  private static final Pattern OPS_FINDER = compileUnionPattern(
      "\\binstanceof\\b",
      "\\bdelete\\b",
      "\\btypeof\\b",
      "\\bvoid\\b",
      Pattern.quote(">>>="),
      Pattern.quote("<<="),
      Pattern.quote(">>="),
      Pattern.quote(">>>"),
      Pattern.quote("==="),
      Pattern.quote("--"),
      Pattern.quote("-="),
      Pattern.quote("!="),
      Pattern.quote("/="),
      Pattern.quote("^="),
      Pattern.quote("*="),
      Pattern.quote("&&"),
      Pattern.quote("&="),
      Pattern.quote("%="),
      Pattern.quote("++"),
      Pattern.quote("+="),
      Pattern.quote("<<"),
      Pattern.quote("<="),
      Pattern.quote("=="),
      Pattern.quote(">="),
      Pattern.quote(">>"),
      Pattern.quote("|="),
      Pattern.quote("||"),
      "\\bnew\\b",
      "\\bin\\b",
      Pattern.quote("-"),
      Pattern.quote("!"),
      Pattern.quote("/"),
      Pattern.quote("^"),
      Pattern.quote("~"),
      Pattern.quote("*"),
      Pattern.quote("&"),
      Pattern.quote("%"),
      Pattern.quote("+"),
      Pattern.quote("<"),
      Pattern.quote("="),
      Pattern.quote(">"),
      Pattern.quote("|"),
      Pattern.quote("?"));  // just use ? to find ternary operator...

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
      alertSink.add(new IllegalNameError(node, OutputLanguage.JAVASCRIPT, name));
    }
    return name;
  }

  //////////////////////////////////////////////////////////////////////
  // String manipulation
  //////////////////////////////////////////////////////////////////////

  public static String toJavaScriptStringLiteral(String s) {
    return '"' + CharEscapers.javascriptEscaper().escape(s) + '"';
  }
}
