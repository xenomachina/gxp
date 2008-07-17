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

package com.google.gxp.compiler.reparent;

import com.google.gxp.compiler.GxpcTestCase;
import com.google.gxp.compiler.alerts.Alert;
import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.WarningAlert;
import com.google.gxp.compiler.alerts.common.BadNodePlacementError;
import com.google.gxp.compiler.alerts.common.MissingAttributeError;
import com.google.gxp.compiler.alerts.common.UnknownAttributeError;
import com.google.gxp.compiler.base.ClassImport;
import com.google.gxp.compiler.base.CollapseExpression;
import com.google.gxp.compiler.base.Concatenation;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.Import;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.NativeType;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.NullRoot;
import com.google.gxp.compiler.base.OutputElement;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.base.PackageImport;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.ifexpand.IfExpandedTree;
import com.google.gxp.compiler.parser.GxpNamespace;
import com.google.gxp.compiler.parser.Namespace;
import com.google.gxp.compiler.parser.NullNamespace;
import com.google.gxp.compiler.parser.OutputNamespace;
import com.google.gxp.compiler.parser.ParsedAttribute;
import com.google.gxp.compiler.parser.ParsedElement;
import com.google.gxp.compiler.parser.TextElement;
import com.google.gxp.compiler.schema.BuiltinSchemaFactory;

import java.util.*;

/**
 * Tests for {@link Reparenter}.
 */
public class ReparenterTest extends GxpcTestCase {
  private static final Namespace GXP_NS = GxpNamespace.INSTANCE;
  private static final Namespace HTML_NS =
      new OutputNamespace(BuiltinSchemaFactory.INSTANCE.fromContentTypeName("text/html"));
  private static final TemplateName TEMPLATE_NAME =
      TemplateName.create("pkg", "Test");

  private static final List<ParsedAttribute> NO_ATTRS = Collections.emptyList();

  private final AlertSetBuilder expectedAlerts = new AlertSetBuilder();

  private AlertSet actualAlerts = AlertSet.EMPTY;

  /**
   * We test the alerts in tearDown. Individual tests can put Alerts they
   * expect to see in expectedAlerts. actualAlerts is automatically set by
   * reparent().
   */
  public void tearDown() throws Exception {
    assertEquals(expectedAlerts.buildAndClear(), actualAlerts);
  }

  private void assertEmptyValue(Expression childNode) {
    if (childNode instanceof CollapseExpression) {
      childNode = ((CollapseExpression) childNode).getSubexpression();
    }
    assertTrue(childNode + " is not a StringConstant.",
               childNode instanceof StringConstant);
    StringConstant stringConstant = (StringConstant) childNode;
    assertEquals("", stringConstant.evaluate());
  }

  private ReparentedTree reparent(IfExpandedTree parseTree) {
    ReparentedTree result = new Reparenter(BuiltinSchemaFactory.INSTANCE,
                                           TEMPLATE_NAME.toString())
        .apply(parseTree);

    actualAlerts = result.getAlerts();
    assertEquals(parseTree.getSourcePosition(), result.getSourcePosition());
    return result;
  }

  /*
   * To make it easier to programmatically create parse trees, there are a
   * bunch of helper methods to create various types of parsed nodes.
   */

  /**
   * AlertSetBuilder used as AlertSink by all parsed node helper methods.
   */
  private final AlertSetBuilder parseAlerts = new AlertSetBuilder();

  private final String filename = "[" + getName() + ".gxp]";
  int line = 0;
  /**
   * @return a unique but deterministic source position for each location in
   * the simulated parse tree.
   */
  private SourcePosition pos(Node... children) {
    line++;
    int column = 1;
    for (Node node : children) {
      column = Math.max(column, node.getSourcePosition().getColumn() + 1);
    }
    return new SourcePosition(filename, line, column);
  }

  /**
   * Creates a parse tree.
   */
  private IfExpandedTree tree(ParsedElement... children) {
    return new IfExpandedTree(new SourcePosition(filename),
                              parseAlerts.buildAndClear(), list(children));
  }

  /**
   * Creates a template element.
   */
  private ParsedElement template(List<ParsedAttribute> attrs,
                                 ParsedElement... children) {
    return GXP_NS.createElement(parseAlerts, pos(children), "<gxp:template>",
                                "template", attrs, list(children));
  }

  /**
   * Creates an HTML element.
   */
  private ParsedElement tag(String tagName,
                            List<ParsedAttribute> attrs,
                            ParsedElement... children) {
    return HTML_NS.createElement(parseAlerts, pos(children),
                                 "<" + tagName + ">", tagName, attrs,
                                 list(children));
  }

  /**
   * Creates a text element.
   */
  private TextElement text(String value) {
    return new TextElement(pos(), value);
  }

  /**
   * Creates an import element.
   */
  private ParsedElement imp(List<ParsedAttribute> attrs,
                            ParsedElement... children) {
    return GXP_NS.createElement(parseAlerts, pos(children), "<gxp:import>",
                                "import", attrs, list(children));
  }

  /**
   * Creates a param element.
   */
  private ParsedElement param(List<ParsedAttribute> attrs,
                              ParsedElement... children) {
    return GXP_NS.createElement(parseAlerts, pos(children), "<gxp:param>",
                                "param", attrs, list(children));
  }

  private ParsedAttribute attr(String name, String value) {
    return new ParsedAttribute(pos(), NullNamespace.INSTANCE, name, value,
                               name);
  }

  public void testBaseCase() throws Exception {
    IfExpandedTree input = tree();
    ReparentedTree output = reparent(input);
    assertTrue(output.getRoot() instanceof NullRoot);
  }

  public void testWithAlerts() throws Exception {
    Alert parseWarning = new WarningAlert(new SourcePosition("baz", 1, 2),
                                          "test warning"){};
    parseAlerts.add(parseWarning);
    expectedAlerts.add(parseWarning);

    ReparentedTree output = reparent(tree());
    assertTrue(output.getRoot() instanceof NullRoot);
  }

  private void assertOneRoot(ReparentedTree tree) {
    assertNotNull(tree.getRoot());
    assertEquals(1, tree.getChildren().size());
    assertEquals(tree.getRoot(), tree.getChildren().get(0));
  }

  private Template getTemplate(Root root) {
    assertTrue(root + " is not a Template",
               root instanceof Template);
    return (Template)root;
  }

  public void testEmptyNamelessTemplate() throws Exception {
    ParsedElement template = template(NO_ATTRS);
    expectedAlerts.add(new MissingAttributeError(template, "name"));
    IfExpandedTree parseTree = tree(template);
    ReparentedTree output = reparent(parseTree);
    assertOneRoot(output);
    Template root = getTemplate(output.getRoot());
    assertEquals(TEMPLATE_NAME.toString(), root.getName().toString());
    assertEquals(0, root.getImports().size());
    assertEquals(0, root.getParameters().size());
    assertEmptyValue(root.getContent());
  }

  public void testNamedTemplate() throws Exception {
    IfExpandedTree parseTree = tree(template(list(attr("name", "pkg.Test"))));
    ReparentedTree output = reparent(parseTree);
    assertOneRoot(output);
    Template root = getTemplate(output.getRoot());
    assertEquals(TEMPLATE_NAME, root.getName());
    assertEquals(0, root.getImports().size());
    assertEquals(0, root.getParameters().size());
    assertEmptyValue(root.getContent());
  }

  public void testTemplateWithBadAttr() throws Exception {
    ParsedAttribute spaz = attr("spaz", "I don't exist!");
    ParsedElement template = template(list(attr("name", "pkg.Test"), spaz));
    expectedAlerts.add(new UnknownAttributeError(template, spaz));
    IfExpandedTree parseTree = tree(template);
    ReparentedTree output = reparent(parseTree);
    assertOneRoot(output);
    Template root = getTemplate(output.getRoot());
    assertEquals(TEMPLATE_NAME, root.getName());
    assertEquals(0, root.getImports().size());
    assertEquals(0, root.getParameters().size());
    assertEmptyValue(root.getContent());
  }

  public void testTemplateWithImports() throws Exception {
    IfExpandedTree parseTree =
        tree(template(list(attr("name", "pkg.Test")),
                      imp(list(attr("class", "com.google.Foo"))),
                      imp(list(attr("package", "com.google.bar"))),
                      imp(list(attr("class", "com.google.Baz")))));
    ReparentedTree output = reparent(parseTree);
    assertOneRoot(output);
    Template root = getTemplate(output.getRoot());
    assertEquals(TEMPLATE_NAME, root.getName());
    List<Import> imports = root.getImports();
    assertEquals(3, imports.size());
    assertEquals(templateName("com.google.Foo"),
                 ((ClassImport) imports.get(0)).getClassName());
    assertEquals("com.google.bar",
                 ((PackageImport) imports.get(1)).getPackageName());
    assertEquals(templateName("com.google.Baz"),
                 ((ClassImport) imports.get(2)).getClassName());
    assertEquals(0, root.getParameters().size());
    assertEmptyValue(root.getContent());
  }

  public void testImportWithText() throws Exception {
    TextElement textNode = text("gremlin");
    ParsedElement badImp = imp(list(attr("class", "com.google.Foo")),
                               textNode);
    IfExpandedTree parseTree =
        tree(template(list(attr("name", "pkg.Test")),
                      badImp,
                      imp(list(attr("package", "com.google.bar"))),
                      imp(list(attr("class", "com.google.Baz")))));
    ReparentedTree output = reparent(parseTree);
    expectedAlerts.add(new BadNodePlacementError(textNode, badImp));
    assertOneRoot(output);
    Template root = getTemplate(output.getRoot());
    assertEquals(TEMPLATE_NAME, root.getName());
    List<Import> imports = root.getImports();
    assertEquals(3, imports.size());
    assertEquals(templateName("com.google.Foo"),
                 ((ClassImport) imports.get(0)).getClassName());
    assertEquals("com.google.bar",
                 ((PackageImport) imports.get(1)).getPackageName());
    assertEquals(templateName("com.google.Baz"),
                 ((ClassImport) imports.get(2)).getClassName());
    assertEquals(0, root.getParameters().size());
    assertEmptyValue(root.getContent());
  }

  public void testTemplateWithParams() throws Exception {
    IfExpandedTree parseTree =
        tree(template(list(attr("name", "pkg.Test")),
                      param(list(attr("name", "x"),
                                 attr("type", "int"))),
                      param(list(attr("type", "String"),
                                 attr("default", "\"xyzzy\""),
                                 attr("name", "s")))));
    ReparentedTree output = reparent(parseTree);
    assertOneRoot(output);
    Template root = getTemplate(output.getRoot());
    assertEquals(TEMPLATE_NAME, root.getName());
    assertEquals(0, root.getImports().size());
    List<Parameter> params = root.getParameters();
    assertEquals(2, params.size());
    assertEquals("x", params.get(0).getPrimaryName());
    assertEquals("int",
                 ((NativeType) params.get(0).getType()).getNativeType(OutputLanguage.JAVA));
    assertEquals(null, params.get(0).getDefaultValue());
    assertEquals("s", params.get(1).getPrimaryName());
    assertEquals("String",
                 ((NativeType) params.get(1).getType()).getNativeType(OutputLanguage.JAVA));
    Expression defValue = params.get(1).getDefaultValue();
    assertEquals("\"xyzzy\"", ((NativeExpression) defValue).getNativeCode());
    assertEmptyValue(root.getContent());
  }

  public void testTemplateWithKids() throws Exception {
    IfExpandedTree parseTree =
        tree(template(list(attr("name", "pkg.Test")),
                      tag("img", list(attr("src", "Uno"))),
                      tag("p", list(attr("class", "Dos"))),
                      tag("div", list(attr("id", "Windows")))));
    ReparentedTree output = reparent(parseTree);
    assertOneRoot(output);
    Template root = getTemplate(output.getRoot());
    assertEquals(TEMPLATE_NAME, root.getName());
    assertEquals(0, root.getImports().size());
    assertEquals(0, root.getParameters().size());
    CollapseExpression collapse = (CollapseExpression) root.getContent();
    Concatenation kids = (Concatenation) collapse.getSubexpression();
    assertEquals(3, kids.getValues().size());
    assertEquals("img",
                 ((OutputElement) kids.getValues().get(0)).getLocalName());
    assertEquals("p",
                 ((OutputElement) kids.getValues().get(1)).getLocalName());
    assertEquals("div",
                 ((OutputElement) kids.getValues().get(2)).getLocalName());
    // TODO(laurence): test all other node types
  }
}
