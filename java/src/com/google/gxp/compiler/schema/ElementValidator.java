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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.*;
import java.io.Serializable;

/**
 * <p>Schema component which represents an XML element.  SchemaParser creates
 * these out of &lt;element&gt; elements.
 *
 * <p>Each ElementValidator corresponds to a single type of element in the
 * output, but may be used to validate an arbitrary number of elements of that
 * type.  For example, one instance of ElementValidator could be used to
 * validate all XHTML &lt;img&gt; elements.
 *
 * <p>ElementValidators are retrieved via {@link
 * Schema#getElementValidator(String)}.
 */
@SuppressWarnings("serial") // let java pick the serialVersionUID
public final class ElementValidator implements Serializable {
  private final String tagName;
  private final ImmutableSet<ElementValidator.Flag> flags;
  private final String innerContentType;
  private final ImmutableMap<String, DocType> docTypeMap;
  private final ImmutableMap<String, AttributeValidator> attrMap;

  /**
   * The tag name (or "local name") of this element.
   */
  public String getTagName() {
    return tagName;
  }

  /**
   * @return whether the corresponding ElementValidator.Flag has been enabled
   * for this element.
   */
  public boolean isFlagSet(ElementValidator.Flag flag) {
    return flags.contains(flag);
  }

  /**
   * Some elements contain a different content type than their surrounding
   * environment. For example, XHTML <code>&lt;style&gt;</code> elements
   * contain <code>text/css</code> rather than HTML.
   *
   * @return the MIME "content type" of the element's body, or null if the
   * element does not cause a content type change.
   */
  public String getInnerContentType() {
    return innerContentType;
  }

  /**
   * @return the DocType this ElementValidator allows that has the given name,
   * or null if no DocType with the specified name is allowed.
   */
  public DocType getDocType(String name) {
    return docTypeMap.get(name);
  }

  /**
   * @return the AttributeValidator map for this element.
   */
  public  Map<String, AttributeValidator> getAttributeValidatorMap() {
    return Collections.unmodifiableMap(attrMap);
  }

  /**
   * @return the AttributeValidator for the given attribute, if this
   * element allows an attribute with the specified name. Otherwise, returns
   * null.
   */
  public AttributeValidator getAttributeValidator(String attrName) {
    return attrMap.get(attrName);
  }

  /**
   * @param tagName name of the element.
   * @param flags the set of flags which are enabled for this attribute.
   * @param innerContentType the MIME "content type" of the element's body, or
   * null if the element does not cause a content type change.
   * @param docTypes DocTypes this ElementValidator allows to be referenced in
   * this element with the gxp:doctype attribute.
   * @param attrs AttributeValidators for all of the attribute allowed in this
   * element.
   */
  public ElementValidator(String tagName,
                          Set<ElementValidator.Flag> flags,
                          String innerContentType, Iterable<DocType> docTypes,
                          Iterable<AttributeValidator> attrs) {
    this.tagName = Preconditions.checkNotNull(tagName);
    this.flags = ImmutableSet.copyOf(flags);
    this.innerContentType = innerContentType;

    ImmutableMap.Builder<String, DocType> docTypeMapBuilder =
        ImmutableMap.builder();
    for (DocType docType : docTypes) {
      docTypeMapBuilder.put(docType.getName(), docType);
    }
    docTypeMap = docTypeMapBuilder.build();

    ImmutableMap.Builder<String, AttributeValidator> attrMapBuilder =
        ImmutableMap.builder();
    for (AttributeValidator attr : attrs) {
      attrMapBuilder.put(attr.getName(), attr);
    }
    attrMap = attrMapBuilder.build();
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || ((that instanceof ElementValidator) && equals((ElementValidator) that));
  }

  private boolean equals(ElementValidator that) {
    return Objects.equal(attrMap, that.attrMap)
        && Objects.equal(docTypeMap, that.docTypeMap)
        && Objects.equal(flags, that.flags)
        && Objects.equal(innerContentType, that.innerContentType)
        && Objects.equal(tagName, that.tagName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        attrMap,
        docTypeMap,
        flags,
        innerContentType,
        tagName);
  }

  /**
   * Element flags.
   */
  public static enum Flag {
    // Element is not allowed to have child elements (in the output).
    CHILDLESS,

    // Element is an implied CDATA element when generating SGML output.
    // See:
    //   http://w3.org/TR/REC-html40/appendix/notes.html#notes-specifying-data
    EVILCDATA,

    // End tag is not allowed in SGML-mode.
    NOENDTAG,

    // Some flags from http://www.w3.org/TR/REC-html40/index/elements.html
    // included for completeness.
    OPTIONALENDTAG,
    DEPRECATED,
    LOOSEDTD,
    FRAMESETDTD,

    // Body doesn't need to be translated. (ie: don't complain about content
    // not in a <gxp:msg>)
    INVISIBLEBODY,

    // Multiple spaces are significant. For example, in an HTML <pre> element.
    PRESERVESPACES,

    // Not part of a real standard. Could eventually be used for generating
    // warnings but is currently just a form of documentation.
    NONSTANDARD,
  }
}
