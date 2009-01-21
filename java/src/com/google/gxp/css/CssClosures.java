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

import com.google.gxp.base.GxpClosure;
import com.google.gxp.base.GxpClosures;
import com.google.gxp.base.GxpContext;

import java.io.IOException;

/**
 * Container for CSS specific GXP Closures
 */
public class CssClosures {
  private CssClosures() {}

  public static final CssClosure EMPTY = new CssClosure() {
      public void write(Appendable out, GxpContext context) {}
    };

  /**
   * @return a {@code CssClosure} that renders {@code css} as literal
   * CSS
   */
  public static CssClosure fromCss(final String css) {
    return wrap(GxpClosures.fromString(css));
  }

  /**
   * Renders a sequence of {@link CssClosure} instances by calling
   * their write methods in order.
   *
   * @param closures A list of {@code CssClosure} objects to be rendered
   * @return A new {@code CssClosure} that renders the list
   */
  public static CssClosure concat(final Iterable<? extends CssClosure> closures) {
    return wrap(GxpClosures.concat(closures));
  }

  /**
   * Renders a sequence of {@link CssClosure} instances by calling their
   * write methods in order. Varargs form of {@link #concat(Iterable)}.
   *
   * @param closures a series of closure objects to be rendered in order
   * @return A new {@code CssClosure} that renders the closures in order
   */
  public static CssClosure concat(final CssClosure... closures) {
    return wrap(GxpClosures.concat(closures));
  }

  /**
   * Wrap a {@code GxpClosure} with a {@code CssClosure}.
   */
  private static CssClosure wrap(final GxpClosure closure) {
    return new CssClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          closure.write(out, gxpContext);
        }
      };
  }
}
