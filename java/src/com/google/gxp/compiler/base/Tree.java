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
import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.alerts.SourcePosition;

import java.util.Collections;

/**
 * A {@code Forest}, but with only one child: the "root".
 *
 * @param <E> type of root node
 */
public abstract class Tree<E extends Root> extends Forest<E> {
  /**
   * @param sourcePosition the {@link SourcePosition} of this {@code Tree}
   * @param alerts the {@link com.google.gxp.compiler.alerts.Alert}s
   * associated with this {@code Tree}
   * @param root the only child of this {@code Tree}
   */
  protected Tree(SourcePosition sourcePosition, AlertSet alerts, E root) {
    super(sourcePosition, alerts, Collections.singletonList(Objects.nonNull(root)));
  }

  /**
   * @return the root of this tree, or null if there is none.
   */
  public E getRoot() {
    return getChildren().get(0);
  }
}
