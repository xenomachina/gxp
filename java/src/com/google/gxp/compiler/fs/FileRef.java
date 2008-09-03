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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.io.Characters;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import javax.tools.FileObject;
import javax.tools.JavaFileObject.Kind;

/**
 * A value object that refers to a file in some {@code FileSystem}s.
 *
 * <p>A {@code FileRef}'s "name" consists of a leading slash ("/") followed by
 * a sequence of zero or more slash separated components. Only the canonical
 * name "/" may end in a slash.  Adjacent slashes are collapsed into a single
 * slash.  Sample legal names are: "/" and "/a/b", but not "/a/" or "/a//b".
 * (The latter two names will be normalized into "/a" and "/a/b".) This
 * abstracts away from the system's notion of what a filename looks like,
 * whether it's "C:\windows\foo.txt" or "/usr/include/linux/stddef.h".
 *
 * <p>FileRef doesn't have any support for special filenames like "." or "..",
 * but some implementations of {@link FileSystem#parseFilename(String)}
 * will resolve these when parsing.
 *
 * <p>Note that there is no such thing as a relative {@code FileRef}; {@code
 * FileRef}s are always absolute. The {@link FileRef#join(String)} method may
 * be used to create {@code FileRef}s relative to existing {@code FileRef}s.
 *
 * <p>You can convert between the types of filenames the user is used to
 * seeing, "filenames", and {@code FileRef} objects using {@link
 * FileSystem#parseFilename(String)} and {@link
 * FileRef#toFilename()}.
 */
public final class FileRef implements FileObject {

  private final FileStore store;
  private final String name;

  // regex that matches 0 or more legal extension characters
  private static final String EXTENSION_CHARS = "[^\\./]*";

  private static final Pattern SUFFIX_CHARS_PATTERN =
    Pattern.compile("^[^\\/]*");

  private static final Pattern EXTENSION_PATTERN =
      Pattern.compile("\\.(" + EXTENSION_CHARS + ")$");

  public FileRef(FileStore store, String name) {
    this.store = Preconditions.checkNotNull(store);
    this.name = normalize(Preconditions.checkNotNull(name));
  }

  public String getName() {
    return name;
  }

  public URI toUri() {
    return store.toUri(this);
  }

  /**
   * Returns the time that the file was last modified, measured in milliseconds
   * since the epoch.
   */
  public long getLastModified() {
    return store.getLastModified(this);
  }

  /**
   * Delete this file
   */
  public boolean delete() {
    return store.delete(this);
  }

  /**
   * Opens this filename for writing text.
   *
   * <p>The {@code Writer} returned will throw a {@link
   * java.nio.charset.UnmappableCharacterException} if unmappable characters
   * (ie: characters that do not exist in the specified encoding) are written
   * to it.
   */
  public Writer openWriter(Charset encoding) throws IOException {
    return new OutputStreamWriter(openOutputStream(), encoding.newEncoder());
  }

  public Writer openWriter() throws IOException {
    return openWriter(store.getDefaultCharset());
  }

  /**
   * Opens this filename for reading text.
   *
   * TODO(harryh): the below note makes this not properly implement FileObject
   * but I'm intentionally leaving it this way for now, because I think this
   * is better.  Might have to revisit this decision later.
   *
   * <p>The {@code Writer} returned will throw a {@link
   * java.nio.charset.UnmappableCharacterException} if unmappable characters
   * (ie: characters that do not exist in the specified encoding) are written
   * to it.
   */
  public Reader openReader(Charset encoding) throws IOException {
    return new InputStreamReader(openInputStream(), encoding.newDecoder());
  }

  public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    return openReader(store.getDefaultCharset());
  }

  /**
   * Opens this filename for reading bytes.
   */
  public InputStream openInputStream() throws IOException {
    return store.openInputStream(this);
  }

  /**
   * Opens this filename for writing bytes.
   */
  public OutputStream openOutputStream() throws IOException {
    return store.openOutputStream(this);
  }

  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    return Characters.toString(openReader(ignoreEncodingErrors));
  }

  /**
   * @return a new FileRef where the final extension has been removed.
   * If the filename has no suffx, return this.
   */
  public FileRef removeExtension() {
    Matcher m = EXTENSION_PATTERN.matcher(name);
    return m.find()
        ? new FileRef(this.store, name.substring(0, m.start()))
        : this;
  }

  public Kind getKind() {
    Matcher m = EXTENSION_PATTERN.matcher(name);
    if (m.find()) {
      String extension = name.substring(m.start());
      for (Kind kind : Kind.values()) {
        if (extension.equals(kind.extension)) {
          return kind;
        }
      }
    }

    return Kind.OTHER;
  }

  /**
   * @return a new FileRef with an additional suffix. Note that the suffix
   * includes the dot.
   */
  public FileRef addSuffix(String suffix) {
    if (!SUFFIX_CHARS_PATTERN.matcher(suffix).matches()) {
      throw new IllegalArgumentException("Illegal characters in suffix,"
                                         + " \"" + suffix + "\".");
    }
    return new FileRef(this.store, name + suffix);
  }

  public String toFilename() {
    return store.toFilename(this);
  }

  public String toRelativeFilename() {
    return store.toRelativeFilename(this);
  }

  /**
   * Joins a path suffix onto this FileRef creating a new FileRef which is
   * effectively 'suffix' relative to 'this'.
   * eg: new FileRef("/a/b").join("/x/y") is new FileRef("/a/b/x/y")
   */
  public FileRef join(String suffix) {
    return new FileRef(this.store, this.name + "/" + suffix);
  }

  /**
   * @return whether this filename is an ancestor of the specified FileRef,
   * "that".
   */
  public boolean isAncestorOf(FileRef that) {
    return store.equals(that.store)
        && ("/".equals(name) || that.name.startsWith(name + "/"));
  }

  /**
   * @return a String representation of this FileRef for debugging purposes.
   */
  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "(\"" + store + ":" + name + "\")";
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(store, name);
  }

  @Override
  public boolean equals(Object that) {
    return this == that ||
        (that instanceof FileRef && this.equals((FileRef) that));
  }

  public boolean equals(FileRef that) {
    return this.store.equals(that.store) && this.name.equals(that.name);
  }

  private static final Pattern NORMAL_PATTERN =
      Pattern.compile("/([^/]+(/[^/]+)*)?");
  private static final Pattern MULTIPLE_SLASH_PATTERN =
      Pattern.compile("//+");

  private static String normalize(String name) {
    if (NORMAL_PATTERN.matcher(name).matches()) {
      // short-circuit if string already appears to be normalized
      return name;
    }

    String newName = MULTIPLE_SLASH_PATTERN.matcher("/" + name).replaceAll("/");
    if ((newName.length() > 1) && newName.endsWith("/")) {
      return newName.substring(0, newName.length() - 1);
    }
    return newName;
  }
}
