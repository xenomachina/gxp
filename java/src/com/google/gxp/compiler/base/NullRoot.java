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
import com.google.gxp.compiler.schema.Schema;

import java.util.Collections;
import java.util.List;

/**
 * A null {@Root}. Appears in the tree when no appropriate {@code Root} could
 * be created.
 */
public class NullRoot extends AbstractNode implements Root {
  private final TemplateName.FullyQualified name;

  public NullRoot(Node fromNode, TemplateName.FullyQualified name) {
    super(fromNode);
    this.name = Objects.nonNull(name);
  }

  public TemplateName.FullyQualified getName() {
    return name;
  }

  public Schema getSchema() {
    return null;
  }

  public <T> T acceptVisitor(RootVisitor<T> visitor) {
    return visitor.visitNullRoot(this);
  }

  public Callable getCallable() {
    return null;
  }

  public InstanceCallable getInstanceCallable() {
    return null;
  }

  public Implementable getImplementable() {
    return null;
  }

  public List<Import> getImports() {
    return Collections.<Import>emptyList();
  }
}
