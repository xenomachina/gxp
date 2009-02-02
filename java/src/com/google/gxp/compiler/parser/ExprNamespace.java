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

import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.UnknownAttributeError;

import java.util.List;

/**
 * The http://google.com/2001/gxp/expressions (aka "expr:") namespace.
 */
public class ExprNamespace implements Namespace {
  private ExprNamespace(){}

  public String getUri() {
    return "http://google.com/2001/gxp/expressions";
  }

  public static final Namespace INSTANCE = new ExprNamespace();

  public ParsedElement createElement(AlertSink alertSink,
                                     SourcePosition sourcePosition,
                                     String displayName,
                                     String tagName,
                                     List<ParsedAttribute> attrs,
                                     List<ParsedElement> children) {
    // <expr:s /> -> <gxp:eval expr='s' />
    List<ParsedAttribute> newAttrs = Lists.newArrayList();
    for (ParsedAttribute attr : attrs) {
      if (attr.getName().equals("expr")) {
        alertSink.add(new UnknownAttributeError(displayName, sourcePosition,
                                                attr.getDisplayName()));
      } else {
        newAttrs.add(attr);
      }
    }
    newAttrs.add(new ParsedAttribute(sourcePosition, NullNamespace.INSTANCE, "expr",
                                     tagName, "expr"));

    return new GxpNamespace.GxpElement(sourcePosition, displayName, newAttrs,
                                       children, GxpNamespace.ElementType.EVAL);
  }

  public <T> T acceptVisitor(NamespaceVisitor<T> visitor) {
    return visitor.visitExprNamespace(this);
  }
}
