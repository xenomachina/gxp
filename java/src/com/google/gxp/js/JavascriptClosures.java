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

package com.google.gxp.js;

import com.google.gxp.base.GxpClosure;
import com.google.gxp.base.GxpClosures;
import com.google.gxp.base.GxpContext;

import java.io.IOException;

/**
 * Creates Javascript specific GXP Closures
 */
public final class JavascriptClosures {
  private JavascriptClosures() {}

  public static final JavascriptClosure EMPTY = new JavascriptClosure() {
      public void write(Appendable out, GxpContext context) {}
    };

  /**
   * @return a {@code JavascriptClosure} that renders {@code js} as literal
   * Javascript
   */
  public static JavascriptClosure fromJavascript(final String js) {
    return wrap(GxpClosures.fromString(js));
  }

  /**
   * Renders a sequence of {@link JavascriptClosure} instances by calling
   * their write methods in order.
   *
   * @param closures A list of {@code JavascriptClosure} objects to be rendered
   * @return A new {@code JavascriptClosure} that renders the list
   */
  public static JavascriptClosure concat(final Iterable<? extends JavascriptClosure> closures) {
    return wrap(GxpClosures.concat(closures));
  }

  /**
   * Renders a sequence of {@link JavascriptClosure} instances by calling their
   * write methods in order. Varargs form of {@link #concat(Iterable)}.
   *
   * @param closures a series of closure objects to be rendered in order
   * @return A new {@code JavascriptClosure} that renders the closures in order
   */
  public static JavascriptClosure concat(final JavascriptClosure... closures) {
    return wrap(GxpClosures.concat(closures));
  }

  /**
   * Wrap a {@code GxpClosure} with a {@code JavascriptClosure}.
   */
  private static JavascriptClosure wrap(final GxpClosure closure) {
    return new JavascriptClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          closure.write(out, gxpContext);
        }
      };
  }
}
