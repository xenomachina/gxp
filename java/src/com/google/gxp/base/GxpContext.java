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
 * effectively a collection of parameters that are implicitly passed to all
 * sub-templates. Future additions to this should preferably be made in an
 * application-neutral way.
 */
public final class GxpContext {
  private final Locale locale;
  private final boolean forcingXmlSyntax;
  private final Charset charset;

  /**
   * Builder for {@code GxpContext}. Typical usage pattern is:
   * <pre>
   * GxpContext gc = GxpContext.builder(myLocale).setCharset(myCharset).build();
   * </pre>
   */
  public static final class Builder {
    private final Locale locale;
    private boolean forcingXmlSyntax = false;
    private Charset charset = Charsets.US_ASCII;

    private Builder(Locale locale) {
      this.locale = Preconditions.checkNotNull(locale);
    }

    /**
     * Builds a {@code GxpContext} based on the state of this builder.
     */
    public GxpContext build() {
      return new GxpContext(this);
    }

    public Builder forceXmlSyntax() {
      this.forcingXmlSyntax = true;
      return this;
    }

    public Builder setCharset(Charset charset) {
      this.charset = Preconditions.checkNotNull(charset);
      return this;
    }
  }

  /**
   * Creates a {@code GxpContext.Builder}.
   *
   * @param locale {@link Locale} to use in {@code GxpContext}.
   */
  public static Builder builder(Locale locale) {
    return new Builder(locale);
  }

  /**
   * Creates a {@code GxpContext} from a {@code Builder}.
   */
  private GxpContext(Builder builder) {
    this.locale = Preconditions.checkNotNull(builder.locale);
    this.forcingXmlSyntax = builder.forcingXmlSyntax;
    this.charset = Preconditions.checkNotNull(builder.charset);
  }

  /**
   * @param locale the Locale to use when writing a {@code GxpTemplate}
   */
  public GxpContext(Locale locale) {
    this(builder(locale));
  }

  /**
   * @return the {@code Locale}.
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * @return {@code true} if this context always renders strict xml compliant output (even for
   * schemas that support SGML)
   */
  public boolean isForcingXmlSyntax() {
    return forcingXmlSyntax;
  }

  /**
   * @return the {@code Charset} which this context renders output in
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
