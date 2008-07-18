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

import java.util.List;

/**
 * Interface describing a part of the contents of a message or translation.
 * This interface allows us to represent contents with different kinds of
 * meanings or metadata differently.
 */
public interface MessageFragment {

  /**
   * @return the message as it appears in the source file
   */
  public String getOriginal();

  /**
   * @return the message as it appears with placeholder "presentations"
   * substituted for the placeholder "originals"
   */
  public String getPresentation();

  /**
   * @param format The format in which this fragment will be written.
   *
   * @return an XML fragment representing the message
   */
  public String toXml(BundleFormat format);

  /**
   * Indicates whether these two fragments can exist in the same message.
   * For example, two placeholders with the same presentation and different
   * content conflict.
   *
   * @param f MessageFragment to compare against
   * @return true if the fragments conflict with one another
   */
  public boolean conflictsWith(MessageFragment f);

  /**
   * Used for normalizing the list of fragments in a {@code Message}. Text
   * fragments should output their presentation to {@code sb}. Other fragments
   * should convert {@code sb} to a {@code TextFragment} and add it and
   * themselves to {@code output}.
   *
   * @param sb StringBuilder for merging {@code TextFragment}s
   * @param output list to add MessageFragments to the end of
   */
  public void partialNormalize(StringBuilder sb,
                               List<? super MessageFragment> output);
}
