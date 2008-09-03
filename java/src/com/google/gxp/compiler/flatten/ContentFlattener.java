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

package com.google.gxp.compiler.flatten;

import com.google.common.base.CharEscapers;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.base.AttrBundleReference;
import com.google.gxp.compiler.base.Concatenation;
import com.google.gxp.compiler.base.Conditional;
import com.google.gxp.compiler.base.ConvertibleToContent;
import com.google.gxp.compiler.base.ExceptionExpression;
import com.google.gxp.compiler.base.EscapeExpression;
import com.google.gxp.compiler.base.ExampleExpression;
import com.google.gxp.compiler.base.ExhaustiveExpressionVisitor;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.IsXmlExpression;
import com.google.gxp.compiler.base.OutputElement;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.reparent.Attribute;
import com.google.gxp.compiler.schema.AttributeValidator;
import com.google.gxp.compiler.schema.DocType;
import com.google.gxp.compiler.schema.ElementValidator;
import com.google.gxp.compiler.schema.Schema;
import com.google.gxp.compiler.validate.ValidatedTree;

import java.util.*;

/**
 * Flattens OutputElements (like HTML tags) into StringConstants.
 *
 * <p>For example:
 *
 * <center><img class=fig src="https://www.corp.google.com/eng/designdocs/gxp/java-rewrite/ContentFlattening.png"></center>
 *
 * <p>Note that the OutputElement is replaced with a pair of StringConstant
 * nodes for the start and end tags.
 */
public class ContentFlattener implements Function<ValidatedTree, ContentFlattenedTree> {

  public ContentFlattenedTree apply(ValidatedTree tree) {
    AlertSetBuilder alertSetBuilder = new AlertSetBuilder(tree.getAlerts());
    Root root = tree.getRoot().acceptVisitor(new Visitor(alertSetBuilder));

    return new ContentFlattenedTree(tree.getSourcePosition(), alertSetBuilder.buildAndClear(),
                                    root);
  }

  /**
   * Visits the nodes of a ValidatedTree, and "flattens" certain nodes.  The
   * act of flattening converts nodes with n children into a pair of nodes with
   * n siblings (or more, if the chidren were also flattened) between them, so
   * the result of visiting a node is a list of nodes.
   */
  private static class Visitor extends ExhaustiveExpressionVisitor {
    private final AlertSink alertSink;

    Visitor(AlertSink alertSink) {
      this.alertSink = Preconditions.checkNotNull(alertSink);
    }

    private Expression flattenDocType(OutputElement element, DocType docType) {
      Schema elementSchema = element.getSchema();
      Expression xmlDoctype =
          new StringConstant(element, elementSchema,
                             docType.toXml(element.getLocalName()));

      Expression sgmlDoctype;
      if (docType.isSgmlCompatible()) {
        sgmlDoctype =
            new StringConstant(element, elementSchema,
                               docType.toSgml(element.getLocalName()));
      } else {
        String exceptionMessage = String.format(
            "Doctype '%s' incompatible with non-XML syntax",
            docType.getName());
        sgmlDoctype = new ExceptionExpression(
            element.getSourcePosition(), elementSchema,
            ExceptionExpression.Kind.NOT_SUPPORTED_IN_SGML_MODE,
            exceptionMessage);
      }
      return new Conditional(element, elementSchema,
                             new IsXmlExpression(element, elementSchema),
                             xmlDoctype, sgmlDoctype);
    }

    private Expression flattenXmlns(OutputElement element) {
      Schema elementSchema = element.getSchema();
      String xmlns = elementSchema.getNamespaceUri();

      List<Expression> concatList = Lists.newArrayList();
      concatList.add(new StringConstant(element, elementSchema, " xmlns"));
      if (elementSchema.getTagPrefix() != null) {
        concatList.add(new StringConstant(element, elementSchema, ":"));
        concatList.add(new StringConstant(element, elementSchema,
                                          elementSchema.getTagPrefix()));
      }
      concatList.add(new StringConstant(element, elementSchema, "=\""));
      concatList.add(new StringConstant(element, elementSchema,
                                        CharEscapers.XML_ESCAPE.escape(xmlns)));
      concatList.add(new StringConstant(element, elementSchema, "\""));
      return new Conditional(
          element, elementSchema,
          new IsXmlExpression(element, elementSchema),
          Concatenation.create(element.getSourcePosition(), elementSchema,
                               concatList),
          new StringConstant(element, elementSchema, ""));
    }

    @Override
    public Expression visitOutputElement(OutputElement element) {
      List<Expression> values = Lists.newArrayList();
      ElementValidator elementValidator = element.getValidator();
      Schema elementSchema = element.getSchema();
      String tagPrefix = elementSchema.getTagPrefix();
      DocType docType = element.getDocType();
      if (docType != null) {
        values.add(flattenDocType(element, docType));
      }
      values.add(new StringConstant(element, elementSchema, "<"));
      if (tagPrefix != null) {
        values.add(new StringConstant(element, elementSchema, tagPrefix + ":"));
      }
      values.add(new StringConstant(element, elementSchema,
                                    element.getLocalName()));

      if (docType != null) {
        values.add(flattenXmlns(element));
      }

      for (final Attribute attr : element.getAttributes()) {
        AttributeValidator attrValidator =
            elementValidator.getAttributeValidator(attr.getName());
        Expression empty = new StringConstant(attr, elementSchema, "");
        if (attrValidator.isFlagSet(AttributeValidator.Flag.BOOLEAN)) {
          Expression attrValue = attr.getValue().acceptVisitor(this);
          if (attrValue.hasStaticString()) {
            values.add(buildBooleanAttrExpression(attr, element));
          } else {
            if (attrValue instanceof ConvertibleToContent) {
              ConvertibleToContent ctc = (ConvertibleToContent)attrValue;
              attrValue = ctc.getSubexpression();
            }
            values.add(new Conditional(element, elementSchema,
                                       attrValue,
                                       buildBooleanAttrExpression(attr,
                                                                  element),
                                       empty));
          }
        } else {
          String example = attrValidator.getExample();
          Expression condition = attr.getCondition();
          if (condition != null) {
            values.add(new Conditional(element, elementSchema, condition,
                                       buildAttrExpression(attr, element, example), empty));
          } else {
            values.add(buildAttrExpression(attr, element, example));
          }
        }
      }

      for (String attrBundle : element.getAttrBundles()) {
        values.add(new EscapeExpression(elementSchema,
                                        new AttrBundleReference(element, attrBundle)));
      }

      if (elementValidator.isFlagSet(ElementValidator.Flag.NOENDTAG)) {
        values.add(new Conditional(
            element, elementSchema,
            new IsXmlExpression(element, elementSchema),
            new StringConstant(element, elementSchema, " /"),
            new StringConstant(element, elementSchema, "")));
      }
      values.add(new StringConstant(element, elementSchema, ">"));
      values.add(element.getContent().acceptVisitor(this));
      if (!elementValidator.isFlagSet(ElementValidator.Flag.NOENDTAG)) {
        values.add(new StringConstant(element, elementSchema, "</"));
        if (tagPrefix != null) {
          values.add(new StringConstant(element, elementSchema,
                                        tagPrefix + ":"));
        }
        values.add(new StringConstant(element, elementSchema,
                                      element.getLocalName() + ">"));
      }
      return Concatenation.create(element.getSourcePosition(), elementSchema,
                                  values);
    }

    /**
     * Construct an Expression that represents an Attribute
     *
     * If the attribute value is dynamuc and if the attribute doesn't have
     * an example set in the schama, then the entire expression is surrounded
     * with a {@code ExampleExpression} making it so the attribute won't
     * appear at all in the translation console example if this element is part
     * of a Placeholder an a <gxp:msg> tag
     */
    private Expression buildAttrExpression(Attribute attr,
                                           OutputElement element,
                                           String example) {
      boolean surroundWithExample = false;

      List<Expression> list = Lists.newArrayList();

      list.add(new StringConstant(attr, element.getSchema(), " " + attr.getName() + "=\""));
      Expression value = attr.getValue().acceptVisitor(this);
      if (!value.hasStaticString()) {
        if (example != null) {
          value = new ExampleExpression(value, example);
        } else {
          surroundWithExample = true;
        }
      }
      list.add(value);
      list.add(new StringConstant(attr, element.getSchema(), "\""));

      Expression flattenedAttr = Concatenation.create(element.getSourcePosition(),
                                                      element.getSchema(), list);

      return (surroundWithExample)
          ? new ExampleExpression(flattenedAttr, "")
          : flattenedAttr;
    }

    /**
     * Constructs an Expression to output a boolean attribute.
     * Example: selected="selected" if in XML mode else selected
     */
    private Expression buildBooleanAttrExpression(Attribute attr,
                                                  OutputElement element) {
      Schema elementSchema = element.getSchema();
      List<Expression> values = Lists.newArrayList();

      values.add(new StringConstant(attr, elementSchema,
                                    " " + attr.getName()));
      values.add(new Conditional(
                     attr, elementSchema,
                     new IsXmlExpression(attr, elementSchema),
                     new StringConstant(attr, elementSchema,
                                        "=\"" + attr.getName() + "\""),
                     new StringConstant(attr, elementSchema, "")));

      return Concatenation.create(attr.getSourcePosition(), elementSchema,
                                  values);
    }
  }
}
