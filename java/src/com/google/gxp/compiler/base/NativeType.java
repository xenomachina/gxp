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
import com.google.common.base.Preconditions;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;

/**
 * Native types are essentially black-boxes as far as GXP is concerned. The
 * output language is responsible for doing whatever type checking it can with
 * these.
 */
@SuppressWarnings("serial") // let java pick the serialVersionUID
public class NativeType extends Type {
  private MultiLanguageAttrValue nativeType;

  public NativeType(SourcePosition sourcePosition, String displayName,
                    MultiLanguageAttrValue nativeType) {
    super(sourcePosition, displayName);
    this.nativeType = Preconditions.checkNotNull(nativeType);
  }

  public NativeType(Node fromNode, MultiLanguageAttrValue nativeType) {
    this(fromNode.getSourcePosition(), fromNode.getDisplayName(), nativeType);
  }

  // TODO(harryh): this constructor should go away at some point
  public NativeType(Node fromNode, String type) {
    this(fromNode, new MultiLanguageAttrValue(type));
  }

  public String getNativeType(OutputLanguage outputLanguage) {
    return nativeType.get(outputLanguage);
  }

  @Override
  public boolean onlyAllowedInParam() {
    return false;
  }

  @Override
  public boolean takesDefaultParam() {
    return true;
  }

  @Override
  public boolean takesRegexParam() {
    return true;
  }

  @Override
  public boolean takesConstructorParam() {
    return true;
  }

  @Override
  public Expression parseObjectConstant(String paramName,
                                        ObjectConstant objectConstant,
                                        AlertSink alertSink) {
    return objectConstant.withType(this);
  }

  @Override
  public <T> T acceptTypeVisitor(TypeVisitor<T> visitor) {
    return visitor.visitNativeType(this);
  }

  @Override
  public String toString() {
    return "NativeType";
  }

  @Override
  public boolean matches(Type that) {
    // You might think that we should be comparing nativeType here, but that
    // would be wrong.  "java.lang.String" matches "String" etc.
    return (that instanceof NativeType);
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof NativeType && equals((NativeType) that));
  }

  public boolean equals(NativeType that) {
    return this.equalsType(that)
        && Objects.equal(nativeType, that.nativeType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        typeHashCode(),
        nativeType);
  }
}
