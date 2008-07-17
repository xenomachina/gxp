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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.Alert;
import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Default implementation of {@code XmlEventHandler}, used by {@code Parser}.
 * Constructs {@code ParsedNode}s based on events.
 */
class DefaultXmlEventHandler implements XmlEventHandler {
  // settings
  private final NamespaceSet namespaces;
  private final AlertSink alertSink;
  private final SourceEntityResolver entityResolver;

  // intermediate state
  private final Deque<ElementBuilder> builderStack =
      new ArrayDeque<ElementBuilder>();

  // result
  private List<ParsedElement> rootElements = Lists.newArrayList();

  public DefaultXmlEventHandler(NamespaceSet namespaces, AlertSink alertSink,
                                SourceEntityResolver entityResolver) {
    this.namespaces = Objects.nonNull(namespaces);
    this.alertSink = Objects.nonNull(alertSink);
    this.entityResolver = Objects.nonNull(entityResolver);
  }

  /**
   * @return the root ParsedElement of the parsed document.
   */
  List<ParsedElement> getParsedRoots() {
    while (!builderStack.isEmpty()) {
      consumeElement(builderStack.pop().build());
    }
    return rootElements;
  }

  public void add(Alert alert) {
    alertSink.add(alert);
  }

  public void addAll(AlertSet alertSet) {
    alertSink.addAll(alertSet);
  }

  public void startPrefixMapping(SourcePosition sourcePosition, String prefix,
                                 String uri) {
    // TODO(laurence): collect mappings used, and warn about unused mappings
    // at the end of the document?
  }

  public void endPrefixMapping(SourcePosition sourcePosition, String prefix) {
    // TODO(laurence): collect mappings used, and warn about unused mappings
    // at the end of the document?
  }

  public void startElement(SourcePosition sourcePosition, String nsUri,
                           String localName, String qName,
                           Iterable<? extends ParsedAttribute> attrs) {
    builderStack.push(new ElementBuilder(alertSink, sourcePosition,
                                         namespaces, nsUri, localName, qName,
                                         attrs));
  }

  public void endElement(SourcePosition sourcePosition, String nsUri,
                         String localName, String qName) {
    ElementBuilder builder = builderStack.pop();
    builder.checkEndTag(nsUri, localName, qName);
    consumeElement(builder.build());
  }

  private void consumeElement(ParsedElement element) {
    if (element != null) {
      if (builderStack.isEmpty()) {
        if (!rootElements.isEmpty()) {
          alertSink.add(
              new AdditionalRootElementError(element.getSourcePosition(),
                                             rootElements.get(0),
                                             element));
        }
        rootElements.add(element);
      } else {
        builderStack.peek().addChild(element);
      }
    }
  }

  public void characters(SourcePosition sourcePosition, char[] ch,
                         int start, int length) {
    builderStack.peek().addText(sourcePosition, ch, start, length);
  }

  public void processingInstruction(SourcePosition sourcePosition,
                                    String target, String data) {
    alertSink.add(new IgnoredXmlWarning(sourcePosition,
                                        "processing instruction"));
  }

  public void skippedEntity(SourcePosition sourcePosition, String name) {
    alertSink.add(new UndefinedEntityError(sourcePosition, name));
  }

  public void notationDecl(SourcePosition sourcePosition, String name,
                           String publicId, String systemId) {
    alertSink.add(new IgnoredXmlWarning(sourcePosition,
                                        "notation declaration"));
  }

  public void unparsedEntityDecl(SourcePosition sourcePosition, String name,
                                 String publicId, String systemId,
                                 String notationName) {
    alertSink.add(new IgnoredXmlWarning(sourcePosition,
                                        "unparsed entity declaration"));
  }

  /**
   * delegates to the entityResolver supplied to the constructor
   */
  public InputStream resolveEntity(SourcePosition sourcePosition,
                                   String publicId, String systemId)
      throws IOException {
    return this.entityResolver.resolveEntity(
        sourcePosition, publicId, systemId, alertSink);
  }

  public ParsedAttribute parseAttribute(SourcePosition sourcePosition,
                                        String nsUri, String name, String value,
                                        String qName) {
    Namespace namespace = (nsUri == null)
        ?  NullNamespace.INSTANCE
        : namespaces.get(this, sourcePosition, nsUri);
    return (namespace == null)
        ? null
        : new ParsedAttribute(sourcePosition, namespace, name, value, qName);
  }

  /**
   * Utility class for building ParsedElements. Collects all of the children
   * and attributes incrementally, and then builds a ParsedNode with the
   * complete set of them.
   */
  private static class ElementBuilder {
    private final AlertSink alertSink;
    private final NamespaceSet namespaces;
    private final SourcePosition elementPosition;
    private final String uri;
    private final String localName;
    private final String qName;
    private final List<ParsedAttribute> attrs;
    private final List<ParsedElement> children = Lists.newArrayList();
    private final StringBuilder textBuffer = new StringBuilder();
    private SourcePosition textPosition;

    /**
     * @param alertSink {@code AlertSink} to report {@code Alert}s
     * to
     * @param elementPosition position of the element
     * @param namespaces {@code NamespaceSet} used for resolving attribute
     * namespaces
     * @param uri namespace URI for the element
     * @param localName XML "local name" of the element (eg: "eval")
     * @param qName XML "qualified name" (eg: "gxp:eval") of the element
     * @param xmlAttrs attributes as provided by SAX parser
     */
    ElementBuilder(AlertSink alertSink,
                   SourcePosition elementPosition,
                   NamespaceSet namespaces,
                   String uri, String localName, String qName,
                   Iterable<? extends ParsedAttribute> attrs) {
      this.alertSink = alertSink;
      this.namespaces = namespaces;
      this.elementPosition = elementPosition;
      this.uri = uri;
      this.localName = localName;
      this.qName = qName;
      this.attrs = ImmutableList.copyOf(attrs);
    }

    /**
     * Adds the specified {@code ParsedNode} as a child of the element that
     * will be built.
     */
    public void addChild(ParsedElement child) {
      flushTextBuffer();
      children.add(Objects.nonNull(child));
    }

    /**
     * Verifies that an end tag matches the start tag this ElementBuilder was
     * created for.
     */
    void checkEndTag(String endUri, String endLocalName, String endQName) {
      if (!endUri.equals(uri)) {
        throw new IllegalArgumentException();
      }
      if (!endLocalName.equals(localName)) {
        throw new IllegalArgumentException();
      }
      if (!endQName.equals(qName)) {
        throw new IllegalArgumentException();
      }
    }

    /**
     * Builds the element.
     */
    ParsedElement build() {
      Namespace ns = namespaces.get(alertSink, elementPosition, uri);
      if (ns == null) {
        // Ignore element and children if we don't recognize namespace.
        return null;
      } else {
        flushTextBuffer();
        return ns.createElement(alertSink, elementPosition,
                                tagDisplayName(qName), localName, attrs,
                                children);
      }
    }

    private static String tagDisplayName(String qName) {
      return "<" + qName + ">";
    }

    /**
     * Adds text to this element. Text is accumulated so that adjacent blobs of
     * text can be combined into a single {@code ParsedText} child.
     */
    public void addText(SourcePosition sourcePosition, char[] ch, int start,
                        int length) {
      if (textPosition == null) {
        textPosition = sourcePosition;
      }
      textBuffer.append(ch, start, length);
    }

    /**
     * Collects any accumulated text and adds a new {@code ParsedText} child
     * containing that text, if any.
     */
    private void flushTextBuffer() {
      if (textBuffer.length() > 0) {
        if (textPosition == null) {
          throw new NullPointerException();
        }
        String text = textBuffer.toString();

        textBuffer.setLength(0);

        children.add(new TextElement(textPosition, text));
        textPosition = null;
      }
    }
  }
}
