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

package com.google.gxp.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gxp.compiler.alerts.DefaultAlertPolicy;
import com.google.gxp.compiler.alerts.ErroringAlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.BooleanType;
import com.google.gxp.compiler.base.BundleType;
import com.google.gxp.compiler.base.ClassImport;
import com.google.gxp.compiler.base.CollapseExpression;
import com.google.gxp.compiler.base.Concatenation;
import com.google.gxp.compiler.base.Conditional;
import com.google.gxp.compiler.base.Constructor;
import com.google.gxp.compiler.base.ContentType;
import com.google.gxp.compiler.base.EscapeExpression;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.FormalTypeParameter;
import com.google.gxp.compiler.base.Implementable;
import com.google.gxp.compiler.base.ImplementsDeclaration;
import com.google.gxp.compiler.base.Import;
import com.google.gxp.compiler.base.Interface;
import com.google.gxp.compiler.base.InstanceType;
import com.google.gxp.compiler.base.JavaAnnotation;
import com.google.gxp.compiler.base.MultiLanguageAttrValue;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.NativeType;
import com.google.gxp.compiler.base.OutputElement;
import com.google.gxp.compiler.base.PackageImport;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.SpaceOperator;
import com.google.gxp.compiler.base.SpaceOperatorSet;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.base.TemplateType;
import com.google.gxp.compiler.base.ThrowsDeclaration;
import com.google.gxp.compiler.base.Type;
import com.google.gxp.compiler.base.FormalParameter;
import com.google.gxp.compiler.parser.CallNamespace;
import com.google.gxp.compiler.parser.GxpNamespace;
import com.google.gxp.compiler.parser.Namespace;
import com.google.gxp.compiler.parser.ParsedAttribute;
import com.google.gxp.compiler.parser.ParsedElement;
import com.google.gxp.compiler.reparent.Attribute;
import com.google.gxp.compiler.schema.AttributeValidator;
import com.google.gxp.compiler.schema.BuiltinSchemaFactory;
import com.google.gxp.compiler.schema.DocType;
import com.google.gxp.compiler.schema.ElementValidator;
import com.google.gxp.compiler.schema.Schema;

import java.util.*;
import junit.framework.TestCase;

/**
 * Miscellaneous helper functions for writing GXP compiler tests.
 */
public class GxpcTestCase extends TestCase {
  public <T> List<T> list() {
    return Collections.emptyList();
  }

  public <T> List<T> list(T... items) {
    return ImmutableList.copyOf(items);
  }

  // used to synthesize unique (but somewhat deterministic) source positions.
  private int counter = 0;

  public SourcePosition pos() {
    counter++;
    return new SourcePosition("TEST" + counter, counter, counter);
  }

  public ParsedAttribute parsedAttr(
      Namespace namespace, String name, String value) {
    return new ParsedAttribute(pos(), namespace, name, value,
                               "[" + namespace.getUri() + "]:" + name);
  }

  public Attribute attr(
      Namespace namespace, String name, Expression value) {
    return new Attribute(pos(), namespace + ":" + name + " attribute",
                         namespace, name, value, null, null);
  }

  public TemplateName templateName(String dottedName) {
    return TemplateName.parseDottedName(
        new ErroringAlertSink(DefaultAlertPolicy.INSTANCE), pos(), dottedName);
  }

  public TemplateName.FullyQualified fqTemplateName(String dottedName) {
    return TemplateName.parseFullyQualifiedDottedName(
        new ErroringAlertSink(DefaultAlertPolicy.INSTANCE), pos(), dottedName);
  }

  public Schema htmlSchema() {
    return schema("text/html");
  }

  public Schema schema(String contentType) {
    return new BuiltinSchemaFactory().fromContentTypeName(contentType);
  }

  public ContentType htmlContentType() {
    return new ContentType(pos(), "html", htmlSchema());
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Types
  ////////////////////////////////////////////////////////////////////////////////

  protected BooleanType booleanType(SourcePosition pos) {
    return new BooleanType(pos, "BooleanType");
  }

  protected BundleType bundleType(SourcePosition pos, Schema schema, String element,
                                  String attribute) {
    Map<String, AttributeValidator> map = Maps.newHashMap();
    map.put(attribute, schema.getElementValidator(element).getAttributeValidator(attribute));
    return new BundleType(pos, "BundleType", schema, map);
  }

  protected ContentType contentType(SourcePosition pos, Schema schema) {
    return new ContentType(pos, "ContentType", schema);
  }

  protected InstanceType instanceType(SourcePosition pos, String dottedName) {
    return new InstanceType(pos, "InstanceType", fqTemplateName(dottedName));
  }

  protected NativeType nativeType(SourcePosition pos, String type) {
    return new NativeType(pos, "NativeType", new MultiLanguageAttrValue(type));
  }

  protected TemplateType templateType(SourcePosition pos, String dottedName) {
    return new TemplateType(pos, "TemplateType", fqTemplateName(dottedName));
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Root elements
  ////////////////////////////////////////////////////////////////////////////////

  public Template template(String dottedName, Schema schema,
                           List<Import> imports,
                           List<ThrowsDeclaration> throwsDeclarations,
                           List<Parameter> parameters,
                           List<FormalTypeParameter> formalTypeParameters,
                           Expression content) {
    return template(pos(), dottedName, schema, imports, throwsDeclarations,
                    parameters, formalTypeParameters, content);
  }

  public Template template(SourcePosition pos, String dottedName, Schema schema,
                           List<Import> imports,
                           List<ThrowsDeclaration> throwsDeclarations,
                           List<Parameter> parameters,
                           List<FormalTypeParameter> formalTypeParameters,
                           Expression content) {
    Constructor constructor = Constructor.empty(pos, "<gxp:template>");
    return new Template(pos, "<gxp:template>", fqTemplateName(dottedName),
                        schema, Collections.<JavaAnnotation>emptyList(), constructor, imports,
                        Collections.<ImplementsDeclaration>emptyList(),
                        throwsDeclarations,
                        parameters, formalTypeParameters,
                        content);
  }

  public Interface iface(SourcePosition pos, String dottedName, Schema schema,
                         List<Import> imports,
                         List<ThrowsDeclaration> throwsDeclarations,
                         List<Parameter> parameters,
                         List<FormalTypeParameter> formalTypeParameters) {
    TemplateName name = fqTemplateName(dottedName);
    List<Parameter> params = Lists.newArrayList(parameters);
    params.add(new Parameter(new FormalParameter(pos,
                                                 Implementable.INSTANCE_PARAM_NAME,
                                                 Implementable.INSTANCE_PARAM_NAME,
                                                 new TemplateType(pos, name.toString(), name))));

    return new Interface(pos, "<gxp:interface>", fqTemplateName(dottedName),
                         schema, Collections.<JavaAnnotation>emptyList(), imports,
                         throwsDeclarations, params, formalTypeParameters);
  }

  public ClassImport classImport(String className) {
    return new ClassImport(pos(), "<gxp:import>", fqTemplateName(className));
  }

  public PackageImport packageImport(String packageName) {
    return new PackageImport(pos(), "<gxp:import>", packageName);
  }

  public Parameter param(String name, String typeStr,
                         Expression defaultValue) {
    Type type = nativeType(pos(), typeStr);
    FormalParameter formal = new FormalParameter(pos(), "<gxp:param>", name, false, type,
                                                 defaultValue, false, null, null, false,
                                                 SpaceOperatorSet.NULL);
    return new Parameter(formal, Collections.<JavaAnnotation>emptyList(),
                         defaultValue, false, null, false, str(""));
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Parsed GXP Elements
  ////////////////////////////////////////////////////////////////////////////////

  public GxpNamespace.GxpElement gxpAttr(List<ParsedAttribute> attrs,
                                         List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:attr>", attrs, children,
                                       GxpNamespace.ElementType.ATTR);
  }

  public GxpNamespace.GxpElement gxpEval(List<ParsedAttribute> attrs,
                                         List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:eval>", attrs, children,
                                       GxpNamespace.ElementType.EVAL);
  }

  public GxpNamespace.GxpElement gxpCond(List<ParsedAttribute> attrs,
                                         List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:cond>", attrs, children,
                                       GxpNamespace.ElementType.COND);
  }

  public GxpNamespace.GxpElement gxpClause(List<ParsedAttribute> attrs,
                                           List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:clause>", attrs, children,
                                       GxpNamespace.ElementType.CLAUSE);
  }

  public GxpNamespace.GxpElement gxpIf(List<ParsedAttribute> attrs,
                                       List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:if>", attrs, children,
                                       GxpNamespace.ElementType.IF);
  }

  public GxpNamespace.GxpElement gxpElif(List<ParsedAttribute> attrs,
                                         List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:elif>", attrs, children,
                                       GxpNamespace.ElementType.ELIF);
  }

  public GxpNamespace.GxpElement gxpElse(List<ParsedAttribute> attrs,
                                         List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:else>", attrs, children,
                                       GxpNamespace.ElementType.ELSE);
  }

  public GxpNamespace.GxpElement gxpImport(List<ParsedAttribute> attrs,
                                           List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:import>", attrs, children,
                                       GxpNamespace.ElementType.IMPORT);
  }

  public GxpNamespace.GxpElement gxpAbbr(List<ParsedAttribute> attrs,
                                         List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:abbr>", attrs, children,
                                       GxpNamespace.ElementType.ABBR);
  }

  public GxpNamespace.GxpElement gxpLoop(List<ParsedAttribute> attrs,
                                         List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:loop>", attrs, children,
                                       GxpNamespace.ElementType.LOOP);
  }

  public GxpNamespace.GxpElement gxpParam(List<ParsedAttribute> attrs,
                                          List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:param>", attrs, children,
                                       GxpNamespace.ElementType.PARAM);
  }

  public GxpNamespace.GxpElement gxpTemplate(List<ParsedAttribute> attrs,
                                             List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:template>", attrs, children,
                                       GxpNamespace.ElementType.TEMPLATE);
  }

  public GxpNamespace.GxpElement gxpThrows(List<ParsedAttribute> attrs,
                                           List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:throws>", attrs, children,
                                       GxpNamespace.ElementType.THROWS);
  }

  public GxpNamespace.GxpElement gxpTypeParam(List<ParsedAttribute> attrs,
                                              List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:typeparam>", attrs, children,
                                       GxpNamespace.ElementType.TYPEPARAM);
  }

  public GxpNamespace.GxpElement gxpMsg(List<ParsedAttribute> attrs,
                                        List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:msg>", attrs, children,
                                       GxpNamespace.ElementType.MSG);
  }

  public GxpNamespace.GxpElement gxpNoMsg(List<ParsedAttribute> attrs,
                                          List<ParsedElement> children) {

    return new GxpNamespace.GxpElement(pos(), "<gxp:nomsg>", attrs, children,
                                       GxpNamespace.ElementType.NOMSG);
  }

  public GxpNamespace.GxpElement gxpPh(List<ParsedAttribute> attrs,
                                       List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:ph>", attrs, children,
                                       GxpNamespace.ElementType.PH);
  }

  public GxpNamespace.GxpElement gxpEph(List<ParsedAttribute> attrs,
                                        List<ParsedElement> children) {
    return new GxpNamespace.GxpElement(pos(), "<gxp:eph>", attrs, children,
                                       GxpNamespace.ElementType.EPH);
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Expressions
  ////////////////////////////////////////////////////////////////////////////////

  public CallNamespace.CallElement oldCall(String tagName,
                                           List<ParsedAttribute> attrs,
                                           List<ParsedElement> children) {
    return new CallNamespace.CallElement(pos(), "<call:" + tagName + ">",
                                         tagName, attrs, children);
  }

  public CallNamespace.CallElement newCall(
      String nsName, String tagName, List<ParsedAttribute> attrs,
      List<ParsedElement> children) {
    return new CallNamespace.CallElement(pos(),
                                         "<" + nsName + ":" + tagName + ">",
                                         tagName, attrs, children);
  }

  public StringConstant str(String value) {
    return new StringConstant(pos(), null, value);
  }

  public Expression collapse(Expression subexpression,
                             SpaceOperator interiorSpaceOperator,
                             SpaceOperator exteriorSpaceOperator) {
    return CollapseExpression.create(
        subexpression, new SpaceOperatorSet(interiorSpaceOperator,
                                            exteriorSpaceOperator));
  }

  public Expression escape(Schema schema,
                           Expression subexpression) {
    return new EscapeExpression(schema, subexpression);
  }

  public NativeExpression expr(String expr) {
    return new NativeExpression(pos(), "(" + expr + ")", new MultiLanguageAttrValue(expr),
                                null, null);
  }

  public Expression concat(Schema schema, Expression... values) {
    return Concatenation.create(pos(), schema, list(values));
  }

  public OutputElement tag(ElementValidator validator,
                           DocType docType,
                           List<Attribute> attributes,
                           List<String> bundles,
                           Expression content) {
    return new OutputElement(pos(), "<" + validator.getTagName() + ">",
                             htmlSchema(), htmlSchema(), validator.getTagName(),
                             validator, docType, attributes, bundles, null,
                             content);
  }

  public Conditional.Clause clause(Expression predicate,
                                   Expression expression) {
    return new Conditional.Clause(pos(), "<gxp:clause>", predicate,
                                  expression);
  }
}
