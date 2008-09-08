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
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.schema.Schema;

/**
 * A native (that is, in the output language) expression.
 */
public class NativeExpression extends Expression {
  private final MultiLanguageAttrValue nativeCode;
  private final String example;
  private final String phName;

  public NativeExpression(SourcePosition sourcePosition, String displayName,
                          MultiLanguageAttrValue nativeCode, String example, String phName) {
    super(sourcePosition, displayName, null);
    this.nativeCode = Preconditions.checkNotNull(nativeCode);
    this.example = example;
    this.phName = phName;
  }

  public NativeExpression(Node fromNode, MultiLanguageAttrValue nativeCode,
                          String example, String phName) {
    super(fromNode, null);
    this.nativeCode = Preconditions.checkNotNull(nativeCode);
    this.example = example;
    this.phName = phName;
  }

  public NativeExpression(SourcePosition pos, String displayName,
                          MultiLanguageAttrValue nativeCode) {
    this(pos, displayName, nativeCode, null, null);
  }

  public NativeExpression(Node fromNode, MultiLanguageAttrValue nativeCode) {
    this(fromNode, nativeCode, null, null);
  }

  public NativeExpression(Node fromNode, MultiLanguageAttrValue nativeCode, Schema schema) {
    super(fromNode, schema);
    this.nativeCode = Preconditions.checkNotNull(nativeCode);
    this.example = null;
    this.phName = null;
  }

  public String getNativeCode(OutputLanguage outputLanguage) {
    return nativeCode.get(outputLanguage);
  }

  public String getDefaultNativeCode() {
    return nativeCode.getDefault();
  }

  public String getExample() {
    return example;
  }

  public String getPhName() {
    return phName;
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitNativeExpression(this);
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof NativeExpression)
        && equals((NativeExpression) that);
  }

  public boolean equals(NativeExpression that) {
    return equalsExpression(that)
        && nativeCode.equals(that.nativeCode)
        && Objects.equal(example, example)
        && Objects.equal(phName, that.phName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        nativeCode,
        example,
        phName);
  }
}
