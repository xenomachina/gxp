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

import java.util.List;

/**
 * Value class for creating a Translation Console translation, and displaying
 * it in various forms (including XML suitable for inclusion in an XTB).
 */
public final class Translation extends BaseMessage {

  private final String language;

  /**
   * Creates the object. This makes duplicates of the parameter data to
   * guarantee they cannot be mutated later by the caller.
   *
   * @param id of the translation
   * @param language the translation belong to
   * @param fragments of the message (text and placeholders)
   */
  Translation(String id, String language, List<MessageFragment> fragments) {
    super(id, fragments);
    this.language = language;
  }

  public String getLanguage() {
    return language;
  }

  /**
   * Returns the translation rendered as an XTB fragment.  The XTB DTD is
   * located at
   *
   * /home/build/nonconf/google3/i18n/translationbundle.dtd.
   *
   * @return XTB fragment (String).
   */
  @Override
  public String toXml() {
    StringBuilder sb = new StringBuilder();

    sb.append("<translation");
    appendAttribute(sb, "id", getId());
    sb.append(">");
    sb.append(getFragmentsAsXml());
    sb.append("</translation>");

    return sb.toString();
  }

  @Override
  protected String getFragmentsAsXml() {
    StringBuilder sb = new StringBuilder();

    for (MessageFragment f : fragments) {
      sb.append(f.toXml(BundleFormat.XTB));
    }

    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<Translation [id: ")
      .append(id)
      .append(", language: ")
      .append(language)
      .append(", content: ")
      .append(fragments)
      .append("]>");

    return sb.toString();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }

    if (other instanceof Translation) {
      Translation t = (Translation) other;
      return (Objects.equal(id, t.id) && Objects.equal(fragments, t.fragments)
              && Objects.equal(t.language, language));
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, fragments, language);
  }
}
