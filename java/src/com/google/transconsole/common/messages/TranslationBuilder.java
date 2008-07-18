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

import java.util.ArrayList;
import java.util.List;

/**
 * Class for constructing translations. Translation objects are immutable; this
 * class should be used to produce them.
 *
 * Example:
 *
 * TranslationBuilder tb = new TranslationBuilder();
 * tb.setId("1234567890987654321");
 * tb.setLanguage("fr");
 * tb.appendText("Translated text with, ");
 * tb.appendPlaceholder("PLACEHOLDER_MARKER");
 * Translation t = tb.createTranslation();
 */
public class TranslationBuilder {

  private String id;
  private String language;
  private List<MessageFragment> fragments;

  /**
   * Sets up builder with empty message data.
   */
  public TranslationBuilder() {
    fragments = new ArrayList<MessageFragment>();
  }

  /**
   * Appends more text to the existing content of the translation.
   *
   * @param text to add to the end of the translation.
   * @return A reference to this builder for chaining.
   */
  public TranslationBuilder appendText(String text) {
    fragments.add(new TextFragment(text));
    return this;
  }

  /**
   * Appends the specified fragment to the existing content of the
   * translation.
   *
   * @param fragment fragment to add to the end of the translation.
   * @return A reference to this builder for chaining.
   */
  public TranslationBuilder appendFragment(MessageFragment fragment) {
    fragments.add(fragment);
    return this;
  }

  /**
   * Appends a placeholder to the end of the translation.
   *
   * @param presentation ID the translator should see instead of the text
   * @throws InvalidMessageException if any placeholder values are empty
   * @return A reference to this builder for chaining.
   */
  public TranslationBuilder appendPlaceholder(String presentation)
      throws InvalidMessageException {

    if (presentation == null || presentation.length() == 0) {
      throw new InvalidMessageException(
          "Invalid placeholder specification: presentation required");
    }

    if (!presentation.matches(Placeholder.VALID_PLACEHOLDER_REG_EXP)) {
      throw new InvalidMessageException(
          "Invalid placeholder specification: only caps, digits, and " +
          "underscores allowed in presentation");
    }

    Placeholder p = new Placeholder(presentation);

    fragments.add(p);

    return this;
  }

  /**
   * Sets the ID of the translation.
   *
   * @param id of the translation.
   * @return A reference to this builder for chaining.
   */
  public TranslationBuilder setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * Sets the language of the translation.
   *
   * @param language code of the translation.
   * @return A reference to this builder for chaining.
   */
  public TranslationBuilder setLanguage(String language) {
    this.language = language;
    return this;
  }

  /**
   * Creates a translation object using builder's current information.
   *
   * @return immutable Translation
   * @throws InvalidMessageException if id is not set
   */
  public Translation createTranslation() throws InvalidMessageException {
    if (id == null) {
      throw new InvalidMessageException("Translation must have an id");
    }

    Translation t = new Translation(id, language, fragments);

    return t;
  }
}
