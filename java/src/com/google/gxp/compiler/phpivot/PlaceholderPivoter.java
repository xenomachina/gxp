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

package com.google.gxp.compiler.phpivot;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.BadNodePlacementError;
import com.google.gxp.compiler.base.Concatenation;
import com.google.gxp.compiler.base.Conditional;
import com.google.gxp.compiler.base.DefaultingExpressionVisitor;
import com.google.gxp.compiler.base.EscapeExpression;
import com.google.gxp.compiler.base.ExampleExpression;
import com.google.gxp.compiler.base.ExhaustiveExpressionVisitor;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.NoMessage;
import com.google.gxp.compiler.base.PlaceholderEnd;
import com.google.gxp.compiler.base.PlaceholderNode;
import com.google.gxp.compiler.base.PlaceholderStart;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.base.UnexpectedNodeException;
import com.google.gxp.compiler.flatten.ContentFlattenedTree;

import java.util.*;

/**
 * Converts {@code GxpNamespace.PHElement}/{@code GxpNamespace.EPHElement}
 * pairs into nodes with children (ie: the siblings between the original pair).
 * For example:
 *
 * <center><img src="https://www.corp.google.com/eng/designdocs/gxp/java-rewrite/PlaceholderPivoting.png"></center>
 */
public class PlaceholderPivoter implements Function<ContentFlattenedTree, PlaceholderPivotedTree> {

  public PlaceholderPivotedTree apply(ContentFlattenedTree tree) {
    AlertSetBuilder alertSetBuilder = new AlertSetBuilder(tree.getAlerts());
    Worker worker = new Worker(alertSetBuilder);
    Root root = tree.getRoot().acceptVisitor(worker.defaultVisitor);

    return new PlaceholderPivotedTree(tree.getSourcePosition(), alertSetBuilder.buildAndClear(),
                                      root);
  }

  private static class Worker {
    private final AlertSink alertSink;
    Worker(AlertSink alertSink) {
      this.alertSink = Preconditions.checkNotNull(alertSink);
    }

    private final ExhaustiveExpressionVisitor defaultVisitor =
        new ExhaustiveExpressionVisitor() {
          @Override
          public Expression visitConcatenation(Concatenation node) {
            ConcatenationVisitor concatVisitor = new ConcatenationVisitor(node);
            for (Expression subExpression : node.getValues()) {
              subExpression.acceptVisitor(concatVisitor);
            }

            return node.withValues(concatVisitor.getValues());
          }

          @Override
          public Expression visitPlaceholderStart(PlaceholderStart node) {
            alertSink.add(new BadNodePlacementError(node, null));
            return new StringConstant(node, node.getSchema(), "");
          }

          @Override
          public Expression visitPlaceholderEnd(PlaceholderEnd node) {
            alertSink.add(new EphMissingPhError(node));
            return new StringConstant(node, node.getSchema(), "");
          }
        };

    private class ConcatenationVisitor
        extends DefaultingExpressionVisitor<Void> {
      private final List<Expression> values = Lists.newArrayList();
      private final List<Expression> phChildren = Lists.newArrayList();

      private PlaceholderStart phStart;
      private List<Expression> destination = values;

      private final Concatenation concat;

      ConcatenationVisitor(Concatenation concat) {
        this.concat = Preconditions.checkNotNull(concat);
      }

      public List<Expression> getValues() {
        if (phStart != null) {
          alertSink.add(new PhMissingEphError(phStart));
        }
        return ImmutableList.copyOf(values);
      }

      @Override
      protected Void defaultVisitExpression(Expression node) {
        destination.add(node.acceptVisitor(defaultVisitor));
        return null;
      }

      @Override
      public Void visitPlaceholderStart(PlaceholderStart node) {
        if (phStart == null) {
          phStart = node;
          phChildren.clear();
          destination = phChildren;
        } else {
          alertSink.add(new BadNodePlacementError(node, phStart));
        }
        return null;
      }

      @Override
      public Void visitPlaceholderEnd(PlaceholderEnd node) {
        if (phStart == null) {
          alertSink.add(new EphMissingPhError(node));
        } else {
          Expression content =
              Concatenation.create(phStart.getSourcePosition(),
                                   concat.getSchema(),
                                   phChildren);
          if (content.alwaysEmpty()) {
            alertSink.add(new EmptyPlaceholderError(phStart));
          } else {
            String example = phStart.getExample();
            if (example == null) {
              example = createExample(alertSink, phStart, content);
            }
            values.add(new PlaceholderNode(phStart, phStart.getName(), example,
                                           content));
          }
          phStart = null;
          destination = values;
          phChildren.clear();
        }
        return null;
      }

      @Override
      public Void visitConcatenation(Concatenation node) {
        // Sanity check: concatenations should never be inside of
        // concatenations.
        throw new UnexpectedNodeException(node);
      }
    }

    private static String createExample(AlertSink alertSink,
                                        PlaceholderStart phStart,
                                        Expression content) {
      String result = content.acceptVisitor(GET_EXAMPLE_VISITOR);
      if (result == null) {
        alertSink.add(new PlaceholderRequiresExampleError(phStart));
        result = "<var>" + phStart.getName() + "</var>";
      }
      return result;
    }
  }

  private static final GetExampleVisitor GET_EXAMPLE_VISITOR =
      new GetExampleVisitor();

  private static class GetExampleVisitor
      extends DefaultingExpressionVisitor<String> {
    public String defaultVisitExpression(Expression value) {
      if (value.hasStaticString()) {
        return value.getStaticString(null, null);
      } else {
        return null;
      }
    }

    public String visitEscapeExpression(EscapeExpression expr) {
      return expr.getSubexpression().acceptVisitor(this);
    }

    public String visitNativeExpression(NativeExpression expr) {
      return expr.getExample();
    }

    public String visitNoMessage(NoMessage noMsg) {
      return noMsg.getSubexpression().acceptVisitor(this);
    }

    public String visitConcatenation(Concatenation concat) {
      StringBuilder sb = new StringBuilder();
      for (Expression value : concat.getValues()) {
        String subString = value.acceptVisitor(this);
        if (subString == null) {
          return null;
        } else {
          sb.append(subString);
        }
      }
      return sb.toString();
    }

    public String visitConditional(Conditional conditional) {
      List<Conditional.Clause> clauses = conditional.getClauses();
      if (clauses.size() == 1) {
        Expression predicate = clauses.get(0).getPredicate();
        if (predicate.alwaysEqualToXmlEnabled()) {
          return conditional.getElseExpression().acceptVisitor(this);
        }
      }

      return null;
    }

    public String visitExampleExpression(ExampleExpression value) {
      return value.getExample();
    }
  };
}
