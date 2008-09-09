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

import com.google.common.base.CharEscaper;
import com.google.common.base.CharEscapers;
import com.google.common.base.Preconditions;
import com.google.gxp.base.GxpContext;
import com.google.i18n.Localizable;

import java.io.IOException;

/**
 * Base Appender for all markup (XML/SGML) appenders, handles appending of
 * java primitives, and other types that can be handled by all
 * {@code MarkupAppender}s.
 */
public class MarkupAppender<T extends MarkupClosure> {

  /**
   * For use by markup schemas that don't need a custom appender.
   */
  public static final MarkupAppender<MarkupClosure> INSTANCE = new MarkupAppender<MarkupClosure>();

  protected MarkupAppender() { }

  //////////////////////////////////////////////////////////////////////
  // CharEscaper
  //////////////////////////////////////////////////////////////////////

  /**
   * Subclasses should override this method if they want to escape in a non
   * standard way.
   *
   * @return a {@code CharEscaper} to use for escaping unescaped character
   * sequences.
   */
  protected CharEscaper getCharEscaper(GxpContext gxpContext) {
    return CharEscapers.xmlEscaper();
  }

  //////////////////////////////////////////////////////////////////////
  // Primitives
  //////////////////////////////////////////////////////////////////////

  /**
   * Appends a {@code byte} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, byte value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Appends a {@code short} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, short value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Appends a {@code int} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, int value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Appends a {@code long} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, long value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Appends a {@code float} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, float value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Appends a {@code double} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, double value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Appends a {@code char} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, char value)
      throws IOException {
    getCharEscaper(gxpContext).escape(out).append(value);
    return out;
  }

  // NOTE: boolean is absent from the list of writable primitives because it
  //       has no obvious representation ("true" and "false" are gross and
  //       English centric)

  //////////////////////////////////////////////////////////////////////
  // Objects
  //////////////////////////////////////////////////////////////////////

  /**
   * Appends a {@code T} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, T closure)
      throws IOException {
    Preconditions.checkNotNull(closure);
    closure.write(out, gxpContext);
    return out;
  }

  /**
   * Appends a {@code GxpAttrBundle} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, GxpAttrBundle<T> bundle)
      throws IOException {
    Preconditions.checkNotNull(bundle);
    bundle.write(out, gxpContext);
    return out;
  }

  /**
   * Appends a {@code Localizable} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, Localizable value)
      throws IOException {
    Preconditions.checkNotNull(value);
    append(out, gxpContext, value.toString(gxpContext.getLocale()));
    return out;
  }

  /**
   * Appends a {@code CharSequence} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, CharSequence value)
      throws IOException {
    Preconditions.checkNotNull(value);
    getCharEscaper(gxpContext).escape(out).append(value);
    return out;
  }
}
