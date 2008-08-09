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

import com.google.common.collect.ImmutableMap;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.OutputLanguage;

import java.util.*;

/**
 * The http://google.com/2001/gxp/cpp (aka "cpp:") namespace.
 */
public class CppNamespace implements OutputLanguageNamespace {
  private CppNamespace(){}

  public String getUri() {
    return "http://google.com/2001/gxp/code/cpp";
  }

  public static final OutputLanguageNamespace INSTANCE = new CppNamespace();

  public ParsedElement createElement(AlertSink alertSink,
                                     SourcePosition sourcePosition,
                                     String displayName,
                                     String tagName,
                                     List<ParsedAttribute> attrs,
                                     List<ParsedElement> children) {
    ElementType type = ELEMENTS.get(tagName);
    if (type == null) {
      alertSink.add(new UnknownElementError(sourcePosition, this, displayName));
      return null;
    } else {
      return new CppElement(sourcePosition, displayName, attrs, children, type);
    }
  }

  public static class CppElement extends ParsedElement {
    private final ElementType elementType;

    public CppElement(SourcePosition sourcePostion,
                      String displayName,
                      List<ParsedAttribute> attrs,
                      List<? extends ParsedElement> children,
                      ElementType elementType) {
      super(sourcePostion, displayName, attrs, children);
      this.elementType = elementType;
    }

    @Override
    public <T> T acceptVisitor(ParsedElementVisitor<T> visitor) {
      return elementType.acceptVisitor(visitor, this);
    }

    @Override
    protected CppElement withChildrenImpl(List<ParsedElement> children) {
      return new CppElement(getSourcePosition(), getDisplayName(),
                            getAttributes(), children, elementType);
    }
  }

  private static enum ElementType {
    INCLUDE {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, CppElement element) {
        return visitor.visitCppIncludeElement(element);
      }
    };

    abstract <T> T acceptVisitor(ParsedElementVisitor<T> visitor, CppElement element);
  }

  private static final Map<String, ElementType> ELEMENTS = initElements();

  private static Map<String, ElementType> initElements() {
    ImmutableMap.Builder<String, ElementType> builder = ImmutableMap.builder();
    for (ElementType type : ElementType.values()) {
      builder.put(type.name().toLowerCase(), type);
    }
    return builder.build();
  }

  public <T> T acceptVisitor(NamespaceVisitor<T> visitor) {
    return visitor.visitCppNamespace(this);
  }

  public OutputLanguage getOutputLanguage() {
    return OutputLanguage.CPP;
  }
}
