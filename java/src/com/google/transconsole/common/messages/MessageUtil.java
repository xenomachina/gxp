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

/**
 * Utility methods for operating on messages.
 */
public class MessageUtil {
  /**
   * Generates a numerical identifier for a message based on its
   * contents, an arbitrary "meaning" of the message, and a MIME content-type.
   *
   * Meaning is provided to disambiguate between two messages that have
   * identical source content but are semantically distinct; they can be given
   * different "meaning" strings to ensure that they are assigned distinct
   * message IDs.
   *
   * @param message Source string for which ID is desired.
   * @param meaning Explanation of the "meaning" of a string.
   * @param contentType MIME content-type of the message
   * @return Numerical ID associated with this message, meaning, and content-type.
   */
  public static String generateMessageId(String message, String meaning, String contentType) {
    long fp = Hash.fingerprint(message);

    // if there is a meaning, combine its fingerprint with the message's
    if (meaning != null) {
      long meaning_fp =  Hash.fingerprint(meaning);
      fp = meaning_fp + (fp << 1) + (fp < 0 ? 1 : 0);
    }

    // if there is a content-type, combine its fingerprint with the message's
    if (contentType != null) {
      long content_type_fp = Hash.fingerprint(contentType);
      fp = content_type_fp + (fp << 1) + (fp < 0 ? 1 : 0);
    }

    // to avoid negative IDs we strip the high-order bit
    return Long.toString((fp & 0x7fffffffffffffffL));
  }
}
