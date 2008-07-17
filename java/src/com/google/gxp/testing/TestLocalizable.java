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

package com.google.gxp.testing;

import com.google.i18n.Localizable;

import java.util.Locale;

/**
 * Simple implementation of {@link Localizable} useful for testing purposes.
 */
public class TestLocalizable implements Localizable {
  /**
   * Returns string {@code "[toString()]"}.
   */
  public String toString() {
    return "[toString()]";
  }

  /**
   * Returns string <code>"[toString(<var>locale</var>)]"</code>, where
   * <code><var>locale</var></code> is the string form of the supplied {@code
   * Locale}. For example, if the parameter is {@code Locale.US} then the result
   * will be {@code "[toString(en_US)]"}.
   */
  public String toString(Locale locale) {
    return "[toString(" + locale + ")]";
  }
}
