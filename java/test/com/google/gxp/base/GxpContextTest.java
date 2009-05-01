/**
 * Copyright (C) 2006 Google Inc.
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

package com.google.gxp.base;

import com.google.common.base.Charsets;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * Tests for {@link com.google.gxp.base.GxpContext}.
 */
public class GxpContextTest extends TestCase {
  public void testBasicConstruction() throws Exception {
    GxpContext gc;

    gc = GxpContext.builder(Locale.US).build();
    assertSame(Locale.US, gc.getLocale());
    assertFalse(gc.isForcingXmlSyntax());
    assertSame(Charsets.US_ASCII, gc.getCharset());

    gc = GxpContext.builder(Locale.CHINA).build();
    assertSame(Locale.CHINA, gc.getLocale());
    assertFalse(gc.isForcingXmlSyntax());
    assertSame(Charsets.US_ASCII, gc.getCharset());
  }

  public void testBasicConstruction_rejectsNull() throws Exception {
    try {
      GxpContext.builder(null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {
      // expected
    }
  }

  public void testForceXmlSyntax() throws Exception {
    GxpContext gc;

    gc = GxpContext.builder(Locale.US)
        .forceXmlSyntax().build();
    assertSame(Locale.US, gc.getLocale());
    assertTrue(gc.isForcingXmlSyntax());
    assertSame(Charsets.US_ASCII, gc.getCharset());

    gc = GxpContext.builder(Locale.US)
        .build();
    assertSame(Locale.US, gc.getLocale());
    assertFalse(gc.isForcingXmlSyntax());
    assertSame(Charsets.US_ASCII, gc.getCharset());
  }

  public void testSetCharset() throws Exception {
    GxpContext gc;

    gc = GxpContext.builder(Locale.US)
        .setCharset(Charsets.UTF_8).build();
    assertSame(Locale.US, gc.getLocale());
    assertFalse(gc.isForcingXmlSyntax());
    assertSame(Charsets.UTF_8, gc.getCharset());
  }

  public void testSetCharset_rejectsNull() throws Exception {
    GxpContext.Builder builder = GxpContext.builder(Locale.US);
    try {
      builder.setCharset(null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {
      // expected
    }
  }

  public void testLegacyConstructor_LocaleOnly() throws Exception {
    GxpContext gc;

    gc = new GxpContext(Locale.US);
    assertSame(Locale.US, gc.getLocale());
    assertFalse(gc.isForcingXmlSyntax());
    assertSame(Charsets.US_ASCII, gc.getCharset());

    gc = new GxpContext(Locale.CHINA);
    assertSame(Locale.CHINA, gc.getLocale());
    assertFalse(gc.isForcingXmlSyntax());
    assertSame(Charsets.US_ASCII, gc.getCharset());

    try {
      new GxpContext(null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {
      // expected
    }
  }
}
