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

package com.google.gxp.compiler.codegen;

import com.google.common.base.CharEscaper;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.OutputLanguage;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for {@code OutputLanguage} utility classes. Contains
 * code common to all {@code OutputLanguages}.
 */
public abstract class OutputLanguageUtil {
  private final ImmutableSet<String> reservedWords;
  private final ImmutableSet<String> forbiddenOps;
  private final Pattern opsFinder;
  private final CharEscaper expressionEscaper;
  private final CharEscaper stringEscaper;

  protected OutputLanguageUtil(Iterable<String> reservedWords,
                               Iterable<String> forbiddenOps,
                               Pattern opsFinder,
                               CharEscaper expressionEscaper,
                               CharEscaper stringEscaper) {
    this.reservedWords = ImmutableSet.copyOf(reservedWords);
    this.forbiddenOps = ImmutableSet.copyOf(forbiddenOps);
    this.opsFinder = Preconditions.checkNotNull(opsFinder);
    this.expressionEscaper = Preconditions.checkNotNull(expressionEscaper);
    this.stringEscaper = Preconditions.checkNotNull(stringEscaper);
  }

  /**
   * Validate the given {@code NativeExpression} and add {@code Alert}s to the
   * {@code AlertSink} if necessary.
   */
  public String validateExpression(AlertSink alertSink, NativeExpression expr,
                                   OutputLanguage outputLanguage) {
    String result = expr.getNativeCode(outputLanguage);
    if (result == null) {
      alertSink.add(new MissingExpressionError(expr, outputLanguage));
      return "";
    }

    String s = removeCommentsAndLiterals(alertSink, expr, outputLanguage);

    Character c = findMismatches(s);
    if (c != null) {
      alertSink.add(new IllegalExpressionError(expr, outputLanguage));
    }

    Matcher m = opsFinder.matcher(s);
    while (m.find()) {
      if (forbiddenOps.contains(m.group())) {
        alertSink.add(new IllegalOperatorError(expr, outputLanguage, m.group()));
      }
    }

    return expressionEscaper.escape(result);
  }

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
  private String removeCommentsAndLiterals(AlertSink alertSink,
                                           NativeExpression expr,
                                           OutputLanguage outputLanguage) {
    String str = expr.getNativeCode(outputLanguage);

    StringBuilder sb = new StringBuilder();
    int start = 0;

    Matcher m = TRICKY_JAVA_TOKEN.matcher(str);
    while (m.find()) {
      sb.append(str.substring(start, m.start()));
      start = m.end();
      if (m.group(1) != null) {
        sb.append("'x'");
        if (m.group(2) == null) {
          alertSink.add(new IllegalExpressionError(expr, outputLanguage));
        }
      } else if (m.group(3) != null) {
        sb.append("\"\"");
        if (m.group(4) == null) {
          alertSink.add(new IllegalExpressionError(expr, outputLanguage));
        }
      } else if (m.group(5) != null && m.group(6) == null) {
        alertSink.add(new IllegalExpressionError(expr, outputLanguage));
      } else if (m.group(7) != null && m.group(8) == null) {
        alertSink.add(new IllegalExpressionError(expr, outputLanguage));
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

  private static final Collection<Character> NESTING_OPENINGS = NESTING_PAIRS.keySet();
  private static final Collection<Character> NESTING_CLOSINGS = NESTING_PAIRS.values();

  /**
   * looks for mismatched ()s, []s, and {}s
   *
   * @reurn Character representing the mismatched item, or null if there
   *        are no mismatches
   */
  private Character findMismatches(String s) {
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

  // compile all the patterns into a giant or Expression;
  protected static Pattern compileUnionPattern(String... patterns) {
    return Pattern.compile(Joiner.on("|").join(patterns));
  }

  /**
   * Validate that the given name is a valid variable name.  Add an
   * {@code Alert} to the {@code AlertSink} if it isn't.
   *
   * @return the name
   */
  public String validateName(AlertSink alertSink, Node node, String name,
                             OutputLanguage outputLanguage) {
    if (reservedWords.contains(name)) {
      alertSink.add(new IllegalNameError(node, outputLanguage, name));
    }
    return name;
  }

  /**
   * Convert a string to a string literal for the given
   * {@code OutputLanguage}.
   */
  public String toStringLiteral(String s) {
    return '"' + stringEscaper.escape(s) + '"';
  }
}
