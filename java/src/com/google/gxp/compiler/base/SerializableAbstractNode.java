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

package com.google.gxp.compiler.base;

import com.google.common.base.Objects;
import com.google.gxp.compiler.alerts.SourcePosition;

import java.io.Serializable;

/**
 * Straightforward serializable implementation of {@link Node}.
 */
@SuppressWarnings("serial") // let java pick the serialVersionUID
public class SerializableAbstractNode implements Node, Serializable {
  private final SourcePosition sourcePosition;
  private final String displayName;

  /**
   * @param sourcePosition the {@link SourcePosition} of this {@code Node}
   * @param displayName the display name of this {@code Node}
   */
  protected SerializableAbstractNode(SourcePosition sourcePosition, String displayName) {
    this.sourcePosition = Objects.nonNull(sourcePosition);
    this.displayName = Objects.nonNull(displayName);
  }

  /**
   * Creates an {@code SerializableAbstractNode} based on another Node.
   *
   * @param fromNode the {@code Node} that this {@code Node} is derived from
   */
  protected SerializableAbstractNode(Node fromNode) {
    this(fromNode.getSourcePosition(), fromNode.getDisplayName());
  }

  public SourcePosition getSourcePosition() {
    return sourcePosition;
  }

  public final String getDisplayName() {
    return displayName;
  }

  protected final boolean equalsAbstractNode(SerializableAbstractNode that) {
    return Objects.equal(getSourcePosition(), that.getSourcePosition())
        && Objects.equal(getDisplayName(), that.getDisplayName());
  }

  protected final int abstractNodeHashCode() {
    return Objects.hashCode(
        getSourcePosition(),
        getDisplayName());
  }
}
