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

/**
 * Tests for {@code PlaintextAppender}.
 */
public class PlaintextAppenderTest extends BaseFunctionalTestCase {
  private final PlaintextAppender APPENDER = PlaintextAppender.INSTANCE;

  public void testAppend() throws Exception {
    // primitives
    APPENDER.append(out, gxpContext, (byte)65);
    assertOutputEquals("65");

    APPENDER.append(out, gxpContext, (short)1024);
    assertOutputEquals("1024");

    APPENDER.append(out, gxpContext, 123456789);
    assertOutputEquals("123456789");

    APPENDER.append(out, gxpContext, 16777216512L);
    assertOutputEquals("16777216512");

    APPENDER.append(out, gxpContext, 38.125f);
    assertOutputEquals("38.125");

    APPENDER.append(out, gxpContext, 938.05625d);
    assertOutputEquals("938.05625");

    APPENDER.append(out, gxpContext, 'a');
    assertOutputEquals("a");

    // closures
    PlaintextClosure pc = PlaintextClosures.fromPlaintext("< > & ' \" zarf");
    APPENDER.append(out, gxpContext, pc);
    assertOutputEquals("< > & ' \" zarf");

    // objects
    APPENDER.append(out, gxpContext, "< > & ' \" zarf");
    assertOutputEquals("< > & ' \" zarf");

    APPENDER.append(out, gxpContext, new TestLocalizable());
    assertOutputEquals("[toString(en_US)]");
  }

  public void testAppendNulls() throws Exception {
    try {
      APPENDER.append(out, gxpContext, (PlaintextClosure)null);
      fail("should throw NullPointerException");
    } catch (NullPointerException e) {
      // good
    }

    try {
      APPENDER.append(out, gxpContext, (String)null);
      fail("should throw NullPointerException");
    } catch (NullPointerException e) {
      // good
    }

    try {
      APPENDER.append(out, gxpContext, (Localizable)null);
      fail("should throw NullPointerException");
    } catch (NullPointerException e) {
      // good
    }
  }
}
