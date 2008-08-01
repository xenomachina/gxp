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

package com.google.gxp.compiler.fs;

import com.google.common.base.Charsets;
import com.google.common.base.Join;
import com.google.common.io.Characters;
import com.google.gxp.testing.MoreAsserts;

import junit.framework.TestCase;

import java.io.*;
import java.nio.charset.UnmappableCharacterException;

/**
 * Tests of {@link SystemFileSystem}.
 */
public class SystemFileSystemTest extends TestCase {
  private final SystemFileSystem fs = SystemFileSystem.INSTANCE;

  /**
   * Constructs a native pathname consisting of the specified components. Pass
   * "" as the first parameter for the result to start with the separator or as
   * the last parameter to have the result end with the separator.
   */
  private static String path(String first, String... rest) {
    return Join.join(File.separator, first, (Object[]) rest);
  }

  /**
   * Constructs a native absolute pathname consisting of the specified
   * components.
   */
  private static String absPath(String first, String... rest) {
    return File.separator + path(first, rest);
  }

  public void testGetRoot() throws Exception {
    assertEquals("/", fs.getRoot().getName());
  }

  public String getTmpDir() {
    String tmp = System.getProperty("java.io.tmpdir");
    return tmp.endsWith(File.separator) ? tmp.substring(0, tmp.length() - 1) : tmp;
  }

  public String getJavaTestsDir() {
    return path("java", "test", "");
  }

  public void testOpenInputStream() throws Exception {
    String systemFnam =
        getJavaTestsDir()
        + path("com", "google", "gxp", "compiler", "fs", "")
        + "SystemFileSystemTest-testOpenInputStream-input.txt";
    FileRef fnam = fs.parseFilename(systemFnam);

    Reader reader = new InputStreamReader(fnam.openInputStream(),
                                          "ISO-8859-1");
    String result = Characters.toString(reader);
    reader.close();

    assertEquals("Lorem ipsum dolor sit amet\n", result);
  }

  public void testOpenWriter() throws Exception {
    assertOpenWriterWorks("output1.txt",
                          "hello world");
    assertOpenWriterWorks("dir1" + File.separator
                          + "dir2" + File.separator
                          + "dir3" + File.separator
                          + "output2.txt",
                          "Lorem ipsum dolor sit amet");
  }

  public void testGetLastModified() throws Exception {
    assertEquals(0, fileRef("idontexist").getLastModified());

    // Since we're actually testing interaction with the real filesystem, the
    // best we can do is approximate when it comes to timestamps.

    // current time "floor-ed" to second
    long lowerBound = System.currentTimeMillis();
    lowerBound -= lowerBound % 1000L;

    fileRef("iexistnow").openOutputStream().close();
    long actual = fileRef("iexistnow").getLastModified();

    // current time "ceiling-ed" to second
    long upperBound = System.currentTimeMillis() + 999L;
    upperBound -= upperBound % 1000L;

    assertTrue("Timestamp too early: " + actual + " < " + lowerBound,
               actual >= lowerBound);
    assertTrue("Timestamp too late: " + actual + " > " + upperBound,
               actual <= upperBound);
  }

  private FileRef fileRef(String relPath) throws Exception {
    String qualifiedSystemFnam =
        getTmpDir() + File.separator + relPath;
    FileRef result = fs.parseFilename(qualifiedSystemFnam);
    assertEquals(qualifiedSystemFnam, result.toFilename());
    return result;
  }

  public void assertOpenWriterWorks(String systemFnam, String content)
      throws Exception {
    FileRef fnam = fileRef(systemFnam);
    Writer writer = fnam.openWriter(Charsets.ISO_8859_1);
    writer.write(content);
    writer.close();

    Reader reader =
        new InputStreamReader(new FileInputStream(fnam.toFilename()));
    String result = Characters.toString(reader);
    reader.close();

    assertEquals(content, result);
  }

  public void testOpenWriter_encodingFailure() throws Exception {
    String systemFnam = getTmpDir() + File.separator + "ascii.txt";
    FileRef fnam = fs.parseFilename(systemFnam);
    Writer writer = fnam.openWriter(Charsets.US_ASCII);
    try {
      writer.write(
          "\u0125\u00e9\u0142\u013e\u014d \u0175\u00f6\u0159\u013a\u0111");
      fail("Wrote non-ASCII characters to US-ASCII Writer!");
    } catch (UnmappableCharacterException ucx) {
      // yay!
    } finally {
      writer.close();
    }
  }

  public void testParseFilename() throws Exception {
    assertEquals(new File("foobar").getAbsolutePath(),
                 fs.parseFilename("foobar").toFilename());
    try {
      fs.parseFilename(null);
      fail("null system filename should not be parseable");
    } catch (NullPointerException npe) {
      // yay!
    }
  }

  public void testToFilename() throws Exception {
    assertEquals(absPath("foobar"), fs.getRoot().join("foobar").toFilename());

    // tests of proper % escaping
    assertEquals(absPath("foo%bar"), fs.getRoot().join("foo%bar").toFilename());
    assertEquals(absPath("foo%%bar"), fs.getRoot().join("foo%%bar").toFilename());
  }

  public void testToRelativeFilename() throws Exception {
    String oldUserDir = System.getProperty("user.dir");
    try {
      System.setProperty("user.dir", new File(absPath("x", "y")).getAbsolutePath());

      assertEquals(new File(absPath("z")).getAbsolutePath(),
                   fs.parseFilename(absPath("z")).toRelativeFilename());
      assertEquals("a", fs.parseFilename(absPath("x", "y", "a")).toRelativeFilename());
      assertEquals(path("a", "b"),
                   fs.parseFilename(absPath("x", "y", "a", "b")).toRelativeFilename());
      assertEquals(path("a", "b"), fs.parseFilename(path("a", "b")).toRelativeFilename());
    } finally {
      System.setProperty("user.dir", oldUserDir);
    }
  }

  public void testParseFilenameList() throws Exception {
    MoreAsserts.assertContentsInOrder(
        fs.parseFilenameList("foo" + File.pathSeparator + "bar"
                             + File.pathSeparator + "baz"),
        fs.parseFilename("foo"),
        fs.parseFilename("bar"),
        fs.parseFilename("baz"));
  }
}
