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

import junit.framework.TestCase;

import static com.google.testing.util.MoreAsserts.*;

/**
 * Tests for {@code Alert}.
 */
public class AlertTest extends TestCase {
  private ErrorAlert error(SourcePosition pos, String message) {
    return new ErrorAlert(pos, message){};
  }

  private WarningAlert warning(SourcePosition pos, String message) {
    return new WarningAlert(pos, message){};
  }

  public void testBadParams() throws Exception {
    try {
      error(null, "foo");
      fail();
    } catch (NullPointerException npe) {
      // yay
    }

    try {
      error(new SourcePosition("bar"), null);
      fail();
    } catch (NullPointerException npe) {
      // yay
    }
  }

  public void testToString() throws Exception {
    assertEquals("/foo:1:2:1:2: oops",
                 error(new SourcePosition("foo", 1, 2), "oops").toString());
    assertEquals("/baz:0:0:0:0: darn",
                 error(new SourcePosition("baz"), "darn").toString());
  }

  public void testEqualsAndHashCode() throws Exception {
    Alert f1 = error(new SourcePosition("foo", 1, 2), "zarf");
    Alert f2 = error(new SourcePosition("foo", 1, 2), "zarf");
    Alert b1 = warning(new SourcePosition("bar"), "quux");
    Alert b2 = warning(new SourcePosition("bar"), "quux");

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
}
