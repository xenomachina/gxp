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

import java.util.List;

/**
 * The http://google.com/2001/gxp/msg (aka "msg:") namespace. Can only
 * be used for attributes, not elements.
 */
public class MsgNamespace implements Namespace {
  private MsgNamespace(){}

  public String getUri() {
    return "http://google.com/2001/gxp/msg";
  }

  public static final Namespace INSTANCE = new MsgNamespace();

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
    return visitor.visitMsgNamespace(this);
  }
}
