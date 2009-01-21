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

package com.google.gxp.rss;

import com.google.gxp.base.GxpClosure;
import com.google.gxp.base.GxpClosures;
import com.google.gxp.base.GxpContext;

import java.io.IOException;

/**
 * Utility {@code RssClosure}s
 */
public class RssClosures {
  private RssClosures() {}

  public static final RssClosure EMPTY = new RssClosure() {
      public void write(Appendable out, GxpContext context) {}
    };

  /**
   * Convert a rss String into a {@code RssClosure} that emits the
   * String without any escaping.
   */
  public static RssClosure fromRss(final String rss) {
    return wrap(GxpClosures.fromString(rss));
  }

  /**
   * Wrap a {@code GxpClosure} with a {@code RssClosure}.
   */
  private static RssClosure wrap(final GxpClosure closure) {
    return new RssClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          closure.write(out, gxpContext);
        }
      };
  }
}
