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

package com.google.gxp.base;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * This is a context used for expanding GXP templates.  The context is
 * effectively a collection of parameters that are implicitely passed to all
 * sub-templates. Future additions to this should preferably be made in an
 * application-neutral way.
 */
public final class GxpContext {
  private final Locale locale;
  private final boolean useXmlSyntax;
  private final Charset charset;

  /**
   * @param locale the Locale to use when writing a {@code GxpTemplate}
   */
  public GxpContext(Locale locale) {
    this(locale, false, Charsets.US_ASCII);
  }

  /**
   * @param locale the Locale to use when writing a {@code GxpTemplate}
   * @param useXmlSyntax flag indicating if gxp should generate XML (instead of SGML)
   */
  public GxpContext(Locale locale, boolean useXmlSyntax) {
    this(locale, useXmlSyntax, Charsets.US_ASCII);
  }

  /**
   * @param locale the Locale to use when writing a {@code GxpTemplate}
   * @param useXmlSyntax flag indicating if gxp should generate XML (instead of SGML)
   * @param charset the {@code Charset} to escape output into
   */
  public GxpContext(Locale locale, boolean useXmlSyntax, Charset charset) {
    this.locale = Preconditions.checkNotNull(locale);
    this.useXmlSyntax = useXmlSyntax;
    this.charset = Preconditions.checkNotNull(charset);
  }

  /**
   * @return the {@code Locale}.
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * @return {@code true} if this context renders strict xml compliant output
   */
  public boolean isUsingXmlSyntax() {
    return useXmlSyntax;
  }

  /**
   * @return the {@code Charset} into which output should be escaped
   * into.
   */
  public Charset getCharset() {
    return charset;
  }

  ////////////////////////////////////////////////////////////////////////////
  // Utility Functions
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Convert a closure to a string in the given context
   *
   * @param closure the closure to turn into a string
   * @return a string representation of the closure in the current context
   */
  public String getString(GxpClosure closure) {
    StringBuilder sb = new StringBuilder();
    try {
      closure.write(sb, this);
    } catch (IOException e) {
      // this shouldn't be possible
      throw new AssertionError(e);
    }
    return sb.toString();
  }

  /**
   * Helper function for null, empty, and whitespace closure testing.
   *
   * @return true if closure == null or contains only whitespace
   * characters.
   */
  public boolean isEmptyOrWhitespace(GxpClosure closure) {
    if (closure != null) {
      for (char c : getString(closure).toCharArray()) {
        if (!Character.isWhitespace(c)) {
          return false;
        }
      }
    }
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  // The following are for gxpc internal use only
  ////////////////////////////////////////////////////////////////////////////

  private boolean is_top_level_call = true;

  /**
   * Will return true once and only once.
   *
   * While this method is "public", it is only intended for use by the GXP
   * compiler.
   */
  public boolean isTopLevelCall() {
    boolean result = is_top_level_call;
    is_top_level_call = false;
    return result;
  }
}
