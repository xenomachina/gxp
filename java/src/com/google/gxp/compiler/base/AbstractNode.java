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

import com.google.gxp.compiler.alerts.SourcePosition;

/**
 * Straightforward abstract implementation of {@link Node}.
 */
public abstract class AbstractNode implements Node {
  private final SerializableAbstractNode delegate;

  /**
   * @param sourcePosition the {@link SourcePosition} of this {@code Node}
   * @param displayName the display name of this {@code Node}
   */
  protected AbstractNode(SourcePosition sourcePosition, String displayName) {
    delegate = new SerializableAbstractNode(sourcePosition, displayName);
  }

  /**
   * Creates an {@code AbstractNode} based on another Node.
   *
   * @param fromNode the {@code Node} that this {@code Node} is derived from
   */
  protected AbstractNode(Node fromNode) {
    delegate = new SerializableAbstractNode(fromNode);
  }

  public SourcePosition getSourcePosition() {
    return delegate.getSourcePosition();
  }

  public final String getDisplayName() {
    return delegate.getDisplayName();
  }

  protected final boolean equalsAbstractNode(AbstractNode that) {
    return delegate.equalsAbstractNode(that.delegate);
  }

  protected final int abstractNodeHashCode() {
    return delegate.abstractNodeHashCode();
  }
}
