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

import com.google.common.base.Preconditions;

/**
 * A C++ File Import.  The internal implementation of
 * {@code <cpp:include file='file' />} which is equivilant to
 * {@code #include "file.h"}.
 */
public class CppFileImport extends Import {
  private final String fileName;

  public CppFileImport(Node fromNode, String fileName) {
    super(fromNode);
    this.fileName = Preconditions.checkNotNull(fileName);
  }

  public String getFileName() {
    return fileName;
  }

  @Override
  public <T> T acceptVisitor(ImportVisitor<T> visitor) {
    return visitor.visitCppFileImport(this);
  }

  @Override
  public String getTarget() {
    return String.format("\"%s.h\"", getFileName());
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof CppFileImport) && equals((CppFileImport)that);
  }

  public boolean equals(CppFileImport that) {
    return getFileName().equals(that.getFileName());
  }
}
