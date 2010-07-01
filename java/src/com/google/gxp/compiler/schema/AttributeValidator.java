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
import com.google.common.collect.ImmutableSet;
import com.google.gxp.base.AttributeHook;

import java.io.Serializable;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <p>Schema component which represents an XML attribute. SchemaParser creates
 * these out of &lt;attribute&gt; elements.
 *
 * <p>Each AttributeValidator corresponds to a single type of attribute. Note
 * that unlike elements and doctypes, there can be multiple attributes in the
 * same schema that have the same name. If two attributes have the same name
 * they must <em>not</em> be attached to the same ElementValidator.
 *
 * <p>AttributeValidators are retrieved via {@link
 * ElementValidator#getAttributeValidator(String)}.
 */
@SuppressWarnings("serial") // let java pick the SerialVersionUID
public final class AttributeValidator implements Serializable {
  private final String name;
  private final String contentType;
  private final Pattern pattern;
  private final ImmutableSet<AttributeValidator.Flag> flags;
  private final ImmutableSet<AttributeHook> hooks;
  private final String defaultValue;

  public String getName() {
    return name;
  }

  public String getContentType() {
    return contentType;
  }

  public boolean isValidValue(String value) {
    return (pattern == null) || pattern.matcher(value).matches();
  }

  public boolean isFlagSet(AttributeValidator.Flag flag) {
    return flags.contains(flag);
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * @param name name of this attribute. eg: "src"
   * @param pattern attribute values should match this pattern. If null, then
   * all values are considered acceptable.
   * @param flags the set of flags which are enabled for this attribute.
   * @param hooks the set of hooks which are enabled for this attribute.
   * @param defaultValue the value that should be used for this attribute in
   * output when no value is set in input. If null, then value may be left
   * unset in output.
   * @param example an example to use for this attribute if it is contained
   * within a placeholder.  If null the attribute doesn't need to be included
   * in the placeholder example.
   */
  public AttributeValidator(String name, String contentType, Pattern pattern,
                            Set<AttributeValidator.Flag> flags,
                            Set<AttributeHook> hooks,
                            String defaultValue) {
    this.name = Preconditions.checkNotNull(name);
    this.contentType = contentType;
    this.pattern = pattern;
    this.flags = ImmutableSet.copyOf(flags);
    this.hooks = ImmutableSet.copyOf(hooks);
    this.defaultValue = defaultValue;
  }

  /**
   * Attribute flags.
   */
  public enum Flag {
    BOOLEAN,
    DEPRECATED,
    FRAMESETDTD,
    LOOSEDTD,
    REQUIRED,
    VISIBLETEXT,
    NONSTANDARD,

    // For use in internal tools only (like AdsICS or PDB). This is
    // because the attribute is not approved for use on externally visible
    // services.
    //
    // TODO: add switch to enable "INTERNAL_ONLY" attrs (disable them by
    // default)
    INTERNAL_ONLY,
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || ((that instanceof AttributeValidator) && equals((AttributeValidator) that));
  }

  /**
   * NOTE: Patterns are not equals() to each other even if they have the
   *       same regex, so compare the regex directly instead of relying
   *       on Pattern.equals().  Lame!
   */
  public boolean equals(AttributeValidator that) {
    String p1 = (pattern == null) ? null : pattern.pattern();
    String p2 = (that.pattern == null) ? null : that.pattern.pattern();

    return Objects.equal(name, that.name)
        && Objects.equal(contentType, that.contentType)
        && Objects.equal(p1, p2)
        && Objects.equal(flags, that.flags)
        && Objects.equal(hooks, that.hooks)
        && Objects.equal(defaultValue, that.defaultValue);
  }

  @Override
  public int hashCode() {
    String patternString = (pattern == null) ? null : pattern.pattern();
    return Objects.hashCode(
        name,
        contentType,
        patternString,
        flags,
        hooks,
        defaultValue);
  }
}
