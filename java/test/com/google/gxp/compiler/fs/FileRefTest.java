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

import junit.framework.TestCase;

/**
 * Tests of {@link FileRef}.
 */
public class FileRefTest extends TestCase {
  // TODO(laurence): should this use a mock FileSystem instead?
  private static final FileSystem fs = new InMemoryFileSystem();

  public void testGetName() throws Exception {
    assertEquals("/foo/bar/baz", fn("foo/bar/baz").getName());
    assertEquals("/foo/bar/baz", fn("/foo/bar/baz").getName());
    assertEquals("/foo/bar/baz", fn("/foo/bar/baz/").getName());
    assertEquals("/foo/bar/baz", fn("foo/bar/baz/").getName());
    assertEquals("/foo/bar/baz", fn("foo//bar/baz").getName());
    assertEquals("/foo/bar/baz", fn("/foo/bar//baz").getName());
    assertEquals("/foo/bar/baz", fn("/foo/bar//baz/").getName());
    assertEquals("/foo/bar/baz", fn("foo//bar/baz/").getName());
    assertEquals("/foo/bar/baz", fn("///foo////bar/////baz//////").getName());
  }

  public void testRemoveExtension() throws Exception {
    // no extension to remove
    assertEquals(fn("foo/bar/baz"),
                 fn("foo/bar/baz").removeExtension());

    // basic extension
    assertEquals(fn("foo/bar/baz"),
                 fn("foo/bar/baz.java").removeExtension());

    // double extension
    assertEquals(fn("foo/bar/baz.java"),
                 fn("foo/bar/baz.java.bak").removeExtension());

    // dot not in last component
    assertEquals(fn("/foo/bar.baz/quux"),
                 fn("/foo/bar.baz/quux.java").removeExtension());
    assertEquals(fn("/foo/bar.baz/quux"),
                 fn("/foo/bar.baz/quux").removeExtension());
  }

  public void testAddSuffix() throws Exception {
    assertEquals(fn("foo/bar/baz.java"),
                 fn("foo/bar/baz").addSuffix(".java"));

    assertEquals(fn("foo/bar/baz.java.bak"),
                 fn("foo/bar/baz.java").addSuffix(".bak"));

    // null suffix disallowed
    try {
      fn("/foo/bar/baz.txt").addSuffix(null);
      fail("Should not be able to apply null suffix.");
    } catch (NullPointerException npe) {
      // yay!
    }

    // slashes disallowed in suffixes
    try {
      fn("/foo/bar/baz.txt").addSuffix("x/y");
      fail("Should not be able to apply suffix that contains '/'");
    } catch (IllegalArgumentException iax) {
      // yay!
    }
  }

  public void testToString() throws Exception {
    FileRef fnam = fn("/foo/bar/baz.java");
    // don't want to be tempted to use toString() to get name.
    assertFalse(fnam.toString().equals(fnam.getName()));
    assertTrue(fnam.toString().contains(fnam.getName()));
  }

  public void testHashCodeAndEquals() throws Exception {
    Object f1 = fn("/foo/bar/baz.java");
    Object f2 = fn("/foo/bar/baz.java");
    Object g1 = fn("/foo/bar/quux.java");
    Object g2 = fn("/foo/bar/quux.java");

    // we actually don't care if these tests fail so much, but if they do it
    // isn't clear that the other tests actually mean anything
    assertNotSame(f1, f2);
    assertNotSame(g1, g2);

    // equals()
    assertEquals(f1, f1);
    assertEquals(f1, f2);
    assertEquals(g2, g2);
    assertEquals(g1, g2);
    assertFalse(f1.equals("/foo/bar/baz.java"));
    assertFalse(f1.equals(g2));
    assertFalse(g1.equals(f2));
    assertFalse(g1.equals("/foo/bar/quux.java"));

    // hashCode()
    assertEquals(f1.hashCode(), f2.hashCode());
    assertEquals(g1.hashCode(), g2.hashCode());

    // not strictly required, but desirable...
    assertTrue(f1.hashCode() != g2.hashCode());
    assertTrue(g1.hashCode() != f2.hashCode());
  }

  public void testIsAncestorOf() throws Exception {
    assertTrue(fn("/foo/bar").isAncestorOf(fn("/foo/bar/baz")));
    assertTrue(fn("/foo/bar").isAncestorOf(fn("/foo/bar/baz/quux")));
    assertTrue(fn("/").isAncestorOf(fn("/foo/bar/baz/quux")));
    assertTrue(fn("/").isAncestorOf(fn("")));

    assertFalse(fn("/foo/bar").isAncestorOf(fn("/foo/barbaz/quux")));
    assertFalse(fn("/foo/bar").isAncestorOf(fn("/foo/bar")));
    assertFalse(fn("/foo/bar").isAncestorOf(fn("/foo")));
    assertFalse(fn("/bar/baz").isAncestorOf(fn("/foo/bar/baz/quux")));
  }

  public void testJoin() throws Exception {
    assertEquals(fn("/foo/bar/baz/quux"), fn("foo/bar").join("baz/quux"));
    assertEquals(fn("/foo/bar"), fn("/").join("/foo/bar"));
    assertEquals(fn("/foo.zip/bar.txt"), fn("foo.zip").join("bar.txt"));
  }

  private FileRef fn(String s) {
    return fs.parseFilename(s);
  }
}
