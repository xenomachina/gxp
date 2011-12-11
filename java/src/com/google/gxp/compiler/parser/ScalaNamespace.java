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
import com.google.gxp.compiler.base.OutputLanguage;

import java.util.*;

/**
 * The http://google.com/2001/gxp/scala (aka "scala:") namespace. Can only
 * be used for attributes, not elements.
 */
public class ScalaNamespace implements OutputLanguageNamespace {
  private ScalaNamespace(){}

  public String getUri() {
    return "http://google.com/2001/gxp/code/scala";
  }

  public static final OutputLanguageNamespace INSTANCE = new ScalaNamespace();

  public ParsedElement createElement(AlertSink alertSink,
                                     SourcePosition sourcePosition,
                                     String displayName,
                                     String tagName,
                                     List<ParsedAttribute> attrs,
                                     List<ParsedElement> children) {
    alertSink.add(new UnknownElementError(sourcePosition, this, displayName));
    return null;
  }

  public <T> T acceptVisitor(NamespaceVisitor<T> visitor) {
    return visitor.visitScalaNamespace(this);
  }

  public OutputLanguage getOutputLanguage() {
    return OutputLanguage.SCALA;
  }
}
