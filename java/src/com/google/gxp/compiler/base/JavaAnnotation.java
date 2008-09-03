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
import com.google.gxp.compiler.alerts.SourcePosition;

/**
 * A {@link Node} representing a Java Annotation.
 */
public class JavaAnnotation extends AbstractNode {
  private final Element element;
  private final String with;

  public enum Element {
    CLASS,
    CONSTRUCTOR,
    INSTANCE,
    INTERFACE,
    PARAM;
  }

  public JavaAnnotation(SourcePosition sourcePosition,
                        String displayName,
                        Element element,
                        String with) {
    super(sourcePosition, displayName);
    this.element = element;
    this.with = Preconditions.checkNotNull(with);
  }

  public JavaAnnotation(Node fromNode,
                        Element element,
                        String with) {
    this(fromNode.getSourcePosition(), fromNode.getDisplayName(), element, with);
  }

  public Element getElement() {
    return element;
  }

  public String getWith() {
    return with;
  }

  public JavaAnnotation withElement(Element newElement) {
    return Objects.equal(element, newElement)
        ? this
        : new JavaAnnotation(this, newElement, getWith());
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof JavaAnnotation && equals((JavaAnnotation)that));
  }

  public boolean equals(JavaAnnotation that) {
    return equalsAbstractNode(that)
        && Objects.equal(getElement(), that.getElement())
        && Objects.equal(getWith(), that.getWith());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        abstractNodeHashCode(),
        getElement(),
        getWith());
  }
}
