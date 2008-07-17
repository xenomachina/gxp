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

package com.google.gxp.compiler.bind;

import com.google.common.collect.ImmutableSet;
import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.Tree;

import java.util.Set;

/**
 * The output of {@link Binder}.
 */
public class BoundTree extends Tree<Root> {
  private final ImmutableSet<Callable> requirements;

  public BoundTree(SourcePosition sourcePosition, AlertSet alerts, Root root,
                   Set<Callable> requirements) {
    super(sourcePosition, alerts, root);
    this.requirements = ImmutableSet.copyOf(requirements);
  }

  public Set<Callable> getRequirements() {
    return requirements;
  }
}
