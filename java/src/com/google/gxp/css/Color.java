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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.gxp.base.GxpContext;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a CSS color.
 */
public final class Color implements CssClosure {
  private final short red;
  private final short green;
  private final short blue;

  /**
   * @param red red component, from 0 to 255.
   * @param green green component, from 0 to 255.
   * @param blue blue component, from 0 to 255.
   */
  public Color(int red, int green, int blue) {
    this.red = validateComponent(red);
    this.green = validateComponent(green);
    this.blue = validateComponent(blue);
  }

  /**
   * Static factory method taking a single integer.
   *
   * @param rgb an integer storing the RGB components in the three least
   * significant bytes. Red component is in bits 16-23, green is in 8-15 and
   * blue in 0-7.
   */
  public static Color from24BitRgb(int rgb) {
    return new Color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
  }

  /**
   * @return a CSS representation of this color to the specified Appendable.
   * Will always be safe to insert into an HTML &lt;style&gt; element.
   */
  public String toString() {
    GxpContext gxpContext = new GxpContext(Locale.US);
    return gxpContext.getString(this);
  }

  /**
   * Appends a CSS representation of this color to the specified Appendable.
   * Will always be safe to insert into an HTML &lt;style&gt; element.
   */
  public void write(Appendable out, GxpContext gxpContext) throws IOException {
    out.append("#");
    if (isFourBit(red) && isFourBit(green) && isFourBit(blue)) {
      appendNybble(out, red);
      appendNybble(out, green);
      appendNybble(out, blue);
    } else {
      appendByte(out, red);
      appendByte(out, green);
      appendByte(out, blue);
    }
  }

  private short validateComponent(int x) {
    if (x < 0 || x > 255 ) {
      throw new IllegalArgumentException(x + " is not a valid component value."
                                         + " Must be in range [0,255].");
    }
    return (short)x;
  }

  private static void appendNybble(Appendable out, int x) throws IOException {
    out.append(Integer.toHexString(x & 0xf));
  }

  private static void appendByte(Appendable out, int x) throws IOException {
    if (x <= 0xf) {
      out.append("0");
    }
    out.append(Integer.toHexString(x));
  }

  private static boolean isFourBit(int x) {
    return (x % 0x11) == 0;
  }

  public boolean equals(Object that) {
    return (that instanceof Color) && equals((Color) that);
  }

  public boolean equals(Color that) {
    return this.red == that.red
        && this.blue == that.blue
        && this.green == that.green;
  }

  public int hashCode() {
    return Objects.hashCode(red, blue, green);
  }

  /**
   * Parses a given String into a Color value. String should be a hash
   * character ("#") followed by a 3 or 6 digit RGB hex value.
   * @throws IllegalArgumentException if String cannot be interpreted as a
   * Color.
   */
  public static Color valueOf(String s) {
    Matcher m = THREE_DIGIT_PATTERN.matcher(s);
    if (m.matches()) {
      return new Color(Integer.parseInt(m.group(1), 16) * 0x11,
                       Integer.parseInt(m.group(2), 16) * 0x11,
                       Integer.parseInt(m.group(3), 16) * 0x11);
    }
    m = SIX_DIGIT_PATTERN.matcher(s);
    if (m.matches()) {
      return new Color(Integer.parseInt(m.group(1), 16),
                       Integer.parseInt(m.group(2), 16),
                       Integer.parseInt(m.group(3), 16));
    }
    Color color = KEYWORD_MAP.get(s);
    if (color != null) {
      return color;
    }
    throw new IllegalArgumentException("Can't parse \"" + s
                                       + "\" as a CSS color.");
  }

  // constants for a subset of css predefined colors taken from this list:
  // http://www.w3.org/TR/css3-color/#svg-color
  //
  // additional colors can be added on request
  public static final Color AQUA = new Color(0, 255, 255);
  public static final Color BLACK = new Color(0, 0, 0);
  public static final Color BLUE = new Color(0, 0, 255);
  public static final Color FUCHSIA = new Color(255, 0, 255);
  public static final Color GRAY = new Color(128, 128, 128);
  public static final Color GREEN = new Color(0, 128, 0);
  public static final Color GREY = new Color(128, 128, 128);
  public static final Color INDIGO = new Color(75, 0, 130);
  public static final Color LIME = new Color(0, 255, 0);
  public static final Color MAROON = new Color(128, 0, 0);
  public static final Color NAVY = new Color(0, 0, 128);
  public static final Color OLIVE = new Color(128, 128, 0);
  public static final Color ORANGE = new Color(255, 165, 0);
  public static final Color PURPLE = new Color(128, 0, 128);
  public static final Color RED = new Color(255, 0, 0);
  public static final Color SILVER = new Color(192, 192, 192);
  public static final Color TEAL = new Color(0, 128, 128);
  public static final Color VIOLET = new Color(238, 130, 238);
  public static final Color WHITE = new Color(255, 255, 255);
  public static final Color YELLOW = new Color(255, 255, 0);

  private static final Map<String, Color> KEYWORD_MAP =
    new ImmutableMap.Builder<String, Color>()
      .put("aqua", AQUA)
      .put("black", BLACK)
      .put("blue", BLUE)
      .put("fuchsia", FUCHSIA)
      .put("gray", GRAY)
      .put("green", GREEN)
      .put("grey", GREY)
      .put("indigo", INDIGO)
      .put("lime", LIME)
      .put("maroon", MAROON)
      .put("navy", NAVY)
      .put("olive", OLIVE)
      .put("orange", ORANGE)
      .put("purple", PURPLE)
      .put("red", RED)
      .put("silver", SILVER)
      .put("teal", TEAL)
      .put("violet", VIOLET)
      .put("white", WHITE)
      .put("yellow", YELLOW)
      .build();

  private static final Pattern THREE_DIGIT_PATTERN =
      Pattern.compile("#([0-9a-f])([0-9a-f])([0-9a-f])",
                      Pattern.CASE_INSENSITIVE);

  private static final Pattern SIX_DIGIT_PATTERN =
      Pattern.compile("#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})",
                      Pattern.CASE_INSENSITIVE);
}
