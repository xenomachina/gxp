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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

/**
 * Base class for Translation Console message and translation.
 */
public abstract class BaseMessage implements Iterable<MessageFragment> {
  protected final String id;
  protected final ImmutableList<MessageFragment> fragments;

  /**
   * Creates the object. This makes duplicates of the parameter data to
   * guarantee they cannot be mutated later by the caller.
   *
   * @param id of the message
   * @param fragments of the message (text and placeholders)
   */
  BaseMessage(String id, List<MessageFragment> fragments) {
    this.id = id;
    this.fragments = ImmutableList.copyOf(normalizeFragments(fragments));
  }

  /**
   * Returns the "presentation" version of the message (using the names of any
   * placeholders, rather than the original values or examples).
   *
   * @return presentation text
   */
  public String getPresentation() {
    StringBuilder sb = new StringBuilder();

    for (MessageFragment f : fragments) {
      sb.append(f.getPresentation());
    }

    return sb.toString();
  }

  protected void appendAttribute(StringBuilder sb, String name, String value) {
    if (value == null) {
      return;
    }

    sb.append(" ");
    sb.append(name);
    sb.append("=\"");
    sb.append(CharEscapers.xmlEscaper().escape(value));
    sb.append("\"");
  }

  /**
   * Returns the fragments of this message in XML.
   *
   * @return XML elements in a {@code String}.
   */
  protected abstract String getFragmentsAsXml();

  protected String collapseXml(String s) {
    return s.trim().replaceAll("\\s+", " ");
  }

  /**
   * Returns the message rendered as an XML element.
   *
   * @return XML element
   */
  public abstract String toXml();

  // Accessor Methods
  public String getId() {
    return id;
  }

  public List<MessageFragment> getFragments() {
    return fragments;
  }

  @Override
  public Iterator<MessageFragment> iterator() {
    return fragments.iterator();
  }

  /**
   * Normalizes the fragments by merging adjacent TextFragments and
   * obliterating empty ones.
   */
  private static List<MessageFragment> normalizeFragments(
      List<MessageFragment> input) {
    StringBuilder sb = new StringBuilder();
    List<MessageFragment> result = Lists.newArrayList();
    for (MessageFragment fragment : input) {
      fragment.partialNormalize(sb, result);
    }
    if (sb.length() > 0) {
      result.add(new TextFragment(sb.toString()));
    }
    return result;
  }
}
