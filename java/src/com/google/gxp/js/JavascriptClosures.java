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

import com.google.common.base.Preconditions;
import com.google.gxp.base.GxpContext;

import java.io.IOException;

/**
 * Creates Javascript specific GXP Closures
 */
public class JavascriptClosures {
  private JavascriptClosures() {}

  public static final JavascriptClosure EMPTY = new JavascriptClosure() {
      public void write(Appendable out, GxpContext context) {}
    };

  /**
   * @return a {@code JavascriptClosure} that renders {@code js} as literal
   * Javascript
   */
  public static final JavascriptClosure fromJavascript(final String js) {
    Preconditions.checkNotNull(js);
    return new JavascriptClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          out.append(js);
        }
      };
  }
}
