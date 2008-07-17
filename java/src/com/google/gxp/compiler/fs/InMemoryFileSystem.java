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

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A {@link FileSystem} implementation that resides in-memory. This is
 * primarily useful for testing, though it could theoretically be used as a
 * stub {@code FileSystem} in other situations. An {@code InMemoryFileSystem}
 * starts out empty. Its filename representation is exactly the same as
 * the abstract representation used by {@link FileRef} (this is an abstract
 * {@code FileSystem}, after all).
 *
 * <p>A note about timestamps: each instance of {@code InMemoryFileSystem} has
 * its own internal "virtual clock" which is initialized to 0 and <em>does not
 * change except via explicit calls to setCurrentTime</em>. When files are
 * created their last modified time will be set to the value of this virtual
 * clock if its value is positive. Files created when the clock has a
 * non-positive value will have an unretrievable last modified time.
 */
public class InMemoryFileSystem extends AbstractFileSystem {
  private final Map<FileRef,ByteArrayOutputStream> files = Maps.newHashMap();
  private final Map<FileRef,Long> timestamps = Maps.newHashMap();

  private long currentTime = 0L;

  /**
   * Returns the current time as far as this InMemoryFileSystem is concerned.
   */
  public long getCurrentTime() {
    return currentTime;
  }

  /**
   * Sets the current time as far as this InMemoryFileSystem is concerned.
   */
  public void setCurrentTime(long newCurrentTime) {
    currentTime = newCurrentTime;
  }

  private final FileStore store = new FileStore() {
    public Charset getDefaultCharset() {
      return InMemoryFileSystem.this.getDefaultCharset();
    }

    public URI toUri(FileRef fileRef) {
      return URI.create("inmem://" + fileRef.getName());
    }

    public InputStream openInputStream(FileRef fileRef)
        throws IOException {
      ByteArrayOutputStream file = files.get(fileRef);
      if (file == null) {
        throw new FileNotFoundException();
      } else {
        return new ByteArrayInputStream(file.toByteArray());
      }
    }

    public OutputStream openOutputStream(FileRef fileRef) {
      ByteArrayOutputStream file = new ByteArrayOutputStream();
      files.put(fileRef, file);
      if (currentTime > 0) {
        timestamps.put(fileRef, getCurrentTime());
      } else {
        timestamps.remove(fileRef);
      }
      return file;
    }

    public String toFilename(FileRef fileRef) {
      return fileRef.getName();
    }

    public String toRelativeFilename(FileRef fileRef) {
      // no cwd for InMemoryFileSystem
      return toFilename(fileRef);
    }

    public long getLastModified(FileRef fileRef) {
      Long result = timestamps.get(fileRef);
      return (result == null) ? 0 : result;
    }

    public boolean delete(FileRef fileRef) {
      if (files.remove(fileRef) != null) {
        timestamps.remove(fileRef);
        return true;
      } else {
        return false;
      }
    }

    @Override
    public String toString() {
      return "InMemoryFileSystem.FileStore@" + System.identityHashCode(this);
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

  /**
   * @return the complete set of names of the files that exist on this {@code
   * InMemoryFileSystem}.
   */
  public Set<FileRef> getManifest() {
    return Collections.unmodifiableSet(files.keySet());
  }

  @Override
  public Charset getDefaultCharset() {
    return Charsets.US_ASCII;
  }
}
