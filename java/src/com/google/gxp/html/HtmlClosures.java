/*
 * Copyright (C) 2007 Google Inc.
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
import com.google.common.collect.ImmutableList;
import com.google.common.io.Characters;
import com.google.gxp.base.GxpContext;
import com.google.i18n.Localizable;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Utility {@code HtmlClosure}s
 */
public class HtmlClosures {
  private HtmlClosures() {}

  public static final HtmlClosure EMPTY = new HtmlClosure() {
      public void write(Appendable out, GxpContext context) {}
    };

  /**
   * Convert an html String into a {@code HtmlClosure} that emits the
   * String without any escaping.
   */
  public static final HtmlClosure fromHtml(final String html) {
    Preconditions.checkNotNull(html);
    return new HtmlClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          out.append(html);
        }
      };
  }

  /**
   * Convert a plaintext String into a {@code HtmlClosure} that emits the
   * HTML-escaped form of the String.
   *
   * If you are using this in a GXP, you are doing something wrong.  GXP is
   * designed to handle all escaping for you.
   */
  public static final HtmlClosure fromPlaintext(final String text) {
    Preconditions.checkNotNull(text);
    return new HtmlClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          CharEscapers.escape(out, CharEscapers.HTML_ESCAPE).append(text);
        }
      };
  }

  /**
   * Create a {@code HtmlClosure} based on a {@code Localizable}.
   *
   * The contents of the Localizable will be escaped on output.
   */
  public static final HtmlClosure fromLocalizable(final Localizable value) {
    Preconditions.checkNotNull(value);
    return new HtmlClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          CharEscapers.escape(out, CharEscapers.HTML_ESCAPE).append(
              value.toString(gxpContext.getLocale()));
        }
      };
  }

  /**
   * Create a {@code HtmlClosure} that will emit all the data avaliable from the
   * {@code File} at the time the closure is evaluated.
   */
  public static final HtmlClosure fromFile(final File file, final Charset charset) {
    Preconditions.checkNotNull(file);
    Preconditions.checkNotNull(charset);
    return new HtmlClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          FileInputStream fis = new FileInputStream(file);
          InputStreamReader isr = new InputStreamReader(fis, charset);
          Characters.copy(isr, out);
          isr.close();
          fis.close();
        }
      };
  }

  /**
   * Renders a sequence of {@link HtmlClosure} instances by calling their
   * write methods in order.
   *
   * @param closures A list of {@code HtmlClosure} objects to be rendered
   * @return A new {@code HtmlClosure} that renders the list
   */
  public static final HtmlClosure concat(final Iterable<? extends HtmlClosure> closures) {
    Preconditions.checkContentsNotNull(closures);
    final Iterable<HtmlClosure> closuresCopy = ImmutableList.copyOf(closures);
    return new HtmlClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          for (HtmlClosure closure : closuresCopy) {
            closure.write(out, gxpContext);
          }
        }
      };
  }

  /**
   * Renders a sequence of {@link HtmlClosure} instances by calling their
   * write methods in order. Varargs form of {@link #concat(Iterable)}.
   *
   * @param closures a series of closure objects to be rendered in order
   * @return A new {@code HtmlClosure} that renders the closures in order
   */
  public static final HtmlClosure concat(final HtmlClosure... closures) {
    return concat(ImmutableList.of(closures));
  }
}
