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

package com.google.gxp.compiler.phinsert;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.base.Concatenation;
import com.google.gxp.compiler.base.ExhaustiveExpressionVisitor;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.OutputElement;
import com.google.gxp.compiler.base.PlaceholderStart;
import com.google.gxp.compiler.base.PlaceholderEnd;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.Util;
import com.google.gxp.compiler.collapse.SpaceCollapsedTree;
import com.google.gxp.compiler.schema.Schema;

import java.util.List;

/**
 * Inserts Placeholder nodes as appropriate for elements containing
 * {@code gxp:ph} attributes.
 */
public class PlaceholderInserter implements Function<SpaceCollapsedTree, PlaceholderInsertedTree> {

  public PlaceholderInsertedTree apply(SpaceCollapsedTree tree) {
    AlertSetBuilder alertSetBuilder = new AlertSetBuilder(tree.getAlerts());
    Root root = tree.getRoot().acceptVisitor(new Visitor(alertSetBuilder));

    return new PlaceholderInsertedTree(tree.getSourcePosition(), alertSetBuilder.buildAndClear(),
                                       root);
  }

  private static class Visitor extends ExhaustiveExpressionVisitor {
    private final AlertSink alertSink;

    Visitor(AlertSink alertSink) {
      this.alertSink = Objects.nonNull(alertSink);
    }

    @Override
    public Expression visitNativeExpression(NativeExpression expr) {
      String phName = expr.getPhName();
      if (phName == null) {
        return expr;
      }

      Schema exprSchema = expr.getSchema();
      List<Expression> values = Lists.newArrayList();

      values.add(new PlaceholderStart(expr, exprSchema, phName, null));
      values.add(expr);
      values.add(new PlaceholderEnd(expr, exprSchema));

      return Concatenation.create(expr.getSourcePosition(), exprSchema,
                                  values);
    }

    @Override
    public Expression visitOutputElement(OutputElement element) {
      String phName = element.getPhName();
      if (phName == null) {
        return super.visitOutputElement(element);
      }

      Schema elementSchema = element.getSchema();
      boolean whitespaceContent = element.getContent().alwaysOnlyWhitespace();
      List<Expression> values = Lists.newArrayList();

      String name = whitespaceContent ? phName : phName + "_start";
      values.add(new PlaceholderStart(element, elementSchema, name, null));
      if (whitespaceContent) {
        values.add(element);
      } else {
        List<Expression> contentValues = Lists.newArrayList();
        contentValues.add(new PlaceholderEnd(element, elementSchema));
        contentValues.add(element.getContent().acceptVisitor(this));
        contentValues.add(new PlaceholderStart(element, elementSchema,
                                               phName + "_end", null));
        Expression content = Concatenation.create(element.getSourcePosition(),
                                                  null, contentValues);
        values.add(element.withAttributesAndContent(
                       Util.map(element.getAttributes(),
                                getAttributeFunction()),
                       content));
      }
      values.add(new PlaceholderEnd(element, elementSchema));

      return Concatenation.create(element.getSourcePosition(), elementSchema,
                                  values);
    }
  }
}
