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
 * {@code Implementable}; {@code UnboundImplementsDeclarations} are "unbound"
 * because a {@code TemplateName} for the {@code Implementable} is known, but
 * it is unknown whether an {@code Implementable} with that {@code TemplateName}
 * exists.
 */
public class UnboundImplementsDeclaration extends ImplementsDeclaration {
  TemplateName templateName;

  public UnboundImplementsDeclaration(SourcePosition sourcePosition,
                                      String displayName,
                                      TemplateName templateName) {
    super(sourcePosition, displayName);
    this.templateName = Objects.nonNull(templateName);
  }

  public UnboundImplementsDeclaration(Node fromNode, TemplateName templateName) {
    this(fromNode.getSourcePosition(), fromNode.getDisplayName(), templateName);
  }

  public TemplateName getTemplateName() {
    return templateName;
  }

  public <T> T acceptImplementsVisitor(ImplementsVisitor<T> v) {
    return v.visitUnboundImplementsDeclaration(this);
  }
}
