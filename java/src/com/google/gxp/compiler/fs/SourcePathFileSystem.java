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

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * A {@code FileSystem} wrapper that deals with the weird mappings between
 * source paths and the output dir in GXP. As a wrapper {@code
 * SourcePathFileSystem} necessarily have to deal with {@code Filenames} at two
 * different ({@code FileSystem}) levels: the "wrapped" {@code FileSystem} and
 * the {@code SourcePathFileSystem} itself.
 */
public class SourcePathFileSystem implements FileSystem {
  // These maps contain the (bi-directional) mapping for source files.
  private final BiMap<FileRef, FileRef> spFsToWrappedFsSourceFileRef = Maps.newHashBiMap();

  private final BiMap<FileRef, FileRef> wrappedFsToSpFsSourceFileRef =
      spFsToWrappedFsSourceFileRef.inverse();

  private final FileRef outDir;
  private final FileSystem wrappedFs;

  public SourcePathFileSystem(FileSystem wrappedFs,
                              Iterable<FileRef> sourcePath,
                              Iterable<FileRef> sourceFiles,
                              FileRef outDir) {
    this.outDir = outDir;
    this.wrappedFs = wrappedFs;
    List<FileRef> sortedSourcePath = Lists.sortedCopy(sourcePath, FILEREF_LENGTH_COMPARATOR);
    for (FileRef sourceFile : sourceFiles) {
      FileRef baseDir = findBaseDir(sourceFile, sortedSourcePath);
      if (baseDir == null) {
        throw new IllegalArgumentException(sourceFile.toFilename() + " not in source path");
      } else {
        spFsToWrappedFsSourceFileRef.put(chop(baseDir, sourceFile), sourceFile);
      }
    }
  }

  private static FileRef findBaseDir(FileRef sourceFile, List<FileRef> sourcePath) {
    for (FileRef sourceDir : sourcePath) {
      if (sourceDir.isAncestorOf(sourceFile)) {
        return sourceDir;
      }
    }
    return null;
  }

  private static final Comparator<FileRef> FILEREF_LENGTH_COMPARATOR = new Comparator<FileRef>() {
    public int compare(FileRef f1, FileRef f2) {
      int len1 = f1.getName().length();
      int len2 = f2.getName().length();
      return (len1 > len2) ? -1 : (len1 == len2 ? 0 : 1);
    }
  };

  private FileRef chop(FileRef ancestor, FileRef descendant) {
    return new FileRef(store, descendant.getName().substring(ancestor.getName().length()));
  }

  private final FileStore store = new FileStore() {
    public Charset getDefaultCharset() {
      return SourcePathFileSystem.this.getDefaultCharset();
    }

    public URI toUri(FileRef fileRef) {
      return toWrappedFsFileRef(fileRef, false).toUri();
    }

    public long getLastModified(FileRef fileRef) {
      return toWrappedFsFileRef(fileRef, false).getLastModified();
    }

    public String toFilename(FileRef fileRef) {
      return toWrappedFsFileRef(fileRef, false).toFilename();
    }

    public String toRelativeFilename(FileRef fileRef) {
      return toWrappedFsFileRef(fileRef, false).toRelativeFilename();
    }

    public InputStream openInputStream(FileRef fileRef) throws IOException {
      return toWrappedFsFileRef(fileRef, false).openInputStream();
    }

    public OutputStream openOutputStream(FileRef fileRef)
        throws IOException {
      FileRef wrappedFsFileRef = toWrappedFsFileRef(fileRef, true);
      if (wrappedFsFileRef == null) {
        throw new IOException("Attempted to open source file, "
                              + fileRef.toFilename() + ", for writing.");
      } else {
        return wrappedFsFileRef.openOutputStream();
      }
    }

    public boolean delete(FileRef fileRef) {
      return toWrappedFsFileRef(fileRef, false).delete();
    }
  };

  public FileRef getRoot() {
    return new FileRef(store, "/");
  }

  public FileRef parseFilename(String filename) {
    return FROM_WRAPPED_FS_FILEREF.apply(wrappedFs.parseFilename(filename));
  }

  public List<FileRef> parseFilenameList(String filenameList) {
    return Lists.transform(wrappedFs.parseFilenameList(filenameList),
                           FROM_WRAPPED_FS_FILEREF);
  }

  public Charset getDefaultCharset() {
    return wrappedFs.getDefaultCharset();
  }

  /**
   * Returns the set of source {@code FileRef}s relative to this
   * {@code SourcePathFileSystem}.
   */
  public Set<FileRef> getSourceFileRefs() {
    return Collections.unmodifiableSet(spFsToWrappedFsSourceFileRef.keySet());
  }

  private FileRef toWrappedFsFileRef(FileRef fnam, boolean forWriting) {
    FileRef result = spFsToWrappedFsSourceFileRef.get(fnam);
    if (result == null) {
      result = outDir.join(fnam.getName());
    } else if (forWriting) {
      return null;
    }
    return result;
  }

  private final Function<FileRef, FileRef> FROM_WRAPPED_FS_FILEREF
    = new Function<FileRef, FileRef>() {
      public FileRef apply(FileRef fnam) {
        FileRef result = wrappedFsToSpFsSourceFileRef.get(fnam);
        if (result == null) {
          // can't do outDir.isAncenstorOf(fnam) because FileSystems of fnam and outDir can differ
          if (outDir.getName().equals("/") || fnam.getName().startsWith(outDir.getName() + "/")) {
            return chop(outDir, fnam);
          } else {
            throw new IllegalArgumentException(fnam + " not in " + outDir + " or in source list");
          }
        }
        return result;
      }
    };
}
