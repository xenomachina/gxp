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

package com.google.gxp.compiler.escape;

import com.google.common.base.CharEscapers;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.base.AbbrExpression;
import com.google.gxp.compiler.base.AttrBundleParam;
import com.google.gxp.compiler.base.BooleanConstant;
import com.google.gxp.compiler.base.BoundCall;
import com.google.gxp.compiler.base.Call;
import com.google.gxp.compiler.base.CallVisitor;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.CollapseExpression;
import com.google.gxp.compiler.base.Concatenation;
import com.google.gxp.compiler.base.Conditional;
import com.google.gxp.compiler.base.ConstructedConstant;
import com.google.gxp.compiler.base.ContentType;
import com.google.gxp.compiler.base.DefaultingTypeVisitor;
import com.google.gxp.compiler.base.EscapeExpression;
import com.google.gxp.compiler.base.ExhaustiveExpressionVisitor;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.ExtractedMessage;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.ObjectConstant;
import com.google.gxp.compiler.base.OutputElement;
import com.google.gxp.compiler.base.PlaceholderEnd;
import com.google.gxp.compiler.base.PlaceholderNode;
import com.google.gxp.compiler.base.PlaceholderStart;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.Type;
import com.google.gxp.compiler.base.TypeVisitor;
import com.google.gxp.compiler.base.UnboundCall;
import com.google.gxp.compiler.base.UnexpectedNodeException;
import com.google.gxp.compiler.base.UnextractedMessage;
import com.google.gxp.compiler.base.Util;
import com.google.gxp.compiler.base.ValidatedCall;
import com.google.gxp.compiler.phinsert.PlaceholderInsertedTree;
import com.google.gxp.compiler.reparent.Attribute;
import com.google.gxp.compiler.schema.ContentFamily;
import com.google.gxp.compiler.schema.ContentFamilyVisitor;
import com.google.gxp.compiler.schema.Schema;

import java.util.*;

/**
 * Escapes content in the tree.
 */
public class Escaper implements Function<PlaceholderInsertedTree, EscapedTree> {

  /**
   * Escapes content in the supplied SpaceCollapsedTree to produce
   * an EscapedTree.
   */
  public EscapedTree apply(PlaceholderInsertedTree tree) {
    AlertSetBuilder alertSetBuilder = new AlertSetBuilder(tree.getAlerts());
    Worker worker = new Worker(alertSetBuilder);
    Root root = worker.apply(tree.getRoot());

    return new EscapedTree(tree.getSourcePosition(), alertSetBuilder.buildAndClear(), root);
  }

  private class Worker implements Function<Root, Root> {
    private final AlertSink alertSink;

    Worker(AlertSink alertSink) {
      this.alertSink = Preconditions.checkNotNull(alertSink);
    }

    private final Map<Schema, Visitor> visitors = Maps.newHashMap();

    private Visitor visitor(Schema schema) {
      Visitor result = visitors.get(schema);
      if (result == null) {
        result = new Visitor(schema);
        visitors.put(schema, result);
      }
      return result;
    }

    public Root apply(Root root) {
      return (root.getSchema() == null)
          ? root
          : root.acceptVisitor(visitor(root.getSchema()));
    }

    private class Visitor
        extends ExhaustiveExpressionVisitor
        implements CallVisitor<Expression> {

      private final Schema schema;

      // used to keep track of what Attribute (if any) we are inside of
      private final Deque<Attribute> attrStack = new ArrayDeque<Attribute>();

      Visitor(Schema schema) {
        this.schema = Preconditions.checkNotNull(schema);
      }

      @Override
      protected Expression postProcess(Expression result) {
        if (!schema.allows(result.getSchema())) {
          alertSink.add(new TypeError(schema, result));
          return new StringConstant(result, schema, "");
        } else {
          return result;
        }
      }

      @Override
      public Template visitTemplate(Template template) {
        Template result = super.visitTemplate(template);
        if (!schema.allows(result.getContent().getSchema())) {
          throw new AssertionError();
        }
        return result;
      }

      private final Function<Parameter, Parameter> parameterTransformer =
        new Function<Parameter, Parameter>() {
          public Parameter apply(Parameter param) {
            Expression defaultValue = param.getDefaultValue();
            if (defaultValue != null) {
              Visitor valueVisitor = param.getType().acceptTypeVisitor(typeVisitor);
              param = param.withDefaultValue(defaultValue.acceptVisitor(valueVisitor));
            }
            return param.withComment(
                param.getComment().acceptVisitor(Visitor.this));
          }
        };

      @Override
      protected Function<Parameter, Parameter> getParameterTransformer() {
        return parameterTransformer;
      }

      @Override
      public Expression visitAbbrExpression(AbbrExpression node) {
        // if the gxp:abbr specifies a content-type we need a visitor using the
        // appropriate Schema to visit the value
        Visitor valueVisitor = node.getType().acceptTypeVisitor(typeVisitor);
        return postProcess(node.withValueAndContent(valueVisitor.apply(node.getValue()),
                                                    apply(node.getContent())));
      }

      @Override
      public Expression visitStringConstant(StringConstant node) {
        ContentFamily contentFamily = schema.getContentFamily();
        return postProcess(
            new StringConstant(
                node, schema,
                contentFamily.acceptVisitor(STATIC_ESCAPE_VISITOR, node.evaluate())));
      }

      @Override
      public Expression visitAttrBundleParam(AttrBundleParam node) {
        return postProcess(node.transform(visitor(node.getSchema())));
      }

      @Override
      public Expression visitBooleanConstant(BooleanConstant node) {
        return postProcess(new EscapeExpression(schema, node));
      }

      @Override
      public Expression visitObjectConstant(ObjectConstant node) {
        return postProcess(new EscapeExpression(schema, node));
      }

      @Override
      public Expression visitConstructedConstant(ConstructedConstant node) {
        return postProcess(new EscapeExpression(schema, node));
      }

      @Override
      public Expression visitNativeExpression(NativeExpression node) {
        return postProcess(new EscapeExpression(schema, node));
      }

      @Override
      public Expression visitConcatenation(Concatenation node) {
        return postProcess(Concatenation.create(node.getSourcePosition(),
                                                schema,
                                                Util.map(node.getValues(),
                                                         this)));
      }

      @Override
      public Expression visitOutputElement(OutputElement element) {
        Schema innerSchema = element.getInnerSchema();
        Visitor contentVisitor = (innerSchema == null)
            ? this : visitor(innerSchema);
        return postProcess(
            element.withAttributesAndContent(
                Util.map(element.getAttributes(), getAttributeFunction()),
                element.getContent().acceptVisitor(contentVisitor)));
      }

      @Override
      public Attribute visitAttribute(Attribute attr) {
        attrStack.push(attr);
        Schema schema = attr.getInnerSchema();
        if (schema != null) {
          Visitor visitor = visitor(schema);
          attr = attr.withValue(attr.getValue().acceptVisitor(visitor));
        }

        Attribute result = super.visitAttribute(attr);
        attrStack.pop();
        return result;
      }

      @Override
      public Expression visitConditional(Conditional conditional) {
        List<Conditional.Clause> clauses = Lists.newArrayList();
        for (Conditional.Clause clause : conditional.getClauses()) {
          clauses.add(visitClause(clause));
        }
        Expression elseExpression = apply(conditional.getElseExpression());
        return postProcess(conditional.withSchemaAndClauses(schema, clauses, elseExpression));
      }

      @Override
      public Expression visitUnextractedMessage(UnextractedMessage msg) {

        // if <gxp:msg> didn't have a content-type attribute,
        // it takes on the schema of its surrounding context
        Schema msgSchema = msg.getSchema();
        if (msgSchema == null) {
          msgSchema = schema.getMsgSchema();
        }

        Expression result = msg.withContentAndSchema(
            msg.getContent().acceptVisitor(visitor(msgSchema)),
            msgSchema);

        if (!result.getSchema().isTranslatable()) {
          alertSink.add(new UntranslatableMsgError(result));
        }

        return postProcess(
            result.getSchema().equals(schema)
              ? result
              : new EscapeExpression(schema, result));
      }

      @Override
      public Expression visitCall(Call call) {
        return postProcess(call.acceptCallVisitor(this));
      }

      private TypeVisitor<Visitor> typeVisitor =
        new DefaultingTypeVisitor<Visitor>() {
          protected Visitor defaultVisitType(Type type) {
            return Visitor.this;
          }

          public Visitor visitContentType(ContentType type) {
            return visitor(type.getSchema());
          }
        };

      public Expression visitBoundCall(BoundCall call) {
        Callable callee = call.getCallee();

        // tranform params
        ImmutableMap.Builder<String, Attribute> newParams = ImmutableMap.builder();

        for (Map.Entry<String, Attribute> p : call.getAttributes().entrySet()) {
          String key = p.getKey();
          Visitor visitor = callee
              .getParameterByPrimary(key).getType()
              .acceptTypeVisitor(typeVisitor);
          Attribute value = visitor.visitAttribute(p.getValue());
          newParams.put(key, value);
        }

        Expression result = call.withParams(newParams.build());

        // If we are inside of an attribute with a content-type that matches the
        // content-type of the call, we'll need to escape the results into the
        // content-type of the element
        if (!attrStack.isEmpty()
            && !result.getSchema().equals(schema)
            && result.getSchema().equals(attrStack.peek().getInnerSchema())) {
          result = new EscapeExpression(schema, result);
        }

        return result;
      }

      public Expression visitUnboundCall(UnboundCall call) {
        throw new UnexpectedNodeException(call);
      }

      public Expression visitValidatedCall(ValidatedCall call) {
        throw new UnexpectedNodeException(call);
      }

      @Override
      public Expression visitPlaceholderStart(PlaceholderStart ph) {
        return postProcess(ph.withSchema(schema));
      }

      @Override
      public Expression visitPlaceholderEnd(PlaceholderEnd eph) {
        return postProcess(eph.withSchema(schema));
      }

      @Override
      public Expression visitEscapeExpression(EscapeExpression node) {
        return postProcess(new EscapeExpression(schema, node));
      }

      @Override
      public Expression visitCollapseExpression(CollapseExpression node) {
        throw new UnexpectedNodeException(node);
      }

      @Override
      public Expression visitExtractedMessage(ExtractedMessage node) {
        throw new UnexpectedNodeException(node);
      }

      @Override
      public Expression visitPlaceholderNode(PlaceholderNode node) {
        throw new UnexpectedNodeException(node);
      }
    }
  }

  private static final ContentFamilyVisitor<String, String>
      STATIC_ESCAPE_VISITOR =
        new ContentFamilyVisitor<String, String>() {
          public String visitMarkup(String s) {
            // TODO(laurence): what about non-HTML markup???
            return CharEscapers.htmlEscaper().escape(s);
          }

          public String visitJavaScript(String s) {
            return s;
          }

          public String visitCss(String s) {
            return s;
          }

          public String visitPlaintext(String s) {
            return s;
          }
        };
}
