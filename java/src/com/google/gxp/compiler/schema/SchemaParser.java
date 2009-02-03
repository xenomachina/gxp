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

package com.google.gxp.compiler.schema;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.alerts.Alert.Severity;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.IOError;
import com.google.gxp.compiler.alerts.common.SaxAlert;
import com.google.gxp.compiler.fs.FileRef;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <p>Parser for GXP schemas. See schema.dtd for the format of a schema, and
 * html.xml for an example.
 */
public final class SchemaParser {

  public static final SchemaParser INSTANCE = new SchemaParser();

  private SchemaParser() { }

  private Schema parse(FileRef input) throws IOException, SAXException {
    InputStream inputStream = input.openInputStream();
    InputSource inputSource = new InputSource(inputStream);
    SaxEventHandler eventHandler = new SaxEventHandler(input);
    XMLReader xmlReader;
    xmlReader = XMLReaderFactory.createXMLReader();
    xmlReader.setContentHandler(eventHandler);

    xmlReader.parse(inputSource);
    inputStream.close();
    return eventHandler.getSchema();
  }

  public static Schema getSchema(FileRef input, AlertSink alertSink) {
    SourcePosition pos = new SourcePosition(input);
    try {
      return Preconditions.checkNotNull(INSTANCE.parse(input));
    } catch (SAXException saxException) {
      alertSink.add(new SaxAlert(pos, Severity.ERROR, saxException));
      return null;
    } catch (IOException iox) {
      alertSink.add(new IOError(pos, iox));
      return null;
    }
  }

  private static class SaxEventHandler implements ContentHandler {
    private boolean done = false;

    private FileRef source;
    private String schemaName;
    private String schemaContentType;
    private String schemaNamespaceUri;
    private String schemaTagPrefix;

    private String schemaCppType;
    private String schemaCppAppender;
    private List<String> schemaCppImports = Lists.newArrayList();

    private String schemaJavaType;
    private String schemaJavaAppender;
    private List<String> schemaJavaImports = Lists.newArrayList();

    private String schemaJavaScriptType;
    private List<String> schemaJavaScriptImports = Lists.newArrayList();

    private boolean schemaDefaultsToSgml;
    private String schemaSgmlContentType;

    private List<SchemaRef> schemaAllowedSchemaRefs = Lists.newArrayList();

    private Map<String, ElementBuilder> elementBuilders = Maps.newHashMap();

    public SaxEventHandler(FileRef source) {
      this.source = Preconditions.checkNotNull(source);
    }

    Schema getSchema() {
      if (!done) {
        throw new IllegalStateException();
      }
      return new Schema(getSourcePosition(), "<schema>",
                        schemaName, schemaNamespaceUri, schemaContentType,
                        schemaDefaultsToSgml, schemaSgmlContentType, schemaTagPrefix,
                        schemaCppType, schemaCppAppender, schemaCppImports,
                        schemaJavaType, schemaJavaAppender, schemaJavaImports,
                        schemaJavaScriptType, schemaJavaScriptImports,
                        elementBuilders.values(), schemaAllowedSchemaRefs, null);
    }

    /** Implements {@code ContentHandler}. */
    public void setDocumentLocator(Locator locator) {
      // TODO(harryh): save this so we can record document positions
    }

    private int lineNumber = 0;
    private int columnNumber = 0;

    /**
     * @return the current {@code SourcePosition} while parsing.
     */
    private SourcePosition getSourcePosition() {
      // Lines and columns start at 1. A value of 0 means we don't know, so in
      // that case just use the whole file as the position.
      if ((lineNumber > 0) && (columnNumber > 0)) {
        return new SourcePosition(source, lineNumber, columnNumber);
      } else {
        return new SourcePosition(source);
      }
    }

    /** Implements {@code ContentHandler}. */
    public void startDocument() throws SAXException {
    }

    /** Implements {@code ContentHandler}. */
    public void endDocument() throws SAXException {
      done = true;
    }

    /** Implements {@code ContentHandler}. */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
      throw new Error("TODO(laurence): implement");
    }

    /** Implements {@code ContentHandler}. */
    public void endPrefixMapping(String prefix) throws SAXException {
      throw new Error("TODO(laurence): implement");
    }

    private int depth = 0;
    private boolean sawAttrs = false;

    private Map<String, DocType> docTypeMap = Maps.newHashMap();
    private Map<String, PatternElement> patterns = Maps.newHashMap();

    /** Implements {@code ContentHandler}. */
    public void startElement(String uri, String localName, String qName,
                             Attributes attrs) throws SAXException {
      depth++;
      Map<String, String> attrMap = parseAttributes(attrs);
      if (localName.equals("schema")) {
        if (depth != 1) {
          throw new IllegalStateException("Nested <schema>.");
        }

        schemaName = attrMap.remove("name");
        schemaContentType = attrMap.remove("content-type");
        schemaNamespaceUri = attrMap.remove("namespace");
        schemaTagPrefix = attrMap.remove("tag-prefix");

        schemaCppType = attrMap.remove("cpp-type");
        schemaCppAppender = attrMap.remove("cpp-appender");
        String cppImportsStr = attrMap.remove("cpp-imports");
        if (cppImportsStr != null) {
          for (String cppImport : split(cppImportsStr)) {
            schemaCppImports.add(cppImport);
          }
        }

        schemaJavaType = attrMap.remove("java-type");
        schemaJavaAppender = attrMap.remove("java-appender");
        String javaImportsStr = attrMap.remove("java-imports");
        if (javaImportsStr != null) {
          for (String javaImport : split(javaImportsStr)) {
            schemaJavaImports.add(javaImport);
          }
        }

        schemaJavaScriptType = attrMap.remove("javascript-type");
        String javaScriptImportsStr = attrMap.remove("javascript-imports");
        if (javaScriptImportsStr != null) {
          for (String javaScriptImport : split(javaScriptImportsStr)) {
            schemaJavaScriptImports.add(javaScriptImport);
          }
        }

        schemaDefaultsToSgml = "true".equals(attrMap.remove("default-to-sgml"));
        schemaSgmlContentType = attrMap.remove("sgml-content-type");

        String allowedContentTypes = attrMap.remove("allowed-content-types");
        if (allowedContentTypes != null) {
          for (String allowedContentType : split(allowedContentTypes)) {
            schemaAllowedSchemaRefs.add(new SchemaRef(allowedContentType));
          }
        }

        assertNoMoreAttrs(attrMap);
      } else {
        if (depth != 2) {
          throw new IllegalStateException("<" + localName
                                          + "> must be child of schema");
        }
        if (localName.equals("doctype")) {
          DocType docType = createDocType(attrMap);
          docTypeMap.put(docType.getName(), docType);
        } else if (localName.equals("element")) {
          if (sawAttrs) {
            throw new IllegalStateException("<element> cannot appear after "
                                            + "<attribute>");
          }
          ElementBuilder elementBuilder = createElementBuilder(attrMap);
          elementBuilders.put(elementBuilder.getName(), elementBuilder);
        } else if (localName.equals("pattern")) {
          PatternElement patternElement = createPattern(attrMap);
          patterns.put(patternElement.getName(), patternElement);
        } else if (localName.equals("attribute")) {
          sawAttrs = true;
          AttributeElement attrElement = createAttributeElement(attrMap);
          Set<String> exceptElementNames = attrElement.getExceptElementNames();
          Set<String> elementNames = attrElement.getElementNames();
          if (exceptElementNames.isEmpty()) {
            for (String elementName : elementNames) {
              elementBuilders.get(elementName).add(attrElement);
            }
          } else {
            if (!elementNames.isEmpty()) {
              throw new RuntimeException("can't specify both elements "
                                         + "and except-elements");
            }
            for (String elementName : elementBuilders.keySet()) {
              if (!exceptElementNames.contains(elementName)) {
                elementBuilders.get(elementName).add(attrElement);
              }
            }
          }
        } else {
          throw new IllegalArgumentException("unrecognized tag <"
                                             + localName + ">");
        }
      }
    }

    private DocType createDocType(Map<String, String> attrMap) {
      String name = attrMap.remove("name");
      String publicId = attrMap.remove("public-id");
      String systemId = attrMap.remove("system-id");
      String sgmlPublicId = attrMap.remove("sgml-public-id");
      String sgmlSystemId = attrMap.remove("sgml-system-id");
      assertNoMoreAttrs(attrMap);

      return new DocType(name, publicId, systemId, sgmlPublicId, sgmlSystemId);
    }

    private ElementBuilder createElementBuilder(Map<String, String> attrMap) {
      String name = attrMap.remove("name");
      String flagNames = attrMap.remove("flags");
      String contentType = attrMap.remove("content");
      String docTypeNames = attrMap.remove("doctypes");
      assertNoMoreAttrs(attrMap);

      EnumSet<ElementValidator.Flag> flags =
          EnumSet.noneOf(ElementValidator.Flag.class);
      if (flagNames != null) {
        for (String flagName : split(flagNames)) {
          flags.add(ElementValidator.Flag.valueOf(xmlToEnum(flagName)));
        }
      }

      Set<DocType> docTypes = Sets.newHashSet();
      if (docTypeNames != null) {
        for (String docTypeName : split(docTypeNames)) {
          if (docTypeMap.containsKey(docTypeName)) {
            docTypes.add(docTypeMap.get(docTypeName));
          } else {
            throw new IllegalArgumentException("can't find definition for "
                                               + "doctype named \""
                                               + docTypeName + "\".");
          }
        }
      }

      return new ElementBuilder(name, flags, contentType, docTypes);
    }

    private PatternElement createPattern(Map<String, String> attrMap) {
      String name = attrMap.remove("name");
      String regex = attrMap.remove("regex");
      assertNoMoreAttrs(attrMap);
      return new PatternElement(name, regex);
    }

    private static class PatternElement {
      private final String name;
      private final String regex;

      public String getName() {
        return name;
      }

      public String getRegex() {
        return regex;
      }

      PatternElement(String name, String regex) {
        this.name = name;
        this.regex = regex;
      }
    }

    private AttributeElement createAttributeElement(Map<String, String> attrMap) {
      String name = attrMap.remove("name");
      String elementNames = attrMap.remove("elements");
      String exceptElementNames = attrMap.remove("except-elements");
      String contentType = attrMap.remove("content");
      String patternName = attrMap.remove("pattern");
      String regex = attrMap.remove("regex");
      String flagNames = attrMap.remove("flags");
      String defaultValue = attrMap.remove("default");
      String example = attrMap.remove("example");
      assertNoMoreAttrs(attrMap);

      if (patternName != null) {
        if (regex != null) {
          throw new RuntimeException();
        } else {
          regex = patterns.get(patternName).getRegex();
        }
      }
      Pattern pattern = (regex == null) ? null : Pattern.compile(regex);

      EnumSet<AttributeValidator.Flag> flags =
          EnumSet.noneOf(AttributeValidator.Flag.class);
      if (flagNames != null) {
        for (String flagName : split(flagNames)) {
          flags.add(AttributeValidator.Flag.valueOf(xmlToEnum(flagName)));
        }
      }

      return new AttributeElement(name, contentType, pattern, flags,
                                  defaultValue, example,
                                  Sets.newHashSet(split(elementNames)),
                                  Sets.newHashSet(split(exceptElementNames)));
    }

    private Map<String, String> parseAttributes(Attributes attrs) {
      Map<String, String> attrMap = Maps.newHashMap();
      int n = attrs.getLength();
      for (int i = 0; i < n; i++) {
        attrMap.put(attrs.getLocalName(i), attrs.getValue(i));
      }
      return attrMap;
    }

    private static void assertNoMoreAttrs(Map<String, ?> attrMap) {
      if (!attrMap.isEmpty()) {
        throw new RuntimeException("Unknown attrs: " + attrMap.keySet());
      }
    }

    /** Implements {@code ContentHandler}. */
    public void endElement(String uri, String localName, String qName)
        throws SAXException {
      depth--;
    }

    /** Implements {@code ContentHandler}. */
    public void characters(char ch[], int start, int length)
        throws SAXException {
      for (int i = start; i < start + length; i++) {
        if (!Character.isWhitespace(ch[i])) {
          throw new RuntimeException("illegal content: '" + ch[i] + "'");
        }
      }
    }

    /** Implements {@code ContentHandler}. */
    public void ignorableWhitespace(char ch[], int start, int length)
        throws SAXException {
    }

    /** Implements {@code ContentHandler}. */
    public void processingInstruction(String target, String data)
        throws SAXException {
      throw new Error("TODO(laurence): implement");
    }

    /** Implements {@code ContentHandler}. */
    public void skippedEntity(String name) throws SAXException {
      throw new Error("TODO(laurence): implement");
    }

    private static String[] split(String s) {
      if (s == null) {
        return new String[0];
      } else {
        return s.split("\\s+");
      }
    }

    private static String xmlToEnum(String s) {
      if (s.matches("[-a-z]+")) {
        return s.toUpperCase().replace("-", "_");
      } else {
        throw new RuntimeException("Illegal value " + s);
      }
    }
  }
}
