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
import com.google.gxp.compiler.parser.Namespace;
import com.google.gxp.compiler.parser.NullNamespace;

/**
 * The (name-space-qualified) name of an XML attribute.
 */
public class AttributeName {
  private final Namespace ns;
  private final String localName;

  public AttributeName(String localName) {
    this(NullNamespace.INSTANCE, localName);
  }

  public AttributeName(Namespace ns, String localName) {
    this.ns = Preconditions.checkNotNull(ns);
    this.localName = Preconditions.checkNotNull(localName);
  }

  public Namespace getNamespace() {
    return ns;
  }

  public String getLocalName() {
    return localName;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof AttributeName && equals((AttributeName) that));
  }

  public boolean equals(AttributeName that) {
    return Objects.equal(getNamespace(), that.getNamespace())
        && Objects.equal(getLocalName(), that.getLocalName());
  }

  public int hashCode() {
    return Objects.hashCode(
        getNamespace(),
        getLocalName());
  }
}
