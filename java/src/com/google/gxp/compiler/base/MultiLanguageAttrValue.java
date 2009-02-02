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

package com.google.gxp.compiler.base;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * An attribute that can be specified with varying language namespace prefixes
 * to indicated that different values should be used for different
 * {@code OutputLanguage}s.
 */
@SuppressWarnings("serial") // let java pick the SerialVersionUID
public class MultiLanguageAttrValue implements Serializable {
  private final ImmutableMap<OutputLanguage, String> map;
  private final String defaultValue;

  public MultiLanguageAttrValue(Map<OutputLanguage, String> map, String defaultValue) {
    this.map = ImmutableMap.copyOf(map);
    this.defaultValue = defaultValue;
  }

  public MultiLanguageAttrValue(String defaultValue) {
    this(ImmutableMap.<OutputLanguage, String>of(), Preconditions.checkNotNull(defaultValue));
  }

  public String get(OutputLanguage outputLanguage) {
    return map.containsKey(outputLanguage)
        ? map.get(outputLanguage)
        : defaultValue;
  }

  public String getDefault() {
    return defaultValue;
  }

  public boolean isEmpty() {
    return map.isEmpty() && (defaultValue == null);
  }

  private static final Pattern TRIVIAL_EVAL = Pattern.compile("\\w+");

  public boolean isTrivialEval() {
    return map.isEmpty() && defaultValue != null &&
        TRIVIAL_EVAL.matcher(defaultValue).matches();
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || ((that instanceof MultiLanguageAttrValue) && equals((MultiLanguageAttrValue) that));
  }

  public boolean equals(MultiLanguageAttrValue that) {
    return Objects.equal(map, that.map)
        && Objects.equal(defaultValue, that.defaultValue);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        map,
        defaultValue);
  }
}
