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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.TemplateName;

import java.util.*;
import java.io.Serializable;

/**
 * A node in a {@code DependencyGraph}. Represents the relationship between a
 * CompilationUnit and its requirements (the things it depends on).
 */
@SuppressWarnings("serial") // We want the javac-generated serialVersionUID.
public class DependencyNode implements Serializable {
  private final TemplateName.FullyQualified name;
  private final long lastModified;
  private final Set<Callable> requirements;

  public DependencyNode(TemplateName.FullyQualified name,
                        long lastModified,
                        Set<Callable> requirements) {
    this.name = Preconditions.checkNotNull(name);
    this.lastModified = lastModified;
    this.requirements = ImmutableSet.copyOf(requirements);
  }

  public TemplateName.FullyQualified getName() {
    return name;
  }

  public long getLastModified() {
    return lastModified;
  }

  public Set<Callable> getRequirements() {
    return requirements;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof DependencyNode && equals((DependencyNode) that));
  }

  public boolean equals(DependencyNode that) {
    return Objects.equal(getName(), that.getName())
        && (getLastModified() == that.getLastModified())
        && Objects.equal(getRequirements(), that.getRequirements());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        getName(),
        getLastModified(),
        getRequirements());
  }
}
