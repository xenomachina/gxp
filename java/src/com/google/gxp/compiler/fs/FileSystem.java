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

import java.nio.charset.Charset;
import java.util.List;

/**
 * Provides a means for opening named "files" for reading or writing, and also
 * doing any other file-system operations.  Most of these operations are
 * actually accessed via {@code FileRef} objects, while this class acts as a
 * factory, creating {@code FileRef} objects out of filesystem-specific names
 * for files.
 *
 * <p>Note that this is not intended to be a "complete" file-system interface.
 * It currently only contains things needed by the GXP compiler.
 */
public interface FileSystem {
  /**
   * Returns a {@code FileRef} for the root of this {@code FileSystem}. The
   * the abstract name of the root is always "/".
   */
  FileRef getRoot();

  /**
   * Parses a filename (for this filesystem) into a {@code FileRef}.
   *
   * @throws IllegalArgumentException if filename cannot be parsed into a
   * {@code FileRef}.
   */
  FileRef parseFilename(String filename);

  /**
   * Parses a system path in this filesystem into an list of abstract FileRef
   * objects.  A "system path" is a string that specifies a list of filenames.
   * These are typically used for search paths, eg: class path, library path,
   * command path, etc. They are usually represented by some number of system
   * filenames separated by a system-specific delimiter, but the actual
   * representation is implementation specific.
   *
   * @return an Unmodifiable {@code List} of {@code FileRef}s
   *
   * @throws IllegalArgumentException if filename cannot be parsed
   * into a list of {@code FileRef}s.
   */
  List<FileRef> parseFilenameList(String filenameList);

  /**
   * @return the default {@code Charset} for this FileSystem.
   */
  Charset getDefaultCharset();
}
