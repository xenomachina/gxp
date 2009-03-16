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

package com.google.gxp.compiler.dynamictests;

import com.google.gxp.compiler.codegen.DefaultCodeGeneratorFactory;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.testing.dynamic.BaseRunningTestCase;

/**
 * Tests of dynamic compiation in which the contents of the gxp are actually
 * changed at runtime.
 *
 * TODO(harryh): add tests with > 1 gxp.
 */
public class DynamicTest extends BaseRunningTestCase {

  @Override
  protected DefaultCodeGeneratorFactory getCodeGeneratorFactory() {
    DefaultCodeGeneratorFactory codeGeneratorFactory = super.getCodeGeneratorFactory();
    codeGeneratorFactory.setDynamicModeEnabled(true);
    return codeGeneratorFactory;
  }

  public void testDynamicCompilation() throws Throwable {
    FileRef gxp = createFile("TestGxp", "hello, world!");
    compileAndLoad(gxp);

    // initial output using the statically compiled methods
    assertOutputEquals("hello, world!");
    assertCompilationCountEquals(0);

    // change to alternate valid output
    advanceClock();
    createFile("TestGxp", "goodbye, world!");
    assertOutputEquals("goodbye, world!");
    assertCompilationCountEquals(1);

    // make sure we don't recompile again when nothing has changed
    assertOutputEquals("goodbye, world!");
    assertCompilationCountEquals(1);

    // change to a file that will generate gxp compilation errors
    advanceClock();
    createFile("TestGxp", "<b>foo");
    assertGxpCompilationError();

    // still have bad gxp
    advanceClock();
    assertGxpCompilationError();

    // change to a file that will have java compilation errors
    advanceClock();
    createFile("TestGxp", "<gxp:eval expr='bad'/>");
    assertJavaCompilationError();

    // still have bad java
    advanceClock();
    assertJavaCompilationError();

    // back to where we started
    advanceClock();
    createFile("TestGxp", "hello, world!");
    assertOutputEquals("hello, world!");
  }

  public void testDynamicCompilationDefaults() throws Throwable {
    FileRef gxp =
        createFile("TestGxp2",
                   "<gxp:param name='message' type='String' default='\"hello world\"'/>");

    compileAndLoad(gxp, String.class);

    assertDefaultEquals("message", "hello world");
    assertCompilationCountEquals(0);

    advanceClock();
    createFile("TestGxp2",
               "<gxp:param name='message' type='String' default='\"goodbye world\"'/>");

    assertDefaultEquals("message", "goodbye world");
    assertCompilationCountEquals(1);

    assertDefaultEquals("message", "goodbye world");
    assertCompilationCountEquals(1);
  }

  public void testDynamicCompilationDeleteFile() throws Throwable {
    FileRef gxp = createFile("TestGxp3", "hello, world!");
    compileAndLoad(gxp);

    // initial output using the statically compiled methods
    assertOutputEquals("hello, world!");
    assertCompilationCountEquals(0);

    // delete file
    advanceClock();
    deleteFile("TestGxp3");
    assertOutputEquals("hello, world!");
    assertCompilationCountEquals(0);

    // put it back
    advanceClock();
    createFile("TestGxp3", "hello again!");
    assertOutputEquals("hello again!");
    assertCompilationCountEquals(1);
  }

  public void testDynamicCompilationParamChange() throws Throwable {
    FileRef gxp = createFile("TestGxp4",
                             "<gxp:param name='name' type='String' />",
                             "",
                             "Hello <expr:name />!");
    compileAndLoad(gxp, String.class);
    assertOutputEquals("Hello World!", "World");

    // change # of parameters
    createFile("TestGxp4",
               "<gxp:param name='name' type='String' />",
               "<gxp:param name='age'  type='Integer' />",
               "",
               "Hello <expr:name />!",
               "You are <expr:age/> years old.");
    assertGxpParamChangeError("World");

    // go back and make it work again
    createFile("TestGxp4",
               "<gxp:param name='name' type='String' />",
               "",
               "Hello <expr:name />!");
    assertOutputEquals("Hello Bob!", "Bob");

    // change parameter type
    createFile("TestGxp4",
               "<gxp:param name='age' type='Integer' />",
               "",
               "You are <expr:age /> years old.");
    assertGxpParamChangeError("Bob");

    // go back and make it work again
    createFile("TestGxp4",
               "<gxp:param name='name' type='String' />",
               "",
               "Hello <expr:name />!");
    assertOutputEquals("Hello Alice!", "Alice");
  }

  public void testPackagePrivate() throws Throwable {
    FileRef gxp = createFile("PackagePrivateGxp", "hello, world!");
    compileAndLoad(gxp);

    // initial output
    assertOutputEquals("hello, world!");

    // test access of package private data
    advanceClock();
    createFile("PackagePrivateGxp", "<gxp:eval expr='new PackagePrivateClosure()' />");
    assertOutputEquals("private data");
  }

  public void testStackTraceRewriting() throws Throwable {
    FileRef gxp = createFile("ThrowerGxp", "<gxp:throws exception='BarException' />");
    compileAndLoad(gxp);

    // do a runtime edit to make it throw an exception
    advanceClock();
    createFile("ThrowerGxp",
               "<gxp:throws exception='BarException' />",
               "<gxp:eval expr='BarException.throwOne()' />");
    try {
      assertOutputEquals("");
      fail("should throw BarException");
    } catch (BarException e) {
      StackTraceElement ste = e.getStackTrace()[1];
      assertEquals("ThrowerGxp.gxp", ste.getFileName());
      assertEquals(3, ste.getLineNumber());
      assertEquals("com.google.gxp.compiler.dynamictests.ThrowerGxp", ste.getClassName());
    }

    // move the exception to a different line #
    advanceClock();
    createFile("ThrowerGxp",
               "<gxp:throws exception='BarException' />",
               "",
               "<gxp:eval expr='BarException.throwOne()' />");
    try {
      assertOutputEquals("");
      fail("should throw BarException");
    } catch (BarException e) {
      StackTraceElement ste = e.getStackTrace()[1];
      assertEquals("ThrowerGxp.gxp", ste.getFileName());
      assertEquals(4, ste.getLineNumber());
      assertEquals("com.google.gxp.compiler.dynamictests.ThrowerGxp", ste.getClassName());
    }
  }
}
