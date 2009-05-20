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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.gxp.compiler.alerts.Alert.Severity;
import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.DefaultAlertPolicy;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.codegen.DefaultCodeGeneratorFactory;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.fs.FileSystem;
import com.google.gxp.compiler.fs.InMemoryFileSystem;
import com.google.gxp.compiler.parser.FileSystemEntityResolver;
import com.google.gxp.compiler.parser.Parser;
import com.google.gxp.compiler.parser.SaxXmlParser;
import com.google.gxp.compiler.schema.BuiltinSchemaFactory;
import com.google.gxp.compiler.schema.SchemaFactory;

import static com.google.gxp.testing.MoreAsserts.assertContainsRegex;

import junit.framework.TestCase;

import java.io.*;
import java.util.regex.Pattern;

/**
 * Tests for CompilationSet.
 */
public class CompilationSetTest extends TestCase {
  // TODO(konigsberg): add tests for processing of INFO alerts.
  private final FileSystem fs = new InMemoryFileSystem();
  private static final String DOCTYPE =
      "<!DOCTYPE gxp:template SYSTEM "
      + "\"http://gxp.googlecode.com/svn/trunk/resources/xhtml.ent\">";
  private static final String NAMESPACE_DECLS =
      " xmlns='http://www.w3.org/1999/xhtml'"
      + " xmlns:gxp='http://google.com/2001/gxp'"
      + " xmlns:call='http://google.com/2001/gxp/templates'"
      + " xmlns:expr='http://google.com/2001/gxp/expressions'";

  private void assertContains(String expected, String actual) {
    assertContainsRegex(Pattern.quote(expected), actual);
  }

  /**
   * Common logic for testing code generation paths. Compiles the supplied
   * source into the specified languages and returns the Alerts that are
   * generated.  The generated code will be stored in fs.
   */
  private void testCodeGenPath(String gxpSource,
                               AlertSink alertSink,
                               OutputLanguage... outputLanguages)
      throws Exception {
    // Create source file.
    FileRef gxpFileRef = fs.parseFilename("inmemory/gxp/test/Main.gxp");
    Writer gxpFile = gxpFileRef.openWriter(Charsets.US_ASCII);
    gxpFile.write(gxpSource);
    gxpFile.close();

    // Compile it.
    SchemaFactory schemaFactory = BuiltinSchemaFactory.INSTANCE;
    Parser parser = new Parser(schemaFactory, SaxXmlParser.INSTANCE,
                               new FileSystemEntityResolver(fs));
    CompilationSet cSet =
        new CompilationSet.Builder(parser,
                                   new DefaultCodeGeneratorFactory(),
                                   SimpleCompilationManager.INSTANCE)
        .build(gxpFileRef);

    cSet.compile(alertSink, DefaultAlertPolicy.INSTANCE,
                 ImmutableList.of(outputLanguages));
  }

  /**
   * Helper for testing the Java code generation path. Compiles the supplied
   * source into Java, verifies that the Alerts are as expected, and returns
   * the resulting Java code. Also checks some invariants.
   */
  private String testJavaPath(String gxpSource, AlertSet expectedAlerts)
      throws Exception {
    String javaCode = generateCode(
        gxpSource, expectedAlerts, OutputLanguage.JAVA);

    // Check some invariants.
    assertContains("DO NOT EDIT", javaCode);
    assertContains("package inmemory.gxp.test;", javaCode);
    assertContains("class Main", javaCode);

    return javaCode;
  }

  private String testXmbPath(String gxpSource, AlertSet expectedAlerts)
      throws Exception {
    String xmbCode = generateCode(
        gxpSource, expectedAlerts, OutputLanguage.XMB);

    // Check some invariants.
    assertContains("<messagebundle>", xmbCode);
    assertTrue(xmbCode.endsWith("</messagebundle>\n"));

    return xmbCode;
  }

  private String generateCode(String gxpSource, AlertSet expectedAlerts,
                              OutputLanguage outputLanguage)
      throws Exception {

    // Generate the code
    AlertSetBuilder builder = new AlertSetBuilder();

    testCodeGenPath(gxpSource, filterAlertSink(builder), outputLanguage);

    // Validate the list of (non-info) alerts)
    assertEquals(expectedAlerts, builder.buildAndClear());

    // Get output.
    String outputSystemFilename =
        "inmemory/gxp/test/Main" + outputLanguage.getSuffix(0);
    String code = getCodeFor(outputSystemFilename);
    return code;
  }

  private FilteredAlertSink filterAlertSink(AlertSetBuilder builder) {
    FilteredAlertSink filter = new FilteredAlertSink(
        builder, DefaultAlertPolicy.INSTANCE, Severity.INFO);
    return filter;
  }

  private String getCodeFor(String systemFilename) throws IOException {
    FileRef fileRef = fs.parseFilename(systemFilename);
    String code = new String(ByteStreams.toByteArray(fileRef.openInputStream()));
    return code;
  }

  /**
   * Tests the simplest Java code generation path.
   */
  public void testBaseJavaPath() throws Exception {
    String javaCode = testJavaPath("<gxp:template"
                                   + NAMESPACE_DECLS
                                   + " name='inmemory.gxp.test.Main'/>",
                                   AlertSet.EMPTY);
    assertContains("public static void write("
                   + "final java.lang.Appendable gxp$out,"
                   + " final com.google.gxp.base.GxpContext gxp_context)",
                   javaCode);
  }

  /**
   * Tests the Java code generation path with parameters.
   */
  public void testJavaPathWithParameters() throws Exception {
    String javaCode = testJavaPath("<gxp:template"
                                   + NAMESPACE_DECLS
                                   + " name='inmemory.gxp.test.Main'>"
                                   + "<gxp:param name='s' type='String'/>"
                                   + "<gxp:param name='i' type='int'/>"
                                   + "</gxp:template>",
                                   AlertSet.EMPTY);
    assertContains("public static void write("
                   + "final java.lang.Appendable gxp$out,"
                   + " final com.google.gxp.base.GxpContext gxp_context,"
                   + " final String s,"
                   + " final int i)",
                   javaCode);
  }

  /**
   * Tests the Java code generation path with imports.
   */
  public void testJavaPathWithImports() throws Exception {
    String javaCode = testJavaPath("<gxp:template"
                                   + NAMESPACE_DECLS
                                   + " name='inmemory.gxp.test.Main'>"
                                   + "<gxp:import class='this.is.Class1'/>"
                                   + "<gxp:import package='this.is.pkg1'/>"
                                   + "<gxp:import class='this.is.Class2'/>"
                                   + "<gxp:import package='this.is.pkg2'/>"
                                   + "</gxp:template>",
                                   AlertSet.EMPTY);
    assertContains("import this.is.Class1;", javaCode);
    assertContains("import this.is.pkg1.*;", javaCode);
    assertContains("import this.is.Class2;", javaCode);
    assertContains("import this.is.pkg2.*;", javaCode);
  }

  /**
   * Tests the Java code generation path with static content.
   */
  public void testJavaPathWithStaticContent() throws Exception {
    String javaCode = testJavaPath("<gxp:template"
                                   + NAMESPACE_DECLS
                                   + " name='inmemory.gxp.test.Main'>"
                                   + "hello, world!"
                                   + "</gxp:template>",
                                   AlertSet.EMPTY);
    assertContains("gxp$out.append(\"hello, world!\");", javaCode);
  }

  /**
   * Tests the Java code generation path with static content that needs
   * escaping.
   */
  public void testJavaPathWithEscapableStaticContent() throws Exception {
    String javaCode = testJavaPath(DOCTYPE + "<gxp:template"
                                   + NAMESPACE_DECLS
                                   + " name='inmemory.gxp.test.Main'>"
                                   + "&nbsp; \" &amp; &#x4321; &lt; &gt; &apos;"
                                   + "</gxp:template>",
                                   AlertSet.EMPTY);
    assertContains("gxp$out.append("
                   + "\"&nbsp; &quot; &amp; &#17185; &lt; &gt; &#39;\");",
                   javaCode);
  }

  /**
   * Tests the Java code generation path with static tags.
   */
  public void testJavaPathWithStaticHtml() throws Exception {
    String javaCode = testJavaPath("<gxp:template"
                                   + NAMESPACE_DECLS
                                   + " name='inmemory.gxp.test.Main'>"
                                   + "hello, <b>world</b>!"
                                   + "</gxp:template>",
                                   AlertSet.EMPTY);
    assertContains("gxp$out.append(\"hello, <b>world</b>!\");", javaCode);
  }

  /**
   * Tests the Java code generation path with static tags that have attributes.
   * Note that this also tests that attribute order is preserved.
   */
  public void testJavaPathWithStaticHtmlWithAttrs() throws Exception {
    String javaCode = testJavaPath("<gxp:template"
                                   + NAMESPACE_DECLS
                                   + " name='inmemory.gxp.test.Main'>"
                                   + "hello, <a id='1' href='hi'>link</a>!"
                                   + " goodbye, <a href='bye' id='2'>link</a>!"
                                   + "</gxp:template>",
                                   AlertSet.EMPTY);
    assertContains("gxp$out.append(\""
                   + "hello, <a id=\\\"1\\\""
                   + " href=\\\"hi\\\">link</a>!"
                   + " goodbye, <a href=\\\"bye\\\""
                   + " id=\\\"2\\\">link</a>!"
                   + "\");", javaCode);
  }

  public void testJavaPathWithExprAttr() throws Exception {
    String javaCode = testJavaPath("<gxp:template"
                                   + NAMESPACE_DECLS
                                   + " name='inmemory.gxp.test.Main'>"
                                   + "The <a expr:href='6 * 9'>answer</a>."
                                   + "</gxp:template>",
                                   AlertSet.EMPTY);
    assertContains("gxp$out.append(\"The <a href=\\\"\");", javaCode);
    assertContains("HtmlAppender.INSTANCE.append(gxp$out, gxp_context, (6 * 9));", javaCode);
    assertContains("gxp$out.append(\"\\\">answer</a>.\");", javaCode);
  }

  public void testJavaPathWithEvalElement() throws Exception {
    String javaCode = testJavaPath("<gxp:template"
                                   + NAMESPACE_DECLS
                                   + " name='inmemory.gxp.test.Main'>"
                                   + "The answer is <gxp:eval expr='6 * 9'/>."
                                   + "</gxp:template>",
                                   AlertSet.EMPTY);
    assertContains("gxp$out.append(\"The answer is \");", javaCode);
    assertContains("HtmlAppender.INSTANCE.append(gxp$out, gxp_context, (6 * 9));", javaCode);
    assertContains("gxp$out.append(\".\");", javaCode);
  }

  public void testCompletelyBrokenInput() throws Exception {
    // we just want to make sure that this doesn't throw an exception
    for (OutputLanguage outputLanguage : OutputLanguage.values()) {
      AlertSetBuilder builder = new AlertSetBuilder();
      testCodeGenPath("<foo>", builder, outputLanguage);
      assertFalse(builder.buildAndClear().isEmpty());
      // TODO(laurence): also test that Alerts look reasonable?
    }
  }

  /**
   * Verifies that when generating two languages from one compilation
   * prevents any output files from being generated, while only reporting
   * one error.
   */
  public void testCompilingErroneousFileTwice() throws Exception {
    String gxpSource = "<gxp:template"
        + NAMESPACE_DECLS
        + " name='inmemory.gxp.test.Main'>\n"
        + "<gxp:eval/>"
        + "</gxp:template>";

    String javaFilename = "inmemory/gxp/test/Main.java";
    String xmbFilename = "inmemory/gxp/test/Main.xmb";

    // Generate the code
    AlertSetBuilder javaBuilder = new AlertSetBuilder();


    testCodeGenPath(gxpSource, filterAlertSink(javaBuilder),
                    OutputLanguage.JAVA);
    try {
      getCodeFor(javaFilename);
      fail("Exception expected");
    } catch (IOException expected) {
      // expected
    }

    AlertSet javaAlertSet = javaBuilder.buildAndClear();

    AlertSetBuilder xmbBuilder = new AlertSetBuilder();
    testCodeGenPath(gxpSource, filterAlertSink(xmbBuilder), OutputLanguage.XMB);
    try {
      getCodeFor(xmbFilename);
      fail("Exception expected");
    } catch (IOException expected) {
      // expected
    }

    AlertSet xmbAlertSet = xmbBuilder.buildAndClear();

    assertEquals(javaAlertSet, xmbAlertSet);

    AlertSetBuilder dualLanguageBuilder = new AlertSetBuilder();
    testCodeGenPath(gxpSource, filterAlertSink(dualLanguageBuilder),
                    OutputLanguage.JAVA, OutputLanguage.XMB);

    try {
      getCodeFor(javaFilename);
      fail("Exception expected");
    } catch (IOException expected) {
      // expected
    }

    try {
      getCodeFor(xmbFilename);
      fail("Exception expected");
    } catch (IOException expected) {
      // expected
    }
    assertEquals(dualLanguageBuilder.buildAndClear(), javaAlertSet);
  }

}
