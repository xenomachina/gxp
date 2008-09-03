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

package com.google.gxp.compiler.functests;

import java.util.Iterator;

/**
 * Trivial {@code Iterable} that will count from 0 to (max - 1).
 */
public class IntIterable implements Iterable<Integer> {
  private final int max;

  public IntIterable(int max) {
    this.max = max;
  }

  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {
      private int current = 0;

      public boolean hasNext() {
        return (current < max);
      }

      public Integer next() {
        current++;
        return (current - 1);
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
