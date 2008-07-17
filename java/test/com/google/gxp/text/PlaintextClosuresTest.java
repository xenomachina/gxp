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

package com.google.gxp.text;

import com.google.gxp.testing.BaseFunctionalTestCase;
import com.google.gxp.testing.TestLocalizable;
import com.google.i18n.Localizable;

import java.util.Locale;

/**
 * Tests for {@code PlaintextClosures}
 */
public class PlaintextClosuresTest extends BaseFunctionalTestCase {
  public void testEmpty() throws Exception {
    PlaintextClosures.EMPTY.write(out, gxpContext);
    assertOutputEquals("");
  }

  public void testFromPlaintext() throws Exception {
    PlaintextClosures.fromPlaintext("foo < bar > baz & qux \" quux").write(out, gxpContext);
    assertOutputEquals("foo < bar > baz & qux \" quux");

    try {
      PlaintextClosures.fromPlaintext(null);
      fail("should have thrown NullPointerException");
    } catch (NullPointerException e) {
      // good
    }
  }

  public void testFromLocalizable() throws Exception {
    Localizable l = new TestLocalizable();

    PlaintextClosures.fromLocalizable(l).write(out, createGxpContext(Locale.US, false));
    assertOutputEquals("[toString(en_US)]");

    PlaintextClosures.fromLocalizable(l).write(out, createGxpContext(Locale.UK, false));
    assertOutputEquals("[toString(en_GB)]");

    try {
      PlaintextClosures.fromLocalizable(null);
      fail("should have thrown NullPointerException");
    } catch (NullPointerException e) {
      // good
    }
  }
}
