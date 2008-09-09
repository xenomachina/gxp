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

import java.util.List;
import java.util.regex.Pattern;

/**
 * Fragment of a message containing only ordinary text.
 */
public final class TextFragment implements MessageFragment {

  /*
   * Will be used to replace all single occurences of % into %%
   */
  private static final Pattern ESCAPE_PERCENT = Pattern.compile("%");

  private final String presentation;

  public TextFragment(String presentation) {
    this.presentation = presentation;
  }

  /**
   * Provides the original string as it was defined in the source code.
   *
   * Special characters which can identify a placeholder need to be escaped,
   * which this method takes care of.
   *
   * @return The original string, as it was defined in the source code.
   */
  public String getOriginal() {
    return ESCAPE_PERCENT.matcher(presentation).replaceAll("%%");
  }

  public String getPresentation() {
    return presentation;
  }

  /**
   * @param format The format in which this fragment will be written.
   *
   * @return the string with proper XML escaping done on the content
   */
  public String toXml(BundleFormat format) {
    return CharEscapers.xmlContentEscaper().escape(getPresentation());
  }

  /** {@inheritDoc} */
  public boolean conflictsWith(MessageFragment f) {
    return false;
  }

  public void partialNormalize(StringBuilder sb,
      List<? super MessageFragment> output) {
    sb.append(presentation);
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof TextFragment && equals((TextFragment) that));
  }

  public boolean equals(TextFragment that) {
    return Objects.equal(getPresentation(), that.getPresentation());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getPresentation());
  }
}
