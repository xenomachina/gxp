/*
 * Copyright (C) 2005 Google Inc.
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

package com.google.i18n;

import java.util.Locale;
import java.util.MissingResourceException;

/**
 * Interface for a {@link String} that can be localized into different
 * {@link Locale}s.
 */
public interface Localizable {
  /**
   * Returns the {@link String} for the specified {@link Locale}.
   *
   * @throws {@link MissingResourceException} if there is no localized
   * {@link String} for the specified {@link Locale}.
   */
  public String toString(Locale locale);
}
