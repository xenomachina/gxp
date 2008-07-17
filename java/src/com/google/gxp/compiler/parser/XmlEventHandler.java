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

import java.io.*;
import java.util.*;

/**
 * Abstract event handler for handling XML parse events. Note that these
 * methods are based on the methods in {@link org.xml.sax.ContentHandler
 * ContentHandler}, {@link org.xml.sax.DTDHandler DTDHandler}, and {@link
 * org.xml.sax.EntityResolver EntityResolver}. The primary difference is that
 * {@code SourcePosition}s are passed to applicable events as parameters and
 * the {@link AlertSink} methods take the place of the methods from {@link
 * org.xml.sax.ErrorHandler ErrorHandler}.
 */
interface XmlEventHandler extends AlertSink {
  void startPrefixMapping(SourcePosition sourcePosition, String prefix,
                          String uri);

  void endPrefixMapping(SourcePosition sourcePosition, String prefix);

  void startElement(SourcePosition sourcePosition, String nsUri,
                    String localName, String qName,
                    Iterable<? extends ParsedAttribute> attrs);

  void endElement(SourcePosition sourcePosition, String nsUri,
                  String localName, String qName);

  void characters(SourcePosition sourcePosition, char[] chars,
                  int start, int length);

  void processingInstruction(SourcePosition sourcePosition,
                             String target, String data);

  void skippedEntity(SourcePosition sourcePosition, String name);

  void notationDecl(SourcePosition sourcePosition, String name,
                    String publicId, String systemId);

  void unparsedEntityDecl(SourcePosition sourcePosition, String name,
                          String publicId, String systemId,
                          String notationName);

  InputStream resolveEntity(SourcePosition sourcePosition, String publicId,
                            String systemId) throws IOException;

  ParsedAttribute parseAttribute(SourcePosition sourcePosition, String nsUri,
                                 String name, String value, String qName);
}
