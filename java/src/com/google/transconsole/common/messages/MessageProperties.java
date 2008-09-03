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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Properties associated with a message, other than its content.
 *
 * TODO(pdoyle): Missing support for:
 * - "seq" (do we need this at all?)
 */
final class MessageProperties {

  private String contentType;
  private String description;
  private String meaning;
  private String name;
  private final Set<String> sources;
  private boolean isHidden;
  private boolean isObsolete;

  public MessageProperties() {
    contentType = null;
    description = null;
    name = null;
    sources = Sets.newTreeSet();
    meaning = null;             // ID generator defines null == empty meaning
    isHidden = false;
    isObsolete = false;
  }

  /**
   * Note: does not copy arguments, so caller should make defensive copies if
   * appropriate.
   */
  private MessageProperties(String contentType, String description,
                            String meaning, String name, Set<String> sources,
                            boolean isHidden, boolean isObsolete) {
    this.contentType = contentType;
    this.description = description;
    this.name = name;
    this.sources = sources;
    this.meaning = meaning;
    this.isHidden = isHidden;
    this.isObsolete = isObsolete;
  }

  /**
   * Copy constructor
   */
  protected MessageProperties(MessageProperties original) {
    this.contentType = original.contentType;
    this.description = original.description;
    this.name = original.name;
    this.sources = Sets.newTreeSet(original.sources);
    this.meaning = original.meaning;
    this.isHidden = original.isHidden;
    this.isObsolete = original.isObsolete;
  }

  protected void setContentType(String contentType) {
    this.contentType = Preconditions.checkNotNull(contentType);
  }

  public String getContentType() {
    return contentType;
  }

  /**
   * Sets the engineer's description of the purpose of the message.  The
   * description cannot be null.
   *
   * @param description of the message
   */
  protected void setDescription(String description) {
    this.description = Preconditions.checkNotNull(description);
  }

  public String getDescription() {
    return description;
  }

  protected void setMeaning(String meaning) {
    this.meaning = meaning;
  }

  public String getMeaning() {
    return meaning;
  }

  protected void setHidden(boolean hidden) {
    this.isHidden = hidden;
  }

  public boolean isHidden() {
    return isHidden;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public void setObsolete(boolean obsolete) {
    this.isObsolete = obsolete;
  }

  public boolean isObsolete() {
    return this.isObsolete;
  }

  public void addSource(String source) {
    this.sources.add(source);
  }

  public Set<String> getSources() {
    return this.sources;
  }

  /**
   * Merges this MessageProperties with the supplied MessageProperties to
   * produce a "merged" one.
   *
   * @throws IllegalArgumentException if the message IDs are not the same.
   * @throws InvalidMessageException if any of the message attributes are
   * incompatible.
   */
  MessageProperties merge(MessageProperties that)
      throws InvalidMessageException {

    String mergedContentType = contentType;
    if (!Objects.equal(contentType, that.contentType)) {
      try {
        throw new InvalidMessageException(
            "Cannot merge messages with incompatible content-types.");
      } catch (InvalidMessageException e) {
        e.printStackTrace();
        throw e;
      }
    }

    String mergedDescription;
    if (Objects.equal(description, that.description)
        || (that.description == null)
        || (that.description.trim().length() == 0)) {
      mergedDescription = description;
    } else if ((description == null) || (description.trim().length() == 0)) {
      mergedDescription = that.description;
    } else {
      throw new InvalidMessageException(
          "Cannot merge messages with incompatible descriptions.");
    }

    String mergedMeaning;
    if (Objects.equal(meaning, that.meaning)) {
      mergedMeaning = meaning;
    } else {
      throw new InvalidMessageException(
          "Cannot merge messages with different meanings.");
    }

    String mergedName;
    if (Objects.equal(name, that.name) || (that.name == null)) {
      mergedName = name;
    } else if (name == null) {
      mergedName = that.name;
    } else {
      throw new InvalidMessageException(
          "Cannot merge messages with different names.");
    }

    Set<String> mergedSources = Sets.newTreeSet();
    mergedSources.addAll(sources);
    mergedSources.addAll(that.sources);

    boolean mergedIsHidden = isHidden && that.isHidden;
    boolean mergedIsObsolete = isObsolete && that.isObsolete;

    return new MessageProperties(mergedContentType, mergedDescription, mergedMeaning,
                                 mergedName, mergedSources, mergedIsHidden,
                                 mergedIsObsolete);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(contentType, description, meaning,
                            name, sources, isHidden, isObsolete);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof MessageProperties)) {
      return false;
    }
    MessageProperties otherProperties = (MessageProperties) other;
    return (Objects.equal(contentType, otherProperties.contentType)
            && Objects.equal(description, otherProperties.description)
            && Objects.equal(meaning, otherProperties.meaning)
            && Objects.equal(name, otherProperties.name)
            && Objects.equal(sources, otherProperties.sources)
            && Objects.equal(isHidden, otherProperties.isHidden)
            && Objects.equal(isObsolete, otherProperties.isObsolete));
  }
}
