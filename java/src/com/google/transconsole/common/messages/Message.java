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

import com.google.common.base.CharEscapers;
import com.google.common.base.Objects;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Value class for creating a Translation Console message, and displaying it in
 * various forms (including XML suitable for inclusion in an XMB).
 */
public final class Message extends BaseMessage {

  private final MessageProperties properties;

  /**
   * Creates the object.  This makes duplicates of the parameter data to
   * guarantee they cannot be mutated later by the caller.
   *
   * @param fragments of the message (text and placeholders)
   * @param properties of the message
   */
  Message(List<MessageFragment> fragments, MessageProperties properties) {
    super(null, fragments);
    this.properties = new MessageProperties(properties);
  }

  /**
   * Creates the object for the case of an existing message which already have
   * an id assigned.  This makes duplicates of the parameter data to guarantee
   * they cannot be mutated later by the caller.
   *
   * @param id of the message
   * @param fragments of the message (text and placeholders)
   * @param properties of the message
   */
  Message(String id, List<MessageFragment> fragments,
          MessageProperties properties) {
    super(id, fragments);
    this.properties = new MessageProperties(properties);
  }

  /**
   * Returns the "original" version of the message (using the original values
   * from any placeholders, rather than their names or examples).
   *
   * @return original text
   */
  public String getOriginal() {
    StringBuilder sb = new StringBuilder();

    for (MessageFragment f : super.fragments) {
      sb.append(f.getOriginal());
    }

    return sb.toString();
  }

  private void appendSource(StringBuilder sb) {
    for (String source : properties.getSources()) {
      sb.append("<source>");
      sb.append(CharEscapers.xmlContentEscaper().escape(source));
      sb.append("</source>");
    }
  }

  @Override
  protected String getFragmentsAsXml() {
    StringBuilder sb = new StringBuilder();

    for (MessageFragment f : fragments) {
      sb.append(f.toXml(BundleFormat.XMB));
    }

    return sb.toString();
  }

  /**
   * Returns the message rendered as an XMB fragment.  The XMB DTD is located
   * at
   *
   * /home/build/nonconf/google3/i18n/messagebundle.dtd.
   *
   * @return XMB fragment (String).
   */
  @Override
  public String toXml() {
    StringBuilder sb = new StringBuilder();

    sb.append("<msg");
    appendAttribute(sb, "id", getId());
    appendAttribute(sb, "desc", properties.getDescription());
    appendAttribute(sb, "meaning", properties.getMeaning());
    appendAttribute(sb, "name", properties.getName());

    if (properties.isHidden()) {
      appendAttribute(sb, "is_hidden", "1");
    }

    if (properties.isObsolete()) {
      appendAttribute(sb, "obsolete", "obsolete");
    }

    // If the "collapsed" version of the string (removing any extra whitespace)
    // is different from the original version, we emit the original version
    // and mark it with "xml:space=preserve".  This is the marker used to
    // indicate non-standard whitespace, which we preserve in the string.

    String contentXml = getFragmentsAsXml();
    String collapsedContentXml = collapseXml(contentXml);

    if (!contentXml.equals(collapsedContentXml)) {
      appendAttribute(sb, "xml:space", "preserve");
    }

    sb.append(">");
    appendSource(sb);
    sb.append(contentXml);
    sb.append("</msg>");

    return sb.toString();
  }

  /**
   * Returns an ID if it exist and if not, generates a unique ID for the message
   * based upon its presentation and its meaning.
   *
   * @return ID string (right now, this is a string form of a long)
   */
  @Override
  public String getId() {
    if (super.id != null) {
      return super.id;
    }

    return MessageUtil.generateMessageId(getPresentation(), getMeaning(),
                                         getContentType());
  }

  public String getContentType() {
    return properties.getContentType();
  }

  public String getDescription() {
    return properties.getDescription();
  }

  public String getMeaning() {
    return properties.getMeaning();
  }

  public Set<String> getSources() {
    return Collections.unmodifiableSet(properties.getSources());
  }

  public String getName() {
    return properties.getName();
  }

  public boolean isHidden() {
    return properties.isHidden();
  }

  public boolean isObsolete() {
    return properties.isObsolete();
  }

  /**
   * Merges this message with supplied message to produce a new "merged"
   * message.
   *
   * @throws IllegalArgumentException if the message IDs are not the same.
   * @throws InvalidMessageException if any of the message attributes are
   * incompatible.
   */
  public Message merge(Message that) throws InvalidMessageException {
    String id = getId();
    String thatId = that.getId();

    if (!Objects.equal(id, thatId)) {
      throw new IllegalArgumentException(
          "Cannot merge messages with different IDs: " + id + " != " + thatId);
    }

    if (!super.fragments.equals(that.fragments)) {
      throw new InvalidMessageException(id,
          "Cannot merge messages with different content.");
    }

    MessageProperties mergedProperties;
    try {
      mergedProperties = properties.merge(that.properties);
    } catch (InvalidMessageException ive) {
      throw new InvalidMessageException(id, ive.getMessage());
    }

    return new Message(super.fragments, mergedProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.id, super.fragments, properties);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Message)) {
      return false;
    }
    Message otherMsg = (Message) other;
    return (Objects.equal(id, otherMsg.id)
            && Objects.equal(fragments, otherMsg.fragments)
            && Objects.equal(properties, otherMsg.properties));
  }

  public String toString() {
    return "[" + getPresentation() + "] ["
        + getMeaning() + "] [" + getContentType() + "]";
  }
}
