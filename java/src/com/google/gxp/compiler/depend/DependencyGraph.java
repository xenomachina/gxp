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

package com.google.gxp.compiler.depend;

import com.google.common.collect.ImmutableMap;
import com.google.gxp.compiler.CompilationManager;
import com.google.gxp.compiler.CompilationSet;
import com.google.gxp.compiler.CompilationTask;
import com.google.gxp.compiler.CompilationUnit;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.TemplateName;

import java.util.Map;
import java.util.Set;

/**
 * A graph of DependencyNode instances.
 */
@SuppressWarnings("serial") // let java pick the SerialVersionUID
public class DependencyGraph implements CompilationManager {
  private final Map<TemplateName.FullyQualified, DependencyNode> nodes;

  public DependencyGraph(CompilationSet cSet) {
    ImmutableMap.Builder<TemplateName.FullyQualified, DependencyNode> mapBuilder =
        ImmutableMap.builder();
    for (CompilationUnit unit : cSet.getCompilationUnits()) {
      Set<Callable> requirements = getRequirements(unit);
      TemplateName.FullyQualified name = unit.getTemplateName();
      DependencyNode node = new DependencyNode(name, getLastModified(unit),
                                               requirements);
      mapBuilder.put(name, node);
    }
    this.nodes = mapBuilder.build();
  }

  private Set<Callable> getRequirements(CompilationUnit unit) {
    return unit.getBoundTree().getRequirements();
  }

  private static long getLastModified(CompilationUnit unit) {
    return unit.getSourceFileRef().getLastModified();
  }

  // implements CompilationManager
  public boolean sourceChanged(CompilationTask task) {
    CompilationUnit unit = task.getCompilationUnit();
    DependencyNode node = nodes.get(unit.getTemplateName());
    if (node != null) {
      long oldTimestamp = node.getLastModified();
      if (oldTimestamp > 0L) {
        return oldTimestamp != getLastModified(unit);
      }
    }
    // We never saw the file before or couldn't get a reliable timestamp,
    // so play it safe.
    return true;
  }

  // implements CompilationManager
  public boolean usedInterfacesChanged(CompilationTask task) {
    CompilationUnit unit = task.getCompilationUnit();
    DependencyNode node = nodes.get(unit.getTemplateName());
    if (node != null) {
      Set<Callable> oldRequirements = node.getRequirements();
      return !oldRequirements.equals(getRequirements(unit));
    }
    // We never saw the file before so play it safe.
    return true;
  }
}
