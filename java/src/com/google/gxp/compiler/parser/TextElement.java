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

import java.util.*;

/**
 * A Text {@code ParsedElement}.  Represents a constant string in the
 * parse tree.
 */
public class TextElement extends ParsedElement {
  private final String text;

  public TextElement(SourcePosition pos, String text) {
    super(pos, "text",
          Collections.<ParsedAttribute>emptyList(),
          Collections.<ParsedElement>emptyList());
    this.text = Objects.nonNull(text);
  }

  public <T> T acceptVisitor(ParsedElementVisitor<T> visitor) {
    return visitor.visitTextElement(this);
  }

  @Override
  protected ParsedElement withChildrenImpl(List<ParsedElement> children) {
    throw new UnsupportedOperationException();
  }

  public String getText() {
    return text;
  }
}
