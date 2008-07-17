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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.AbstractNode;

import java.util.List;

/**
 * Represents an XML element in the parse tree.
 */
public abstract class ParsedElement extends AbstractNode {
  private final ImmutableList<ParsedAttribute> attrs;
  private final ImmutableList<ParsedElement> children;

  protected ParsedElement(SourcePosition sourcePosition,
                          String displayName,
                          List<ParsedAttribute> attrs,
                          List<? extends ParsedElement> children) {
    super(sourcePosition, displayName);
    this.attrs = ImmutableList.copyOf(attrs);
    this.children = ImmutableList.copyOf(children);
  }

  public List<ParsedAttribute> getAttributes() {
    return attrs;
  }

  public List<ParsedElement> getChildren() {
    return children;
  }

  /**
   * Indicates if the element is allowed to be the root element of a GXP
   * document.
   */
  public boolean canBeRoot() {
    return false;
  }

  /**
   * Accepts the specified {@code ParsedElementVisitor} and returns the result
   * of the visit.
   */
  public abstract <T> T acceptVisitor(ParsedElementVisitor<T> visitor);

  /**
   * <p>This incomplete implementation checks to see if the chidren are the
   * same as this {@code ParsedElement}'s children, and if so returns this.
   * Otherwise it delegates to the {@code withChildrenImpl} method.
   */
  public final ParsedElement withChildren(List<ParsedElement> newChildren) {
    return ((children == newChildren) || Iterables.elementsEqual(children, newChildren))
        ? this
        : withChildrenImpl(newChildren);
  }

  /**
   * Underlying implementation for the slow path of the {@code withChildren}
   * method. Subclasses should override this so that it creates a new {@code
   * ParsedElement} that's identical to themselves except for having the
   * specified children.
   */
  protected abstract ParsedElement withChildrenImpl(List<ParsedElement> children);
}
