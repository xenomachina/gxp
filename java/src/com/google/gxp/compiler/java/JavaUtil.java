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

package com.google.gxp.compiler.java;

import com.google.common.base.CharEscapers;
import com.google.common.base.Join;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.MissingTypeError;
import com.google.gxp.compiler.base.JavaAnnotation;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.NativeType;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.codegen.IllegalExpressionError;
import com.google.gxp.compiler.codegen.IllegalNameError;
import com.google.gxp.compiler.codegen.IllegalOperatorError;
import com.google.gxp.compiler.codegen.MissingExpressionError;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains static functions for validating java expressions and types,
 * and a couple additional java utility functions.
 */
public class JavaUtil {

  // READ THIS BEFORE YOU CHANGE THE LIST BELOW!
  //
  // Urs asked that *all* Java operators be disabled. He said they can be
  // re-enabled only if he or Amit P. is convinced. So the operators that
  // are commented out of the lists below were explicitly allowed by one
  // of them. If you want to enable other operators (by commenting them
  // out of these lists) you need to talk to one of them.

  private static final Set<String> FORBIDDEN_OPS = ImmutableSet.of(
      // simple boolean
      // "!",   <gxp:abbr name='showClickthrough' type='boolean' expr='!foo'>
      // "!=",  <gxp:abbr> again, we want to compute booleans
      // "==",

      // boolean connectives
      //  "&&", useful for stuff like "if x != null && x.condition()"
      //  "||", the alternative (nested if) requires repeating the else clause

      // boolean comparators
      // ">",   useful for stuff like "only show this button if foo > bar"
      // ">=",
      // "<",
      // "<=",

      // arithmetic
      // "*",
      // "+",
      // "-",
      // "/",
      // "%",

      // unusual
      // "?",        needed for rowspan calculations
      "instanceof",
      // "new",      Necessary for conversions, like EnumerationIterator

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

  private static final String NOT_IN_CAST_LOOKAHEAD = "(?!\\s*[,)>])";

  // the order is important! The '|' operator  is non-greedy in
  // regexes. Sorting in order of descending length works.
  //
  // Note that for the >>> and >> operators we use negative lookeahead to
  // make sure that the item isn't followed by a comma, close paren or >.
  // In this case we have a generic cast, not an operator.  This is kind
  // of a hack, and isn't 100% correct (it won't handle generic methods
  // like someVar.<List<Foo>>.someMethod() ) but I think it's the best
  // we can do without much more sophisticated expression parsing
  private static final Pattern OPS_FINDER = compileUnionPattern(
      "\\binstanceof\\b",
      Pattern.quote(">>>="),
      Pattern.quote("<<="),
      Pattern.quote(">>="),
      Pattern.quote(">>>") + NOT_IN_CAST_LOOKAHEAD,
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
      Pattern.quote(">>") + NOT_IN_CAST_LOOKAHEAD,
      Pattern.quote("|="),
      Pattern.quote("||"),
      "\\bnew\\b",
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

  // the following regex has 4 pieces, one for each of the "tricky" tokens
  // we're trying to collapse. Each of these in turn has two groups. The
  // first is the entire token. The second is the terminator part of the
  // token. The existence of the first group of a piece can be used to
  // determine the token type of a match, and the emptiness of the second
  // piece can be used to determine if the token is unterminated.

  // TODO(jjb):    make java regex's suck less so that our code doesn't
  // TODO(madbot): have to be $%^&*(ing incomprehensible modem line noise

  private static final Pattern TRICKY_JAVA_TOKEN = Pattern.compile(
      "('(?:[^\\n'\\\\]|\\\\.)*(')?)"          // char literals
      + "|(\"(?:[^\\n\"\\\\]|\\\\.)*(\")?)"    // string literals
      + "|(/\\*(?:[^*]|\\*+[^/*])*(\\*/)?)"    // multi-line comments
      + "|(//[^\\n]*(\\n)?)", Pattern.DOTALL); // single-line comments

  /**
   * Remove comments, string literals, and character literals from the
   * input string returning what's left.
   *
   * Add alerts to the sink if there are unclosed comments or literals
   */
  private static String removeCommentsAndLiterals(AlertSink alertSink, NativeExpression expr) {
    String str = expr.getNativeCode(OutputLanguage.JAVA);

    StringBuilder sb = new StringBuilder();
    int start = 0;

    Matcher m = TRICKY_JAVA_TOKEN.matcher(str);
    while (m.find()) {
      sb.append(str.substring(start, m.start()));
      start = m.end();
      if (m.group(1) != null) {
        sb.append("'x'");
        if (m.group(2) == null) {
          alertSink.add(new IllegalExpressionError(expr, OutputLanguage.JAVA));
        }
      } else if (m.group(3) != null) {
        sb.append("\"\"");
        if (m.group(4) == null) {
          alertSink.add(new IllegalExpressionError(expr, OutputLanguage.JAVA));
        }
      } else if (m.group(5) != null && m.group(6) == null) {
        alertSink.add(new IllegalExpressionError(expr, OutputLanguage.JAVA));
      } else if (m.group(7) != null && m.group(8) == null) {
        alertSink.add(new IllegalExpressionError(expr, OutputLanguage.JAVA));
      }
    }
    sb.append(str.substring(start));
    return sb.toString();
  }

  private static final Map<Character, Character> NESTING_PAIRS
    = ImmutableMap.<Character, Character>builder()
        .put('(', ')')
        .put('[', ']')
        .put('{', '}')
        .build();

  private static final Collection<Character> NESTING_OPENINGS
    = NESTING_PAIRS.keySet();

  private static final Collection<Character> NESTING_CLOSINGS
    = NESTING_PAIRS.values();

  /**
   * looks for mismatched ()s, []s, and {}s
   *
   * @reurn Character representing the mismatched item, or null if there
   *        are no mismatches
   */
  private static Character findMismatches(String s) {
    Deque<Character> state = new ArrayDeque<Character>();
    for (Character c : s.toCharArray()) {
      if (NESTING_OPENINGS.contains(c)) {
        state.push(NESTING_PAIRS.get(c));
      } else if (NESTING_CLOSINGS.contains(c)) {
        if (state.isEmpty() || !state.peek().equals(c)) {
          return c;
        }
        state.pop();
      }
    }
    if (!state.isEmpty()) {
      return state.pop();
    }

    return null;
  }

  /**
   * Validate the given NativeExpression and adds alerts to the sink if
   * necessary.
   */
  public static String validateExpression(AlertSink alertSink, NativeExpression expr) {
    String result = expr.getNativeCode(OutputLanguage.JAVA);
    if (result == null) {
      alertSink.add(new MissingExpressionError(expr, OutputLanguage.JAVA));
      return "";
    }

    String s = removeCommentsAndLiterals(alertSink, expr);

    Character c = findMismatches(s);
    if (c != null) {
      alertSink.add(new IllegalExpressionError(expr, OutputLanguage.JAVA));
    }

    Matcher m = OPS_FINDER.matcher(s);
    while (m.find()) {
      if (FORBIDDEN_OPS.contains(m.group())) {
        alertSink.add(new IllegalOperatorError(expr, OutputLanguage.JAVA, m.group()));
      }
    }

    return CharEscapers.javaStringUnicodeEscaper().escape(result);
  }

  private static final Set<String> RESERVED_WORDS = ImmutableSet.of(
      // keywords
      "abstract",
      "assert",
      "boolean",
      "break",
      "byte",
      "case",
      "catch",
      "char",
      "class",
      "const",
      "continue",
      "default",
      "do",
      "double",
      "else",
      "enum",
      "extends",
      "final",
      "finally",
      "float",
      "for",
      "goto",
      "if",
      "implements",
      "import",
      "instanceof",
      "int",
      "interface",
      "long",
      "native",
      "new",
      "package",
      "private",
      "protected",
      "public",
      "return",
      "short",
      "static",
      "strictfp",
      "super",
      "switch",
      "synchronized",
      "this",
      "throw",
      "throws",
      "transient",
      "try",
      "void",
      "volatile",
      "while",
      // literals
      "true",
      "false",
      "null");

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
    String ret = type.getNativeType(OutputLanguage.JAVA);
    if (ret == null) {
      alertSink.add(new MissingTypeError(type, OutputLanguage.JAVA));
      return ret;
    }

    ret = ret.replace('{', '<').replace('}', '>').trim();

    // tokenize the type
    Queue<String> tokens = new LinkedList<String>();
    String s = type.getNativeType(OutputLanguage.JAVA).trim();
    while (s.length() != 0) {
      Matcher m = TYPE_TOKEN_REGEX.matcher(s);
      if (m.find()) {
        tokens.add(m.group(1));
        s = m.group(2).trim();
      } else {
        alertSink.add(new IllegalJavaTypeError(type));
        return ret;
      }
    }

    if (!(parseType(tokens) && tokens.isEmpty())) {
      alertSink.add(new IllegalJavaTypeError(type));
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
    for (String subType : type.getNativeType(OutputLanguage.JAVA).split("&")) {
      subTypes.add(validateType(alertSink, new NativeType(type, subType)));
    }

    return Join.join(" & ", subTypes);
  }

  /**
   * Validate that the given name is a valid java variable name.  Add an
   * {@code Alert} to the {@code AlertSink} if it isn't.
   *
   * @return the name
   */
  public static String validateName(AlertSink alertSink, Node node, String name) {
    if (RESERVED_WORDS.contains(name)) {
      alertSink.add(new IllegalNameError(node, OutputLanguage.JAVA, name));
    }
    return name;
  }

  /**
   * Validate that the given {@link JavaAnnotation} contains a well formed
   * Java annotation.
   *
   * @return the well formed annotation.
   */
  public static String validateAnnotation(AlertSink alertSink, JavaAnnotation annotation) {
    // TODO(harryh): actually do some validation
    return annotation.getWith();
  }

  // Comments on parse functions are from the grammar in the Java 1.5
  // Language Spec.  Types in Java 1.5 are a bit complicated due to
  // generics. They aren't regular (ie: cannot be matched by a regular
  // expression), so we use a simple LL(1) recursive descent parser.

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

  //////////////////////////////////////////////////////////////////////
  // Functions for moving back and forth between reference and
  // primitive types
  //////////////////////////////////////////////////////////////////////

  /**
   * @return the most general reference type that corresponds to the specified
   * a Java type, or the specified Java type if it is already a reference type
   * (ie: a class/interface).
   */
  public static String toReferenceType(String type) {
    String result = PRIMITIVE_TO_BOXED_MAP.get(type);
    return (result == null) ? type : result;
  }

  public static String unbox(String expr, String type) {
    if (PRIMITIVE_TO_BOXED_MAP.containsKey(type)) {
      return "(" + expr + ")." + type + "Value()";
    } else {
      return expr;
    }
  }

  //////////////////////////////////////////////////////////////////////
  // String manipulation
  //////////////////////////////////////////////////////////////////////

  public static String toJavaStringLiteral(String s) {
    return "\"" + CharEscapers.javaStringEscaper().escape(s) + "\"";
  }

  //////////////////////////////////////////////////////////////////////
  // Primitive Parsing
  //////////////////////////////////////////////////////////////////////

  // strcitly check for either "true" or "false"
  private static final Predicate<String> ISVALID_BOOLEAN
    = new Predicate<String>() {
      public boolean apply(String s) {
        s = s.trim();
        return (s.equals("true") || s.equals("false"));
      }
    };

  private static final Predicate<String> ISVALID_BYTE
    = new Predicate<String>() {
      public boolean apply(String s) {
        Byte.valueOf(s.trim());
        return true;
      }
    };


  // as long as s is a single character return it unmodified
  private static final Predicate<String> ISVALID_CHAR
    = new Predicate<String>() {
      public boolean apply(String s) {
        return (s.length() == 1);
      }
    };


  private static final Predicate<String> ISVALID_DOUBLE
    = new Predicate<String>() {
      public boolean apply(String s) {
        Double.valueOf(s.trim());
        return true;
      }
    };


  private static final Predicate<String> ISVALID_FLOAT
    = new Predicate<String>() {
      public boolean apply(String s) {
        Float.valueOf(s.trim());
        return true;
      }
    };


  private static final Predicate<String> ISVALID_INT
    = new Predicate<String>() {
      public boolean apply(String s) {
        Integer.valueOf(s.trim());
        return true;
      }
    };


  private static final Predicate<String> ISVALID_LONG
    = new Predicate<String>() {
      public boolean apply(String s) {
        Long.valueOf(s.trim());
        return true;
      }
    };


  private static final Predicate<String> ISVALID_SHORT
    = new Predicate<String>() {
      public boolean apply(String s) {
        Short.valueOf(s.trim());
        return true;
      }
    };

  private static final Map<String, Predicate<String>> PRIMITIVE_TO_VALIDATOR
    = ImmutableMap.<String, Predicate<String>>builder()
      .put("boolean", ISVALID_BOOLEAN)
      .put("byte",    ISVALID_BYTE)
      .put("char",    ISVALID_CHAR)
      .put("double",  ISVALID_DOUBLE)
      .put("float",   ISVALID_FLOAT)
      .put("int",     ISVALID_INT)
      .put("long",    ISVALID_LONG)
      .put("short",   ISVALID_SHORT)
      .build();

  /**
   * @return true if the primitive is a valid literal of the specified
   * type, false otherwise.
   */
  public static final boolean isValidPrimitive(String primitive, String type) {
    try {
      return PRIMITIVE_TO_VALIDATOR.get(type).apply(primitive);
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
