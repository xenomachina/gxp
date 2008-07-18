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
import com.google.common.io.Bytes;

import junit.framework.TestCase;

import java.util.*;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.Writer;

import com.google.gxp.testing.MoreAsserts;

/**
 * Tests of {@link InMemoryFileSystem}.
 */
public class InMemoryFileSystemTest extends TestCase {

  private final InMemoryFileSystem fs = new InMemoryFileSystem();

  public void testGetRoot() throws Exception {
    assertEquals("/", fs.getRoot().getName());
  }

  public void testGetLastModified() throws Exception {
    // nonexistant file
    FileRef file = fs.parseFilename("/foo/bar/baz.txt");
    assertEquals(0, file.getLastModified());

    // create file without setting time
    file.openOutputStream().close();
    assertEquals(0, file.getLastModified());

    // create file after setting time to a positive value
    fs.setCurrentTime(12345);
    file.openOutputStream().close();
    assertEquals(12345, file.getLastModified());

    // create file after setting time to a non-positive value
    fs.setCurrentTime(0);
    file.openOutputStream().close();
    assertEquals(0, file.getLastModified());
  }

  public void testOpenInputStream_nonexistant() throws Exception {
    FileRef hello = fs.parseFilename("/foo/bar/baz");
    try {
      hello.openInputStream();
      fail("Was able to open nonexistant file.");
    } catch (FileNotFoundException fnfx) {
      // yay!
    }
  }

  public void testOpenInputStreamAndOpenWriter() throws Exception {
    FileRef hello = fs.parseFilename("/foo/bar/baz");
    FileRef goodbye = fs.parseFilename("/snarf/quux");

    // Write a couple of files, and make sure we can read them back.
    // Two files used to ensure the file system keeps them separate.
    String helloText = "hello, world!";
    String goodbyeText = "goodbye, world!";

    Writer out = hello.openWriter(Charsets.US_ASCII);
    out.write(helloText);
    out.close();
    out = goodbye.openWriter(Charsets.US_ASCII);
    out.write(goodbyeText);
    out.close();

    // Re-create filenames, just to make sure we aren't relying on object
    // identity.
    hello = fs.parseFilename("/foo/bar/baz");
    goodbye = fs.parseFilename("/snarf/quux");

    MoreAsserts.assertEquals(helloText.getBytes(),
                             Bytes.toByteArray(hello.openInputStream()));
    MoreAsserts.assertEquals(goodbyeText.getBytes(),
                             Bytes.toByteArray(goodbye.openInputStream()));
  }

  public void testOpenWriter_overwriteExistingFile() throws Exception {
    FileRef fnam = fs.parseFilename("/foo/bar/baz");

    String oldText = "that was then";
    Writer out = fnam.openWriter(Charsets.US_ASCII);
    out.write(oldText);
    out.close();
    MoreAsserts.assertEquals(oldText.getBytes(),
                             Bytes.toByteArray(fnam.openInputStream()));

    String newText = "this is now";
    out = fnam.openWriter(Charsets.US_ASCII);
    out.write(newText);
    out.close();
    MoreAsserts.assertEquals(newText.getBytes(),
                             Bytes.toByteArray(fnam.openInputStream()));
  }

  public void testToFilename() throws Exception {
    assertEquals("/foo/bar/baz",
                 fs.parseFilename("foo/bar/baz").toFilename());
  }

  public void testParseFilename() throws Exception {
    assertEquals(fs.parseFilename("/foo/bar/baz"),
                 fs.parseFilename("foo/bar/baz"));
  }

  public void assertFilenameCorrect(FileSystem fs, String s)
      throws Exception {
    // check both directions:
    assertEquals(fs.parseFilename(s), fs.parseFilename(s));
    assertEquals(fs.parseFilename(s).getName(),
                 fs.parseFilename(s).toFilename());
  }

  public void testFilenamesSameAsFilenameValues() throws Exception {
    assertFilenameCorrect(fs, "foo/bar/baz");
    assertFilenameCorrect(fs, "/foo/bar/baz");
    assertFilenameCorrect(fs, "/foo/bar/baz/");
    assertFilenameCorrect(fs, "foo/bar/baz/");
    assertFilenameCorrect(fs, "foo//bar/baz");
    assertFilenameCorrect(fs, "/foo/bar//baz");
    assertFilenameCorrect(fs, "/foo/bar//baz/");
    assertFilenameCorrect(fs, "foo//bar/baz/");
    assertFilenameCorrect(fs, "///foo////bar/////baz//////");
  }

  public void testParseFilenameList() throws Exception {
    MoreAsserts.assertContentsInOrder(
        fs.parseFilenameList("foo/bar/baz:zork/zelda/zaxxon"),
        fs.parseFilename("foo/bar/baz"),
        fs.parseFilename("zork/zelda/zaxxon"));
  }

  public void testGetManifest() throws Exception {
    // 0 files
    assertTrue(fs.getManifest().isEmpty());

    // 1 file
    fs.parseFilename("/foo/bar").openWriter(Charsets.US_ASCII).close();
    MoreAsserts.assertContentsAnyOrder(fs.getManifest(),
                                       fs.parseFilename("/foo/bar"));

    // 2 files
    fs.parseFilename("/foo/baz").openWriter(Charsets.US_ASCII).close();
    MoreAsserts.assertContentsAnyOrder(fs.getManifest(),
                                       fs.parseFilename("/foo/bar"),
                                       fs.parseFilename("/foo/baz"));

    // overwrote existing file
    fs.parseFilename("/foo/baz").openWriter(Charsets.US_ASCII).close();
    MoreAsserts.assertContentsAnyOrder(fs.getManifest(),
                                       fs.parseFilename("/foo/bar"),
                                       fs.parseFilename("/foo/baz"));
  }

  public void testDelete() throws Exception {
    FileRef file = fs.parseFilename("/foo");

    // create file
    file.openWriter(Charsets.US_ASCII).close();

    // test read
    Reader r = file.openReader(Charsets.US_ASCII);
    r.close();

    // delete file
    assertTrue(file.delete());

    // verify it doesn't exit anymore
    try {
      r = file.openReader(Charsets.US_ASCII);
      fail("file should not exist anymore.");
    } catch (FileNotFoundException e) {
      // success!
    }
  }
}
