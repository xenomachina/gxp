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
 * bundle with unacceptable properties.
 */
public class InvalidMessageBundleException extends Exception {

  public InvalidMessageBundleException(String msg) {
    super(msg);
  }

  public InvalidMessageBundleException(String msg, InvalidMessageException cause) {
    super(msg, cause);
  }

  @Override
  public InvalidMessageException getCause() {
    return (super.getCause() == null)
        ? null
        : (InvalidMessageException) super.getCause();
  }
}
