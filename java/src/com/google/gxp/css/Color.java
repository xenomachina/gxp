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
import com.google.gxp.base.GxpContext;

import java.io.IOException;
import java.util.Locale;
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
    throw new IllegalArgumentException("Can't parse \"" + s
                                       + "\" as a CSS color.");
  }

  private static final Pattern THREE_DIGIT_PATTERN =
      Pattern.compile("#([0-9a-f])([0-9a-f])([0-9a-f])",
                      Pattern.CASE_INSENSITIVE);

  private static final Pattern SIX_DIGIT_PATTERN =
      Pattern.compile("#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})",
                      Pattern.CASE_INSENSITIVE);
}
