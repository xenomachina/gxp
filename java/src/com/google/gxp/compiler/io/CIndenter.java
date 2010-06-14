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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for generating nicely indented code in C-like languages. It's
 * almost an Appendable "decorator" in that it wraps around an existing
 * Appendable. It has a different interface from Appendable though, so it
 * doesn't implement Appendable.
 *
 * <p>It performs the following transoformations on the input before passing it
 * through to the underlying Appendable:
 * <ul>
 * <li>adds newlines. CIndenter is line based.
 * <li>adds indentation. The indentation level is automatically adjusted based
 * on the presence of curly braces.
 * <li>(optionally) adds trailing comments.
 * </ul>
 */
public final class CIndenter {
  private static final String INDENT = "  ";
  private static final String HALF_INDENT = " ";
  private static final Pattern NEWLINE_PATTERN = Pattern.compile("\n");

  private final Appendable out;
  private final List<String> halfIndentMarkers;
  private int indentationLevel = 0;

  public CIndenter(Appendable out, String... halfIndentMarkers) {
    this.out = Preconditions.checkNotNull(out);
    this.halfIndentMarkers = ImmutableList.copyOf(halfIndentMarkers);
  }

  private void appendIndent(boolean oneLessSpace) throws IOException {
    for (int i = 0; i < indentationLevel; i++) {
      out.append((oneLessSpace && (i == indentationLevel - 1)) ? HALF_INDENT : INDENT);
    }
  }

  /**
   * Appends the tail comment to the line (if there is one). Inserts an
   * appropriate amount of spaces so that the tail comment will begin at
   * column 80, unless the line is already too long in which case 3 spaces
   * are inserted.
   */
  private void appendTailComment(int lineLength, String tailComment)
      throws IOException {
    if (tailComment.length() > 0) {
      int indentLength = indentationLevel * INDENT.length();
      int spacerLength = Math.max(80 - (lineLength + indentLength), 3);
      for (int i = 0; i < spacerLength; i++) {
        out.append(" ");
      }
      out.append(tailComment);
    }
  }

  public void addIndent() {
    indentationLevel++;
  }

  /**
   * Appends the specified line with indentation and a trailing newline.
   * The presence of leading or trailing curly braces will adjust the
   * indentation. A leading "}" will cause this line and subsequent lines to be
   * outdented, while a trailing "{" will cause subsequent lines to be
   * indented.
   * @param line the line to be appended
   * @throws RuntimeIOException if underlying Appendable throws IOException.
   */
  public void appendLine(CharSequence line) {
    appendLine(line, "");
  }

  /**
   * Like {@link #appendLine(CharSequence)}, but also accepts a trailing
   * comment.
   * @param line the line to be appended
   * @param tailComment a "line comment" to add to the end of the line. This is
   * appended after the braces are inspected, making it possible to have a
   * "trailing" open brace which causes an indent which is actually followed by
   * this comment. Note that the caller is responsible for ensuring that this
   * is actually a comment by using a "//" prefix, or whatever else is
   * necessary for the language being generated.
   * @throws RuntimeIOException if underlying Appendable throws IOException.
   */
  public void appendLine(CharSequence line, String tailComment) {
    try {
      if (line.length() == 0) {
        if (tailComment.length() > 0) {
          appendIndent(false);
          appendTailComment(0, tailComment);
        }
        out.append("\n");
      } else {
        if (line.charAt(0) == '}') {
          indentationLevel--;
        }
        appendIndent(halfIndentMarkers.contains(line));

        Matcher m = NEWLINE_PATTERN.matcher(line);
        int sliceStart = 0;
        while (m.find()) {
          CharSequence cs = line.subSequence(sliceStart, m.start());
          out.append(cs);
          appendTailComment(cs.length(), tailComment);
          out.append("\n");
          appendIndent(halfIndentMarkers.contains(line));
          sliceStart = m.end();
        }
        CharSequence cs = line.subSequence(sliceStart, line.length());
        out.append(cs);
        appendTailComment(cs.length(), tailComment);
        out.append("\n");
        if (line.charAt(line.length() - 1) == '{') {
          indentationLevel++;
        }
      }
    } catch (IOException iox) {
      throw new RuntimeIOException(iox);
    }
  }
}
