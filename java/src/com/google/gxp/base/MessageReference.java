/*
 * Copyright (C) 2004 Google Inc.
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

import com.google.i18n.Localizable;
import java.util.Locale;

/**
 * Reference to a message. This allows code to refer to messages in a
 * locale-independent way; the reference is only converted to a {@link
 * Locale}-specific {@link Message} when it is formatted.
 */
public final class MessageReference {
  private final String source_;
  private final long id_;

  public MessageReference(String source, long id) {
    source_ = source;
    id_ = id;
  }

  /**
   * Returns the message id of this message.
   * It is the same value as that is supplied in the constructor.
   */
  public long getId() {
    return id_;
  }

  /**
   * This can be used to get a Message for any type of MessageReference.  It
   * should only be used when it is not possible to use the get method on
   * MessageReference[0-9,N] directly.
   */
  public Message getMessage(Locale locale) {
    return GxpTemplate.getRawMessage(source_, locale, id_);
  }

  protected static String localizedStringOf(Locale loc, Object o) {
    if (o instanceof Localizable) {
      return ((Localizable)o).toString(loc);
    } else {
      return o.toString();
    }
  }
}
