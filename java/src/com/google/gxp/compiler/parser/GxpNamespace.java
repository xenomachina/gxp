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

import java.util.*;

/**
 * Implementation of the http://google.com/2001/gxp (aka "gxp:") namespace.
 */
public class GxpNamespace implements Namespace {
  private GxpNamespace(){}

  public String getUri() {
    return "http://google.com/2001/gxp";
  }

  public static final Namespace INSTANCE = new GxpNamespace();

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
      return new GxpElement(sourcePosition, displayName, attrs, children, type);
    }
  }

  public static class GxpElement extends ParsedElement {
    private final ElementType elementType;

    public GxpElement(SourcePosition sourcePostion,
                      String displayName,
                      List<ParsedAttribute> attrs,
                      List<? extends ParsedElement> children,
                      ElementType elementType) {
      super(sourcePostion, displayName, attrs, children);
      this.elementType = elementType;
    }

    public ElementType getElementType() {
      return elementType;
    }

    @Override
    public boolean canBeRoot() {
      return elementType.canBeRoot();
    }

    @Override
    public <T> T acceptVisitor(ParsedElementVisitor<T> visitor) {
      return elementType.acceptVisitor(visitor, this);
    }

    @Override
    protected GxpElement withChildrenImpl(List<ParsedElement> children) {
      return new GxpElement(getSourcePosition(), getDisplayName(),
                            getAttributes(), children, elementType);
    }
  }

  /**
   * Enum of all of the element types. Note that the element's XML name is the
   * enum value's name lowercased.
   */
  public static enum ElementType {
    ABBR {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitAbbrElement(element);
      }
    },
    ATTR {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitAttrElement(element);
      }
    },
    CLAUSE {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitClauseElement(element);
      }
    },
    COND {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitCondElement(element);
      }
    },
    CONSTRUCTOR {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitConstructorElement(element);
      }
    },
    ELIF {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitElifElement(element);
      }
    },
    ELSE {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitElseElement(element);
      }
    },
    EPH {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitEPHElement(element);
      }
    },
    EVAL {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitEvalElement(element);
      }
    },
    IF {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitIfElement(element);
      }
    },
    IMPLEMENTS {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitImplementsElement(element);
      }
    },
    IMPORT {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitImportElement(element);
      }
    },
    INTERFACE(true) {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitInterfaceElement(element);
      }
    },
    LOOP {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitLoopElement(element);
      }
    },
    MSG {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitMsgElement(element);
      }
    },
    NOMSG {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitNoMsgElement(element);
      }
    },
    PARAM {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitParamElement(element);
      }
    },
    PH {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitPHElement(element);
      }
    },
    TEMPLATE(true) {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitTemplateElement(element);
      }
    },
    THROWS {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitThrowsElement(element);
      }
    },
    TYPEPARAM {
      @Override
      <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element) {
        return visitor.visitTypeParamElement(element);
      }
    };

    private final boolean canBeRoot;

    private ElementType() {
      this(false);
    }

    private ElementType(boolean canBeRoot) {
      this.canBeRoot = canBeRoot;
    }

    boolean canBeRoot() {
      return canBeRoot;
    }

    abstract <T> T acceptVisitor(ParsedElementVisitor<T> visitor, GxpElement element);
  }

  private static final Map<String, ElementType> ELEMENTS = initElements();

  private static Map<String, ElementType> initElements() {
    ImmutableMap.Builder<String, ElementType> builder = ImmutableMap.builder();
    for (ElementType type : ElementType.values()) {
      builder.put(type.name().toLowerCase().replace('_', '-'), type);
    }
    return builder.build();
  }

  public <T> T acceptVisitor(NamespaceVisitor<T> visitor) {
    return visitor.visitGxpNamespace(this);
  }
}
