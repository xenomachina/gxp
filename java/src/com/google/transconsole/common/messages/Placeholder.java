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
import com.google.common.base.Preconditions;

import java.util.List;

/**
 * Message fragment representing a placeholder.  This is a fragment of the
 * message that should not be edited by a translator, along with metadata
 * describing how it should be shown to the translator.
 *
 * TODO(pdoyle): Add tests for presentation string (A-Z0-9_ only)
 */
public final class Placeholder implements MessageFragment {

  public static final String VALID_PLACEHOLDER_REG_EXP = "^[A-Z0-9_]+$";

  private final String original;
  private final String presentation;
  private final String example;

  /**
   * Constructs a new placeholder.  This consists of a substring of the
   * original message string, together with metadata describing what should
   * be shown to the translator instead of the original, and giving an example
   * of what this might look like.
   *
   * @param original substring of message as it appears in source
   * @param presentation text shown to translator instead of original
   * @param example example of what an instance of the original might look like
   *   (if the original is a variable)
   */
  public Placeholder(String original, String presentation, String example) {
    this.original = Preconditions.checkNotNull(original);
    this.presentation = Preconditions.checkNotNull(presentation);
    this.example = Preconditions.checkNotNull(example);
  }

  /**
   * Constructs a placeholder when no example is provided.
   *
   * @param original substring of message as it appears in source
   * @param presentation text shown to translator instead of original
   */
  public Placeholder(String original, String presentation) {
    this(original, presentation, "");
  }

  /**
   * Constructs a placeholder when the original and example is not provided.
   * This is used to describe the metadata of placeholders in translations.
   *
   * @param presentation
   */
  public Placeholder(String presentation) {
    this.original = null;
    this.presentation = Preconditions.checkNotNull(presentation);
    this.example = null;
  }

  @Override
  public String toString() {
    return original;
  }

  public String getOriginal() {
    return toString();
  }

  public String getPresentation() {
    return this.presentation;
  }

  public String getExample() {
    return this.example;
  }

  /**
   * Returns an XML fragment representing the placeholder in the XMB format.
   *
   * If the placeholder was constructed with only its presentation, a
   * placeholder in the XTB format is returned instead.
   *
   * The format is described in
   *
   * /home/build/nonconf/google3/i18n/messagebundle.dtd.
   * /home/build/nonconf/google3/i18n/translationbundle.dtd.
   *
   * @return xml fragment
   */
  public String toXml(BundleFormat format) {
    StringBuilder sb = new StringBuilder();

    sb.append("<ph name=\"");
    sb.append(CharEscapers.xmlEscaper().escape(presentation));
    sb.append("\"");
    if (format.equals(BundleFormat.XMB)) {
      sb.append(">");
       if (example != null) {
         sb.append("<ex>");
         sb.append(CharEscapers.xmlContentEscaper().escape(example));
         sb.append("</ex>");
       }
       if (original != null) {
         sb.append(CharEscapers.xmlContentEscaper().escape(original));
       }
       sb.append("</ph>");
    } else {
      sb.append("/>");
    }

    return sb.toString();
  }

  /**
   * {@inheritDoc}
   *
   * A placeholder conflicts only with other placeholders, and only when
   * they have identical presentations but different examples or originals.
   *
   * @param f MessageFragment to compare with
   * @return true if the fragments conflict
   */
  public boolean conflictsWith(MessageFragment f) {
    if (!(f instanceof Placeholder)) {
      return false;
    }

    Placeholder p = (Placeholder)f;

    if (p.getPresentation().equals(getPresentation())) {
      if (!p.getOriginal().equals(getOriginal()) ||
          !p.getExample().equals(getExample())) {
        return true;
      }
    }

    return false;
  }

  public void partialNormalize(StringBuilder sb,
      List<? super MessageFragment> output) {
    if (sb.length() > 0) {
      output.add(new TextFragment(sb.toString()));
      sb.setLength(0);
    }
    output.add(this);
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof Placeholder && equals((Placeholder) that));
  }

  public boolean equals(Placeholder that) {
    return Objects.equal(getOriginal(), that.getOriginal())
        && Objects.equal(getPresentation(), that.getPresentation())
        && Objects.equal(getExample(), that.getExample());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        getOriginal(),
        getPresentation(),
        getExample());
  }
}
