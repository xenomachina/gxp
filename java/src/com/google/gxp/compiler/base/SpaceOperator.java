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

import com.google.common.base.Function;

import java.util.regex.Pattern;

/**
 * The operators for space collapsing.
 */
public enum SpaceOperator implements Function<String,String> {
  /**
   * Leaves input alone. The identity function.
   */
  PRESERVE {
    @Override
    public String impl(String s) {
      return s;
    }
  },

  /**
   * Destroys all input.
   */
  REMOVE {
    @Override
    public String impl(String s) {
      return "";
    }
  },

  /**
   * Converts all non-empty input to a single space and leaves empty input
   * alone.
   */
  NORMALIZE {
    @Override
    public String impl(String s) {
      return (s.length() > 0) ? " " : "";
    }
  },

  /**
   * Converts input that contains vertical whitespace into a newline, all other
   * non-empty input into a single space, and leaves empty input alone.
   */
  COLLAPSE {
    @Override
    public String impl(String s) {
      if (s.contains("\n") || s.contains("\f")) {
        return "\n";
      } else {
        return NORMALIZE.impl(s);
      }
    }
  };

  protected abstract String impl(String s);

  /**
   * {@inheritdoc}
   *
   * @throws IllegalArgumentException if input contains non-whitespace
   * characters.
   */
  public String apply(String s) {
    if (WHITESPACE_ONLY.matcher(s).matches()) {
      return impl(s);
    } else {
      throw new IllegalArgumentException(
          "Cannot apply SpaceOperator to non-spaces!");
    }
  }

  private static final Pattern WHITESPACE_ONLY = Pattern.compile("^\\s*$");
}
