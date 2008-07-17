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
 * A {@code Type} that refers to an instantiated GXP template.
 */
@SuppressWarnings("serial") // let java pick the serialVersionUID
public class InstanceType extends TemplateType {
  public InstanceType(Node fromNode, TemplateName templateName) {
    super(fromNode, templateName);
  }

  public InstanceType(SourcePosition pos, String displayName, TemplateName templateName) {
    super(pos, displayName, templateName);
  }

  @Override
  public <T> T acceptTypeVisitor(TypeVisitor<T> visitor) {
    return visitor.visitInstanceType(this);
  }

  @Override
  public boolean matches(Type that) {
    if (that instanceof InstanceType) {
      InstanceType thatInstance = (InstanceType) that;
      return Objects.equal(getTemplateName(), thatInstance.getTemplateName());
    }
    return false;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof InstanceType && equals((InstanceType) that));
  }

  public boolean equals(InstanceType that) {
    return this.equalsType(that)
        && Objects.equal(getTemplateName(), that.getTemplateName());
  }
}
