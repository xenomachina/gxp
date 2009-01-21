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

package com.google.gxp.html;

import com.google.common.base.CharEscapers;
import com.google.common.base.Preconditions;
import com.google.gxp.base.GxpClosure;
import com.google.gxp.base.GxpClosures;
import com.google.gxp.base.GxpContext;
import com.google.i18n.Localizable;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Utility {@code HtmlClosure}s
 */
public final class HtmlClosures {
  private HtmlClosures() {}

  public static final HtmlClosure EMPTY = new HtmlClosure() {
      public void write(Appendable out, GxpContext context) {}
    };

  /**
   * Convert an html String into a {@code HtmlClosure} that emits the
   * String without any escaping.
   */
  public static HtmlClosure fromHtml(final String html) {
    return wrap(GxpClosures.fromString(html));
  }

  /**
   * Convert a plaintext String into a {@code HtmlClosure} that emits the
   * HTML-escaped form of the String.
   *
   * If you are using this in a GXP, you are doing something wrong. GXP is
   * designed to handle all escaping for you.
   */
  public static HtmlClosure fromPlaintext(final String text) {
    return wrap(GxpClosures.fromString(text, CharEscapers.asciiHtmlEscaper()));
  }

  /**
   * Create a {@code HtmlClosure} based on a {@code Localizable}.
   *
   * The contents of the Localizable will be escaped on output.
   */
  public static HtmlClosure fromLocalizable(final Localizable value) {
    return wrap(GxpClosures.fromLocalizable(value, CharEscapers.asciiHtmlEscaper()));
  }

  /**
   * Create a {@code HtmlClosure} that will emit all the data avaliable from the
   * {@code File} at the time the closure is evaluated.
   */
  public static HtmlClosure fromFile(final File file, final Charset charset) {
    return wrap(GxpClosures.fromFile(file, charset));
  }

  /**
   * Create a {@code HtmlClosure} that will emit all the data avaliable from the
   * {@code Reader}. If the returned {@code HtmlClosure} is evaluated more than
   * once, {@code reset()} will be called to reset the stream back to its origin
   * on all evaluations but the first.
   */
  public static HtmlClosure fromReader(final Reader reader) throws IOException {
    return wrap(GxpClosures.fromReader(reader));
  }

  /**
   * Renders a sequence of {@link HtmlClosure} instances by calling their
   * write methods in order.
   *
   * @param closures A list of {@code HtmlClosure} objects to be rendered
   * @return A new {@code HtmlClosure} that renders the list
   */
  public static HtmlClosure concat(final Iterable<? extends HtmlClosure> closures) {
    return wrap(GxpClosures.concat(closures));
  }

  /**
   * Renders a sequence of {@link HtmlClosure} instances by calling their
   * write methods in order. Varargs form of {@link #concat(Iterable)}.
   *
   * @param closures a series of closure objects to be rendered in order
   * @return A new {@code HtmlClosure} that renders the closures in order
   */
  public static HtmlClosure concat(final HtmlClosure... closures) {
    return wrap(GxpClosures.concat(closures));
  }

  /**
   * Wrap a {@code GxpClosure} with a {@code HtmlClosure}.
   */
  private static HtmlClosure wrap(final GxpClosure closure) {
    return new HtmlClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          closure.write(out, gxpContext);
        }
      };
  }
}
