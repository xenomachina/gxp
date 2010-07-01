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

package com.google.gxp.css;

import com.google.common.base.CharEscaper;
import com.google.common.base.CharEscaperBuilder;
import com.google.common.base.Preconditions;
import com.google.gxp.base.GxpContext;

import java.io.IOException;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Appender class for text/css
 */
public class CssAppender {
  private CssAppender() {}

  public static final CssAppender INSTANCE = new CssAppender();

  //////////////////////////////////////////////////////////////////////////////////////////
  // Primitives
  //////////////////////////////////////////////////////////////////////////////////////////

  private static final NumberFormat numberFormatter = getNumberFormatter();

  private static NumberFormat getNumberFormatter() {
    // Always use Locale.US because CSS is not user visible and always uses
    // a dot (.) to denote the decimal place.
    NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
    formatter.setGroupingUsed(false);
    return formatter;
  }

  /**
   * Converts value to a CSS literal and appends to Appendable.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, int value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Converts value to a CSS literal and appends to Appendable.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, double value)
      throws IOException {
    out.append(numberFormatter.format(value));
    return out;
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // Closures
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Appends a {@code CssClosure} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, CssClosure closure)
      throws IOException {
    Preconditions.checkNotNull(closure);
    closure.write(out, gxpContext);
    return out;
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // Objects
  //////////////////////////////////////////////////////////////////////////////////////////

  // If adding hex escapes to this list be sure to add a trailing space. If a
  // trailing space is not included then the escape may consume later
  // characters in the string, specifically hex digits and whitespaces. See
  // http://w3.org/TR/2006/WD-CSS21-20061106/syndata.html#escaped-characters
  // for more info.
  private static final CharEscaper CSS_STRING_ESCAPER =
      new CharEscaperBuilder()
        .addEscape('\\', "\\\\")
        .addEscape('\n', "\\a ")
        .addEscape('\'', "\\'")
        .addEscape('"', "\\\"")
        .toEscaper();

  /**
   * Converts value to a CSS literal and appends to Appendable.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, CharSequence value)
      throws IOException {
    Preconditions.checkNotNull(value);
    out.append('"');
    CSS_STRING_ESCAPER.escape(out).append(value);
    out.append('"');
    return out;
  }

  /**
   * Converts value to a CSS literal and appends to Appendable.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, URI value)
      throws IOException {
    Preconditions.checkNotNull(value);
    // TODO(laurence): correct escapes. eg: '\n' -> '\a ', etc.
    // TODO(laurence): use StringUtil.escape
    out.append("url(\"");
    String s = value.toString();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\n':
        case '\r':
        case '\t':
        case '\'':
        case '\"':
        case '\\':
        case ' ':
        case '(':
        case ')':
        case ',':
          out.append("\\");
      }
      out.append(c);
    }
    out.append("\")");
    return out;
  }
}
