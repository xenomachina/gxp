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
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.schema.SchemaFactory;

import java.io.IOException;

/**
 * GXP parser. Instances of this class are reusable (and reentrant) so a single
 * instance may be used to parse multiple source files.
 */
public class Parser {
  private final SchemaFactory schemaFactory;
  private final XmlParser xmlParser;
  private final NamespaceSet namespaces;
  private final SourceEntityResolver entityResolver;

  /**
   * Constructs a {@code Parser} using the specified {@link SchemaFactory} and
   * {@link XmlParser}.
   */
  public Parser(SchemaFactory schemaFactory, XmlParser xmlParser,
                SourceEntityResolver entityResolver) {
    this.schemaFactory = Preconditions.checkNotNull(schemaFactory);
    this.xmlParser = Preconditions.checkNotNull(xmlParser);
    this.namespaces = new NamespaceSet(schemaFactory);
    this.entityResolver = Preconditions.checkNotNull(entityResolver);
  }

  public SchemaFactory getSchemaFactory() {
    return schemaFactory;
  }

  /**
   * Returns a {@code ParseTree} constructed by parsing the contents of the
   * specified {@code InputStream}. The {@code inputName} is used for {@code
   * Alert} reporting.
   */
  public ParseTree parse(FileRef input) throws IOException {
    AlertSetBuilder alertSetBuilder = new AlertSetBuilder();
    DefaultXmlEventHandler eventHandler =
        new DefaultXmlEventHandler(namespaces, alertSetBuilder, entityResolver);

    try {
      xmlParser.parse(input, eventHandler);
    } catch (UnsupportedExternalEntityException e) {
      alertSetBuilder.add(new UnsupportedExternalEntityError(
                              e.getSourcePosition(), e.getEntity()));
    }

    return new ParseTree(new SourcePosition(input), alertSetBuilder.buildAndClear(),
                         eventHandler.getParsedRoots());
  }
}
