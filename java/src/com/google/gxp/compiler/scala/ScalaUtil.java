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

package com.google.gxp.compiler.scala;

import com.google.common.base.CharEscapers;
import com.google.common.collect.ImmutableSet;
import com.google.gxp.compiler.codegen.OutputLanguageUtil;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Contains static functions for validating scala expressions,
 * and a couple additional scala utility functions.
 */
public class ScalaUtil extends OutputLanguageUtil {

  private ScalaUtil() {
    super(RESERVED_WORDS, FORBIDDEN_OPS, OPS_FINDER,
          // TODO(harryh): is javaStringUnicodeEscaper() really the right thing here?
          CharEscapers.javaStringUnicodeEscaper(),
          CharEscapers.javascriptEscaper());
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

  private static final ImmutableSet<String> RESERVED_WORDS = ImmutableSet.of();
      /* TODO(harryh): fill this out "abstract",
      "as",
      "assert",
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
      "with");*/

  /**
   * Static Singleton Instance
   *
   * Must be declared last in the source file.
   */ 
  public static final ScalaUtil INSTANCE = new ScalaUtil();
}
