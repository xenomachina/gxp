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
import com.google.common.base.Preconditions;

/**
 * Internal representation of a &lt;gxp:throws&gt; element.
 */
public class ThrowsDeclaration extends AbstractNode {
  private final String exceptionType;

  public ThrowsDeclaration(Node fromNode, String exceptionType) {
    super(fromNode);
    this.exceptionType = Preconditions.checkNotNull(exceptionType);
  }

  // TODO(laurence): Change this to a Type
  public String getExceptionType() {
    return exceptionType;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof ThrowsDeclaration
            && equals((ThrowsDeclaration) that));
  }

  public boolean equals(ThrowsDeclaration that) {
    return equalsAbstractNode(that)
        && Objects.equal(getExceptionType(), that.getExceptionType());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        abstractNodeHashCode(),
        getExceptionType());
  }
}
