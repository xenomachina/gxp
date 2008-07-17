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

package com.google.gxp.compiler.schema;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.*;
import java.util.*;

/**
 * Helper class for building {@code ElementValidator}s.
 */
class ElementBuilder {
  private final String name;
  private final Set<ElementValidator.Flag> flags;
  private final String contentType;
  private final Set<DocType> docTypes;
  private final Map<String, AttributeElement> attrs = Maps.newHashMap();

  public String getName() {
    return name;
  }

  ElementBuilder(String name, Set<ElementValidator.Flag> flags,
                 String contentType, Set<DocType> docTypes) {
    this.name = name;
    this.flags = ImmutableSet.copyOf(flags);
    this.contentType = contentType;
    this.docTypes = ImmutableSet.copyOf(docTypes);
  }

  public void add(AttributeElement attribute) {
    if (attrs.get(attribute.getName()) != null) {
      throw new IllegalStateException("Duplicate attributes called "
                                      + attribute.getName() + " in <"
                                      + getName() + ">");
    } else {
      attrs.put(attribute.getName(), attribute);
    }
  }

  ElementValidator build() {
    List<AttributeValidator> attrValidators = Lists.newArrayList();
    for (AttributeElement attr : attrs.values()) {
      attrValidators.add(attr.getAttributeValidator());
    }
    return new ElementValidator(name, flags, contentType, docTypes,
                                attrValidators);
  }
}
