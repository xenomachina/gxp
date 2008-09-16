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
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.codegen.MissingExpressionError;
import com.google.gxp.compiler.codegen.OutputLanguageUtil;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Contains static functions for validating javascript expressions,
 * and a couple additional javascript utility functions.
 */
public class JavaScriptUtil extends OutputLanguageUtil {

  private JavaScriptUtil() {
    super(RESERVED_WORDS, CharEscapers.javascriptEscaper());
  }

  // 
  // READ THIS BEFORE YOU CHANGE THE LIST BELOW!
  // 
  // The list of disabled JavaScript operators was originally based on the list
  // of disabled Java Operators. If you want to enable something here, see
  // about getting it enabled for Java as well.
  //
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

  /**
   * Compile all the patterns into a giant or Expression;
   */
  private static Pattern compileUnionPattern(String... patterns) {
    return Pattern.compile(Join.join("|", patterns));
  }

  //
  // the order is important! The '|' operator  is non-greedy in
  // regexes. Sorting in order of descending length works.
  //
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

  private static final ImmutableSet<String> RESERVED_WORDS = ImmutableSet.of(
      "abstract",
      "as",
      "boolean",
      "break",
      "byte",
      "case",
      "catch",
      "char",
      "class",
      "continue",
      "const",
      "debugger",
      "default",
      "delete",
      "do",
      "double",
      "else",
      "enum",
      "export",
      "extends",
      "false",
      "final",
      "finally",
      "float",
      "for",
      "function",
      "goto",
      "if",
      "implements",
      "import",
      "in",
      "instanceof",
      "int",
      "interface",
      "is",
      "long",
      "namespace",
      "native",
      "new",
      "null",
      "package",
      "private",
      "protected",
      "public",
      "return",
      "short",
      "static",
      "super",
      "switch",
      "synchronized",
      "this",
      "throw",
      "throws",
      "transient",
      "true",
      "try",
      "typeof",
      "use",
      "var",
      "void",
      "volitile",
      "while",
      "with");

  /**
   * Static Singleton Instance
   *
   * Must be declared last in the source file.
   */ 
  public static final JavaScriptUtil INSTANCE = new JavaScriptUtil();
}
