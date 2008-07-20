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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * The underlying implementation of a {@code FileSystem}. Typically. if you
 * implement {@code FileSystem} you will also need to implement this interface.
 *
 * <p>Application code should never interact with {@code FileSystemImpl}s, but
 * should instead interact with {@code FileRef}s. {@code FileRef}s know which
 * {@code FileSystemImpl} to interact with.
 */
public interface FileStore {
  /**
   * @return the default {@code Charset} for the FileStore.
   */
  Charset getDefaultCharset();

  /**
   * Opens the stream corresponding to the specified filename for reading.
   * Client code should call {@link FileRef#openInputStream()} instead.
   */
  InputStream openInputStream(FileRef fileRef) throws IOException;

  /**
   * Opens the stream corresponding to the specified filename for writing.
   * Client code should call {@link FileRef#openOutputStream()} or {@link
   * FileRef#openWriter(Charset)} instead.
   */
  OutputStream openOutputStream(FileRef fileRef) throws IOException;

  /**
   * Converts FileRef to the FileSystem's "system name" for that file. This is
   * the version of the filename that users are used to seeing.  Client code
   * should call {@link FileRef#toFilename()} instead.
   */
  String toFilename(FileRef fileRef);

  /**
   * Converts FileRef to the FileSystem's "system name" relative to the cwd if
   * one exists for this FileSystem.  Client code should call
   * {@link FileRef#toRelativeFilename()} instead.
   */
  String toRelativeFilename(FileRef fileRef);

  /**
   * Returns the time that the file was last modified, measured in milliseconds
   * since the epoch or 0 if this information is unavailable.
   */
  long getLastModified(FileRef fileRef);

  /**
   * Delete the file coresponding to the given {@code FileRef}.
   *
   * @return true if a file is deleted, false otherwise.
   */
  boolean delete(FileRef fileRef);

  /**
   * @return a URI for the given {@code FileRef}.
   */
  URI toUri(FileRef fileRef);
}
