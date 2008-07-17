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
 * A GXP formal type parameter. Corresponds to a &lt;gxp:typeparam&gt; element.
 */
public class FormalTypeParameter extends AbstractNode {
  private final String name;
  private final NativeType extendsType; // TODO(laurence): add non-Java support

  public FormalTypeParameter(Node fromNode, String name,
                             NativeType extendsType) {
    super(fromNode);
    this.name = Objects.nonNull(name);
    this.extendsType = extendsType;
  }

  public String getName() {
    return name;
  }

  public NativeType getExtendsType() {
    return extendsType;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof FormalTypeParameter
            && equals((FormalTypeParameter) that));
  }

  public boolean equals(FormalTypeParameter that) {
    return equalsAbstractNode(that)
        && Objects.equal(getName(), that.getName())
        && Objects.equal(getExtendsType(), that.getExtendsType());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        abstractNodeHashCode(),
        getName(),
        getExtendsType());
  }
}
