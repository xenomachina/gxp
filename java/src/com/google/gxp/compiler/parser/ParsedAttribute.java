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

package com.google.gxp.compiler.parser;

import com.google.common.base.Objects;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.AbstractNode;

/**
 * Represents an XML attribute.
 */
public final class ParsedAttribute extends AbstractNode {
  private final Namespace namespace;
  private final String name;
  private final String value;

  /**
   * @return the Namespace of this attribute, or null if it's a local
   * attribute.
   */
  public Namespace getNamespace() {
    return namespace;
  }

  /**
   * @return the (local) name of this attribute.
   */
  public String getName() {
    return name;
  }

  /**
   * @return the value of this attribute.
   */
  public String getValue() {
    return value;
  }

  public ParsedAttribute(SourcePosition sourcePosition,
                         Namespace namespace, String name, String value,
                         String qualifiedName) {
    super(sourcePosition, "'" + qualifiedName + "' attribute");

    this.namespace = Objects.nonNull(namespace);

    if (name.length() < 1) {
      throw new IllegalArgumentException();
    }
    this.name = name;

    this.value = Objects.nonNull(value);

    if (qualifiedName.length() < name.length()) {
      throw new IllegalArgumentException();
    }
  }
}
