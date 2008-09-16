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
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.OutputLanguage;

/**
 * Base class for {@code OutputLanguage} utility classes. Contains
 * code common to all {@code OutputLanguages}.
 */
public abstract class OutputLanguageUtil {
  private final ImmutableSet<String> reservedWords;
  private final CharEscaper stringEscaper;

  protected OutputLanguageUtil(Iterable<String> reservedWords, CharEscaper stringEscaper) {
    this.reservedWords = ImmutableSet.copyOf(reservedWords);
    this.stringEscaper = Preconditions.checkNotNull(stringEscaper);
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
