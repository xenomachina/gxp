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

package com.google.gxp.compiler.alerts;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.gxp.compiler.fs.DummyFileSystem;
import com.google.gxp.compiler.fs.FileRef;

import java.io.Serializable;
import java.io.ObjectStreamException;

/**
 * Value object for representing a position in a source file. The line and
 * column postions can be left out. This is to support alerts that don't have a
 * specific location in a file.
 */
@SuppressWarnings("serial") //let java pick the serialVersionUID
public final class SourcePosition implements Serializable {
  private final FileRef source;
  private final int line;
  private final int column;

  /**
   * Creates a {@code SourcePosition} with unspecified line and column numbers.
   *
   * @param source the source file
   */
  public SourcePosition(FileRef source) {
    this.source = Preconditions.checkNotNull(source);
    this.line = 0;
    this.column = 0;
  }

  /**
   * @param source the source file
   * @param line the line number
   * @param column the column number
   */
  public SourcePosition(FileRef source, int line, int column) {
    this.source = Preconditions.checkNotNull(source);

    if (line < 1) {
      throw new IllegalArgumentException("line must be >= 1");
    }
    this.line = line;

    if (column < 1) {
      throw new IllegalArgumentException("column must be >= 1");
    }
    this.column = column;
  }

  /**
   * Creates a {@code SourcePosition} that is not actually based on a
   * {@code FileRef}.
   */
  public SourcePosition(String sourceName) {
    this(DummyFileSystem.INSTANCE.parseFilename(Preconditions.checkNotNull(sourceName)));
  }

  /**
   * Creates a {@code SourcePosition} that is not actually based on a
   * {@code FileRef}.
   */
  public SourcePosition(String sourceName, int line, int column) {
    this(DummyFileSystem.INSTANCE.parseFilename(Preconditions.checkNotNull(sourceName)),
                                                line, column);
  }

  public FileRef getSource() {
    return source;
  }

  public String getSourceName() {
    return source.toFilename();
  }

  /**
   * @return the line number of this {@code SourcePosition}, or 0 if
   * unspecified. The first line is number 1.
   */
  public int getLine() {
    return line;
  }

  /**
   * @return the column number of this {@code SourcePosition}, or 0 if
   * unspecified. The first column is number 1.
   */
  public int getColumn() {
    return column;
  }

  /**
   * Outputs position in a format that's compatible with the output of
   * {@code jikes +E} (the {@code jikes} documentation calls this
   * "emacs-form").
   */
  @Override
  public String toString() {
    // TODO(laurence): it would be nice to eventually report correct
    // end-positions. For now we just use the start position as the end
    // position.
    return getSource().toRelativeFilename()
        + ":" + line + ":" + column
        + ":" + line + ":" + column;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || ((that instanceof SourcePosition) && equals((SourcePosition) that));
  }

  // Any field referenced in this function must be represented in
  // SerializableSourcePosition
  private boolean equals(SourcePosition that) {
    return Objects.equal(source.toFilename(), that.source.toFilename())
        && line == that.line
        && column == that.column;
  }

  // Any field referenced in this function must be represented in
  // SerializableSourcePosition
  @Override
  public int hashCode() {
    return Objects.hashCode(
        source.toFilename(),
        line,
        column);
  }

  /**
   * Representation of the core of a source position: the name of the file and
   * the position in the file. Any field that is referenced in {@code equals()}
   * or {@code hashCode()} must be stored in this object so that it will be
   * serialized and deserialized.
   */
  private static class SerializableSourcePosition implements Serializable {
    private final int line;
    private final int column;
    private final String sourceName;

    private SerializableSourcePosition(String sourceName, int line, int column) {
      this.line = line;
      this.column = column;
      this.sourceName = sourceName;
    }

    public int getLine() {
      return line;
    }

    public int getColumn() {
      return column;
    }

    public String getSourceName() {
      return sourceName;
    }

    private Object readResolve() throws ObjectStreamException {
      return (line > 0 && column > 0)
          ? new SourcePosition(sourceName, line, column)
          : new SourcePosition(sourceName);
    }
  }

  private Object writeReplace() throws ObjectStreamException {
    return new SerializableSourcePosition(getSourceName(), line, column);
  }
}
