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
import com.google.common.collect.ImmutableList;
import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.alerts.SourcePosition;

import java.util.*;

/**
 * A forest of {@code Node} trees. Distinguished from other {@code Node}s by
 * holding the set of {@code Alert}s associated with the forest.
 *
 * @param <E> type of child nodes
 */
public abstract class Forest<E extends Node> extends AbstractNode {
  private final AlertSet alerts;
  private final ImmutableList<E> children;

  /**
   * @param sourcePosition the {@link SourcePosition} of this {@code Forest}
   * @param alerts the {@link com.google.gxp.compiler.alerts.Alert}s associated
   * with this {@code Forest}
   * @param children the children of this {@code Forest}
   */
  protected Forest(SourcePosition sourcePosition, AlertSet alerts,
                   List<E> children) {
    super(sourcePosition, sourcePosition.getSourceName());
    this.alerts = Objects.nonNull(alerts);
    this.children = ImmutableList.copyOf(children);
  }

  /**
   * @return the set of {@link com.google.gxp.compiler.alerts.Alert}s
   * generated by the production of this {@code Forest}.
   */
  public AlertSet getAlerts() {
    return alerts;
  }

  /**
   * @return the children of this {@code Forest}.
   */
  public List<E> getChildren() {
    return children;
  }
}
