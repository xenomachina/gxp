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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.schema.Schema;

import java.util.*;

/**
 * An {@code Expression} which concatenates other {@code Expression}s together.
 */
public class Concatenation extends Expression {
  private final ImmutableList<Expression> values;

  private Concatenation(Node fromNode, Schema schema,
                        List<Expression> values) {
    super(fromNode, schema);
    this.values = ImmutableList.copyOf(values);
    if (values.size() < 2) {
      throw new IllegalArgumentException(
          "Concatenation cannot have only " + values.size() + " values.");
    }
  }

  /**
   * @param sourcePosition sourcePosition of the result, used only when
   * concatentation turns out to be empty. (otherwise the values will be used
   * to compute this)
   * @param values values to concatenate.
   * @return concatenated Expression. This may be a Concatenation, though it
   * some cases constant folding will result in it being a different type of
   * Expression.
   */
  public static Expression create(SourcePosition sourcePosition,
                                  Schema schema,
                                  List<Expression> values) {
    // TODO(laurence): add more unit tests for this
    // TODO(laurence): add some better consistency checks for concatenating
    // nodes of inconsistent type. This is necessary for content flattener, as
    // it has to deal with cases like "foo <script>bar</script" (where "foo" is
    // HTML but "bar" is JavaScript). Almost need a "reinterpret cast" node to
    // make this make sense.
    values = simplify(schema, values);
    int len = values.size();
    if (len == 0) {
      return new StringConstant(sourcePosition, schema, "");
    } else if (len == 1) {
      return values.get(0);
    } else {
      return new Concatenation(values.get(0), schema, values);
    }
  }

  public List<Expression> getValues() {
    return values;
  }

  public Expression withValues(List<Expression> newValues) {
    return Iterables.elementsEqual(newValues, values)
        ? this
        : create(getSourcePosition(), getSchema(), newValues);
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitConcatenation(this);
  }

  /**
   * Simplifies a list of values that are to be concatenated by flattening out
   * nested {@code Concatenation}s and merging adjacent {@code
   * StringConstant}s.
   */
  private static final List<Expression> simplify(Schema schema,
                                                 List<Expression> values) {
    List<Expression> result = Lists.newArrayList();
    Node fromNode = null;
    StringBuilder sb = new StringBuilder();
    fromNode = simplifyHelper(schema, values, result, fromNode, sb);
    if ((fromNode != null) && (sb.length() > 0)) {
      result.add(new StringConstant(fromNode, schema,
                                    sb.toString()));
    }
    return result;
  }

  /**
   * Helper function for simplify.
   * @param values the input values to be concatenated
   * @param result the list to append the sub values of the concatenation to
   * @param fromNode the Node from which the currently accumulated string was
   * derived from, or null if there is no currently accumulated string
   * @param sb the currently accumulated string
   * @return the new value for fromNode
   */
  private static final Node simplifyHelper(Schema schema,
                                           List<Expression> values,
                                           List<Expression> result,
                                           Node fromNode,
                                           StringBuilder sb) {
    for (Expression value : values) {
      value = Objects.nonNull(value);
      if (value instanceof StringConstant) {
        if (fromNode == null) {
          fromNode = value;
        }
        StringConstant str = (StringConstant) value;
        // TODO(laurence): this is another case where it's kind of weird that
        // we just take one content type and start treating it like it's
        // another. See "todo" above which discusses "reinterpret cast".
        sb.append(str.evaluate());
      } else if (value instanceof Concatenation) {
        Concatenation subConcatenation = (Concatenation) value;
        fromNode = simplifyHelper(schema, subConcatenation.values,
                                  result, fromNode, sb);
      } else {
        if (fromNode != null) {
          if (sb.length() > 0) {
            result.add(new StringConstant(fromNode, schema, sb.toString()));
            sb.setLength(0);
          }
          fromNode = null;
        }
        result.add(value);
      }
    }
    return fromNode;
  }

  @Override
  public boolean hasStaticString() {
    for (Expression e : getValues()) {
      if (!e.hasStaticString()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof Concatenation) && equals((Concatenation) that);
  }

  public boolean equals(Concatenation that) {
    return equalsExpression(that)
        && Iterables.elementsEqual(values, that.values);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        values);
  }

  @Override
  public List<Expression> separate() {
    return getValues();
  }
}
