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
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;

/**
 * A {@code Type} that refers to an GXP template with static methods.
 */
@SuppressWarnings("serial") // let java pick the serialVersionUID
public class TemplateType extends Type {
  private final TemplateName templateName;

  public TemplateType(Node fromNode, TemplateName templateName) {
    super(fromNode);
    this.templateName = Objects.nonNull(templateName);
  }

  public TemplateType(SourcePosition pos, String displayName, TemplateName templateName) {
    super(pos, displayName);
    this.templateName = Objects.nonNull(templateName);
  }

  public TemplateName getTemplateName() {
    return templateName;
  }

  @Override
  public boolean onlyAllowedInParam() {
    return true;
  }

  @Override
  public boolean takesDefaultParam() {
    return false;
  }

  @Override
  public Expression parseObjectConstant(String paramName,
                                        ObjectConstant objectConstant,
                                        AlertSink alertSink) {
    // TODO(harryh): this will never produce valid java code, should probably
    //               add an Alert
    return objectConstant.withType(this);
  }

  @Override
  public <T> T acceptTypeVisitor(TypeVisitor<T> visitor) {
    return visitor.visitTemplateType(this);
  }

  public boolean matches(Type that) {
    if (that instanceof TemplateType) {
      TemplateType thatInstance = (TemplateType) that;
      return Objects.equal(getTemplateName(), thatInstance.getTemplateName());
    }
    return false;
  }

  @Override
  public String toString() {
    return templateName.toString();
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof TemplateType && equals((TemplateType) that));
  }

  public boolean equals(TemplateType that) {
    return this.equalsType(that)
        && Objects.equal(getTemplateName(), that.getTemplateName());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        typeHashCode(),
        getTemplateName());
  }
}
