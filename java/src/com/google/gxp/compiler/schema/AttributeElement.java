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

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Helper class for representing an attribute element parsed from a schema.
 */
class AttributeElement {
  private final AttributeValidator attrValidator;
  private final String contentType;
  private final ImmutableSet<String> elementNames;
  private final ImmutableSet<String> exceptElementNames;

  public String getName() {
    return getAttributeValidator().getName();
  }

  AttributeElement(String name,
                   String contentType, Pattern pattern,
                   Set<AttributeValidator.Flag> flags,
                   String defaultValue, String example,
                   Set<String> elementNames,
                   Set<String> exceptElementNames) {
    this.attrValidator = new AttributeValidator(name, contentType, pattern,
                                                flags, defaultValue, example);
    this.contentType = contentType;
    this.elementNames = ImmutableSet.copyOf(elementNames);
    this.exceptElementNames = ImmutableSet.copyOf(exceptElementNames);
  }

  String getContentType() {
    return contentType;
  }

  Set<String> getElementNames() {
    return elementNames;
  }

  Set<String> getExceptElementNames() {
    return exceptElementNames;
  }

  AttributeValidator getAttributeValidator() {
    return attrValidator;
  }
}
