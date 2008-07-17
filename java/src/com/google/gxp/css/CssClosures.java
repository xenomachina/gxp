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

import com.google.common.base.Preconditions;
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
  public static final CssClosure fromCss(final String css) {
    Preconditions.checkNotNull(css);
    return new CssClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          out.append(css);
        }
      };
  }
}
