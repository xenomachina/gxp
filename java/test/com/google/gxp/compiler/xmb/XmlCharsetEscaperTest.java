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

package com.google.gxp.compiler.xmb;

import com.google.common.base.Charsets;

import junit.framework.TestCase;

/**
 * Tests for {@link XmlCharsetEscaper}.
 */
public class XmlCharsetEscaperTest extends TestCase {
  private static final char[] XML_META_CHARS = {
    '<', '>', '&', '"', '\''
  };

  // Random chars from various charsets.
  private static final char ASCII_CHAR = 'q';
  private static final char LATIN1_CHAR = '\u00e9';
  private static final char UTF8_CHAR = '\u2620';

  private void assertMetaCharsNotEscaped(XmlCharsetEscaper escaper) {
    for (char c : XML_META_CHARS) {
      assertNull(escaper.escape(c));
    }
  }

  private void assertCharArrayEqual(String expected, char[] actual) {
    assertEquals(expected, new String(actual));
  }

  public void testAscii() throws Exception {
    XmlCharsetEscaper escaper = new XmlCharsetEscaper(Charsets.US_ASCII);
    assertNull(escaper.escape(ASCII_CHAR));
    assertCharArrayEqual("&#233;", escaper.escape(LATIN1_CHAR));
    assertCharArrayEqual("&#9760;", escaper.escape(UTF8_CHAR));
  }

  public void testLatin1() throws Exception {
    XmlCharsetEscaper escaper = new XmlCharsetEscaper(Charsets.ISO_8859_1);
    assertNull(escaper.escape(ASCII_CHAR));
    assertNull(escaper.escape(LATIN1_CHAR));
    assertCharArrayEqual("&#9760;", escaper.escape(UTF8_CHAR));
  }

  public void testUtf8() throws Exception {
    XmlCharsetEscaper escaper = new XmlCharsetEscaper(Charsets.UTF_8);
    assertNull(escaper.escape(ASCII_CHAR));
    assertNull(escaper.escape(LATIN1_CHAR));
    assertNull(escaper.escape(UTF8_CHAR));
  }
}
