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

package com.google.gxp.compiler.errortests;

import com.google.gxp.compiler.alerts.common.BadNodePlacementError;
import com.google.gxp.compiler.alerts.common.ContentTypeExpectedAlert;
import com.google.gxp.compiler.alerts.common.InvalidAttributeValueError;
import com.google.gxp.compiler.alerts.common.InvalidNameError;
import com.google.gxp.compiler.alerts.common.MissingAttributeError;
import com.google.gxp.compiler.alerts.common.MissingTypeError;
import com.google.gxp.compiler.alerts.common.NothingToCompileError;
import com.google.gxp.compiler.escape.TypeError;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.bind.ImplementableNotFoundError;
import com.google.gxp.compiler.reparent.BadRegexError;
import com.google.gxp.compiler.reparent.ConflictingAttributesError;
import com.google.gxp.compiler.reparent.DuplicateImportError;
import com.google.gxp.compiler.reparent.MismatchedTemplateNameError;
import com.google.gxp.compiler.reparent.MissingAttributesError;
import com.google.gxp.compiler.reparent.RequiresStaticContentError;
import com.google.gxp.compiler.reparent.UnknownContentTypeError;
import com.google.gxp.compiler.servicedir.AmbiguousImportError;
import com.google.gxp.compiler.validate.DuplicateParameterNameError;
import com.google.gxp.compiler.validate.NumParamsMismatchError;
import com.google.gxp.compiler.validate.ParamConstructorMismatchError;
import com.google.gxp.compiler.validate.ParamDefaultMismatchError;
import com.google.gxp.compiler.validate.ParamNameMismatchError;
import com.google.gxp.compiler.validate.ParamTypeMismatchError;
import com.google.gxp.compiler.validate.SchemaMismatchError;
import com.google.gxp.compiler.validate.TemplateParamWithHasConstructorError;
import com.google.gxp.compiler.validate.TemplateParamWithHasDefaultError;
import com.google.gxp.compiler.validate.TooManyContentParametersError;

/**
 * Collection of tests of proper error reporting by the GXP compiler relating
 * to {@code <gxp:template>} and its special children ({@code <gxp:param>},
 * {@code <gxp:throws>}, etc.)
 */
public class TemplateErrorTest extends BaseTestCase {
  public void testTemplate_filenameMismatch() throws Exception {
    compileNoHeader(
        "<!DOCTYPE gxp:template SYSTEM \"http://gxp.googlecode.com/svn/trunk/resources/xhtml.ent\">",
        "",
        "<gxp:template name='com.google.WrongName'",
        "              xmlns='http://www.w3.org/1999/xhtml'",
        "              xmlns:gxp='http://google.com/2001/gxp'>",
        "</gxp:template>");
    assertAlert(new MismatchedTemplateNameError(
        pos(5,54), "com.google.WrongName",
        "com.google.gxp.compiler.errortests.TestTemplate_filenameMismatch"));
    assertNoUnexpectedAlerts();
  }

  public void testTemplate_invalidName() throws Exception {
    FileRef gxp = createFile("Holy!Cow!", "Hello");
    compileFiles(gxp);
    assertAlert(new InvalidNameError(
             pos(1, 558), "com.google.gxp.compiler.errortests.Holy!Cow!"));
    assertNoUnexpectedAlerts();
  }

  public void testTemplate_nonRoot() throws Exception {
    compile(
        "<gxp:template"
        + " name='com.google.gxp.compiler.errortests.TestTemplate_nonRoot'>",
        "</gxp:template>");
    assertAlert(new BadNodePlacementError(pos(2,1), "<gxp:template>",
                                          "inside <gxp:template>"));
    assertNoUnexpectedAlerts();
  }

  public void testTemplate_unknownContentType() throws Exception {
    compileNoHeader(
        "<!DOCTYPE gxp:template SYSTEM \"http://gxp.googlecode.com/svn/trunk/resources/xhtml.ent\">",
        "",
        "<gxp:template name='com.google.gxp.compiler.errortests."
        + "TestTemplate_unknownContentType'",
        "               xmlns:gxp='http://google.com/2001/gxp'",
        "               content-type='text/bad'>",
        "</gxp:template>");
    assertAlert(new UnknownContentTypeError(pos(5,40), "text/bad"));
    assertAlert(new NothingToCompileError(pos()));
    assertNoUnexpectedAlerts();
  }

  public void testImport_hasBothClassAndPackage() throws Exception {
    compile("<gxp:import package='com.google.a' class='com.google.a.b' />");
    assertAlert(new ConflictingAttributesError(pos(2,1), "<gxp:import>",
                                               "'class' attribute", "'package' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testImport_neitherClassOrPackage() throws Exception {
    compile("<gxp:import/>");
    assertAlert(new MissingAttributesError(pos(2,1), "<gxp:import>",
                                           "class", "package"));
    assertNoUnexpectedAlerts();
  }

  public void testImport_duplicateClassImports() throws Exception {
    compile("<gxp:import class='java.util.List' />",
            "<gxp:import class='java.util.List' />");
    assertAlert(new DuplicateImportError(pos(3,1), "java.util.List"));
    assertNoUnexpectedAlerts();
  }

  public void testImport_invalidClassName() throws Exception {
    compile("<gxp:import class='com.google.bad!name' />");
    assertAlert(new InvalidNameError(pos(2,1), "com.google.bad!name"));
    assertNoUnexpectedAlerts();
  }

  public void testImport_invalidPackageName() throws Exception {
    compile("<gxp:import package='com.google.bad!name' />");
    assertAlert(new InvalidNameError(pos(2,1), "com.google.bad!name"));
    assertNoUnexpectedAlerts();
  }

  public void testImport_nonEmpty() throws Exception {
    compile("<gxp:import class='com.google.foo.bar'>",
            "some text",
            "</gxp:import>");
    assertAlert(new BadNodePlacementError(pos(2,40), "text",
                                          "inside <gxp:import>"));
    assertNoUnexpectedAlerts();
  }

  public void testImport_notTemplateChild() throws Exception {
    compile("<html>",
            "<gxp:import class='com.google.foo.bar' />",
            "</html>");
    assertAlert(new BadNodePlacementError(pos(3,1), "<gxp:import>",
                                          "inside <html>"));
    assertNoUnexpectedAlerts();
  }

  public void testImport_ambiguousImports() throws Exception {
    compile("<gxp:import class='com.google.foo.Baz'/>",
            "<gxp:import class='com.google.bar.Baz'/>");
    assertAlert(new AmbiguousImportError(pos(3,1), "Baz", "com.google.foo.Baz",
                                         "com.google.bar.Baz"));
    assertNoUnexpectedAlerts();
  }

  public void testImport_withWhitespace() throws Exception {
    // good (whitespace around periods
    compile("<gxp:import class='com.google.foo . Bar'/>",
            "<gxp:import package='com.google . baz'/>");
    assertNoUnexpectedAlerts();

    // bad (other whitespace)
    compile("<gxp:import class='com.google.fo o.Bar'/>",
            "<gxp:import class='com.google.b az'/>");
    assertAlert(new InvalidNameError(pos(2,1), "com.google.fo o.Bar"));
    assertAlert(new InvalidNameError(pos(3,1), "com.google.b az"));
    assertNoUnexpectedAlerts();
  }

  public void testParam_badContentInDefault() throws Exception {
    compile("<gxp:param name='p' content-type='text/plain'>",
            "  <gxp:attr name='default'>",
            "    <b>foo</b>",
            "  </gxp:attr>",
            "</gxp:param>");
    assertAlert(new TypeError(pos(4,5), "<b>", "text/html", "text/plain"));
    assertNoUnexpectedAlerts();
  }

  public void testParam_conflictingTypeAttributes() throws Exception {
    compile("<gxp:param name='foo' gxp:type='boolean' content-type='text/html'" +
            "                      type='String' cpp:type='string' />");
    assertAlert(new ConflictingAttributesError(pos(2,1), "<gxp:param>",
                                               "'gxp:type' attribute",
                                               "'content-type' attribute"));
    assertAlert(new ConflictingAttributesError(pos(2,1), "<gxp:param>",
                                               "'gxp:type' attribute",
                                               "'type' attribute"));;
    assertAlert(new ConflictingAttributesError(pos(2,1), "<gxp:param>",
                                               "'gxp:type' attribute",
                                               "'cpp:type' attribute"));
    assertNoUnexpectedAlerts();

    compile("<gxp:param name='foo' content-type='text/html' type='String' java:type='string' />");
    assertAlert(new ConflictingAttributesError(pos(2,1), "<gxp:param>",
                                               "'content-type' attribute",
                                               "'type' attribute"));;
    assertAlert(new ConflictingAttributesError(pos(2,1), "<gxp:param>",
                                               "'content-type' attribute",
                                               "'java:type' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testParam_noTypeAttributes() throws Exception {
    compile("<gxp:param name='foo' />");
    assertAlert(new MissingTypeError(pos(2,1), "<gxp:param>", "Java"));
    assertAlert(new MissingTypeError(pos(2,1), "<gxp:param>", "C++"));
    assertNoUnexpectedAlerts();
  }

  public void testParam_noCppTypeAttributes() throws Exception {
    compile("<gxp:param name='foo' java:type='String' />");
    assertAlert(new MissingTypeError(pos(2,1), "<gxp:param>", "C++"));
    assertNoUnexpectedAlerts();
  }

  public void testParam_noJavaTypeAttributes() throws Exception {
    compile("<gxp:param name='foo' cpp:type='string' />");
    assertAlert(new MissingTypeError(pos(2,1), "<gxp:param>", "Java"));
    assertNoUnexpectedAlerts();
  }

  public void testParam_contentAndTypeSpecified() throws Exception {
    compile("<gxp:param name='foo' type='String' content='*' />");
    assertAlert(new ContentTypeExpectedAlert(pos(2, 1),
                                             "<gxp:param>",
                                             "when content='*' is set."));
    assertNoUnexpectedAlerts();
  }

  public void testParam_defaultNotExpression() throws Exception {
    assertIllegalExpressionDetected(
        "<gxp:param name='x' type='int' default='", "'/>");

    assertIllegalExpressionDetected(
        "<gxp:param name='x' type='int' expr:default='", "'/>");
  }

  public void testParam_duplicateName() throws Exception {
    // standard param
    compile("<gxp:param name='foo' type='String' />",
            "<gxp:param name='foo' type='String' />");
    assertAlert(new DuplicateParameterNameError(pos(3,1), "foo"));
    assertNoUnexpectedAlerts();
  }

  public void testParam_withHasConstructor() throws Exception {
    compile("<gxp:param name='foo' type='String' has-constructor='true' />");
    assertAlert(new TemplateParamWithHasConstructorError(pos(2,1)));
    assertNoUnexpectedAlerts();
  }

  public void testParam_withHasDefault() throws Exception {
    compile("<gxp:param name='foo' type='String' has-default='true' />");
    assertAlert(new TemplateParamWithHasDefaultError(pos(2,1)));
    assertNoUnexpectedAlerts();
  }

  public void testParam_invalidContent() throws Exception {
    compile("<gxp:param name='foo' content='bar' />");
    assertAlert(new InvalidAttributeValueError(pos(2,1),
                                               "'content' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testParam_invalidName() throws Exception {
    assertIllegalVariableNameDetected(
        "name", "<gxp:param name='", "' type='String' />");
  }

  public void testParam_invalidType() throws Exception {
    assertIllegalTypeDetected("<gxp:param name='foo' type='", "' />");
  }

  public void testParam_multipleContainerParams() throws Exception {
    compile("<gxp:param name='body1' content='*' />",
            "<gxp:param name='body2' content='*' />");
    assertAlert(new TooManyContentParametersError(pos(3,1)));
    assertNoUnexpectedAlerts();
  }

  public void testParam_nonHtmlChildren() throws Exception {
    compile("<gxp:param name='foo' type='String'>",
            "<b>html comment</b>",
            "</gxp:param>");
    assertNoUnexpectedAlerts();

    compile("<gxp:param name='foo' type='String'>",
            "<b expr:class='\"foo\"'>html comment</b>",
            "</gxp:param>");
    assertAlert(new RequiresStaticContentError(pos(2,1), "<gxp:param>"));
    assertNoUnexpectedAlerts();

    compile("<gxp:param name='foo' type='String'>",
            "<gxp:eval expr='1' />",
            "</gxp:param>");
    assertAlert(new RequiresStaticContentError(pos(2,1), "<gxp:param>"));
    assertNoUnexpectedAlerts();
  }

  public void testParam_notTemplateChild() throws Exception {
    compile("<html>",
            "<gxp:param name='foo' type='String' />",
            "</html>");
    assertAlert(new BadNodePlacementError(pos(3,1), "<gxp:param>",
                                          "inside <html>"));
    assertNoUnexpectedAlerts();
  }

  public void testParam_unknownContentType() throws Exception {
    compile("<gxp:param name='foo' content-type='text/bad' />",
            "<gxp:param name='body' content='*' content-type='text/bad' />");
    assertAlert(new UnknownContentTypeError(pos(2,1), "text/bad"));
    assertAlert(new UnknownContentTypeError(pos(3,1), "text/bad"));
    assertNoUnexpectedAlerts();
  }

  public void testThrows_invalidClassName() throws Exception {
    compile("<gxp:throws exception='com.google.holy!cow' />");
    assertAlert(new InvalidNameError(pos(2,1), "com.google.holy!cow"));
    assertNoUnexpectedAlerts();

    compile("<gxp:throws exception='FoobarException' />");
    assertNoUnexpectedAlerts();
  }

  public void testThrows_nonEmpty() throws Exception {
    compile("<gxp:throws exception='com.google.foo.bar'>",
            "some text",
            "</gxp:throws>");
    assertAlert(new BadNodePlacementError(pos(2,44), "text",
                                          "inside <gxp:throws>"));
    assertNoUnexpectedAlerts();
  }

  public void testThrows_notTemplateChild() throws Exception {
    compile("<html>",
            "<gxp:throws exception='com.google.foo.bar' />",
            "</html>");
    assertAlert(new BadNodePlacementError(pos(3,1), "<gxp:throws>",
                                          "inside <html>"));
    assertNoUnexpectedAlerts();
  }

  public void testTypeParam_invalidName() throws Exception {
    assertIllegalVariableNameDetected(
        "name", "<gxp:typeparam name='","' extends='SomeClass' />");
  }

  public void testParam_invalidRegex() throws Exception {
    compile("<gxp:param name='s' type='String' regex='*' />");
    assertAlert(new BadRegexError(pos(2, 1), "*"));
    assertNoUnexpectedAlerts();
  }

  public void testTypeParam_invalidType() throws Exception {
    assertIllegalTypeDetected("<gxp:typeparam name='foo' extends='","' />");
  }

  public void testTypeParam_nonEmpty() throws Exception {
    compile("<gxp:typeparam name='T' extends='List'>",
            "some text",
            "</gxp:typeparam>");
    assertAlert(new BadNodePlacementError(pos(2,40), "text",
                                          "inside <gxp:typeparam>"));
    assertNoUnexpectedAlerts();
  }

  public void testTypeParam_notTemplateChild() throws Exception {
    compile("<html>",
            "<gxp:typeparam name='T' extends='List' />",
            "</html>");
    assertAlert(new BadNodePlacementError(pos(3,1), "<gxp:typeparam>",
                                          "inside <html>"));
    assertNoUnexpectedAlerts();
  }

  public void testMoreParamsInInterfaceThanTemplate() throws Exception {
    FileRef iface = createInterfaceFile("testInterface",
                                        "<gxp:param name='foo' type='String' />");

    FileRef implementation = createFile("testImplementation",
                                        "<gxp:implements interface='testInterface' />");

    compileFiles(iface, implementation);
    assertAlert(new NumParamsMismatchError(pos(2,1), 1, 0));
    assertNoUnexpectedAlerts();
  }

  public void testMoreParamsInTemplateThanInterface() throws Exception {
    FileRef iface = createInterfaceFile("testInterface",
                                        "<gxp:param name='foo' type='String' />");

    FileRef implementation = createFile("testImplementation",
                                        "<gxp:implements interface='testInterface' />",
                                        "<gxp:param name='bar' type='int' />",
                                        "<gxp:param name='foo' type='int' />");

    compileFiles(iface, implementation);
    assertAlert(new NumParamsMismatchError(pos(2,1), 1, 2));
    assertNoUnexpectedAlerts();
  }

  public void testCannotImplementTemplate() throws Exception {
    FileRef refTemplate = createFile("template1",
                                     "<gxp:param name='foo' type='String' />");
    FileRef implementation = createFile("template2",
                                        "<gxp:implements interface='template1' />",
                                        "<gxp:param name='foo' type='String' />");
    compileFiles(refTemplate, implementation);
    assertAlert(new ImplementableNotFoundError(pos(2,1), TemplateName.create(null, "template1")));
    assertNoUnexpectedAlerts();
  }

  public void testCannotImplementInterfaceWithDifferentSchema() throws Exception {
    FileRef refInterface = createFileNoHeader("testInterface",
        "<!DOCTYPE gxp:interface SYSTEM \"http://gxp.googlecode.com/svn/trunk/resources/xhtml.ent\">",
        "",
        "<gxp:interface name='com.google.gxp.compiler.errortests.testInterface'",
        "               content-type='text/javascript'",
        "               xmlns:gxp='http://google.com/2001/gxp'>",
        "  <gxp:param name='foo' type='String' />",
        "</gxp:interface>");


    FileRef implementation = createFile("testImplementation",
                                        "<gxp:implements interface='testInterface' />",
                                        "<gxp:param name='foo' type='String' />");

    compileFiles(refInterface, implementation);
    assertAlert(new SchemaMismatchError(
        pos(2,1), "com.google.gxp.compiler.errortests.testImplementation"));
    assertNoUnexpectedAlerts();
  }

  public void testParamNameMismatch() throws Exception {
    FileRef iface = createInterfaceFile("testInterface",
                                        "<gxp:param name='foo' type='String' />");

    FileRef implementation = createFile("testImplementation",
                                        "<gxp:implements interface='testInterface' />",
                                        "<gxp:param name='bar' type='String' />");

    compileFiles(iface, implementation);
    assertAlert(new ParamNameMismatchError(pos(2,1), "foo", "bar"));
    assertNoUnexpectedAlerts();
  }

  public void testParamTypeMismatch() throws Exception {
    FileRef iface = createInterfaceFile("testInterface",
                                        "<gxp:param name='foo' gxp:type='boolean' />");

    FileRef implementation = createFile("testImplementation",
                                        "<gxp:implements interface='testInterface' />",
                                        "<gxp:param name='foo' type='String' />");

    compileFiles(iface, implementation);
    assertAlert(new ParamTypeMismatchError(pos(2,1), "foo", "BooleanType", "NativeType"));
    assertNoUnexpectedAlerts();
  }

  public void testParamContentTypeMismatch() throws Exception {
    FileRef iface = createInterfaceFile("testInterface",
                                        "<gxp:param name='foo' content-type='text/html' />");

    FileRef implementation = createFile("testImplementation",
                                        "<gxp:implements interface='testInterface' />",
                                        "<gxp:param name='foo' content-type='text/plain' />");

    compileFiles(iface, implementation);
    assertAlert(new ParamTypeMismatchError(pos(2,1), "foo", "text/html", "text/plain"));
    assertNoUnexpectedAlerts();
  }

  public void testParamMissingDefault() throws Exception {
    FileRef iface = createInterfaceFile("testInterface",
                                        "<gxp:param name='x' type='String' has-default='true' />");

    FileRef implementation = createFile("testImplementation",
                                        "<gxp:implements interface='testInterface' />",
                                        "<gxp:param name='x' type='String' />");

    compileFiles(iface, implementation);
    assertAlert(new ParamDefaultMismatchError(pos(3,1), "<gxp:param>", "x"));
    assertNoUnexpectedAlerts();
  }

  public void testParamMissingConstructor() throws Exception {
    FileRef iface = createInterfaceFile(
        "testInterface",
        "<gxp:param name='x' type='String' has-constructor='true' />");

    FileRef implementation = createFile("testImplementation",
                                        "<gxp:implements interface='testInterface' />",
                                        "<gxp:param name='x' type='String' />");

    compileFiles(iface, implementation);
    assertAlert(new ParamConstructorMismatchError(pos(3,1), "<gxp:param>", "x"));
    assertNoUnexpectedAlerts();
  }

  public void testNoThisParamSupplied() throws Exception {
    FileRef iface = createInterfaceFile("testInterface");
    FileRef implementation = createFile("testImplementation",
                                        "<call:testInterface />");

    compileFiles(iface, implementation);
    assertAlert(new MissingAttributeError(pos(2,1), "<call:testInterface>", "this"));
    assertNoUnexpectedAlerts();
  }
}
