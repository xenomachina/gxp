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

package com.google.gxp.testing;

import com.google.common.base.CharEscapers;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.CompilationManager;
import com.google.gxp.compiler.CompilationSet;
import com.google.gxp.compiler.SimpleCompilationManager;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.DefaultAlertPolicy;
import com.google.gxp.compiler.alerts.ErroringAlertSink;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.codegen.CodeGeneratorFactory;
import com.google.gxp.compiler.codegen.DefaultCodeGeneratorFactory;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.fs.InMemoryFileSystem;
import com.google.gxp.compiler.parser.FileSystemEntityResolver;
import com.google.gxp.compiler.parser.Parser;
import com.google.gxp.compiler.parser.SaxXmlParser;
import com.google.gxp.compiler.schema.BuiltinSchemaFactory;
import com.google.gxp.compiler.schema.FileBackedSchemaFactory;
import junit.framework.TestCase;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Base TestCase for gxpc tests that require building GXPs as part of the tests.
 * Provide utility methods for building GXPs in memory.
 */
public abstract class BaseBuildingTestCase extends TestCase {
  private FileRef source;
  protected final InMemoryFileSystem fs = new InMemoryFileSystem();

  private final Parser parser =
    new Parser(BuiltinSchemaFactory.INSTANCE, SaxXmlParser.INSTANCE,
               new FileSystemEntityResolver(fs));

  public BaseBuildingTestCase() {
    fs.setCurrentTime(1);
  }

  protected final void advanceClock() {
    fs.setCurrentTime(fs.getCurrentTime() + 1);
  }

  //////////////////////////////////////////////////////////////////////
  // Functions for creating FileRefs and compiling GXPs
  //////////////////////////////////////////////////////////////////////

  /**
   * Returns the {@code FileRef} created in the most recent call to createFile
   * (or one of the compile methods).
   */
  protected final FileRef getSource() {
    return source;
  }

  /**
   * Convenience method for creating a no header file with multiple lines
   */
  protected final FileRef createFileNoHeader(String name, String... lines)
      throws Exception {
    return createFileNoHeader(name, Joiner.on("\n").join(lines));
  }

  /**
   * Create an in memory gxp file based on the given name and contents.
   */
  protected final FileRef createFileNoHeader(String name, String contents)
      throws Exception {
    // create in memory file
    String pkgDir = getPackage().replace('.', '/');
    String sourceName = "/" + pkgDir + "/" + name + ".gxp";
    source = fs.parseFilename(sourceName);

    // write GXP
    Writer writer = source.openWriter(Charsets.US_ASCII);
    writer.write(contents);
    writer.close();

    return source;
  }

  /**
   * Convenience method for creating a file with multiple lines
   */
  protected final FileRef createFile(String name, String... lines)
      throws Exception {
    return createFile(name, Joiner.on("\n").join(lines));
  }

  /**
   * Subclasses can override this function to have extra headers inserted
   * in the autogenerated <gxp:template> definition.
   *
   * @return a list of extra headers for the <gxp:template> declaration.
   */
  protected List<String> extraHeaders() {
    return Collections.emptyList();
  }

  /**
   * Create a single (in memory) GXP file with the specified name
   * containing the specified snippet.  The template will have a gxp:template
   * tag and the standard xmlns declarations on the first line, and will end
   * with the corresponding close tag. There will also be an xmlns declaration,
   * "my", for the call namespace of the generated template.
   */
  protected final FileRef createFile(String name, String snippet)
      throws Exception {

    String pkgDir = getPackage().replace('.', '/');
    String myNs = "http://google.com/2001/gxp/call/" + pkgDir;

    StringBuilder sb = new StringBuilder();
    sb.append("<!DOCTYPE gxp:template SYSTEM"
              + " \"http://gxp.googlecode.com/svn/trunk/resources/xhtml.ent\">");
    sb.append("<gxp:template name='"
              + CharEscapers.xmlEscaper().escape(getPackage() + "." + name) + "'");
    sb.append(" xmlns='http://www.w3.org/1999/xhtml'");
    sb.append(" xmlns:gxp='http://google.com/2001/gxp'");
    sb.append(" xmlns:call='http://google.com/2001/gxp/call'");
    sb.append(" xmlns:expr='http://google.com/2001/gxp/expressions'");
    sb.append(" xmlns:msg='http://google.com/2001/gxp/msg'");
    sb.append(" xmlns:nomsg='http://google.com/2001/gxp/nomsg'");
    sb.append(" xmlns:cpp='http://google.com/2001/gxp/code/cpp'");
    sb.append(" xmlns:java='http://google.com/2001/gxp/code/java'");
    sb.append(" xmlns:js='http://google.com/2001/gxp/code/javascript'");
    sb.append(" xmlns:scala='http://google.com/2001/gxp/code/scala'");
    for (String s : extraHeaders()) {
      sb.append(' ');
      sb.append(s);
    }
    sb.append(" xmlns:my='" + CharEscapers.xmlEscaper().escape(myNs) + "'>");

    return createFileFromParts(name, sb.toString(), snippet, "</gxp:template>");
  }

  /**
   * Convenience method for creating an interface file with multiple lines
   */
  protected final FileRef createInterfaceFile(String name, String... lines)
      throws Exception {
    return createInterfaceFile(name, Joiner.on("\n").join(lines));
  }

  /**
   * Create a single (in memory) GXP file with the specified name
   * containing the specified snippet.  The interface will have a gxp:interface
   * tag and the standard xmlns declarations on the first line, and will end
   * with the corresponding close tag.
   */
  protected final FileRef createInterfaceFile(String name, String snippet)
      throws Exception {

    StringBuilder sb = new StringBuilder();
    sb.append("<!DOCTYPE gxp:interface SYSTEM"
              + " \"http://gxp.googlecode.com/svn/trunk/resources/xhtml.ent\">");
    sb.append("<gxp:interface name='"
              + CharEscapers.xmlEscaper().escape(getPackage() + "." + name) + "'");
    sb.append(" xmlns='http://www.w3.org/1999/xhtml'");
    sb.append(" xmlns:gxp='http://google.com/2001/gxp'");
    sb.append(" xmlns:cpp='http://google.com/2001/gxp/code/cpp'");
    sb.append(" xmlns:java='http://google.com/2001/gxp/code/java'");
    sb.append(" xmlns:js='http://google.com/2001/gxp/code/javascript'");
    for (String s : extraHeaders()) {
      sb.append(' ');
      sb.append(s);
    }
    sb.append(">");

    return createFileFromParts(name, sb.toString(), snippet, "</gxp:interface>");
  }

  /**
   * Create a single (in memory) GXP schema file with the specified name
   * containing the specified contents.
   */
  protected final FileRef createSchemaFile(String name, String contents)
      throws Exception {
    // create in memory file
    String pkgDir = getPackage().replace('.', '/');
    InMemoryFileSystem schemaFs = new InMemoryFileSystem();
    String sourceName = "/" + pkgDir + "/" + name + ".xml";
    source = schemaFs.parseFilename(sourceName);

    // write GXP
    Writer writer = source.openWriter(Charsets.US_ASCII);
    writer.write(contents);
    writer.close();

    return source;
  }

  /**
   * Convenience method for creating a schema file with multiple lines
   */
  protected final FileRef createSchemaFile(String name, String... lines)
      throws Exception {
    return createSchemaFile(name, Joiner.on("\n").join(lines));
  }

  protected final FileRef createFileFromParts(String name, String header,
                                              String snippet, String footer)
      throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append(header);
    sb.append('\n');
    sb.append(snippet);
    sb.append('\n');
    sb.append(footer);

    return createFileNoHeader(name, sb.toString());
  }

  /**
   * Delete the gxp file with the given name
   */
  protected final boolean deleteFile(String name) {
    String pkgDir = getPackage().replace('.', '/');
    String sourceName = "/" + pkgDir + "/" + name + ".gxp";
    FileRef fileRef = fs.parseFilename(sourceName);

    return fileRef.delete();
  }

  /**
   * Returns the CodeGeneratorFactory to use when compiling.
   */
  protected CodeGeneratorFactory getCodeGeneratorFactory() {
    return new DefaultCodeGeneratorFactory();
  }

  /**
   * Returns a predicate which restricts the outputs generated. By default the
   * resulting predicate returns true if and only if the specifed fileRef's
   * base name is the same as one the basename of an element of gxpFiles.
   * Subclasses can override this to change the set of outputs generated.
   */
  protected Predicate<FileRef> getAllowedOutputPredicate(
      Iterable<FileRef> gxpFiles) {
    // construct predicate to only compile files for FileRefs passed directly
    // to compile and not FileRefs included from extraFiles()
    final Set<String> outFiles = Sets.newHashSet();
    for (FileRef gxpFile : gxpFiles) {
      outFiles.add(gxpFile.removeExtension().toFilename());
    }

    return new Predicate<FileRef>() {
      public boolean apply(FileRef fileRef) {
        return outFiles.contains(fileRef.removeExtension().toFilename());
      }
    };
  }

  /**
   * Returns the output languages to generate. Returns the complete set of
   * {@link OutputLanguage} values by default. Subclasses can override this
   * method to reduce the set of output languages generated.
   */
  protected Iterable<OutputLanguage> getOutputLanguages() {
    return Arrays.asList(OutputLanguage.values());
  }

  /**
   * Subclasses can override this function to have extra files made avaliable
   * during the compilation of whatever is being tested. This is useful when
   * writing error tests for a gxp library.
   */
  protected List<FileRef> extraFiles() {
    return Collections.emptyList();
  }

  /**
   * Compiles the passed in gxpFiles and save the generated alerts as
   * actualAlerts.
   */
  protected final CompilationSet compileFiles(FileRef... gxpFiles) {
    return compileFiles(Arrays.asList(gxpFiles));
  }

  protected CompilationManager getCompilationManager() {
    return SimpleCompilationManager.INSTANCE;
  }

  protected CompilationSet.Builder getCompilationSetBuilder() {
    return new CompilationSet.Builder(parser, getCodeGeneratorFactory(), getCompilationManager());
  }

  protected CompilationSet compileFiles(Collection<FileRef> gxpFiles) {
    // build list of files to compile
    List<FileRef> files = Lists.newArrayList();
    files.addAll(gxpFiles);
    files.addAll(extraFiles());

    // compile
    CompilationSet compilationSet = getCompilationSetBuilder().build(files);
    AlertSink alertSink = createAlertSink();
    compilationSet.compile(alertSink, getAlertPolicy(), getOutputLanguages(),
                           getAllowedOutputPredicate(gxpFiles));
    return compilationSet;
  }

  /**
   * Compiles the passed in schemaFiles and save the generated alerts as
   * actualAlerts.
   */
  protected void compileSchemas(FileRef... schemaFiles) {
    // build list of files to compile
    List<FileRef> files = Lists.newArrayList(schemaFiles);

    // compile schema files
    AlertSink alertSink = createAlertSink();
    new FileBackedSchemaFactory(alertSink, files);
  }

  protected final void compileNoHeader(String... lines) throws Exception {
    compileNoHeader(Joiner.on("\n").join(lines));
  }

  protected final void compileNoHeader(String contents) throws Exception {
    FileRef gxpFile = createFileNoHeader(getTemplateBaseName(), contents);
    compileFiles(gxpFile);
  }

  /**
   * Convenience method for compiling multiple lines.
   */
  protected final void compile(String... lines) throws Exception {
    compile(Joiner.on("\n").join(lines));
  }

  protected final void compileInterface(String... lines) throws Exception {
    compileInterface(Joiner.on("\n").join(lines));
  }

  /**
   * Compile a single (in memory) GXP file containing the specified snippet.
   * The template will have a gxp:template tag and the standard xmlns
   * declarations on the first line, and will end with the corresponding close
   * tag. There will also be an xmlns declaration, "my", for the call namespace
   * of the generated template.
   */
  protected final void compile(String snippet) throws Exception {
    FileRef gxpFile = createFile(getTemplateBaseName(), snippet);
    compileFiles(gxpFile);
  }

  protected final void compileInterface(String snippet) throws Exception {
    FileRef gxpFile = createInterfaceFile(getTemplateBaseName(), snippet);
    compileFiles(gxpFile);
  }

  //////////////////////////////////////////////////////////////////////
  // Overridable methods for dealing with Alerts.
  //////////////////////////////////////////////////////////////////////

  /**
   * Returns the AlertPolicy to use.
   */
  protected AlertPolicy getAlertPolicy() {
    return DefaultAlertPolicy.INSTANCE;
  }

  protected AlertSink createAlertSink() {
    return new ErroringAlertSink(getAlertPolicy());
  }

  //////////////////////////////////////////////////////////////////////
  // Functions for retrieving various parts of the name of compiled
  // templates.
  //////////////////////////////////////////////////////////////////////

  /**
   * Returns the fully qualified {@link TemplateName} that will be generated by
   * {@link #compile(String)}.
   */
  protected TemplateName.FullyQualified getTemplateName() {
    return new TemplateName.FullyQualified(
        getPackage(), getTemplateBaseName());
  }

  /**
   * Returns the package name of the template that will be generated by
   * {@link #compile(String)}.
   */
  protected String getPackage() {
    return getClass().getPackage().getName();
  }

  /**
   * Returns the base name of the template that will be generated by
   * {@link #compile(String)}.
   */
  protected String getTemplateBaseName() {
    String name = getName();
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  /**
   * Returns the tag name to make a new-style call to the template that will be
   * generated by {@link #compile(String)}.
   */
  protected String getMyTagName() {
    return "my:" + getTemplateBaseName();
  }
}
