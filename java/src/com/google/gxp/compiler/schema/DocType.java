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

import java.io.Serializable;

/**
 * <p>Schema component which represents an XML doctype declaration (and
 * possibly its SGML counterpart).  SchemaParser creates these out of
 * &lt;doctype&gt; elements.
 *
 * <p>In GXP each doctype has a name which is used to refer to the doctype.
 * There are two places these names can be referenced:
 *
 * <ul>
 * <ol>In a schema, elements can be declared to allow a set of doctypes
 * by listing the doctype names in the 'doctypes' attribute.
 * <ol>In a GXP file, elements which allow doctypes (as specified in the
 * schema) can have a 'gxp:doctype' attribute set to the name of one of that
 * element's allowed doctypes. This will cause the generation of a doctype
 * declaration in to output immediately before the the element.
 * </ul>
 *
 * <p>AttributeValidators are retrieved via {@link
 * ElementValidator#getDocType(String)}.
 */
@SuppressWarnings("serial") // let java pick the SerialVersionUID
public final class DocType implements Serializable {
  private final String name;
  private final String publicId;
  private final String systemId;
  private final String sgmlPublicId;
  private final String sgmlSystemId;

  /**
   * The name of this doctype as used in GXP schemas and in the gxp:doctype
   * attribute. Note that this name is made up by the creator of the GXP
   * schema, and is not a part of some outside standard like XML, SGML or HTML.
   */
  public String getName() {
    return name;
  }

  /**
   * The "public ID" of this doctype in XML.
   *
   * @see <a href="http://www.w3.org/TR/REC-xml/#dt-pubid">dt-pubid</a>
   */
  public String getPublicId() {
    return publicId;
  }

  /**
   * The "system ID" of this doctype in XML.
   *
   * @see <a href="http://www.w3.org/TR/REC-xml/#dt-sysid">dt-sysid</a>
   */
  public String getSystemId() {
    return systemId;
  }

  /**
   * The SGML equivalent of {@link #getPublicId()}.
   */
  public String getSgmlPublicId() {
    return sgmlPublicId;
  }

  /**
   * The SGML equivalent of {@link #getSystemId()}.
   */
  public String getSgmlSystemId() {
    return sgmlSystemId;
  }

  public boolean isSgmlCompatible() {
    return sgmlSystemId != null;
  }

  public DocType(String name, String publicId, String systemId,
                 String sgmlPublicId, String sgmlSystemId) {
    this.name = Objects.nonNull(name);
    this.publicId = publicId;
    this.systemId = Objects.nonNull(systemId);

    // it doesn't make sense to have a public ID without a system ID.
    if (sgmlPublicId != null && sgmlSystemId == null) {
      throw new NullPointerException();
    }
    this.sgmlPublicId = sgmlPublicId;
    this.sgmlSystemId = sgmlSystemId;
  }

  public String toSgml(String rootElement) {
    return toMarkup(rootElement.toUpperCase(), sgmlPublicId, sgmlSystemId);
  }

  private static String XML_DECLARATION = "<?xml version=\"1.0\" ?>\n";

  public String toXml(String rootElement) {
    return XML_DECLARATION + toMarkup(rootElement, publicId, systemId);
  }

  private static String toMarkup(String rootElement, String publicId,
                                 String systemId) {
    return "<!DOCTYPE "
        + Objects.nonNull(rootElement)
        + ((publicId == null)
           ? " SYSTEM" : (" PUBLIC \"" + publicId + "\""))
        + " \"" + Objects.nonNull(systemId) + "\""
        + ">";
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || ((that instanceof DocType) && equals((DocType)that));
  }

  private boolean equals(DocType that) {
    return Objects.equal(getName(), that.getName())
        && Objects.equal(getPublicId(), that.getPublicId())
        && Objects.equal(getSystemId(), that.getSystemId())
        && Objects.equal(getSgmlPublicId(), that.getSgmlPublicId())
        && Objects.equal(getSgmlSystemId(), that.getSgmlSystemId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        getName(),
        getPublicId(),
        getSystemId(),
        getSgmlPublicId(),
        getSgmlSystemId());
  }
}
