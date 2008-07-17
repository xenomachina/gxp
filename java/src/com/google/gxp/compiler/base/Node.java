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

import com.google.gxp.compiler.alerts.SourcePosition;

/**
 * An artifact generated from some source code. It's probably easiest
 * to think of a {@code Node} as an AST node, though most {@code Node}s are
 * only indirectly a result of parsing.
 */
public interface Node {
  /**
   * @return the {@link SourcePosition} of this {@code Node}.
   */
  SourcePosition getSourcePosition();

  /**
   * @return the name to use for this {@code Node} in {@code Alert}s.
   */
  String getDisplayName();
}