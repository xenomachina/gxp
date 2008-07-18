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

package com.google.gxp.compiler.fs;

import com.google.common.collect.Lists;

import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Abstract base class for typical implementations of {@code FileSystem}. This
 * is useful for implementing {@code FileSystem}s that have all of the
 * following characteristics:
 * <ul>
 * <li>use a single {@code FileStore}
 * <li>represent a filename list as a sequence of filenames separated by some
 *     delimiter which can be matched by a regular expression
 * </ul>
 */
public abstract class AbstractFileSystem implements FileSystem {
  /**
   * Returns the FileStore used by this FileSystem.
   */
  protected abstract FileStore getFileStore();

  /**
   * Returns a regular expression which matches filename list delimters.
   */
  protected abstract Pattern getFilenameListDelimiter();

  /**
   * {@inheritDoc}
   *
   * <p>This implementation simply returns a {@code FileRef} with the abstract
   * name "/" on the {@code FileStore} returned by {@link #getFileStore()}.
   */
  public final FileRef getRoot() {
    return new FileRef(getFileStore(), "/");
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation splits {@code filenameList} using the result of
   * {@link #getFilenameListDelimiter()} and parses the resulting filenames
   * with {@link #parseFilename()}.
   */
  public final List<FileRef> parseFilenameList(String filenameList) {
    String[] filenames = getFilenameListDelimiter().split(filenameList, -1);
    List<FileRef> result = Lists.newArrayListWithExpectedSize(filenames.length);
    for (int i = 0; i < filenames.length; i++) {
      result.add(parseFilename(filenames[i]));
    }
    return Collections.unmodifiableList(result);
  }

  public Charset getDefaultCharset() {
    throw new UnsupportedOperationException();
  }
}
