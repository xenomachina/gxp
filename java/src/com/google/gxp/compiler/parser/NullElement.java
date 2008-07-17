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

import com.google.gxp.compiler.base.Node;

import java.util.*;

/**
 * A Null {@code ParsedElement}. Only inserted into the tree by
 * {@code IfExpander} when a out of place node is detected.  Ignored by
 * {@code Reparenter}.
 */
public class NullElement extends ParsedElement {
  public NullElement(Node node) {
    super(node.getSourcePosition(), node.getDisplayName(),
          Collections.<ParsedAttribute>emptyList(),
          Collections.<ParsedElement>emptyList());
  }

  public <T> T acceptVisitor(ParsedElementVisitor<T> visitor) {
    return visitor.visitNullElement(this);
  }

  @Override
  protected ParsedElement withChildrenImpl(List<ParsedElement> children) {
    throw new UnsupportedOperationException();
  }
}
