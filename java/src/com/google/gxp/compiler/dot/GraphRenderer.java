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

package com.google.gxp.compiler.dot;

/**
 * Renders a set of objects as a graph. Conceptually, converts {@code T}'s into
 * a graph and then walks said graph and generates "events" on the specified
 * {@code GraphSink}. In practice, implementations will typically not actually
 * reify a graph, but instead generate the "events" based on the structure of
 * the passed in objects directly.
 *
 * @param <T> the type of object this {@code GraphRenderer} is able to render
 */
public interface GraphRenderer<T> {
  /**
   * Renders a graph of {@code objects} to {@code out}.
   */
  void renderGraph(GraphSink out, Iterable<? extends T> objects);
}
