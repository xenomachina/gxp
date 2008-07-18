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
 * This exception represents the result of attempting to build a message
 * with unacceptable properties.
 */
@SuppressWarnings("serial")
public class InvalidMessageException extends Exception {

  private String id;

  /**
   * Constructs a new {@link InvalidMessageException} with the spcified detail
   * message.
   */
  public InvalidMessageException(String message) {
    super(message);
  }

  /**
   * Constructs an {@link InvalidMessageException} with the specified message
   * identifier and detail message.
   */
  public InvalidMessageException(String id, String message) {
    super(message);
    this.id = id;
  }

  /**
   * @return The identifier of the message that caused this exception to occur
   * or {@code null} if no such identifier is known.
   */
  public String getMessageId() {
    return id;
  }
}
