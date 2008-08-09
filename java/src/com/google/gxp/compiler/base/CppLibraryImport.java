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

/**
 * A C++ Library Import. The internal implementation of
 * {@code <cpp:include library='library' /> which is equivliant to
 * {@code #include <library>}.
 */
public class CppLibraryImport extends Import {
  private final String libraryName;

  public CppLibraryImport(Node fromNode, String libraryName) {
    super(fromNode);
    this.libraryName = Objects.nonNull(libraryName);
  }

  public String getLibraryName() {
    return libraryName;
  }

  @Override
  public <T> T acceptVisitor(ImportVisitor<T> visitor) {
    return visitor.visitCppLibraryImport(this);
  }

  @Override
  public String getTarget() {
    return "<" + getLibraryName() + ">";
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof CppLibraryImport) && equals((CppLibraryImport)that);
  }

  public boolean equals(CppLibraryImport that) {
    return getLibraryName().equals(that.getLibraryName());
  }
}
