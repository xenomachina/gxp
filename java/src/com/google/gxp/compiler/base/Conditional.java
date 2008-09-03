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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.schema.Schema;

import java.util.*;

/**
 * A conditional {@code Expression}. The internal representation of
 * &lt;gxp:cond&gt; and &lt;gxp:if&gt; and their nested clauses.
 */
public class Conditional extends Expression {
  private final ImmutableList<Clause> clauses;
  private final Expression elseExpression;

  /**
   * Every condition that has to be checked is a {@code Clause}.
   */
  public static class Clause extends AbstractNode {
    private final Expression predicate;
    private final Expression expression;

    public Clause(Node fromNode, Expression predicate, Expression expression) {
      this(fromNode.getSourcePosition(), fromNode.getDisplayName(), predicate,
           expression);
    }

    public Clause(SourcePosition sourcePosition, String displayName,
                  Expression predicate, Expression expression) {
      super(sourcePosition, displayName);
      this.predicate = Preconditions.checkNotNull(predicate);
      this.expression = Preconditions.checkNotNull(expression);
    }

    public Expression getPredicate() {
      return predicate;
    }

    public Expression getExpression() {
      return expression;
    }

    public Clause withExpression(Expression newExpression) {
      return (newExpression.equals(expression)) 
          ? this
          : new Clause(this, predicate, newExpression);
    }

    public boolean alwaysEquals(Clause that) {
      return predicate.alwaysEquals(that.predicate)
          && expression.alwaysEquals(that.expression);
    }

    @Override
    public boolean equals(Object that) {
      return (that instanceof Clause) && equals((Clause) that);
    }

    public boolean equals(Clause that) {
      return equalsAbstractNode(that)
          && predicate.equals(that.predicate)
          && expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(
        abstractNodeHashCode(),
        predicate,
        expression);
    }
  }

  public Conditional(Node fromNode, Schema schema,
                     List<Clause> clauses, Expression elseExpression) {
    super(fromNode, schema);
    if (clauses.isEmpty()) {
      throw new IllegalArgumentException("At least one clause is required.");
    }
    if (schema != null) {
      for (Clause clause : clauses) {
        Schema clauseSchema = clause.getExpression().getSchema();
        if (!schema.allows(clauseSchema)) {
          throw new IllegalArgumentException(
              "Incompatible schemas: " + clauseSchema + " clause in "
              + schema + " conditional");
        }
      }
      if (!Objects.equal(schema, elseExpression.getSchema())) {
        throw new IllegalArgumentException(
            "Incompatible schemas: " + elseExpression.getSchema()
            + " else in " + schema + " conditional");
      }
    }
    this.clauses = ImmutableList.copyOf(clauses);
    this.elseExpression = Preconditions.checkNotNull(elseExpression);
  }

  public Conditional(Node fromNode, Schema schema,
                     Expression predicate, Expression expression,
                     Expression elseExpression) {
    this(fromNode, schema,
         createClauses(fromNode, predicate, expression),
         elseExpression);
  }

  private static List<Clause> createClauses(Node fromNode,
                                            Expression predicate,
                                            Expression expression) {
    return ImmutableList.of(new Clause(fromNode, predicate, expression));
  }

  public Conditional withSchemaAndClauses(Schema newSchema,
                                          List<Clause> newClauses,
                                          Expression newElseExpression) {
    return (Objects.equal(getSchema(), newSchema)
            && Iterables.elementsEqual(newClauses, clauses)
            && elseExpression.equals(newElseExpression))
        ? this
        : new Conditional(this, newSchema, newClauses, newElseExpression);
  }

  public List<Clause> getClauses() {
    return clauses;
  }

  public Expression getElseExpression() {
    return elseExpression;
  }

  public Conditional withClauses(List<Clause> newClauses,
                                 Expression newElseExpression) {
    return (Iterables.elementsEqual(newClauses, clauses)
            && elseExpression.equals(newElseExpression))
        ? this
        : new Conditional(this, getSchema(), newClauses, newElseExpression);
  }

  @Override
  public boolean alwaysEquals(Expression that) {
    if ((!(that instanceof Conditional))
        || (that.getSchema() == null)
        || !that.getSchema().equals(getSchema())) {
      return false;
    }

    Conditional thatConditional = (Conditional) that;

    List<Clause> hisClauses = thatConditional.getClauses();
    if (clauses.size() != hisClauses.size()) {
      return false;
    }
    for (int i = 0; i < clauses.size(); i++) {
      if (!clauses.get(i).alwaysEquals(hisClauses.get(i))) {
        return false;
      }
    }
    return elseExpression.alwaysEquals(thatConditional.getElseExpression());
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitConditional(this);
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof Conditional) && equals((Conditional) that);
  }

  public boolean equals(Conditional that) {
    return equalsExpression(that)
        && Iterables.elementsEqual(clauses, that.clauses)
        && elseExpression.equals(that.elseExpression);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        clauses,
        elseExpression);
  }
}
