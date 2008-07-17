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

package com.google.gxp.compiler.depend;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.CompilationManager;
import com.google.gxp.compiler.CompilationSet;
import com.google.gxp.compiler.SimpleCompilationManager;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.testing.BaseBuildingTestCase;

import java.util.Set;

/**
 * Tests of dependency checking. Ensures that output files are updated when they
 * should be, and that we don't needlessly rebuild things that don't need to be
 * rebuilt.
 *
 * <p>Each test has the following basic structure:
 *
 * <pre>
 *   // optionally restrict outputs
 *   restrictOutputs(...javafiles...); // optional
 *
 *   // add 0 or more source files
 *   addFile(gxpfile, sourcecode); // multiple
 *
 *   // build and advance the clock
 *   build();
 *   advanceClock();
 *
 *   // update 0 or more source files
 *   addFile(gxpfile, sourcecode); // multiple
 *
 *   // advance the clock and build again, and assert that what should be built
 *   // is exactly what actually was built.
 *   advanceClock();
 *   build();
 *   assertRebuilt(...javafiles...);
 * </pre>
 *
 * <p>Historical note: This is essentially a translation of
 * gxp/depend_regtest.py.
 */
public class DependencyCheckingTest extends BaseBuildingTestCase {
  private Set<FileRef> sourceFiles = Sets.newHashSet();
  private Predicate<FileRef> allowedOutputPredicate =
      Predicates.<FileRef>alwaysTrue();
  private final FileRef depsFile = fs.getRoot().join("/deps.gxd");
  DependencyGraph dependencyGraph;

  private FileRef addFile(String name, String... lines) throws Exception {
    FileRef result = createFile(name, lines);
    sourceFiles.add(result);
    return result;
  }

  @Override
  protected CompilationManager getCompilationManager() {
    return (dependencyGraph != null) ? dependencyGraph : SimpleCompilationManager.INSTANCE;
  }

  private void assertRebuilt(String... outputs) throws Exception {
    long now = fs.getCurrentTime();
    Set<FileRef> expectedRebuilt = Sets.newHashSet();
    // TODO(laurence): ensure that depsFile is always rebuilt.
    // eg: expectedRebuilt.add(depsFile);
    for (String output : outputs) {
      expectedRebuilt.add(toFileRef(output));
    }

    Set<FileRef> actualRebuilt = Sets.newHashSet();
    for (FileRef output : fs.getManifest()) {
      if (output.getLastModified() >= now) {
        actualRebuilt.add(output);
      }
    }

    assertEquals(expectedRebuilt, actualRebuilt);
  }

  private void build() throws Exception {
    CompilationSet cSet = compileFiles(sourceFiles);
    dependencyGraph = new DependencyGraph(cSet);
  }

  @Override
  protected Predicate<FileRef> getAllowedOutputPredicate(
      Iterable<FileRef> gxpFiles) {
    return allowedOutputPredicate;
  }

  @Override
  protected Iterable<OutputLanguage> getOutputLanguages() {
    return ImmutableList.of(OutputLanguage.JAVA);
  }

  FileRef toFileRef(String name) {
    if (!name.startsWith("/")) {
      String pkgDir = getPackage().replace('.', '/');
      name = "/" + pkgDir + "/" + name;
    }
    return fs.parseFilename(name);
  }

  private void restrictOutputs(String... outputs) {
    final Set<FileRef> allowed = Sets.newHashSet();
    for (String output : outputs) {
      allowed.add(toFileRef(output));
    }
    allowedOutputPredicate = new Predicate<FileRef>() {
      public boolean apply(FileRef fileRef) {
        return allowed.contains(fileRef);
      }
    };
  }

  public void testBaseCase() throws Exception {
    addFile("Main", "hello");

    build();
    advanceClock();

    addFile("Main", "goodbye");

    advanceClock();
    build();
    assertRebuilt("Main.java");
  }

  public void testAddAnotherDefParam() throws Exception {
    addFile("Common",
            "<gxp:param name='x' type='int'/>",
            "<gxp:param name='y' type='int'/>",
            "<gxp:param name='z' type='int' default='0'/>",
            "bar");
    addFile("Main", "foo <call:Common x='1' y='2'/>");

    build();
    advanceClock();

    addFile("Common",
            "<gxp:param name='x' type='int'/>",
            "<gxp:param name='y' type='int'/>",
            "<gxp:param name='z' type='int' default='0'/>",
            "<gxp:param name='t' type='int' default='0'/>",
            "baz");

    advanceClock();
    build();
    assertRebuilt("Common.java", "Main.java");
  }

  public void testAddDefParam() throws Exception {
    addFile("Main", "foo <call:Common x='1' y='2'/>");
    addFile("Common",
            "<gxp:param name='x' type='int'/>",
            "<gxp:param name='y' type='int'/>",
            "bar");

    build();
    advanceClock();

    addFile("Common",
            "<gxp:param name='x' type='int'/>",
            "<gxp:param name='y' type='int'/>",
            "<gxp:param name='z' type='int' default='0'/>",
            "baz");

    advanceClock();
    build();
    assertRebuilt("Common.java", "Main.java");
  }

  public void testChangedCaller() throws Exception {
    addFile("Common",
            "<gxp:param name='x' type='int'/>",
            "<gxp:param name='y' type='int'/>",
            "<gxp:param name='z' type='int' default='-1'/>",
            "bar");
    addFile("Main",
            "foo <call:Common x='4' y='5'/>",
            "bar <call:Common x='1' y='2' z='3'/>");

    build();
    advanceClock();

    addFile("Main",
            "baz <call:Common x='4' y='5'/>",
            "quux <call:Common x='1' y='2' z='3'/>");

    advanceClock();
    build();
    assertRebuilt("Main.java");
  }

  public void testEditCommon() throws Exception {
    addFile("Common",
            "bar");
    addFile("Main",
            "foo <call:Common/>");

    build();
    advanceClock();

    addFile("Common",
            "baz");

    advanceClock();
    build();

    assertRebuilt("Common.java");
  }

  public void testEditCommonParamDefault() throws Exception {
    addFile("Common",
            "<gxp:param name='x' type='int' default='1'/>",
            "bar");
    addFile("Main",
            "foo <call:Common/>");

    build();
    advanceClock();

    addFile("Common",
            "<gxp:param name='x' type='int' default='2'/>",
            "bar");

    advanceClock();
    build();

    assertRebuilt("Common.java");
  }

  public void testEditCommonInterface() throws Exception {
    addFile("Common",
            "<gxp:param name='y' type='int'/>",
            "<gxp:param name='x' type='int'/>",
            "bar");
    addFile("Main",
            "foo <call:Common x='1' y='2'/>");

    build();
    advanceClock();

    addFile("Common",
            "<gxp:param name='x' type='int'/>",
            "<gxp:param name='y' type='int'/>",
            "baz");

    advanceClock();
    build();
    assertRebuilt("Common.java", "Main.java");
  }

  public void testIndirectDependency() throws Exception {
    restrictOutputs("Main.java");
    addFile("Common",
            "<gxp:param name='x' type='int'/>",
            "<gxp:param name='y' type='int'/>",
            "<gxp:param name='z' type='int' default='-1'/>",
            "bar",
            "<call:Indirect a='x'/>");
    addFile("Main",
            "foo <call:Common x='4' y='5'/>",
            "bar <call:Common x='1' y='2' z='3'/>");

    build();
    advanceClock();

    addFile("Main",
            "baz <call:Common x='4' y='5'/>",
            "quux <call:Common x='1' y='2' z='3'/>");

    advanceClock();
    build();
    assertRebuilt("Main.java");
  }

  public void testNoChange() throws Exception {
    addFile("Main", "hello");

    build();
    advanceClock();

    // No changes...

    advanceClock();
    build();
    assertRebuilt();
  }

  public void testNoOutputCaller() throws Exception {
    restrictOutputs("Common.java");
    addFile("Main",
            "foo <call:Common x='4' y='5'/>",
            "bar <call:Common x='1' y='2' z='3'/>");
    addFile("Common",
            "<gxp:param name='x' type='int'/>",
            "<gxp:param name='y' type='int'/>",
            "<gxp:param name='z' type='int' default='-1'/>",
            "bar");

    build();
    advanceClock();

    addFile("Main",
            "baz <call:Common x='4' y='5'/>",
            "quux <call:Common x='1' y='2' z='3'/>");

    advanceClock();
    build();
    assertRebuilt();
  }

  public void testNoOutputCommon() throws Exception {
    restrictOutputs("Main.java");
    addFile("Main", "foo <call:Common x='1' y='2'/>");
    addFile("Common",
            "<gxp:param name='y' type='int'/>",
            "<gxp:param name='x' type='int'/>",
            "bar");

    build();
    advanceClock();

    addFile("Common",
            "<gxp:param name='x' type='int'/>",
            "<gxp:param name='y' type='int'/>",
            "baz");

    advanceClock();
    build();
    assertRebuilt("Main.java");
  }

  public void testNoOutputNoChange() throws Exception {
    restrictOutputs("Main.java");
    addFile("Common",
            "<gxp:param name='x' type='int'/>",
            "<gxp:param name='y' type='int'/>",
            "<gxp:param name='z' type='int' default='-1'/>",
            "bar");
    addFile("Main",
            "foo <call:Common x='4' y='5'/>",
            "bar <call:Common x='1' y='2' z='3'/>");

    build();
    advanceClock();

    // No changes...

    advanceClock();
    build();

    assertRebuilt();
  }

  public void testRearrangeDefParams() throws Exception {
    addFile("Main", "foo <call:Common x='1'/>");
    addFile("Common",
            "<gxp:param name='x' type='int'/>",
            "<gxp:param name='z' type='int' default='4'/>",
            "<gxp:param name='y' type='int' default='5'/>",
            "bar");

    build();
    advanceClock();

    addFile("Common",
            "<gxp:param name='x' type='int'/>",
            "<gxp:param name='y' type='int' default='2'/>",
            "<gxp:param name='z' type='int' default='3'/>",
            "baz");

    advanceClock();
    build();
    assertRebuilt("Common.java", "Main.java");
  }

  public void testRestrictedOutput() throws Exception {
    restrictOutputs("Main.java");
    addFile("Main", "hello");

    build();
    advanceClock();

    addFile("Main", "goodbye");

    advanceClock();
    build();
    assertRebuilt("Main.java");
  }
}
