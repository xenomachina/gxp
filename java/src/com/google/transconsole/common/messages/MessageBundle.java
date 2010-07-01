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

import com.google.common.base.Preconditions;

/**
 * The {@code MessageBundle} class is a container for {@link Message} objects.
 *
 * It also associates project, language, and source information with
 * the collection.
 */
public class MessageBundle extends Bundle<Message> {

  /**
   * Constructs an empty MessageBundle.
   *
   * @param projectId Translation Console ID of project (e.g. "gws").
   * @param languageId Translation Console ID of language (e.g. "en-US").
   */
  public MessageBundle(String projectId, String languageId) {
    super(projectId, languageId);
  }

  /**
   * Adds a {@link Message} to the bundle.
   *
   * Also adds the message's source to the list of bundle sources if
   * it is not null.
   *
   * @param m {@link Message} to add to bundle.
   * @throws InvalidMessageBundleException When adding this {@link Message}
   * to the bundle would result in an invalid bundle being created,
   * or when the {@link Message} {@code m} itself is invalid.
   */
  @Override
  public void addMessage(Message m) throws InvalidMessageBundleException {
    String id = m.getId();
    if (messages.containsKey(id)) {
      try {
        messages.put(id, messages.get(id).merge(m));
      } catch (InvalidMessageException imx) {
        throw new InvalidMessageBundleException(imx.getMessage()
                                                + " (id = " + id
                                                + ", source = \"" + m.getOriginal() + "\")",
                                                imx);
      }
    } else {
      messages.put(id, Preconditions.checkNotNull(m));
    }
  }
}
