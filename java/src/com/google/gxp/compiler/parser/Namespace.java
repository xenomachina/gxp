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
 * Represents an XML namespace.
 */
public interface Namespace {
  /**
   * Creates a Parsed element.
   *
  * @param alertSink used for reporting {@code Alert}s
  * @param sourcePosition the position of the new {@code ParamElement}
  * @param displayName the display name of the tag
  * @param tagName the (local) name of the tag
  * @param attrs the element's attributes
  * @param children the element's children
  */
  ParsedElement createElement(AlertSink alertSink,
                              SourcePosition sourcePosition,
                              String displayName,
                              String tagName,
                              List<ParsedAttribute> attrs,
                              List<ParsedElement> children);

  /**
   * @return the URI of this {@code Namespace}, as would be used in an {@code
   * xmlns} attribute.
   */
  String getUri();

  /**
   * Visitor pattern.
   */
  <T> T acceptVisitor(NamespaceVisitor<T> visitor);
}
