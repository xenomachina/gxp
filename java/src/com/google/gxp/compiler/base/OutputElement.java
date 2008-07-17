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

package com.google.gxp.compiler.base;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.reparent.Attribute;
import com.google.gxp.compiler.schema.DocType;
import com.google.gxp.compiler.schema.ElementValidator;
import com.google.gxp.compiler.schema.Schema;

import java.util.*;

/**
 * Represents an element in the output format. For example, an HTML element
 * (like an img tag).
 */
public class OutputElement extends Expression {
  private final Schema innerSchema;
  private final String localName;
  private final ElementValidator validator;
  private final DocType docType;
  private final ImmutableList<Attribute> attributes;
  private final ImmutableList<String> attrBundles;
  private final String phName;
  private final Expression content;

  /**
   * @param sourcePosition the {@link SourcePosition} of this {@code Node}
   * @param displayName the display name of this {@code Node}
   * @param schema the {@code Schema} of this element.
   * @param innerSchema the {@code Schema} for the contents of this element.
   * can be null if the innerSchema is no different from the {@code Schema} of
   * the element itself.
   * @param localName the XML "local name" of this element. For example,
   * &lt;html:img&gt;'s local name is "img".
   * @param validator the {@code ElementValidator}s of this element
   * @param docType the {@code DocType} to render before this element, or null
   * if none.
   * @param attributes the {@code Attribute}s of this element
   * @param attrBundles the attribute bundles of this element
   * @param phName a placeholder name if the element tags should be contained
   * within placeholders.
   * @param content this element's content
   */
  public OutputElement(SourcePosition sourcePosition, String displayName,
                       Schema schema,
                       Schema innerSchema,
                       String localName,
                       ElementValidator validator,
                       DocType docType,
                       List<Attribute> attributes,
                       List<String> attrBundles,
                       String phName,
                       Expression content) {
    super(sourcePosition, displayName, schema);
    this.innerSchema = innerSchema;
    this.localName = Objects.nonNull(localName);
    this.validator = Objects.nonNull(validator);
    this.docType = docType;
    this.attributes = ImmutableList.copyOf(attributes);
    this.attrBundles = ImmutableList.copyOf(attrBundles);
    this.phName = phName;
    this.content = Objects.nonNull(content);
  }

  /**
   * Helper for creating an OutputElement to replace an existing OutputElement.
   *
   * @param fromNode the original {@code OutputElement} that this element is
   * derived from
   * @param attributes the {@code Attribute}s of this element
   * @param content this element's (new) content
   */
  public OutputElement(OutputElement fromNode, List<Attribute> attributes,
                       Expression content) {
    this(fromNode.getSourcePosition(), fromNode.getDisplayName(),
         fromNode.getSchema(),
         fromNode.getInnerSchema(), fromNode.getLocalName(),
         fromNode.getValidator(), fromNode.getDocType(), attributes,
         fromNode.getAttrBundles(), fromNode.getPhName(), content);
  }

  public Schema getInnerSchema() {
    return innerSchema;
  }

  public String getLocalName() {
    return localName;
  }

  public ElementValidator getValidator() {
    return validator;
  }

  public DocType getDocType() {
    return docType;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public List<String> getAttrBundles() {
    return attrBundles;
  }

  public String getPhName() {
    return phName;
  }

  public Expression getContent() {
    return content;
  }

  public OutputElement withAttributesAndContent(List<Attribute> newAttributes,
                                                Expression newContent) {
    return (Iterables.elementsEqual(newAttributes, attributes) && newContent.equals(content))
        ? this
        : new OutputElement(this, newAttributes, newContent);
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitOutputElement(this);
  }

  @Override
  public boolean hasStaticString() {
    for (Attribute attribute : getAttributes()) {
      if (attribute.getCondition() != null ||
          !attribute.getValue().hasStaticString()) {
        return false;
      }
    }
    return getContent().hasStaticString();
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof OutputElement) && equals((OutputElement) that);
  }

  public boolean equals(OutputElement that) {
    return equalsExpression(that)
        && Objects.equal(innerSchema, that.innerSchema)
        && localName.equals(that.localName)
        && validator.equals(that.validator)
        && Objects.equal(docType, that.docType)
        && Iterables.elementsEqual(attributes, that.attributes)
        && Iterables.elementsEqual(attrBundles, that.attrBundles)
        && Objects.equal(phName, that.phName)
        && content.equals(that.content);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        innerSchema,
        localName,
        validator,
        docType,
        attributes,
        attrBundles,
        phName,
        content);
  }
}
