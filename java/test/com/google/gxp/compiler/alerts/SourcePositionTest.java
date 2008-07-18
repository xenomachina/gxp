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

package com.google.gxp.compiler.alerts;

import com.google.gxp.compiler.fs.FileRef;

import junit.framework.TestCase;

import static com.google.gxp.testing.MoreAsserts.*;

/**
 * Tests for {@link SourcePosition}.
 */
public class SourcePositionTest extends TestCase {
  public void testBadParams() throws Exception {
    try {
      new SourcePosition((FileRef) null, 1, 2);
      fail();
    } catch (NullPointerException npe) {
      // yay
    }

    try {
      new SourcePosition((FileRef) null);
      fail();
    } catch (NullPointerException npe) {
      // yay
    }

    try {
      new SourcePosition("foo", 0, 2);
      fail();
    } catch (IllegalArgumentException iax) {
      // yay
    }

    try {
      new SourcePosition("foo", 2, 0);
      fail();
    } catch (IllegalArgumentException iax) {
      // yay
    }
  }

  public void testToString() throws Exception {
    assertEquals("/foo:1:2:1:2", new SourcePosition("foo", 1, 2).toString());
    assertEquals("/baz:0:0:0:0", new SourcePosition("baz").toString());
  }

  public void testEqualsAndHashCode() throws Exception {
    SourcePosition f1 = new SourcePosition("foo", 1, 2);
    SourcePosition f2 = new SourcePosition("foo", 1, 2);
    SourcePosition b1 = new SourcePosition("bar");
    SourcePosition b2 = new SourcePosition("bar");

    // sanity check
    assertNotSame(f1, f2);
    assertNotSame(b1, b2);

    assertEquals(f1, f2);
    assertEquals(f1.hashCode(), f2.hashCode());
    assertEquals(b1, b2);
    assertEquals(b1.hashCode(), b2.hashCode());

    assertNotEqual(f1, b1);
    assertNotEqual(f1.hashCode(), b1.hashCode());
    assertNotEqual(f1, b2);
    assertNotEqual(f1.hashCode(), b2.hashCode());
    assertNotEqual(f2, b1);
    assertNotEqual(f2.hashCode(), b1.hashCode());
    assertNotEqual(f2, b2);
    assertNotEqual(f2.hashCode(), b2.hashCode());
  }

  public void testGetSourceName() throws Exception {
    assertEquals("/foo", new SourcePosition("foo", 1, 2).getSourceName());
    assertEquals("/bar", new SourcePosition("bar").getSourceName());
  }

  public void testGetLine() throws Exception {
    assertEquals(1, new SourcePosition("foo", 1, 2).getLine());
    assertEquals(0, new SourcePosition("bar").getLine());
  }

  public void testGetColumn() throws Exception {
    assertEquals(2, new SourcePosition("foo", 1, 2).getColumn());
    assertEquals(0, new SourcePosition("bar").getColumn());
  }
}
