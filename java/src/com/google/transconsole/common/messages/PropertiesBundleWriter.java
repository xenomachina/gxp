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

package com.google.transconsole.common.messages;

import com.google.common.base.CharEscaper;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * Writes a MessageBundle object into a java properties file.
 */
public class PropertiesBundleWriter {
  private final MessageBundle bundle;

  public PropertiesBundleWriter(MessageBundle bundle) {
    this.bundle = Preconditions.checkNotNull(bundle);
  }

  /**
   * Writes the contents of the bundle to the Appendable object.
   *
   * @param out Appendable object (file, string buffer, etc.)
   */
  public void write(Appendable out) throws IOException {
    out.append("# Message Translations File for Java\n");
    out.append("# project=");
    out.append(bundle.getProjectId());
    out.append('\n');
    out.append("# language=");
    out.append(bundle.getLanguage());
    out.append("\n\n");

    for (Message m : bundle.getMessages()) {
      out.append(m.getId());
      out.append(" = ");
      if (m.getOriginal().charAt(0) == ' ') {
        out.append('\\');
      }
      MESSAGE_ESCAPER.escape(out).append(m.getOriginal());
      out.append('\n');
    }
  }

  /**
   * CharEscaper that escapes special characters, control characters,
   * and non ascii characters for a java properties file.
   */
  private static final CharEscaper MESSAGE_ESCAPER = new CharEscaper() {
      public char[] escape(char c) {
        switch(c) {
          case '\\':
          case '!':
          case '#':
          case ':':
          case '=':
            char[] x = new char[2];
            x[0] = '\\';
            x[1] = c;
            return x;
          default:
            // control chars or non ascii
            if ((c < 32) || (126 < c)) {
              char[] r = new char[6];
              r[5] = HEX_DIGITS[c & 15];
              c >>>= 4;
              r[4] = HEX_DIGITS[c & 15];
              c >>>= 4;
              r[3] = HEX_DIGITS[c & 15];
              c >>>= 4;
              r[2] = HEX_DIGITS[c & 15];
              r[1] = 'u';
              r[0] = '\\';
              return r;
            }
            return null;
        }
      }
    };

  /**
   * Hex Digits as a char array
   */
  private static final char[] HEX_DIGITS = {
    '0' , '1' , '2' , '3' ,
    '4' , '5' , '6' , '7' ,
    '8' , '9' , 'a' , 'b' ,
    'c' , 'd' , 'e' , 'f' ,
  };
}
