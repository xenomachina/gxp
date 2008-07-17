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
import com.google.gxp.compiler.alerts.SourcePosition;

/**
 * A GXP class import. Corresponds to a &lt;gxp:import&gt; element with a
 * {@code class} attribute.
 */
public class ClassImport extends Import {
  private final TemplateName.FullyQualified className;

  /**
   * Creates a {@code ClassImport} of the specifed {@code
   * TemplateName.FullyQualified}.
   */
  public ClassImport(SourcePosition sourcePosition, String displayName,
                     TemplateName.FullyQualified className) {
    super(sourcePosition, displayName);
    this.className = Objects.nonNull(className);
    Objects.nonNull(className.getPackageName());
  }

  public ClassImport(Node fromNode, TemplateName.FullyQualified className) {
    super(fromNode);
    this.className = Objects.nonNull(className);
  }

  public TemplateName.FullyQualified getClassName() {
    return className;
  }

  @Override
  public <T> T acceptVisitor(ImportVisitor<T> visitor) {
    return visitor.visitClassImport(this);
  }

  @Override
  public String getTarget() {
    return getClassName().toString();
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof ClassImport) && equals((ClassImport)that);
  }

  public boolean equals(ClassImport that) {
    return getClassName().equals(that.getClassName());
  }
}
