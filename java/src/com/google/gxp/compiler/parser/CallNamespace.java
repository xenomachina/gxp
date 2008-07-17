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

import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;

import java.util.*;

/**
 * Abstract CallNamespace
 */
public abstract class CallNamespace implements Namespace {
  protected CallNamespace(){}

  public ParsedElement createElement(AlertSink alertSink,
                                     SourcePosition sourcePosition,
                                     String displayName,
                                     String tagName,
                                     List<ParsedAttribute> attrs,
                                     List<ParsedElement> children) {
    return new CallElement(sourcePosition, displayName,
                           getTagName(tagName),
                           attrs, areAllAttrsExpr(), children);
  }

  public abstract String getUri();

  protected abstract String getTagName(String tagName);

  protected abstract boolean areAllAttrsExpr();

  /**
   * Parsed representation of a {@code <call:*>} element.
   */
  public static class CallElement extends ParsedElement {
    private final String tagName;
    private final boolean allAttrsAreExpr;

    public String getTagName() {
      return tagName;
    }

    public boolean allAttrsAreExpr() {
      return allAttrsAreExpr;
    }

    public CallElement(SourcePosition sourcePosition,
                       String displayName,
                       String tagName,
                       List<ParsedAttribute> attrs,
                       boolean allAttrsAreExpr,
                       List<ParsedElement> children) {
      super(sourcePosition, displayName, attrs, children);
      this.tagName = tagName;
      this.allAttrsAreExpr = allAttrsAreExpr;
    }

    public <T> T acceptVisitor(ParsedElementVisitor<T> visitor) {
      return visitor.visitCallElement(this);
    }

    @Override
    protected CallElement withChildrenImpl(List<ParsedElement> children) {
      return new CallElement(getSourcePosition(), getDisplayName(), tagName,
                             getAttributes(), allAttrsAreExpr, children);
    }
  }

  public <T> T acceptVisitor(NamespaceVisitor<T> visitor) {
    return visitor.visitCallNamespace(this);
  }
}
