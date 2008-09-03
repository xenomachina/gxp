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

package com.google.gxp.compiler.ifexpand;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.BadNodePlacementError;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.Util;
import com.google.gxp.compiler.parser.DefaultingParsedElementVisitor;
import com.google.gxp.compiler.parser.GxpNamespace;
import com.google.gxp.compiler.parser.NullElement;
import com.google.gxp.compiler.parser.ParsedAttribute;
import com.google.gxp.compiler.parser.ParsedElement;
import com.google.gxp.compiler.parser.ParsedElementVisitor;
import com.google.gxp.compiler.parser.ParseTree;

import java.util.*;

/**
 * Converts <code>gxp:if</code> and friends into <code>gxp:cond</code> blocks.
 *
 * <center><img class=fig src="http://go/gxpc.java/IfExpansion.png"></center>
 */
public class IfExpander implements Function<ParseTree, IfExpandedTree> {
  public IfExpandedTree apply(ParseTree parseTree) {
    AlertSetBuilder alertSetBuilder = new AlertSetBuilder(parseTree.getAlerts());
    Worker worker = new Worker(alertSetBuilder);
    List<ParsedElement> children = worker.process(parseTree.getChildren());

    return new IfExpandedTree(parseTree.getSourcePosition(),
                              alertSetBuilder.buildAndClear(),
                              children);
  }

  private static class Worker {
    private final AlertSink alertSink;

    // We use this visitor most of the time.
    private final NormalVisitor normalVisitor = new NormalVisitor();
    private final Function<ParsedElement, ParsedElement> normalFunction
        = new Function<ParsedElement, ParsedElement>() {
          public ParsedElement apply(ParsedElement node) {
            ParsedElementVisitor<ParsedElement> visitor = normalVisitor;
            return node.acceptVisitor(visitor);
          }
        };

    Worker(AlertSink alertSink) {
      this.alertSink = Preconditions.checkNotNull(alertSink);
    }

    public List<ParsedElement> process(List<ParsedElement> nodes) {
      return Util.map(nodes, normalFunction);
    }

    private abstract class BaseVisitor<T>
        extends DefaultingParsedElementVisitor<T> {

      protected List<GxpNamespace.GxpElement> processIfChildren(GxpNamespace.GxpElement node) {
        IfChildVisitor ifChildVisitor = new IfChildVisitor(node);
        for (ParsedElement child : node.getChildren()) {
          child.acceptVisitor(ifChildVisitor);
        }
        return ifChildVisitor.get();
      }
    }

    private class NormalVisitor extends BaseVisitor<ParsedElement> {
      @Override
      protected ParsedElement defaultVisitElement(ParsedElement node) {
        List<ParsedElement> newChildren = Lists.newArrayList();
        for (ParsedElement child : node.getChildren()) {
          newChildren.add(child.acceptVisitor(normalVisitor));
        }
        return node.withChildren(newChildren);
      }

      private ParsedElement reportBadNodeAndContinue(Node badNode,
                                                     Node parentNode) {
        alertSink.add(new BadNodePlacementError(badNode, parentNode));
        return new NullElement(badNode);
      }

      public ParsedElement visitElifElement(GxpNamespace.GxpElement node) {
        return reportBadNodeAndContinue(node, null);
      }

      public ParsedElement visitElseElement(GxpNamespace.GxpElement node) {
        return reportBadNodeAndContinue(node, null);
      }

      public ParsedElement visitIfElement(GxpNamespace.GxpElement node) {
        List<GxpNamespace.GxpElement> clauses = processIfChildren(node);
        List<ParsedAttribute> attrs = Collections.emptyList();
        return new GxpNamespace.GxpElement(
              node.getSourcePosition(),
              node.getDisplayName(),
              attrs,
              clauses,
              GxpNamespace.ElementType.COND);
      }

      public Expression visitValue(Expression value) {
        // There aren't any Values that can contain IfElements, so we don't
        // need to recurse any further.
        return value;
      }
    }

    private class IfChildVisitor extends BaseVisitor<Void> {
      // completed clauses
      private List<GxpNamespace.GxpElement> clauses = Lists.newArrayList();

      // for the current clause
      private List<ParsedElement> clauseChildren = Lists.newArrayList();
      private ParsedElement clauseFromNode;

      private boolean sawElse = false;

      IfChildVisitor(GxpNamespace.GxpElement ifNode) {
        clauseFromNode = ifNode;
      }

      private void flush() {
        if (clauseFromNode != null) {
          clauses.add(new GxpNamespace.GxpElement(
              clauseFromNode.getSourcePosition(),
              clauseFromNode.getDisplayName(),
              clauseFromNode.getAttributes(),
              ImmutableList.copyOf(clauseChildren),
              GxpNamespace.ElementType.CLAUSE));
          clauseFromNode = null;
          clauseChildren = Lists.newArrayList();
        }
      }

      public List<GxpNamespace.GxpElement> get() {
        flush();
        return Collections.unmodifiableList(clauses);
      }

      /**
       * Adds the specified node to the current clause, after recursively
       * processing it.
       */
      protected Void addNodeToCurrentClause(ParsedElement node) {
        clauseChildren.add(node.acceptVisitor(normalVisitor));
        return null;
      }

      @Override
      protected Void defaultVisitElement(ParsedElement node) {
        addNodeToCurrentClause(node);
        return null;
      }

      // For the two functions below I decided to only flush() when encountering
      // correct nodes.  When encountering a superfluous <gxp:else> or <gxp:elif>
      // we'll just throw away the node and continue with compilation.
      //
      // This does have the possible disadvantage of hiding additional alerts from
      // the user that would have been created durring the compilation of these thrown
      // away nodes. I decided, in this case, to come down in favor of fewer rather than
      // more alerts for this type of malformed code.

      public Void visitElifElement(GxpNamespace.GxpElement node) {
        if (sawElse) {
          alertSink.add(new ElifAfterElseError(node));
        } else {
          flush();
          clauseFromNode = node;
        }
        reportBadChildren(node);
        return null;
      }

      public Void visitElseElement(GxpNamespace.GxpElement node) {
        if (sawElse) {
          alertSink.add(new DoubleElseError(node));
        } else {
          flush();
          clauseFromNode = node;
          sawElse = true;
        }
        reportBadChildren(node);
        return null;
      }

      private void reportBadChildren(ParsedElement parentNode) {
        for (Node badNode : parentNode.getChildren()) {
          alertSink.add(new BadNodePlacementError(badNode, parentNode));
        }
      }

      public Void visitIfElement(GxpNamespace.GxpElement node) {
        return addNodeToCurrentClause(node);
      }
    }
  }
}
