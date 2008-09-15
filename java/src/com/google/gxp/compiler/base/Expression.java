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
import com.google.gxp.compiler.alerts.common.StaticValueExpectedError;
import com.google.gxp.compiler.schema.Schema;

import java.util.*;

/**
 * A node that can (eventually) be evaluated.
 */
public abstract class Expression extends AbstractNode {
  private final Schema schema;

  protected Expression(SourcePosition sourcePosition, String displayName,
                       Schema schema) {
    super(sourcePosition, displayName);
    this.schema = schema;
  }

  protected Expression(Node fromNode, Schema schema) {
    super(fromNode);
    this.schema = schema;
  }

  protected Expression(Expression fromExpression) {
    super(fromExpression);
    this.schema = fromExpression.getSchema();
  }

  /**
   * The Schema of this Expression. Note that this may be null, which
   * indicates that the value is unknown, either because we haven't reached
   * the stage of the compilation where these types are determined, or that
   * the type is actually a native type (and hence, will be dealt with by the
   * OutputLanguage's compiler/interpreter).
   */
  public final Schema getSchema() {
    return schema;
  }

  public abstract <T> T acceptVisitor(ExpressionVisitor<T> visitor);

  /**
   * Indicates whether this {@code Expression} can be evaluated in the
   * given {@code OutputLanguage}. Generally always true except for some
   * {@code NativeExpression}s.
   */
  public boolean canEvaluateAs(OutputLanguage outputLanguage) {
    return true;
  }

  /**
   * Indicates whether this {@code Expression} always evaluates to an empty
   * string. Generally false except for empty {@code StringConstant}s.
   */
  public boolean alwaysEmpty() {
    return false;
  }

  /**
   * Indicates whether this {@code Expression} always evaluates to a string
   * containing only whitespace (or an empty string). Generally false except
   * for empty {@code StringConstant}s.
   */
  public boolean alwaysOnlyWhitespace() {
    return alwaysEmpty();
  }

  /**
   * Indicates whether this {@code Expression} will always evaluates to the
   * same value as {@code that} when evaluated in the same scope. Default
   * implementation conservatively returns {@code false}.
   */
  public boolean alwaysEquals(Expression that) {
    return false;
  }

  /**
   * Indicates whether this {@code Expression} always evaluates to the
   * specified value. Generally false except for {@code BooleanConstant}s that
   * have this value.
   */
  public boolean alwaysEquals(boolean value) {
    return false;
  }

  /**
   * Indicates whether this {@code Expression} always evaluates to true if and
   * only if XML mode is enabled.  Generally false except for {@code
   * IsXmlExpression}s.
   */
  public boolean alwaysEqualToXmlEnabled() {
    return false;
  }

  @Override
  public abstract boolean equals(Object that);

  /**
   * Checks equality of the fields defined in Expression. Returns true iff the
   * fields defined in Expression are equal in this and that. Comes in handy
   * when implementing equals(Object) in subclasses.
   */
  protected final boolean equalsExpression(Expression that) {
    return equalsAbstractNode(that)
        && Objects.equal(getSchema(), that.getSchema());
  }

  @Override
  public abstract int hashCode();

  protected final int expressionHashCode() {
    return Objects.hashCode(
        abstractNodeHashCode(),
        getSchema());
  }

  /**
   * Attempts to statically evaluate this Expression as a string. If
   * hasStaticString would return false then an alert will be emitted to the
   * specified AlertSink and the fallback will be returned.
   *
   * <p>The default implementation of this method generates a
   * StaticAttributeExpectedError and returns the fallback.
   */
  public String getStaticString(AlertSink alertSink, String fallback) {
    alertSink.add(new StaticValueExpectedError(this));
    return fallback;
  }

  /**
   * @return true if and only if the Expression can be evaluated statically and
   * it evaluates to a string.
   */
  public boolean hasStaticString() {
    return false;
  }

  /**
   * Separates a concatenation into a list of its sub-expressions. For
   * non-{@code Concatenation}s, this just returns a list containing only that
   * expression.
   */
  public List<Expression> separate() {
    return Collections.singletonList(this);
  }
}
