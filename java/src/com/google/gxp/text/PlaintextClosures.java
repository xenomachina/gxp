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

package com.google.gxp.text;

import com.google.common.base.CharEscapers;
import com.google.gxp.base.GxpClosure;
import com.google.gxp.base.GxpClosures;
import com.google.gxp.base.GxpContext;
import com.google.i18n.Localizable;

import java.io.IOException;

/**
 * Utility {@link PlaintextClosure}s
 */
public class PlaintextClosures {
  private PlaintextClosures() {}

  public static final PlaintextClosure EMPTY = new PlaintextClosure() {
      public void write(Appendable out, GxpContext context) {}
    };

  public static PlaintextClosure fromPlaintext(final String text) {
    return wrap(GxpClosures.fromString(text));
  }

  public static PlaintextClosure fromLocalizable(final Localizable value) {
    return wrap(GxpClosures.fromLocalizable(value, CharEscapers.nullEscaper()));
  }

  /**
   * Wrap a {@code GxpClosure} with a {@code PlaintextClosure}.
   */
  private static PlaintextClosure wrap(final GxpClosure closure) {
    return new PlaintextClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          closure.write(out, gxpContext);
        }
      };
  }
}
