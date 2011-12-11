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
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.MissingTypeError;
import com.google.gxp.compiler.base.NativeType;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.codegen.OutputLanguageUtil;

import java.util.*;
import java.util.regex.Matcher;
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

  private static final Set<String> TYPE_ARGUMENT_QUALIFIERS
    = ImmutableSet.of("extends", "super");

  private static final Pattern IDENTIFIER_REGEX
    = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

  private static final Pattern TYPE_TOKEN_REGEX
    = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*|[\\?\\[\\]<>{},\\.])(.*)",
                      Pattern.DOTALL);

  private static final Map<String, String> PRIMITIVE_TO_BOXED_MAP =
      ImmutableMap.<String, String>builder()
        .put("boolean", "Boolean")
        .put("byte", "Number")
        .put("char", "Character")
        .put("double", "Number")
        .put("float", "Number")
        .put("int", "Number")
        .put("long", "Number")
        .put("short", "Number")
        .build();

  private static final Set<String> PRIMITIVE_TYPES =
      ImmutableSet.copyOf(PRIMITIVE_TO_BOXED_MAP.keySet());

  public static final boolean isPrimitiveType(String s) {
    return PRIMITIVE_TYPES.contains(s);
  }

  private static boolean isIdentifier(String s) {
    return s != null
        && !RESERVED_WORDS.contains(s)
        && IDENTIFIER_REGEX.matcher(s).matches();
  }

  /**
   * Validate the given NativeType and adds alerts to the sink if
   * necessary.
   *
   * @return a String representing the validated type
   */
  public static String validateType(AlertSink alertSink, NativeType type) {
    String ret = type.getNativeType(OutputLanguage.SCALA);
    if (ret == null) {
      alertSink.add(new MissingTypeError(type, OutputLanguage.SCALA));
      return ret;
    }

    ret = ret.replace('{', '[').replace('}', ']').trim();

    // tokenize the type
    Queue<String> tokens = new LinkedList<String>();
    String s = type.getNativeType(OutputLanguage.SCALA).trim();
    while (s.length() != 0) {
      Matcher m = TYPE_TOKEN_REGEX.matcher(s);
      if (m.find()) {
        tokens.add(m.group(1));
        s = m.group(2).trim();
      } else {
        alertSink.add(new IllegalScalaTypeError(type));
        return ret;
      }
    }

    if (!(parseType(tokens) && tokens.isEmpty())) {
      alertSink.add(new IllegalScalaTypeError(type));
    }

    return ret;
  }

  /**
   * Validate the given NativeType and adds alerts to the sink if
   * necessary. Allows for conjunctive types (ex: Foo & Bar).
   *
   * This is taking a bit of a shortcut, as if java ever allowed something like
   * "extends List<? extends Foo & Bar> & Baz" an alert would be incorrectly
   * be generated.  Java doesn't currently allow this though so we're fine.
   *
   * @return a String representing the validated type
   */
  public static String validateConjunctiveType(AlertSink alertSink, NativeType type) {
    List<String> subTypes = Lists.newArrayList();
    for (String subType : type.getNativeType(OutputLanguage.SCALA).split("&")) {
      subTypes.add(validateType(alertSink, new NativeType(type, subType)));
    }

    return Joiner.on(" & ").join(subTypes);
  }

  /**
   * Parses the following rule from the JLS:
   *   Type:
   *     Identifier [TypeArguments]{ . Identifier [TypeArguments]} {[]}
   *     BasicType {[]}
   * (actually, this rule deviates from the JLS, which seems to have a
   * bug in that it doesn't allow arrays of BasicTypes)
   *
   * MODIFIED so that {}s can sub for <>s
   */
  private static boolean parseType(Queue<String> tokens) {
    if (isPrimitiveType(tokens.peek())) {
      tokens.poll();
    } else {
      while (true) {
        if (!isIdentifier(tokens.poll())) {
          return false;
        }
        if ("<".equals(tokens.peek())) {
          if (!parseTypeArguments(tokens, "<", ">")) {
            return false;
          }
        }
        if ("{".equals(tokens.peek())) {
          if (!parseTypeArguments(tokens, "{", "}")) {
            return false;
          }
        }
        if (".".equals(tokens.peek())) {
          tokens.poll();
        } else {
          break;
        }
      }
    }
    while ("[".equals(tokens.peek())) {
      tokens.poll();
      if (!"]".equals(tokens.poll())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Parses the following rule from the JLS:
   *   TypeArguments:
   *     < TypeArgument {, TypeArgument} >
   *
   * MODIFIED so that {}s can sub for <>s
   */
  private static boolean parseTypeArguments(Queue<String> tokens,
                                            String start, String end) {
    if (!tokens.poll().equals(start)) {
      return false;
    }
    while (true) {
      if (!parseTypeArgument(tokens)) {
        return false;
      }
      if (",".equals(tokens.peek())) {
        tokens.poll();
      } else {
        break;
      }
    }
    return (end.equals(tokens.poll()));
  }

  /**
   * Parses the following rule from the JLS:
   *   TypeArgument:
   *     Type
   *     ? [( extends | super ) Type]
   */
  private static boolean parseTypeArgument(Queue<String> tokens) {
    if ("?".equals(tokens.peek())) {
      tokens.poll();
      if (TYPE_ARGUMENT_QUALIFIERS.contains(tokens.peek())) {
        tokens.poll();
        return parseType(tokens);
      }
    } else {
      return parseType(tokens);
    }
    return true;
  }

  /**
   * Static Singleton Instance
   *
   * Must be declared last in the source file.
   */ 
  public static final ScalaUtil INSTANCE = new ScalaUtil();
}
