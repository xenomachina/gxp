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

/**
 * Thrown by {@code NodeVisitor}s upon encountering {@code Node}s that should
 * exist where they're visiting.
 */
public class UnexpectedNodeException extends IllegalArgumentException {
  private static final long serialVersionUID = -1;

  public UnexpectedNodeException(Node node) {
    super("Found " + node.getClass().getCanonicalName()
          + " (\"" + node.getDisplayName()
          + "\") in tree where it should not exist ("
          + node.getSourcePosition() + ").");
  }
}
