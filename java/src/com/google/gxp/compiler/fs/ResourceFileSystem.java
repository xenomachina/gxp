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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * A {@link FileSystem} implementation that is backed by Java resources.
 *
 * {@code OutputStream}s for the returned FileRef will be throw away any
 * supplied data.
 */
public class ResourceFileSystem extends AbstractFileSystem {

  private final FileStore store = new FileStore() {
    public Charset getDefaultCharset() {
      return ResourceFileSystem.this.getDefaultCharset();
    }

    public URI toUri(FileRef fileRef) {
      try {
        return getClass().getResource(fileRef.getName()).toURI();
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }

    public InputStream openInputStream(FileRef fileRef) {
      return getClass().getResourceAsStream(fileRef.getName());
    }

    /**
     * Resource backed {@code FileStore}s are read only. Throw
     * an exception if they are written to.
     */
    public OutputStream openOutputStream(FileRef fileRef) {
      throw new UnsupportedOperationException();
    }

    public String toFilename(FileRef fileRef) {
      return fileRef.getName();
    }

    public String toRelativeFilename(FileRef fileRef) {
      // no cwd for ResourceFileSystem
      return toFilename(fileRef);
    }

    public long getLastModified(FileRef fileRef) {
      return 0;
    }

    public boolean delete(FileRef fileRef) {
      return false;
    }
  };

  protected FileStore getFileStore() {
    return store;
  }

  public FileRef parseFilename(String systemFilename) {
    return new FileRef(store, systemFilename);
  }

  private static Pattern LIST_DELIMITER_PATTERN = Pattern.compile(":");

  protected Pattern getFilenameListDelimiter() {
    return LIST_DELIMITER_PATTERN;
  }
}
