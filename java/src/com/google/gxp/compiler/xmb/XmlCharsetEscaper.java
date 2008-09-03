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

import com.google.common.base.CharEscaper;
import com.google.common.base.Preconditions;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * {@link CharEscaper} which converts characters that cannot be encoded in the
 * specified character set into XML character entity references. Note that
 * <em>this {@code CharEscaper} does not have special support for XML meta
 * characters</em> like angle brackets or ampersands. The client is responsible
 * for ensuring that those characters are already escaped appropriately.
 */
public class XmlCharsetEscaper extends CharEscaper {
  // TODO(laurence): add a unit test.
  // TODO(laurence): combine surrogate pairs (this would require changes to
  // CharEscaper)
  // TODO(laurence): possible optimization: cache or prefetch "canEncode" set?
  // TODO(laurence): possible optimization: cache escaped char arrays, and
  // perhaps even share them between instances?
  private final CharsetEncoder charsetEncoder;

  /**
   * Constructs an instance based on the specified {@code Charset}. Note that
   * the {@code Charset} must support the {@code newEncoder} method.
   */
  public XmlCharsetEscaper(Charset charset) {
    this(charset.newEncoder());
  }

  /**
   * Constructs an instance based on the specified {@code CharsetEncoder}.
   */
  public XmlCharsetEscaper(CharsetEncoder charsetEncoder) {
    this.charsetEncoder = Preconditions.checkNotNull(charsetEncoder);
  }

  // Implements CharEscaper.
  @Override
  public char[] escape(char c) {
    if (charsetEncoder.canEncode(c)) {
      return null;
    } else {
      String num = String.valueOf((int) c);
      char[] result = new char[num.length() + 3];
      result[0] = '&';
      result[1] = '#';
      num.getChars(0, num.length(), result, 2);
      result[result.length - 1] = ';';
      return result;
    }
  }
}
