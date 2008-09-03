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
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.schema.ElementValidator;
import com.google.gxp.compiler.schema.Schema;

import java.util.*;

/**
 * A {@code Namespace} for any output format (most notably XHTML/HTML).
 */
public class OutputNamespace implements Namespace {
  private final Schema schema;

  public String getUri() {
    return schema.getNamespaceUri();
  }

  public OutputNamespace(Schema schema) {
    this.schema = Preconditions.checkNotNull(schema);
  }

  public ParsedElement createElement(AlertSink alertSink,
                                     SourcePosition sourcePosition,
                                     String displayName,
                                     String tagName,
                                     List<ParsedAttribute> attrs,
                                     List<ParsedElement> children) {
    ElementValidator validator = schema.getElementValidator(tagName);
    if (validator == null) {
      alertSink.add(new UnknownElementError(sourcePosition, this,
                                            displayName));
      return null;
    } else {
      return new ParsedOutputElement(sourcePosition, displayName, schema,
                                     validator, attrs, children);
    }
  }

  /**
   * Parsed representation of an output element. (typically an HTML element)
   */
  public static class ParsedOutputElement extends ParsedElement {
    private final Schema schema;
    private final ElementValidator validator;

    public ParsedOutputElement(SourcePosition sourcePosition,
                               String displayName, Schema schema,
                               ElementValidator validator,
                               List<ParsedAttribute> attrs,
                               List<ParsedElement> children) {
      super(sourcePosition, displayName, attrs, children);
      this.schema = schema;
      this.validator = validator;
    }

    public Schema getSchema() {
      return schema;
    }

    public ElementValidator getValidator() {
      return validator;
    }

    public <T> T acceptVisitor(ParsedElementVisitor<T> visitor) {
      return visitor.visitParsedOutputElement(this);
    }

    @Override
    protected ParsedOutputElement withChildrenImpl(List<ParsedElement> children) {
      return new ParsedOutputElement(getSourcePosition(), getDisplayName(),
                                     schema, validator, getAttributes(),
                                     children);
    }
  }

  public <T> T acceptVisitor(NamespaceVisitor<T> visitor) {
    return visitor.visitOutputNamespace(this);
  }
}
