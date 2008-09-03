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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.SerializableAbstractNode;

import java.util.*;

/**
 * A Schema represents an output language like XHTML, RSS, VoiceXML or some
 * other XML language that you might want to generate via GXP. A schema can
 * also optionally support an "SGML" mode. This is probably only useful for
 * XHTML, whose SGML counterpart is HTML. Making it general is slightly easier
 * (and less ugly) than special-casing it, so there you have it.
 *
 * <p>Schemas are generally specified in an XML file which is then parsed with
 * SchemaParser.
 *
 * <p>Schemas are used by the GXP compiler to do input vaidation (for example,
 * checking that an attribute really exists) and also for providing other
 * special knowledge about attributes and elements. (eg: which attributes are
 * boolean, and what doctypes are supported)
 *
 * <p>Most GXP users will probably not need to write schemas. Only one schema
 * is required per output format. If you feel the urge to write a schema, first
 * investigate to see if someone has already written one for the same language.
 */
@SuppressWarnings("serial") // let java pick the SerialVersionUID
public final class Schema extends SerializableAbstractNode implements Comparable<Schema> {
  private final String name;
  private final String namespaceUri;
  private final String contentType;
  private final boolean defaultsToSgml;
  private final String sgmlContentType;
  private final String tagPrefix;
  private final ImmutableMap<String, ElementValidator> validatorMap;
  private final String cppType;
  private final String cppAppender;
  private final ImmutableList<String> cppImports;
  private final String javaType;
  private final String javaAppender;
  private final ImmutableList<String> javaImports;
  private final String javaScriptType;
  private final ContentFamily contentFamily;
  private final ImmutableList<SchemaRef> allowedSchemaRefs;
  private final Schema msgSchema;

  /**
   * @return the short name for this schema. This name will be used as a part
   * of identifiers in Java and other languages, so it would be best to stick
   * with alphanumeric characters and no spaces.
   */
  public String getName() {
    return name;
  }

  /**
   * @return XML namespace this schema is bound to.
   */
  public String getNamespaceUri() {
    return namespaceUri;
  }

  /**
   * @return MIME content-type that would be used for XML documents whose root
   * nodes are from this schema.
   */
  public String getXmlContentType() {
    return contentType;
  }

  /**
   * @return whether this schema is normally used for SGML rather than XML.
   */
  public boolean defaultsToSgml() {
    return defaultsToSgml;
  }

  /**
   * @return MIME content-type that would be used for SGML documents whose root
   * nodes are from this schema.
   */
  public String getSgmlContentType() {
    return sgmlContentType;
  }

  /**
   * @return MIME content-type that's most commonly used to identify this
   * Schema.
   */
  public String getCanonicalContentType() {
    return defaultsToSgml() ? getSgmlContentType() : getXmlContentType();
  }

  /**
   * @return the tag prefix for this schema or null if tags shold be output
   * without a prefix.
   */
  public String getTagPrefix() {
    return tagPrefix;
  }

  /**
   * @return the name of the C++ type used to represent a closure of content
   * from this schema.
   */
  public String getCppType() {
    return cppType;
  }

  public String getCppAppender() {
    return cppAppender;
  }

  /**
   * @return a {@code List} of additional imports that should be declared in
   * every C++ file of this schema type.
   */
  public List<String> getCppImports() {
    return cppImports;
  }

  /**
   * @return the name of the Java type used to represent a closure of content
   * from this schema.
   */
  public String getJavaType() {
    return javaType;
  }

  public String getJavaAppender() {
    return javaAppender;
  }

  /**
   * @return a {@code List} of additional imports that should be declared in
   * every java file of this schema type.
   */
  public List<String> getJavaImports() {
    return javaImports;
  }

  /**
   * @return the name of the JavaScript type used to represent a closure of
   * content from this schema.
   */
  public String getJavaScriptType() {
    return javaScriptType;
  }

  /**
   * @return the {@code ContentFamily} of this Schema.
   */
  public ContentFamily getContentFamily() {
    return contentFamily;
  }

  /**
   * @return the {@code Schema} that should be used for <gxp:msg>s that
   * appear in the context of this schema.
   */
  public Schema getMsgSchema() {
    return (msgSchema == null) ? this : msgSchema;
  }

  /**
   * @return true if the translation console can translate <gxp:msg>s of
   * this content type
   *
   * TODO(harryh): this may need to be orthagonal to having a msgSchema
   */
  public boolean isTranslatable() {
    return msgSchema == null || msgSchema.equals(this);
  }

  @Override
  public String toString() {
    return getCanonicalContentType();
  }

  /**
   * @return true if elements of the given {@code Schema} are allowed in the
   * context of this {@code Schema}.
   */
  public boolean allows(Schema schema) {
    if (schema == this) {
      return true;
    }
    for (SchemaRef ref : allowedSchemaRefs) {
      if (ref.getContentType().equals(schema.getXmlContentType())
          || ref.getContentType().equals(schema.getSgmlContentType())) {
        return true;
      }
    }
    return false;
  }

  public Schema(SourcePosition pos,
                String displayName,
                String name,
                String namespaceUri,
                String contentType,
                boolean defaultsToSgml,
                String sgmlContentType,
                String tagPrefix,
                String cppType,
                String cppAppender,
                List<String> cppImports,
                String javaType,
                String javaAppender,
                List<String> javaImports,
                String javaScriptType,
                Iterable<ElementBuilder> elementBuilders,
                Collection<SchemaRef> allowedSchemaRefs,
                Schema msgSchema) {
    super(pos, displayName);
    this.name = Preconditions.checkNotNull(name);
    this.namespaceUri = Preconditions.checkNotNull(namespaceUri);
    this.contentType = contentType;
    this.defaultsToSgml = defaultsToSgml;
    this.sgmlContentType = sgmlContentType;
    this.tagPrefix = tagPrefix;
    this.cppType = cppType;
    this.cppAppender = cppAppender;
    this.cppImports = ImmutableList.copyOf(cppImports);
    this.javaType = Preconditions.checkNotNull(javaType);
    this.javaAppender = javaAppender;
    this.javaImports = ImmutableList.copyOf(Preconditions.checkNotNull(javaImports));
    this.javaScriptType = javaScriptType;
    this.contentFamily = ContentFamily.fromContentTypeName(
        getCanonicalContentType());

    ImmutableMap.Builder<String, ElementValidator> validatorMapBuilder =
        ImmutableMap.builder();
    for (ElementBuilder elementBuilder : elementBuilders) {
      ElementValidator validator = elementBuilder.build();
      validatorMapBuilder.put(validator.getTagName(), validator);
    }
    validatorMap = validatorMapBuilder.build();
    this.allowedSchemaRefs = ImmutableList.copyOf(allowedSchemaRefs);
    this.msgSchema = msgSchema;
  }

  public ElementValidator getElementValidator(String tagName) {
    return validatorMap.get(tagName);
  }

  @Override
  public int compareTo(Schema that) {
    return getCanonicalContentType().compareTo(that.getCanonicalContentType());
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || ((that instanceof Schema) && equals((Schema) that));
  }

  private boolean equals(Schema that) {
    return equalsAbstractNode(that)
        && Objects.equal(getName(), that.getName())
        && Objects.equal(getNamespaceUri(), that.getNamespaceUri())
        && Objects.equal(getXmlContentType(), that.getXmlContentType())
        && (defaultsToSgml() == that.defaultsToSgml())
        && Objects.equal(getSgmlContentType(), that.getSgmlContentType())
        && Objects.equal(getTagPrefix(), that.getTagPrefix())
        && Objects.equal(getCppType(), that.getCppType())
        && Objects.equal(getCppAppender(), that.getCppAppender())
        && Objects.equal(getCppImports(), that.getCppImports())
        && Objects.equal(getJavaType(), that.getJavaType())
        && Objects.equal(getJavaAppender(), that.getJavaAppender())
        && Objects.equal(getJavaImports(), that.getJavaImports())
        && Objects.equal(getJavaScriptType(), that.getJavaScriptType())
        && Objects.equal(getContentFamily(), that.getContentFamily())
        && Objects.equal(allowedSchemaRefs, that.allowedSchemaRefs)
        && Objects.equal(validatorMap, that.validatorMap)
        && Objects.equal(msgSchema, that.msgSchema);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        abstractNodeHashCode(),
        getName(),
        getNamespaceUri(),
        getXmlContentType(),
        defaultsToSgml(),
        getSgmlContentType(),
        getTagPrefix(),
        getCppType(),
        getCppAppender(),
        getCppImports(),
        getJavaType(),
        getJavaAppender(),
        getJavaImports(),
        getJavaScriptType(),
        getContentFamily(),
        allowedSchemaRefs,
        validatorMap,
        msgSchema);
  }
}
