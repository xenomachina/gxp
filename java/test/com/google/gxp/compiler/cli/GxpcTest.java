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

package com.google.gxp.compiler.cli;

import com.google.common.base.Charsets;
import com.google.gxp.compiler.fs.InMemoryFileSystem;

import java.io.*;
import junit.framework.TestCase;

import static com.google.gxp.testing.MoreAsserts.*;

/**
 * Tests for {@code Gxpc}.
 */
public class GxpcTest extends TestCase {
  private final InMemoryFileSystem fs = new InMemoryFileSystem();
  private final StringBuilder actualStderr = new StringBuilder();
  private final StringBuilder expectedStderr = new StringBuilder();

  private static final String NAMESPACE_DECLS =
      " xmlns='http://www.w3.org/1999/xhtml'"
      + " xmlns:gxp='http://google.com/2001/gxp'"
      + " xmlns:call='http://google.com/2001/gxp/templates'"
      + " xmlns:expr='http://google.com/2001/gxp/expressions'";

  // Note that tests should append to expectedStderr if they expect anything to
  // be written to stderr. By default, expectedStderr is empty.
  @Override
  public void tearDown() {
    assertEquals(expectedStderr.toString(), actualStderr.toString());
  }

  public void testBase() throws Exception {
    assertEquals(1, Gxpc.main(fs, actualStderr, fs.getRoot()));
    expectedStderr.append("gxpc: no input files\n");
  }

  public void testBadFlag() throws Exception {
    assertEquals(1, Gxpc.main(fs, actualStderr, fs.getRoot(),
                              "--nonexistantflag"));
    expectedStderr.append("\"--nonexistantflag\" is not a valid option\n");
  }

  public void testOneFile() throws Exception {
    writeFile("/foo/Bar.gxp",
              "<gxp:template " + NAMESPACE_DECLS + " name='foo.Bar'/>");
    assertEquals(0, Gxpc.main(fs, actualStderr, fs.getRoot(),
                              "--output_language", "java",
                              "/foo/Bar.gxp"));
    assertContentsAnyOrder(fs.getManifest(),
                           fs.parseFilename("/foo/Bar.gxp"),
                           fs.parseFilename("/foo/Bar.java"));
  }

  public void testOneFileWithErrors() throws Exception {
    writeFile("/foo/Bar.gxp",
              "<gxp:template " + NAMESPACE_DECLS
              + " name='foo.Bar'"
              + " badattr='foo'/>");
    assertEquals(1, Gxpc.main(fs, actualStderr, fs.getRoot(),
                              "--output_language", "java",
                              "/foo/Bar.gxp"));
    assertContentsAnyOrder(fs.getManifest(),
                           fs.parseFilename("/foo/Bar.gxp"));
    expectedStderr.append(
        "/foo/Bar.gxp:1:224:1:224: 'badattr' attribute is unknown"
        + " in <gxp:template>.\n");
  }

  public void testTwoFiles() throws Exception {
    writeFile("/foo/Bar.gxp",
              "<gxp:template " + NAMESPACE_DECLS + " name='foo.Bar'/>");
    writeFile("/foo/Baz.gxp",
              "<gxp:template " + NAMESPACE_DECLS + " name='foo.Baz'/>");
    assertEquals(0, Gxpc.main(fs, actualStderr, fs.getRoot(),
                              "--output_language", "java",
                              "/foo/Bar.gxp", "/foo/Baz.gxp"));
    assertContentsAnyOrder(fs.getManifest(),
                           fs.parseFilename("/foo/Bar.gxp"),
                           fs.parseFilename("/foo/Bar.java"),
                           fs.parseFilename("/foo/Baz.gxp"),
                           fs.parseFilename("/foo/Baz.java"));
  }

  public void testDepend() throws Exception {
    writeFile("/foo/Bar.gxp",
              "<gxp:template " + NAMESPACE_DECLS + " name='foo.Bar'/>");
    assertEquals(0, Gxpc.main(fs, actualStderr, fs.getRoot(),
                              "--depend", "/quux/zarf.gxd",
                              "--output_language", "java",
                              "/foo/Bar.gxp"));
    assertContentsAnyOrder(fs.getManifest(),
                           fs.parseFilename("/quux/zarf.gxd"),
                           fs.parseFilename("/foo/Bar.gxp"),
                           fs.parseFilename("/foo/Bar.java"));
  }

  public void testDependIndependentOfOutputFlag() throws Exception {
    writeFile("/foo/Bar.gxp",
              "<gxp:template " + NAMESPACE_DECLS + " name='foo.Bar'/>");
    writeFile("/foo/Baz.gxp",
              "<gxp:template " + NAMESPACE_DECLS + " name='foo.Baz'/>");
    assertEquals(0, Gxpc.main(fs, actualStderr, fs.getRoot(),
                              "--depend", "/quux/zarf.gxd",
                              "--output_language", "java",
                              "--output", "/foo/Baz.java",
                              "/foo/Bar.gxp", "/foo/Baz.gxp"));
    assertContentsAnyOrder(fs.getManifest(),
                           fs.parseFilename("/quux/zarf.gxd"),
                           fs.parseFilename("/foo/Bar.gxp"),
                           fs.parseFilename("/foo/Baz.gxp"),
                           fs.parseFilename("/foo/Baz.java"));
  }

  public void testTwoFilesButOnlyOneAllowedOut() throws Exception {
    writeFile("/foo/Bar.gxp",
              "<gxp:template " + NAMESPACE_DECLS + " name='foo.Bar'/>");
    writeFile("/foo/Baz.gxp",
              "<gxp:template " + NAMESPACE_DECLS + " name='foo.Baz'/>");
    assertEquals(0, Gxpc.main(fs, actualStderr, fs.getRoot(),
                              "--output_language", "java",
                              "--output", "/foo/Baz.java",
                              "/foo/Bar.gxp", "/foo/Baz.gxp"));
    assertContentsAnyOrder(fs.getManifest(),
                           fs.parseFilename("/foo/Bar.gxp"),
                           fs.parseFilename("/foo/Baz.gxp"),
                           fs.parseFilename("/foo/Baz.java"));
  }

  public void testInvalidAllowedOuts() throws Exception {
    writeFile("/foo/Bar.gxp",
              "<gxp:template " + NAMESPACE_DECLS + " name='foo.Bar'/>");
    assertEquals(1, Gxpc.main(fs, actualStderr, fs.getRoot(),
                              "--output_language", "java",
                              "--output", "/foo/Baz.java",
                              "--output", "/foo/Quux.java",
                              "/foo/Bar.gxp"));
    expectedStderr.append(
        "The following are listed as allowed output files but are not possible"
        + " given the specified inputs: /foo/Baz.java, /foo/Quux.java\n");
    assertContentsAnyOrder(fs.getManifest(),
                           fs.parseFilename("/foo/Bar.gxp"));
  }

  public void testTwoFilesButOnlyOneAllowedOutWithXmbEnabled()
      throws Exception {
    writeFile("/foo/Bar.gxp",
              "<gxp:template " + NAMESPACE_DECLS + " name='foo.Bar'/>");
    writeFile("/foo/Baz.gxp",
              "<gxp:template " + NAMESPACE_DECLS + " name='foo.Baz'/>");
    assertEquals(0, Gxpc.main(fs, actualStderr, fs.getRoot(),
                              "--output_language", "java",
                              "--output_language", "xmb",
                              "--output", "/foo/Baz.java",
                              "--output", "/foo/Baz.xmb",
                              "/foo/Bar.gxp", "/foo/Baz.gxp"));
    assertContentsAnyOrder(fs.getManifest(),
                           fs.parseFilename("/foo/Bar.gxp"),
                           fs.parseFilename("/foo/Baz.gxp"),
                           fs.parseFilename("/foo/Baz.java"),
                           fs.parseFilename("/foo/Baz.xmb"));
  }

  public void testDotOutputBasic() throws Exception {
    writeFile("/foo/Bar.gxp",
              "<gxp:template " + NAMESPACE_DECLS + " name='foo.Bar'/>");
    assertEquals(0, Gxpc.main(fs, actualStderr, fs.getRoot(),
                              "--dot", "reparented",
                              "--dot", "if-expanded",
                              "--output_language", "java",
                              "/foo/Bar.gxp"));

    assertContentsAnyOrder(fs.getManifest(),
                           fs.parseFilename("/foo/Bar.gxp"),
                           fs.parseFilename("/foo/Bar.java"),
                           fs.parseFilename("/foo/Bar.03.reparented.dot"),
                           fs.parseFilename("/foo/Bar.02.if-expanded.dot"));
  }


  private void writeFile(String fnam, String content) throws IOException {
    Writer out = fs.parseFilename(fnam).openWriter(Charsets.US_ASCII);
    out.write(content);
    out.close();
  }
}
