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
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.i18n.Localizable;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Utility {@code GxpClosure}s
 */
public final class GxpClosures {
  private GxpClosures() {}

  public static final GxpClosure EMPTY = new GxpClosure() {
      public void write(Appendable out, GxpContext context) {}
    };

  /**
   * Create a {@code GxpClosure} based on a {@code String}
   */
  public static GxpClosure fromString(final String value) {
    return fromString(value, CharEscapers.nullEscaper());
  }

  /**
   * Create a {@code GxpClosure} based on a {@code String} escaped with
   * the given {@code CharEscaper}.
   */
  public static GxpClosure fromString(final String value, final CharEscaper escaper) {
    Preconditions.checkNotNull(value);
    Preconditions.checkNotNull(escaper);
    return new GxpClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          escaper.escape(out).append(value);
        }
      };
  }

  /**
   * Create a {@code GxpClosure} based on a {@code Localizable} escaped with
   * the given {@code CharEscaper}.
   */
  public static GxpClosure fromLocalizable(final Localizable value, final CharEscaper escaper) {
    Preconditions.checkNotNull(value);
    Preconditions.checkNotNull(escaper);
    return new GxpClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          escaper.escape(out).append(value.toString(gxpContext.getLocale()));
        }
      };
  }

  /**
   * Create a {@code GxpClosure} that will emit all the data avaliable from the
   * {@code File} at the time the closure is evaluated.
   */
  public static GxpClosure fromFile(final File file, final Charset charset) {
    Preconditions.checkNotNull(file);
    Preconditions.checkNotNull(charset);
    return new GxpClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          FileInputStream fis = new FileInputStream(file);
          InputStreamReader isr = new InputStreamReader(fis, charset);
          CharStreams.copy(isr, out);
          isr.close();
          fis.close();
        }
      };
  }

  /**
   * Create a {@code GxpClosure} that will emit all the data avaliable from the
   * {@code Reader}. If the returned {@code GxpClosure} is evaluated more than
   * once, {@code reset()} will be called to reset the stream back to its origin
   * on all evaluations but the first.
   */
  public static GxpClosure fromReader(final Reader reader) {
    Preconditions.checkNotNull(reader);
    return new GxpClosure() {
        private boolean firstCall = true;
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          if (!firstCall) {
            reader.reset();
          }
          firstCall = false;
          CharStreams.copy(reader, out);
        }
      };
  }

  /**
   * Renders a sequence of {@link GxpClosure} instances by calling their
   * write methods in order.
   *
   * @param closures A list of {@code GxpClosure} objects to be rendered
   * @return A new {@code GxpClosure} that renders the list
   */
  public static GxpClosure concat(final Iterable<? extends GxpClosure> closures) {
    final Iterable<GxpClosure> closuresCopy = ImmutableList.copyOf(closures);
    return new GxpClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          for (GxpClosure closure : closuresCopy) {
            closure.write(out, gxpContext);
          }
        }
      };
  }

  /**
   * Renders a sequence of {@link GxpClosures} instances by calling their
   * write methods in order. Varargs form of {@link #concat(Iterable)}.
   *
   * @param closures a series of closure objects to be rendered in order
   * @return A new {@code GxpClosure} that renders the closures in order
   */
  public static GxpClosure concat(final GxpClosure... closures) {
    return concat(ImmutableList.copyOf(closures));
  }
}
