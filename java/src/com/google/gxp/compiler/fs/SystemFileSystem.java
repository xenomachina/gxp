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
 * The "system" FileSystem. Acts as an adapter for the file system exposed by
 * {@link java.io.File} (typically the OS's file system). As there is only one
 * java.io file system, there is only one instance of this class.
 */
public final class SystemFileSystem extends AbstractFileSystem {
  private SystemFileSystem() {
    // Do not add state (ie: fields) to this class.
    // All state should be derived from java.io.
  }
  public static final SystemFileSystem INSTANCE = new SystemFileSystem();

  private final FileStore store = new FileStore() {
    public Charset getDefaultCharset() {
      return SystemFileSystem.this.getDefaultCharset();
    }

    public URI toUri(FileRef fileRef) {
      try {
        return new URI("file", null, fileRef.getName(), null);
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException(e);
      }
    }

    public InputStream openInputStream(FileRef fileRef)
        throws IOException {
      return new FileInputStream(fileRefToFile(fileRef));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Note that if the directory for the new file does not exist it will be
     * created automatically.
     */
    public OutputStream openOutputStream(FileRef fileRef)
        throws IOException {
      File file = fileRefToFile(fileRef);
      file.getParentFile().mkdirs();
      return new FileOutputStream(file);
    }

    public String toFilename(FileRef fileRef) {
      return fileRefToFile(fileRef).getPath();
    }

    public String toRelativeFilename(FileRef fileRef) {
      String filename = toFilename(fileRef);
      String cwd = getCwd().toFilename();

      return filename.startsWith(cwd)
          ? filename.substring(cwd.length() + 1)
          : filename;
    }

    public long getLastModified(FileRef fileRef) {
      return fileRefToFile(fileRef).lastModified();
    }

    public boolean delete(FileRef fileRef) {
      return fileRefToFile(fileRef).delete();
    }
  };

  protected FileStore getFileStore() {
    return store;
  }

  private static File fileRefToFile(FileRef fileRef) {
    return new File(fileRef.toUri());
  }

  /**
   * Parses a filename. Takes any filename that would be acceptable for
   * constructing a java.io.File. Note that relative filenames will be resolved
   * relative to the current working directory.
   */
  public FileRef parseFilename(String filename) {
    return new FileRef(store, filenameToFile(filename).toURI().getPath());
  }

  /**
   * @return a {@code FileRef} for the current working directory
   */
  public FileRef getCwd() {
    return parseFilename(System.getProperty("user.dir"));
  }

  private File filenameToFile(String filename) {
    return new File(filename).getAbsoluteFile();
  }

  private static Pattern PATH_SEPARATOR_PATTERN =
      Pattern.compile(Pattern.quote(File.pathSeparator));

  protected Pattern getFilenameListDelimiter() {
    return PATH_SEPARATOR_PATTERN;
  }
}
