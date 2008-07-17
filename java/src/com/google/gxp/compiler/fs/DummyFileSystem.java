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
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * A fake {@link FileSystem} implementation that cannot be read from
 * or written to.
 */
public class DummyFileSystem extends AbstractFileSystem {

  public static final FileSystem INSTANCE = new DummyFileSystem();

  private DummyFileSystem() {
  }

  private final FileStore store = new FileStore() {
    public Charset getDefaultCharset() {
      return DummyFileSystem.this.getDefaultCharset();
    }

    public URI toUri(FileRef fileRef) {
      return URI.create("dummy://" + fileRef.getName());
    }

    public InputStream openInputStream(FileRef fileRef) {
      throw new UnsupportedOperationException();
    }

    public OutputStream openOutputStream(FileRef fileRef) {
      throw new UnsupportedOperationException();
    }

    public String toFilename(FileRef fileRef) {
      return fileRef.getName();
    }

    public String toRelativeFilename(FileRef fileRef) {
      // no cwd for DummyFileSystem
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

  public FileRef parseFilename(String filename) {
    return new FileRef(store, filename);
  }

  private static Pattern LIST_DELIMITER_PATTERN = Pattern.compile(":");

  protected Pattern getFilenameListDelimiter() {
    return LIST_DELIMITER_PATTERN;
  }
}
