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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gxp.testing.MoreAsserts;

import junit.framework.TestCase;

import java.util.*;
import java.io.*;

import org.easymock.EasyMock;

/**
 * Tests of {@link SourcePathFileSystem}.
 */
public class SourcePathFileSystemTest extends TestCase {

  private static final String SRCDIR1 = "/home/laurence/src/java";
  private static final String SRCDIR2 = "/home/laurence/src/javatests";
  private static final String OUTDIR = "/home/laurence/src/bin/java";

  private static final String SRCSUFFIX1 = "/com/google/foo/Bar.gxp";
  private static final String SRCSUFFIX2 = "/com/google/foo/Baz.gxp";
  private static final String SRCSUFFIX3 = "/com/google/foo/Quux.gxp";

  private static final String OUTSUFFIX1 = "/com/google/foo/Bar.java";
  private static final String OUTSUFFIX2 = "/com/google/foo/Baz.xmb";

  // TODO: use InMemoryFileSystem instead of EasyMock
  private FileSystem mockFs = EasyMock.createMock(FileSystem.class);
  private FileStore mockStore = EasyMock.createMock(FileStore.class);

  private final FileRef SRCFILE1 =
      new FileRef(mockStore, SRCDIR1 + SRCSUFFIX1);
  private final FileRef SRCFILE2 =
      new FileRef(mockStore, SRCDIR2 + SRCSUFFIX2);
  private final FileRef SRCFILE3 =
      new FileRef(mockStore, SRCDIR2 + SRCSUFFIX3);

  private final FileRef OUTFILE1 =
      new FileRef(mockStore, OUTDIR + OUTSUFFIX1);
  private final FileRef OUTFILE2 =
      new FileRef(mockStore, OUTDIR + OUTSUFFIX2);

  private final FileRef BOGOFILE =
      new FileRef(mockStore, "/where/the/heck/am/i");

  private SourcePathFileSystem fs = createSourcePathFileSystem();
  private FileRef root = fs.getRoot();

  private final FileRef ABSTRACT_SRCFILE1 =
      root.join(SRCSUFFIX1);
  private final FileRef ABSTRACT_SRCFILE2 =
      root.join(SRCSUFFIX2);
  private final FileRef ABSTRACT_SRCFILE3 =
      root.join(SRCSUFFIX3);

  private final FileRef ABSTRACT_OUTFILE1 =
      root.join(OUTSUFFIX1);
  private final FileRef ABSTRACT_OUTFILE2 =
      root.join(OUTSUFFIX2);

  private SourcePathFileSystem createSourcePathFileSystem() {
    List<FileRef> sourcePath = ImmutableList.of(
          new FileRef(mockStore, SRCDIR1),
          new FileRef(mockStore, SRCDIR2)
        );
    List<FileRef> sourceFiles = ImmutableList.of(
          SRCFILE1,
          SRCFILE2,
          SRCFILE3
        );
    FileRef outDir = new FileRef(mockStore, OUTDIR);

    return new SourcePathFileSystem(mockFs, sourcePath, sourceFiles, outDir);
  }

  public void testGetRoot() throws Exception {
    replay();
    assertEquals("/", fs.getRoot().getName());
  }

  public void testGetLastModified() throws Exception {
    EasyMock.expect(mockStore.getLastModified(SRCFILE1))
        .andReturn(4815162342L);
    replay();

    assertEquals(4815162342L, ABSTRACT_SRCFILE1.getLastModified());
  }

  public void testGetLastModified_returnZero() throws Exception {
    EasyMock.expect(mockStore.getLastModified(SRCFILE1))
        .andReturn(0L);
    replay();

    assertEquals(0L, ABSTRACT_SRCFILE1.getLastModified());
  }

  public void testInvalidSourcefile() throws Exception {
    List<FileRef> sourcePath = ImmutableList.of(new FileRef(mockStore, SRCDIR1));
    List<FileRef> sourceFiles = ImmutableList.of(
          SRCFILE1,
          SRCFILE2,
          SRCFILE3
        );
    FileRef outDir = new FileRef(mockStore, OUTDIR);
    EasyMock.expect(mockStore.toFilename(SRCFILE2))
        .andReturn("[mock result 2]");
    replay();

    try {
      new SourcePathFileSystem(mockFs, sourcePath, sourceFiles, outDir);
      fail("Should not be able to create SourcePathFileSystem with"
           + " source files outside of source path.");
    } catch (IllegalArgumentException exc) {
      // yay!
    }
  }

  public void testToFilename_sourcefile() throws Exception {
    EasyMock.expect(mockStore.toFilename(SRCFILE1))
        .andReturn("[mock result]");
    replay();

    String actual = ABSTRACT_SRCFILE1.toFilename();
    assertEquals("[mock result]", actual);
  }

  public void testToFilename_outfile() throws Exception {
    EasyMock.expect(mockStore.toFilename(OUTFILE1))
        .andReturn("[mock result]");
    replay();

    String actual = ABSTRACT_OUTFILE1.toFilename();
    assertEquals("[mock result]", actual);
  }

  public void testOpenInputStream() throws Exception {
    InputStream expected =
        new ByteArrayInputStream("barbarbar".getBytes("US-ASCII"));
    EasyMock.expect(mockStore.openInputStream(SRCFILE1)).andReturn(expected);
    replay();

    InputStream actual = ABSTRACT_SRCFILE1.openInputStream();
    assertEquals(expected, actual);
  }

  public void testOpenWriter() throws Exception {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    EasyMock.expect(mockStore.openOutputStream(OUTFILE1))
        .andReturn(outStream);
    replay();

    Writer actual = ABSTRACT_OUTFILE1.openWriter(Charsets.ISO_8859_1);
    actual.write("hello");
    actual.flush();
    MoreAsserts.assertEquals("hello".getBytes("ISO-8859-1"),
                             outStream.toByteArray());
  }

  public void testOpenWriter_sourceFile() throws Exception {
    EasyMock.expect(mockStore.toFilename(SRCFILE1))
        .andReturn("[mock result]");
    replay();

    try {
      ABSTRACT_SRCFILE1.openWriter(Charsets.ISO_8859_1);
      fail("Should not be able to open source file for writing.");
    } catch (IOException iox) {
      // yay!
    }
  }

  public void testParseFilename() throws Exception {
    EasyMock.expect(mockFs.parseFilename("[system filename]"))
        .andReturn(SRCFILE1);
    replay();

    FileRef actual = fs.parseFilename("[system filename]");
    assertEquals(ABSTRACT_SRCFILE1, actual);
  }

  public void testParseFilename_invalid() throws Exception {
    EasyMock.expect(mockFs.parseFilename("[system filename]"))
        .andReturn(BOGOFILE);
    replay();

    try {
      fs.parseFilename("[system filename]");
      fail("Should not be able to parse system filename that falls outside of"
           + " source file list or destination directory.");
    } catch (IllegalArgumentException exc) {
      // bogus filename detected
    }
  }

  public void testParseFilenameList() throws Exception {
    EasyMock.expect(mockFs.parseFilenameList("[some list]"))
        .andReturn(Lists.newArrayList(SRCFILE1,
                                      SRCFILE2,
                                      SRCFILE3,
                                      OUTFILE1,
                                      OUTFILE2));
    replay();

    MoreAsserts.assertContentsInOrder(
        fs.parseFilenameList("[some list]"),
        ABSTRACT_SRCFILE1,
        ABSTRACT_SRCFILE2,
        ABSTRACT_SRCFILE3,
        ABSTRACT_OUTFILE1,
        ABSTRACT_OUTFILE2);
  }

  public void testGetSourceFilenames() throws Exception {
    replay();

    MoreAsserts.assertContentsAnyOrder(fs.getSourceFileRefs(),
                                       ABSTRACT_SRCFILE1,
                                       ABSTRACT_SRCFILE2,
                                       ABSTRACT_SRCFILE3);
  }

  /**
   * Regression: Make sure getSourceFilenames works with "/" in source path.
   */
  public void testGetSourceFilenamesWithRootInSourcePath() throws Exception {
    replay();
    fs = new SourcePathFileSystem(
        mockFs,
        ImmutableList.of(new FileRef(mockStore, "/")),
        ImmutableList.of(new FileRef(mockStore, "/foo/Bar.gxp")),
        new FileRef(mockStore, "/"));
    root = fs.getRoot();
    MoreAsserts.assertContentsAnyOrder(fs.getSourceFileRefs(),
                                       root.join("/foo/Bar.gxp"));
  }

  private void replay() {
    EasyMock.replay(mockFs);
    EasyMock.replay(mockStore);
  }

  @Override
  public void tearDown() throws Exception {
    EasyMock.verify(mockFs);
    EasyMock.verify(mockStore);
  }
}
