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
import com.google.gxp.compiler.fs.FileRef;

/**
 * An Alert indicates something that has (potentially) gone wrong, along with
 * the location (if any) where it went wrong.
 *
 * <p>Like {@link java.lang.Throwable}, Alert is meant to be subclassed for
 * each type of "alert". Subclasses may either subclass {@code Alert} directly,
 * or may extend one of {@link InfoAlert}, {@link WarningAlert} or
 * {@link ErrorAlert} if their {@code defaultSeverity} is invariant..
 */
public abstract class Alert {

  /**
   * Variety of alert severities.
   */
  public enum Severity {
    /**
     * Indicates that the {@code Alert} is merely informative, and does not
     * suggest that there is any kind of problem.
     */
    INFO,

    /**
     * Indicates a possible programming error, or otherwise "suspicious"
     * situation, but which isn't severe enough to be considered an error.
     */
    WARNING,

    /**
     * Indicates an error condition: something is definitely wrong.
     */
    ERROR
  }

  private final SourcePosition sourcePosition;
  private final String message;
  private final Severity defaultSeverity;

  // Package private, because all direct subclasses are in this package.
  public Alert(SourcePosition sourcePosition, Severity defaultSeverity,
               String message) {
    this.sourcePosition = Objects.nonNull(
        sourcePosition, "sourcePosition is null");
    this.defaultSeverity = Objects.nonNull(
        defaultSeverity, "defaultSeverity is null");
    this.message = Objects.nonNull(message, "alert message is null");
  }

  /**
   * @return the {@link FileRef} that this {@code Alert} pertains to.
   */
  public final FileRef getSource() {
    return sourcePosition.getSource();
  }

  /**
   * @return the SourcePosition this Alert took place at.
   */
  public final SourcePosition getSourcePosition() {
    return sourcePosition;
  }

  /**
   * @return English plaintext message explaining what went wrong.
   */
  public final String getMessage() {
    return message;
  }

  /**
   * Returns the default severity of this {@code Alert}. Note that you should
   * generally use an {@link AlertPolicy} to determine the actual severity of
   * an {@code Alert}.
   */
  public final Severity getDefaultSeverity() {
    return defaultSeverity;
  }

  /**
   * @return human-readable representation of this Alert.
   */
  @Override
  public final String toString() {
    return sourcePosition.toString() + ": " + getMessage();
  }

  @Override
  public final boolean equals(Object that) {
    return this == that
        || ((that instanceof Alert) && equals((Alert) that));
  }

  private boolean equals(Alert that) {
    return getSourcePosition().equals(that.getSourcePosition())
        && getMessage().equals(that.getMessage())
        && getDefaultSeverity().equals(that.getDefaultSeverity());
  }

  @Override
  public final int hashCode() {
    return Objects.hashCode(
        getSourcePosition(),
        getMessage(),
        getDefaultSeverity());
  }
}
