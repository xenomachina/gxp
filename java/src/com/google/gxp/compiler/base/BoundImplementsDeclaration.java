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
import com.google.common.base.Objects;

/**
 * Represents a declaration that the enclosing template implements an
 * {@code Implementable}; {@code BoundImplementsDeclarations} are "bound"
 * because they have a reference to the {@code Implementable} object
 * referenced by the declaration
 */
public class BoundImplementsDeclaration extends ImplementsDeclaration {
  Implementable gxpInterface;

  public BoundImplementsDeclaration(Implementable gxpInterface,
                                    SourcePosition sourcePosition,
                                    String displayName) {
    super(sourcePosition, displayName);
    this.gxpInterface = Objects.nonNull(gxpInterface);
  }

  public BoundImplementsDeclaration(Implementable gxpInterface, Node node) {
    this(gxpInterface, node.getSourcePosition(), node.getDisplayName());
  }

  public Implementable getImplementable() {
    return gxpInterface;
  }

  public <T> T acceptImplementsVisitor(ImplementsVisitor<T> v) {
    return v.visitBoundImplementsDeclaration(this);
  }
}
