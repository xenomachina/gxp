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

package com.google.gxp.css;

import junit.framework.TestCase;

/**
 * Tests for {@code Color}.
 */
public class ColorTest extends TestCase {
  public void test6Digit() throws Exception {
    // all components need three digits
    assertEquals("#123456", new Color(0x12, 0x34, 0x56).toString());
    assertEquals("#023456", new Color(0x02, 0x34, 0x56).toString());
    assertEquals("#003456", new Color(0x00, 0x34, 0x56).toString());
    assertEquals("#000456", new Color(0x00, 0x04, 0x56).toString());
    assertEquals("#000056", new Color(0x00, 0x00, 0x56).toString());
    assertEquals("#000006", new Color(0x00, 0x00, 0x06).toString());

    // one component only needs one digit, but the other two need two
    assertEquals("#113456", new Color(0x11, 0x34, 0x56).toString());
    assertEquals("#123356", new Color(0x12, 0x33, 0x56).toString());
    assertEquals("#123455", new Color(0x12, 0x34, 0x55).toString());
    assertEquals("#000ff0", new Color(0x00, 0x0f, 0xf0).toString());
    assertEquals("#0ff000", new Color(0x0f, 0xf0, 0x00).toString());
    assertEquals("#0ffff0", new Color(0x0f, 0xff, 0xf0).toString());
    assertEquals("#f0000f", new Color(0xf0, 0x00, 0x0f).toString());
    assertEquals("#f00fff", new Color(0xf0, 0x0f, 0xff).toString());
    assertEquals("#fff00f", new Color(0xff, 0xf0, 0x0f).toString());

    // two components only need one digit, but one needs two
    assertEquals("#113356", new Color(0x11, 0x33, 0x56).toString());
    assertEquals("#113455", new Color(0x11, 0x34, 0x55).toString());
    assertEquals("#113356", new Color(0x11, 0x33, 0x56).toString());
    assertEquals("#123355", new Color(0x12, 0x33, 0x55).toString());
    assertEquals("#113455", new Color(0x11, 0x34, 0x55).toString());
    assertEquals("#123355", new Color(0x12, 0x33, 0x55).toString());
    assertEquals("#fff000", new Color(0xff, 0xf0, 0x00).toString());
    assertEquals("#0fff00", new Color(0x0f, 0xff, 0x00).toString());
    assertEquals("#00fff0", new Color(0x00, 0xff, 0xf0).toString());
    assertEquals("#000fff", new Color(0x00, 0x0f, 0xff).toString());
    assertEquals("#f000ff", new Color(0xf0, 0x00, 0xff).toString());
    assertEquals("#ff000f", new Color(0xff, 0x00, 0x0f).toString());
  }

  public void test3Digit() throws Exception {
    // all components only need one digit
    assertEquals("#000", new Color(0x00, 0x00, 0x00).toString());
    assertEquals("#123", new Color(0x11, 0x22, 0x33).toString());
    assertEquals("#abc", new Color(0xaa, 0xbb, 0xcc).toString());
    assertEquals("#fff", new Color(0xff, 0xff, 0xff).toString());
  }

  public void testBadComponents() throws Exception {
    try {
      new Color(0x00 - 1, 0x00, 0x00);
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }

    try {
      new Color(0x00, 0x00 - 1, 0x00);
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }

    try {
      new Color(0x00, 0x00, 0x00 - 1);
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }

    try {
      new Color(0xff + 1, 0xff, 0xff);
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }

    try {
      new Color(0xff, 0xff + 1, 0xff);
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }

    try {
      new Color(0xff, 0xff, 0xff + 1);
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }
  }

  public void testParsing() throws Exception {
    assertEquals(new Color(0x00, 0x00, 0x00), Color.valueOf("#000"));
    assertEquals(new Color(0x00, 0x00, 0x00), Color.valueOf("#000000"));
    assertEquals(new Color(0x11, 0xaa, 0xff), Color.valueOf("#11aaff"));
    assertEquals(new Color(0x11, 0xaa, 0xff), Color.valueOf("#11AAFF"));
    assertEquals(new Color(0x11, 0xaa, 0xff), Color.valueOf("#1af"));
    assertEquals(new Color(0x11, 0xaa, 0xff), Color.valueOf("#1AF"));
    assertEquals(new Color(0x12, 0x34, 0x56), Color.valueOf("#123456"));
    assertEquals(new Color(0xab, 0xcd, 0xef), Color.valueOf("#abcdef"));
    assertEquals(new Color(0xab, 0xcd, 0xef), Color.valueOf("#ABCDEF"));
    assertEquals(new Color(0xab, 0xcd, 0xef), Color.valueOf("#aBcDeF"));
  }

  public void testCantParse() throws Exception {
    try {
      Color.valueOf(null);
      fail();
    } catch (NullPointerException npe) {
      // yay!
    }

    try {
      Color.valueOf("");
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }

    // no hash
    try {
      Color.valueOf("000000");
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }

    // wrong number of digits
    try {
      Color.valueOf("#00");
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }

    // wrong number of digits
    try {
      Color.valueOf("#0000");
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }

    // wrong number of digits
    try {
      Color.valueOf("#00000");
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }

    // wrong number of digits
    try {
      Color.valueOf("#0000000");
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }

    // bad hex digit
    try {
      Color.valueOf("#abcdeg");
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }

    // trailing junk
    try {
      Color.valueOf("#000000z");
      fail();
    } catch (IllegalArgumentException iax) {
      // yay!
    }
  }

  public void testEquals() throws Exception {
    Color color = new Color(0x11, 0x22, 0x33);
    assertTrue(color.equals(new Color(0x11, 0x22, 0x33)));
    assertFalse(color.equals(new Color(0x00, 0x22, 0x33)));
    assertFalse(color.equals(new Color(0x11, 0x00, 0x33)));
    assertFalse(color.equals(new Color(0x11, 0x22, 0x00)));
    assertFalse(color.equals(null));
  }

  public void testFrom24BitRgb() throws Exception {
    assertEquals("#000", Color.from24BitRgb(0).toString());
    assertEquals("#800000", Color.from24BitRgb(0x800000).toString());
    assertEquals("#00fa98", Color.from24BitRgb(0xFA98).toString());
    assertEquals("#123", Color.from24BitRgb(0x112233).toString());
    assertEquals("#010a0f", Color.from24BitRgb(0x10A0F).toString());
  }
}
