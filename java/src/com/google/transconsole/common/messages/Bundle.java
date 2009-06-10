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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * The Bundle class is a container for {@link BaseMessage} objects.
 *
 * It also associates project, language, and source information
 * with the collection.
 */
public abstract class Bundle <T extends BaseMessage> implements Iterable<T> {

  private String projectId;
  private String languageId;
  protected Map<String, T> messages;

  /**
   * Returns an unmodifiable view of the (ID -> message object) map.
   *
   * Used in merging bundles.
   *
   * @return immutable copy of internal representation of message map.
   */
  protected Map<String, T> getMessageMap() {
    return Collections.unmodifiableMap(messages);
  }

  /**
   * Constructs an empty Bundle.
   *
   * @param projectId Translation Console ID of project (e.g. "gws")  This
   *        should always be a top-level project ID and not a subproject ID.
   * @param languageId Translation Console ID of language (e.g. "en-US").
   */
  public Bundle(String projectId, String languageId) {
    this.projectId = Preconditions.checkNotNull(projectId);
    this.languageId = Preconditions.checkNotNull(languageId);

    messages = Maps.newTreeMap();
  }

  public String getProjectId() {
    return this.projectId;
  }

  public String getLanguage() {
    return this.languageId;
  }

  public T getMessage(String messageId) {
    return messages.get(messageId);
  }

  public boolean containsMessage(String messageId) {
    return messages.containsKey(messageId);
  }

  private final Ordering<T> MESSAGE_ORDERING = new Ordering<T>() {
    public int compare(T m1, T m2) {
      return m1.getId().compareTo(m2.getId());
    }
  };

  /**
   * @return a sorted immutable list of the Message objects in this bundle
   */
  public List<T> getMessages() {
    return ImmutableList.copyOf(MESSAGE_ORDERING.sortedCopy(messages.values()));
  }

  /**
   * Adds a message to the bundle.
   *
   * @param m The {@link BaseMessage} to add to the bundle.
   * @throws InvalidMessageBundleException When the message to be added
   * is invalid, or when adding this message to the bundle would create
   * an invalid bundle.
   */
  public abstract void addMessage(T m) throws InvalidMessageBundleException;

  /**
   * Removes a message from the bundle.
   */
  public void removeMessage(T m) {
    removeMessage(m.getId());
  }

  /**
   * Removes a message from the bundle.
   *
   * If the message doesn't exist, nothing will happen.
   *
   * @param messageId The id of the message to remove.
   */
  public void removeMessage(String messageId) {
    messages.remove(messageId);
  }

  /**
   * Merges another Bundle into this one.
   *
   * This is done by adding all messages from that bundle. It does not modify
   * the other bundle in any way, and subsequent changes to that bundle do
   * not affect this one.
   *
   * @param other {@link Bundle} to be merged with this bundle.
   * @throws InvalidMessageBundleException if the other bundle's properties
   * do not match this bundle's properties
   */
  public void mergeBundle(Bundle<T> other)
      throws InvalidMessageBundleException {

    if (!getProjectId().equals(other.getProjectId())) {
      throw new InvalidMessageBundleException(
          String.format(
              "Cannot merge bundles with different project IDs (%1$s, %2$s)",
              getProjectId(), other.getProjectId()));
    }

    if (!getLanguage().equals(other.getLanguage())) {
      throw new InvalidMessageBundleException(
          String.format(
              "Cannot merge bundles with different languages (%1$s, %2$s)",
              getLanguage(), other.getLanguage()));

    }

    for (T message : other.getMessages()) {
      addMessage(message);
    }
  }

  @Override
  public Iterator<T> iterator() {
    return getMessages().iterator();
  }
}
