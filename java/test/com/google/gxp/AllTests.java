/**
 * Copyright (C) 2006 Google Inc.
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

package com.google.gxp;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * GXP Test Suite Builder
 */
public class AllTests extends TestCase {
  public static TestSuite suite() {
    TestSuite suite = new TestSuite();

    ////////////////////////////////////////////////////////////////////////////////
    // Rutime Library Tests
    ////////////////////////////////////////////////////////////////////////////////

    suite.addTestSuite(com.google.gxp.base.GxpContextTest.class);
    suite.addTestSuite(com.google.gxp.css.ColorTest.class);
    suite.addTestSuite(com.google.gxp.css.CssAppenderTest.class);
    suite.addTestSuite(com.google.gxp.html.HtmlClosuresTest.class);
    suite.addTestSuite(com.google.gxp.js.JavascriptAppenderTest.class);
    suite.addTestSuite(com.google.gxp.js.JavascriptClosuresTest.class);
    suite.addTestSuite(com.google.gxp.text.PlaintextAppenderTest.class);
    suite.addTestSuite(com.google.gxp.text.PlaintextClosuresTest.class);

    ////////////////////////////////////////////////////////////////////////////////
    // Compiler Tests
    ////////////////////////////////////////////////////////////////////////////////

    // compiler tests
    suite.addTestSuite(com.google.gxp.compiler.CompilationSetTest.class);
    suite.addTestSuite(com.google.gxp.compiler.GxpcTestCaseTest.class);
    suite.addTestSuite(com.google.gxp.compiler.alerts.AlertTest.class);
    suite.addTestSuite(com.google.gxp.compiler.alerts.AlertSetTest.class);
    suite.addTestSuite(com.google.gxp.compiler.alerts.SourcePositionTest.class);
    suite.addTestSuite(com.google.gxp.compiler.alerts.UniquifyingAlertSinkTest.class);
    suite.addTestSuite(com.google.gxp.compiler.ant.GxpcTaskTest.class);
    suite.addTestSuite(com.google.gxp.compiler.cli.GxpcFlagsTest.class);
    suite.addTestSuite(com.google.gxp.compiler.cli.GxpcTest.class);
    suite.addTestSuite(com.google.gxp.compiler.collapse.SpaceCollapserTest.class);
    suite.addTestSuite(com.google.gxp.compiler.collapse.SpaceOperatorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.depend.DependencyCheckingTest.class);
    suite.addTestSuite(com.google.gxp.compiler.depend.SerializabilityTest.class);
    suite.addTestSuite(com.google.gxp.compiler.fs.FileRefTest.class);
    suite.addTestSuite(com.google.gxp.compiler.fs.InMemoryFileSystemTest.class);
    suite.addTestSuite(com.google.gxp.compiler.fs.SourcePathFileSystemTest.class);
    suite.addTestSuite(com.google.gxp.compiler.fs.SystemFileSystemTest.class);
    suite.addTestSuite(com.google.gxp.compiler.io.CIndenterTest.class);
    suite.addTestSuite(com.google.gxp.compiler.parser.NamespaceSetTest.class);
    suite.addTestSuite(com.google.gxp.compiler.parser.ParserTest.class);
    suite.addTestSuite(com.google.gxp.compiler.reparent.AttributeMapTest.class);
    suite.addTestSuite(com.google.gxp.compiler.reparent.EditablePartsTest.class);
    suite.addTestSuite(com.google.gxp.compiler.reparent.ReparenterTest.class);
    suite.addTestSuite(com.google.gxp.compiler.schema.SchemaParserTest.class);
    suite.addTestSuite(com.google.gxp.compiler.servicedir.ScopedServiceDirectoryTest.class);
    suite.addTestSuite(com.google.gxp.compiler.xmb.XmlCharsetEscaperTest.class);

    // errortests
    suite.addTestSuite(com.google.gxp.compiler.errortests.AnnotateErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.AttributeBundleErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.BasicErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.CallErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.ConditionalErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.I18nErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.InstantiableErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.InterfaceErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.LoopErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.MultiLingualErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.OutputElementErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.ParseErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.SchemaErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.TemplateErrorTest.class);
    suite.addTestSuite(com.google.gxp.compiler.errortests.UnextractableContentAlertTest.class);

    // functional tests
    suite.addTestSuite(com.google.gxp.compiler.functests.JavaCodeTest.class);
    suite.addTestSuite(com.google.gxp.compiler.functests.annotate.JavaCodeTest.class);
    suite.addTestSuite(com.google.gxp.compiler.functests.bundle.JavaCodeTest.class);
    suite.addTestSuite(com.google.gxp.compiler.functests.call.JavaCodeTest.class);
    suite.addTestSuite(com.google.gxp.compiler.functests.closures.JavaCodeTest.class);
    suite.addTestSuite(com.google.gxp.compiler.functests.i18n.JavaCodeTest.class);
    suite.addTestSuite(com.google.gxp.compiler.functests.instantiable.JavaCodeTest.class);
    suite.addTestSuite(com.google.gxp.compiler.functests.multilingual.JavaCodeTest.class);

    // dynamic tests
    suite.addTestSuite(com.google.gxp.compiler.dynamictests.JavaCodeTest.class);
    suite.addTestSuite(com.google.gxp.compiler.dynamictests.DynamicTest.class);

    ////////////////////////////////////////////////////////////////////////////////
    // Non HTML Markup Tests
    ////////////////////////////////////////////////////////////////////////////////

    // rss
    suite.addTestSuite(com.google.gxp.rss.JavaCodeTest.class);
    suite.addTestSuite(com.google.gxp.rss.RssAppenderTest.class);
    suite.addTestSuite(com.google.gxp.rss.RssClosuresTest.class);

    return suite;
  }
}
