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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.alerts.Alert.Severity;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.SaxAlert;
import com.google.gxp.compiler.fs.FileRef;

import org.apache.xerces.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import java.io.*;
import java.util.*;

/**
 * {@code XmlParser} which uses SAX.
 */
public class SaxXmlParser implements XmlParser {
  private SaxXmlParser() {}

  public static final XmlParser INSTANCE = new SaxXmlParser();

  public void parse(FileRef input, XmlEventHandler eventHandler)
      throws IOException {
    InputStream inputStream = input.openInputStream();
    try {
      InputSource inputSource = new InputSource(inputStream);
      SaxEventAdapter saxEventAdapter = new SaxEventAdapter(input, eventHandler);
      try {
        XMLReader xmlReader = new SAXParser();
        xmlReader.setContentHandler(saxEventAdapter);
        xmlReader.setDTDHandler(saxEventAdapter);
        xmlReader.setEntityResolver(saxEventAdapter);
        xmlReader.setErrorHandler(saxEventAdapter);

        // Enable full namespace handling.
        xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        xmlReader.parse(inputSource);
      } catch (SAXException saxException) {
        if (!saxEventAdapter.getExceptionMessages().contains(saxException.getMessage())) {
          eventHandler.add(new SaxAlert(new SourcePosition(input),
                                        Severity.ERROR, saxException));
        }
      }
    } finally {
      inputStream.close();
    }
  }

  /**
   * Event handler for SAX XML parsing. Adapts SAX events into the form
   * expected by {@code XmlEventHandler}.
   *
   * Each instance of this class should be used only once as parsing state is
   * maintained in the object itself.
   */
  private static class SaxEventAdapter
      implements ContentHandler, DTDHandler, EntityResolver, ErrorHandler {
    // settings
    private final FileRef input;
    private final XmlEventHandler eventHandler;

    // intermediate state
    private Locator saxLocator = null;
    private boolean started = false;
    private boolean ended = false;

    // keep track of exception messages we see.
    // Unfortunately the XMLReader generates entirely different exceptions
    // when it calls the error handler and when it throws the exception, so
    // we have to examine the messages for equality instead of the exceptions
    private final Set<String> exceptionMessages = Sets.newHashSet();

    SaxEventAdapter(FileRef input, XmlEventHandler eventHandler) {
      this.input = Preconditions.checkNotNull(input);
      this.eventHandler = Preconditions.checkNotNull(eventHandler);
    }

    /** Implements {@code ContentHandler}. */
    public void setDocumentLocator(Locator locator) {
      saxLocator = locator;
      recordPosition();
    }

    /** Implements {@code ContentHandler}. */
    public void startDocument() throws SAXException {
      try {
        if (started) {
          throw new AssertionError("startDocument called multiple times?!");
        }
        started = true;
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code ContentHandler}. */
    public void endDocument() throws SAXException {
      try {
        if (!started) {
          throw new AssertionError("endDocument without startDocument?!");
        } else if (ended) {
          throw new AssertionError("endDocument called multiple times?!");
        }
        ended = true;
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code ContentHandler}. */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
      try {
        eventHandler.startPrefixMapping(getSourcePosition(), prefix, uri);
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code ContentHandler}. */
    public void endPrefixMapping(String prefix) throws SAXException {
      try {
        eventHandler.endPrefixMapping(getSourcePosition(), prefix);
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code ContentHandler}. */
    public void startElement(String nsUri, String localName, String qName,
                             Attributes saxAttrs) throws SAXException {
      try {
        int attrCount = saxAttrs.getLength();
        List<ParsedAttribute> attrs = Lists.newArrayListWithExpectedSize(attrCount);
        for (int i = 0; i < attrCount; i++) {
          // XXX Annoyingly, SAX doesn't tell us the attribute's position, so
          // we fudge it and use the containing element's position.
          ParsedAttribute attr = parseAttribute(getSourcePosition(), saxAttrs,
                                                i);
          if (attr != null) {
            attrs.add(attr);
          }
        }
        eventHandler.startElement(getSourcePosition(), nsUri, localName, qName,
                                  attrs);
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code ContentHandler}. */
    public void endElement(String nsUri, String localName, String qName)
        throws SAXException {
      try {
        eventHandler.endElement(getSourcePosition(), nsUri, localName, qName);
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code ContentHandler}. */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
      try {
        eventHandler.characters(getSourcePosition(), ch, start, length);
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code ContentHandler}. */
    public void ignorableWhitespace(char ch[], int start, int length)
        throws SAXException {
      try {
        eventHandler.characters(getSourcePosition(), ch, start, length);
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code ContentHandler}. */
    public void processingInstruction(String target, String data)
        throws SAXException {
      try {
        eventHandler.processingInstruction(getSourcePosition(), target, data);
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code ContentHandler}. */
    public void skippedEntity(String name) throws SAXException {
      try {
        eventHandler.skippedEntity(getSourcePosition(), name);
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code DTDHandler}. */
    public void notationDecl(String name,
                             String publicId,
                             String systemId)
        throws SAXException {
      try {
        eventHandler.notationDecl(getSourcePosition(), name, publicId,
                                  systemId);
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code DTDHandler}. */
    public void unparsedEntityDecl(String name,
                                   String publicId,
                                   String systemId,
                                   String notationName)
        throws SAXException {
      try {
        eventHandler.unparsedEntityDecl(getSourcePosition(), name, publicId,
                                        systemId, notationName);
      } finally {
        recordPosition();
      }
    }

    /**
     * Implements {@code EntityResolver}.
     */
    public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException, IOException {
      try {
        InputStream stream = eventHandler.resolveEntity(getSourcePosition(),
                                                        publicId, systemId);
        InputSource result = new InputSource(stream);
        result.setPublicId(publicId);
        result.setSystemId(systemId);
        return result;
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code ErrorHandler}. */
    public void warning(SAXParseException exception)
        throws SAXException {
      try {
        eventHandler.add(new SaxAlert(getSourcePosition(), Severity.WARNING,
                                      exception));
        exceptionMessages.add(exception.getMessage());
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code ErrorHandler}. */
    public void error(SAXParseException exception)
        throws SAXException {
      try {
        eventHandler.add(new SaxAlert(getSourcePosition(), Severity.ERROR,
                                      exception));
        exceptionMessages.add(exception.getMessage());
      } finally {
        recordPosition();
      }
    }

    /** Implements {@code ErrorHandler}. */
    public void fatalError(SAXParseException exception)
        throws SAXException {
      try {
        eventHandler.add(new SaxAlert(getSourcePosition(), Severity.ERROR,
                                      exception));
        exceptionMessages.add(exception.getMessage());
      } finally {
        recordPosition();
      }
    }

    /**
     * @return a list of seen exception messages
     */
    public Set<String> getExceptionMessages() {
      return Collections.unmodifiableSet(exceptionMessages);
    }

    private int lineNumber = 0;
    private int columnNumber = 0;

    /**
     * During a call to an event handler the locator points just past the end
     * of the "event". We actually want to know the starting position of the
     * event, so at the end of each event we record the position so we can use
     * it in the next event handler.
     */
    private void recordPosition() {
      lineNumber = 0;
      columnNumber = 0;
      if (saxLocator != null) {
        lineNumber = saxLocator.getLineNumber();
        columnNumber = saxLocator.getColumnNumber();
      }
    }

    /**
     * @return the current {@code SourcePosition} while parsing.
     */
    private SourcePosition getSourcePosition() {
      // Lines and columns start at 1. A value of 0 means we don't know, so in
      // that case just use the whole file as the position.
      if ((lineNumber > 0) && (columnNumber > 0)) {
        return new SourcePosition(input, lineNumber, columnNumber);
      } else {
        return new SourcePosition(input);
      }
    }

    /**
     * @param saxAttrs SAX Attributes to extract attribute from
     * @param i index of attribute to extract
     */
    private ParsedAttribute parseAttribute(SourcePosition sourcePosition,
                                           Attributes saxAttrs, int i) {
      String attrName = saxAttrs.getLocalName(i);
      String attrValue = saxAttrs.getValue(i);
      if (attrName.equals("")) {
        // For some reason, SAX turns xmlns attributes into weird nameless
        // attributes so we ignore any attribute that have no name.
        return null;
      } else {
        String attrQName;
        String attrNamespaceUri = saxAttrs.getURI(i);
        if ("".equals(attrNamespaceUri)) {
          attrNamespaceUri = null;
          attrQName = attrName;
        } else {
          attrQName = saxAttrs.getQName(i);
        }
        return eventHandler.parseAttribute(sourcePosition, attrNamespaceUri,
                                           attrName, attrValue, attrQName);
      }
    }
  }
}
