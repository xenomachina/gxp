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

/**
 * The {@code TranslationBundle} class is a container class for
 * {@link Translation} objects.
 *
 * It also associates project, language, and source information with
 * the collection.
 */
public class TranslationBundle extends Bundle<Translation> {

  /**
   * Constructs an empty {@link TranslationBundle}.
   *
   * @param projectId Translation Console ID of project (e.g. {@code "gws"}).
   * This should always be a top-level project ID, and not a subproject ID.
   * @param languageId Translation Console ID of language
   * (e.g. {@code "en-US"}).
   */
  public TranslationBundle(String projectId, String languageId) {
    super(projectId, languageId);
  }

  /**
   * Adds a {@link Translation} to the bundle.
   *
   * @param t {@link Translation} to add to bundle.
   * TODO(?): throw InvalidMessageBundleException When adding this
   * {@link Translation} to the bundle would result in an invalid bundle being
   * created, or when the {@link Translation} {@code t} itself is invalid.
   */
  @Override 
  public void addMessage(Translation t) {
    String id = t.getId();

    if (!messages.containsKey(id)) {
      messages.put(id, Preconditions.checkNotNull(t));
    }
  }
}
