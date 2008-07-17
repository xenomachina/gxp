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
 * A GXP implements. Corresponds to a &lt;gxp:implements&gt; element.
 */
public abstract class ImplementsDeclaration extends AbstractNode {
  public ImplementsDeclaration(SourcePosition sourcePosition,
                               String displayName) {
    super(sourcePosition, displayName);
  }

  public ImplementsDeclaration(Node fromNode) {
    this(fromNode.getSourcePosition(), fromNode.getDisplayName());
  }

  public abstract <T> T acceptImplementsVisitor(ImplementsVisitor<T> v);

}
