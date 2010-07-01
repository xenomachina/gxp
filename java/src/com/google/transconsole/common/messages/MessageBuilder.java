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

import com.google.common.collect.Maps;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for constructing messages.  Message objects are immutable; this
 * class should be used to produce them. Example:
 *
 * <pre>
 * MessageBuilder mb = new MessageBuilder();
 * Message m = mb.appendText("Hello, ")
 *    .appendPlaceholder("%1", "USER", "Fred")
 *    .setDescription("Greeting to user logging in")
 *    .createMessage();
 * </pre>
 */
public class MessageBuilder {

  private String id;
  private List<MessageFragment> fragments;
  private MessageProperties properties;
  private boolean preserveWhitespace;

  /**
   * Sets up builder with empty message data.
   *
   * By default, whitespaces around a message are preserved.
   */
  public MessageBuilder() {
    this(true);
  }

  /**
   * Sets up the builder with an empty message data and sets the policy for
   * whitespace surrounding the content of the message.
   *
   * Setting the parameter to false may result in empty messages.
   *
   * @param preserveWhitespace If true, the whitespaces around the message will
   * be preserved.
   */
  public MessageBuilder(boolean preserveWhitespace) {
    fragments = new ArrayList<MessageFragment>();
    properties = new MessageProperties();
    this.preserveWhitespace = preserveWhitespace;
  }

  /**
   * Appends more text to the existing content of the message.
   *
   * @param text to add to the end of the message.
   * @return A reference to this instance.
   */
  public MessageBuilder appendText(String text) {
    fragments.add(new TextFragment(text));
    return this;
  }

  /**
   * Appends a placeholder to the end of the message.  This method is used
   * when there is a fragment of text that should be "locked down" and
   * immutable by translators, such as reserved words or formatting specifiers.
   * In such cases, we provide a "presentation" value that is shown instead.
   *
   * For example, in the string
   *
   * Welcome to the program, %1!
   *
   * we do not want the %1 to be translated, so we would use something like
   *
   * m.appendText("Welcome to the program, ");
   * m.appendPlaceholder("%1", "USER_NAME", "Fred");
   * m.appendText("!");
   *
   * and the translator would see the string
   *
   * Welcome to the program, USER_NAME!
   *
   * for translation (and our system requires that USER_NAME appear in the
   * translation).
   *
   * @param original text as it appears in the message
   * @param presentation ID the translator should see instead of the text
   * @param example sample value that the original text might have
   * @return A reference to this instance.
   *
   * @throws InvalidMessageException if any placeholder values are empty
   */
  public MessageBuilder appendPlaceholder(String original, String presentation,
      String example) throws InvalidMessageException {

    if (presentation == null || presentation.length() == 0) {
      throw new InvalidMessageException(
          "Invalid placeholder specification: presentation required");
    }

    if (example == null || example.length() == 0) {
      throw new InvalidMessageException(
          "Invalid placeholder specification: example required");
    }

    if (!presentation.matches(Placeholder.VALID_PLACEHOLDER_REG_EXP)) {
      throw new InvalidMessageException(
          "Invalid placeholder specification: only caps, digits, and " +
          "underscores allowed in presentation");
    }

    Placeholder p = new Placeholder(original, presentation, example);

    for (MessageFragment f : fragments) {
      if (p.conflictsWith(f)) {
        throw new InvalidMessageException("Conflicting declarations of "
            + presentation + " within message");
      }
    }

    fragments.add(p);
    return this;
  }

  /**
   * Appends the specified fragment to the existing content of the
   * message.
   *
   * @param fragment fragment to add to the end of the message.
   * @return A reference to this instance.
   */
  public MessageBuilder appendFragment(MessageFragment fragment) {
    fragments.add(fragment);
    return this;
  }

  /**
   * Sets the id of the message. Once the id is set, {@code Message.getId()}
   * will return the set id. If it is not, {@code Message.getId()} will treat it
   * as a new message and generate a new id. Use this only if you are building
   * the structure of an existing message.
   *
   * @param id of the message
   * @return A reference to this instance.
   * @see com.google.transconsole.common.messages.Message#getId()
   */
  public MessageBuilder setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @return A reference to this instance.
   */
  public MessageBuilder setContentType(String contentType) {
    properties.setContentType(contentType);
    return this;
  }

  /**
   * @return A reference to this instance.
   */
  public MessageBuilder setDescription(String description) {
    properties.setDescription(description);
    return this;
  }

  /**
   * @return A reference to this instance.
   */
  public MessageBuilder setMeaning(String meaning) {
    properties.setMeaning(meaning);
    return this;
  }

  /**
   * @return A reference to this instance.
   */
  public MessageBuilder setHidden(boolean isHidden) {
    properties.setHidden(isHidden);
    return this;
  }

  /**
   * Sets the engineer-assigned string ID for the message.  Usually this
   * value is used when the engineer wants to make a manual, explicit
   * reference to a message by ID.  Example:  WELCOME_ERROR_MESSAGE.
   *
   * @param name of the message
   * @return A reference to this instance.
   */
  public MessageBuilder setName(String name) {
    properties.setName(name);
    return this;
  }

  /**
   * Sets whether this message is "obsolete", meaning that it is no longer
   * being used by the running product.  Occasionally it is desirable to
   * preserve messages that are not being used, because they may be used
   * again later.  This flag is also used to represent messages that are not
   * yet live but will be.
   *
   * @param isObsolete if the message is not part of the live product
   * @return A reference to this instance.
   */
  public MessageBuilder setObsolete(boolean isObsolete) {
    properties.setObsolete(isObsolete);
    return this;
  }

  /**
   * Adds a reference to the resource from which the message comes.  Usually
   * this is the name of the file in which it occurs.
   *
   * @param source of the message
   * @return A reference to this instance.
   */
  public MessageBuilder addSource(String source) {
    properties.addSource(source);
    return this;
  }

  /**
   * Creates a message object using builder's current information.
   *
   * @return immutable Message
   * @throws InvalidMessageException if placeholders overlap
   */
  public Message createMessage() throws InvalidMessageException {
    if (!preserveWhitespace && fragments.size() != 0) {
      stripSpacesFromFragments();
    }

    Message m = (id == null)
        ? new Message(fragments, properties)
        : new Message(id, fragments, properties);

    checkForPlaceholdersOverlap(m);

    return m;
  }

  /**
   * Checks for message presentation that "accidentally" forms
   * another placeholder presentation substring through the composition of
   * one or more other fragments.  I.e., a message with a placeholder "FOOBAR"
   * and a placeholder "BARBAZ" could appear to contain an occurrence of
   * "FOOBAR" if it were
   *
   * mb.appendText("FOO");
   * mb.appendPlaceholder("%1", "BARBAZ", "barbaz");
   *
   * @param m message to examine for overlaps
   * @throw {@code InvalidMessageException} if placeholders overlap (an error)
   */
  private void checkForPlaceholdersOverlap(Message m) throws InvalidMessageException {
    int pos = 0;
    HashMap<Integer, Placeholder> phs = Maps.newHashMap();

    for (MessageFragment f : fragments) {
      if (f instanceof Placeholder) {
        phs.put(pos, (Placeholder) f);
      }

      pos += f.getPresentation().length();
    }

    String presentation = m.getPresentation();
    int start;

    for (MessageFragment f : fragments) {
      if (f instanceof Placeholder) {
        start = presentation.indexOf(f.getPresentation(), 0);

        while (start > -1) {
          Placeholder otherPh = phs.get(start);

          if ((otherPh == null &&
               !isSubstringInPlaceholder(start, f.getPresentation().length(), phs))
              || (otherPh != null &&
                  otherPh.getPresentation().length() < f.getPresentation().length())) {
            throw new InvalidMessageException(m.getId(),
                "Placeholder name (" + f.getPresentation() + ") duplicated in message content.");
          }

          start = presentation.indexOf(f.getPresentation(), start + 1);
        }
      }
    }
  }

  /**
   * Returns true if the substring beginning at pos with length len is
   * equal to or contained within a substring that corresponds to one of the
   * placeholder presentations.
   *
   * @param pos the starting index of the string
   * @param len the length of the string
   * @param phs dictionary of starting index of a placeholder, to placeholder
   * @return true as above
   */

  private boolean isSubstringInPlaceholder(int pos, int len,
      HashMap<Integer, Placeholder> phs) {
    for (Map.Entry<Integer, Placeholder> e : phs.entrySet()) {
      if (pos > e.getKey() && (pos + len <=
          e.getKey() + e.getValue().getPresentation().length())) {
        return true;
      }
    }

    return false;
  }

  /**
   * Strips the spaces from both ends of the internal fragment list.
   *
   * This method assumes a non-emtpy list.
   */
  private void stripSpacesFromFragments() {
    trimLeftFragments();
    trimRightFragments();
  }

  private void trimLeftFragments() {
    if (fragments.size() != 0) {
      MessageFragment fragment = fragments.get(0);
      if (fragment instanceof TextFragment) {
        String presentation = trimLeft(fragment.getPresentation());
        if (presentation.length() == 0) {
          fragments.remove(0);
          trimLeftFragments();
        } else {
          fragments.set(0, new TextFragment(presentation));
        }
      }
    }
  }

  private void trimRightFragments() {
    if (fragments.size() != 0) {
      int lastIndex = fragments.size() - 1;
      MessageFragment fragment = fragments.get(lastIndex);
      if (fragment instanceof TextFragment) {
        String presentation = trimRight(fragment.getPresentation());
        if (presentation.length() == 0) {
          fragments.remove(lastIndex);
          trimRightFragments();
        } else {
          fragments.set(lastIndex, new TextFragment(presentation));
        }
      }
    }
  }

  private String trimLeft(String s) {
    int i = 0;
    int limit = s.length() - 1;
    while (i <= limit && Character.isWhitespace(s.charAt(i))) {
      i++;
    }
    return s.substring(i);
  }

  private String trimRight(String s) {
    int i = s.length() - 1;
    while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
      i--;
    }
    return s.substring(0, i + 1);
  }
}
