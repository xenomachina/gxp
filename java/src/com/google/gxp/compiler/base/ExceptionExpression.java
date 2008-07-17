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
import com.google.gxp.compiler.schema.Schema;

/**
 * An Expression that throws an {@code Exception} at runtime.
 */
public class ExceptionExpression extends Expression {
  private final Kind exceptionType;
  private final String message;

  public enum Kind {
    // Probably more kinds of exceptions will be needed at some point...
    NOT_SUPPORTED_IN_SGML_MODE;
  }

  public ExceptionExpression(SourcePosition sourcePosition,
                             Schema schema,
                             Kind exceptionType,
                             String message) {
    super(sourcePosition, "exception", schema);
    this.exceptionType = Objects.nonNull(exceptionType);
    this.message = Objects.nonNull(message);
  }

  public Kind getKind() {
    return exceptionType;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitExceptionExpression(this);
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof ExceptionExpression)
        && equals((ExceptionExpression) that);
  }

  public boolean equals(ExceptionExpression that) {
    return equalsExpression(that)
        && Objects.equal(getKind(), that.getKind())
        && Objects.equal(getMessage(), that.getMessage());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        getKind(),
        getMessage());
  }
}
