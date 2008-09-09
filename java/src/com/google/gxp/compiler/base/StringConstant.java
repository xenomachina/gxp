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

import com.google.common.base.CharEscapers;
import com.google.common.base.Objects;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.schema.Schema;

import java.util.regex.Pattern;

/**
 * A {@code Expression} that can be evaluated at compile time.
 */
public class StringConstant extends Expression {
  // TODO(laurence): remember unescaped value for debugging?
  private final String value;

  public StringConstant(Node fromNode, Schema schema, String value) {
    super(fromNode, schema);
    this.value = value;
  }

  public StringConstant(SourcePosition pos, Schema schema, String value) {
    this(pos, "text", schema, value);
  }

  private StringConstant(SourcePosition sourcePosition, String displayName,
                         Schema schema, String value) {
    super(sourcePosition, displayName, schema);
    this.value = value;
  }

  public String evaluate() {
    return value;
  }

  private static final Pattern ONLY_SPACES =
      Pattern.compile("^\\s*$", Pattern.DOTALL);

  @Override
  public boolean alwaysEmpty() {
    return value.length() == 0;
  }

  @Override
  public boolean alwaysOnlyWhitespace() {
    return ONLY_SPACES.matcher(value).matches();
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitStringConstant(this);
  }

  @Override
  public boolean alwaysEquals(Expression that) {
    // XXX: treat as not always equal if either Schema is null as null
    // means "unknown" (and hence, may not actually be equal)
    return (that instanceof StringConstant)
        && ((StringConstant) that).evaluate() == evaluate()
        && (that.getSchema() != null)
        && (that.getSchema().equals(getSchema()));
  }

  @Override
  public String toString() {
    return getDisplayName() + "=StringConstant@" + getSourcePosition()
        + "(\"" +  CharEscapers.javaStringEscaper().escape(evaluate()) + "\")";
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof StringConstant) && equals((StringConstant) that);
  }

  public boolean equals(StringConstant that) {
    return equalsExpression(that)
        && value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        value);
  }

  /**
   * @return the same value as {@link #evaluate()}.
   */
  @Override
  public String getStaticString(AlertSink alertSink, String fallback) {
    return evaluate();
  }

  @Override
  public boolean hasStaticString() {
    return true;
  }
}
