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
import com.google.common.base.Preconditions;
import com.google.gxp.compiler.alerts.SourcePosition;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A GXP parameter. Corresponds to a &lt;gxp:param&gt; element.
 */
@SuppressWarnings("serial") // let java pick the serialVersionUid
public class FormalParameter extends SerializableAbstractNode {
  private final String primaryName;
  private final boolean consumesContent;
  private final Type type;
  private final boolean hasDefault;
  private final Pattern regex;
  private final boolean hasConstructor;
  private final SpaceOperatorSet spaceOperators;

  public FormalParameter(SourcePosition sourcePosition, String displayName,
                         String primaryName, boolean consumesContent, Type type,
                         Expression defaultValue, boolean hasDefaultFlag,
                         Pattern regex,
                         Expression constructor, boolean hasConstructorFlag,
                         SpaceOperatorSet spaceOperators) {
    super(sourcePosition, displayName);
    this.primaryName = Preconditions.checkNotNull(primaryName);
    this.consumesContent = consumesContent;
    this.type = Preconditions.checkNotNull(type);
    this.hasDefault = (defaultValue != null) || hasDefaultFlag;
    this.regex = regex;
    this.hasConstructor = (constructor != null) || hasConstructorFlag;
    this.spaceOperators = Preconditions.checkNotNull(spaceOperators);
  }

  public FormalParameter(Node fromNode,
                         String primaryName, boolean consumesContent, Type type,
                         Expression defaultValue, boolean hasDefaultFlag,
                         Pattern regex,
                         Expression constructor, boolean hasConstructorFlag,
                         SpaceOperatorSet spaceOperators) {
    this(fromNode.getSourcePosition(), fromNode.getDisplayName(),
         primaryName, consumesContent, type,
         defaultValue, hasDefaultFlag, regex, constructor, hasConstructorFlag,
         spaceOperators);
  }

  public FormalParameter(SourcePosition sourcePosition, String displayName,
                         String primaryName, Type type) {
    this(sourcePosition, displayName, primaryName, false, type,
         null, false, null, null, false, SpaceOperatorSet.NULL);
  }

  /**
   * @return the primary name of this {@code Parameter}.  This is
   * always what is specified in the name parameter in the gxp.
   */
  public String getPrimaryName() {
    return primaryName;
  }

  /**
   * @return the entire list of names this {@code FormalParameter} can accept
   */
  public Set<String> getNames() {
    return getType().acceptTypeVisitor(
        new DefaultingTypeVisitor<Set<String>>() {
          @Override
          public Set<String> defaultVisitType(Type type) {
            return Collections.singleton(getPrimaryName());
          }

          @Override
          public Set<String> visitBundleType(BundleType type) {
            return type.getAttrMap().keySet();
          }
    });
  }

  public boolean consumesContent() {
    return consumesContent;
  }

  public Type getType() {
    return type;
  }

  public boolean hasDefault() {
    return hasDefault || (type.getDefaultValue() != null);
  }

  public Pattern getRegex() {
    return regex;
  }

  public boolean regexMatches(ObjectConstant oc) {
    return (regex == null)
        ? true
        : regex.matcher(oc.getValue()).matches();
  }

  public boolean hasConstructor() {
    return hasConstructor;
  }

  public SpaceOperatorSet getSpaceOperators() {
    return spaceOperators;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof FormalParameter && equals((FormalParameter) that));
  }

  public boolean equals(FormalParameter that) {
    return equalsAbstractNode(that)
        && Objects.equal(getPrimaryName(), that.getPrimaryName())
        && Objects.equal(getType(), that.getType())
        && Objects.equal(hasDefault(), that.hasDefault())
        && Objects.equal(getRegex(), that.getRegex())
        && Objects.equal(hasConstructor(), that.hasConstructor())
        && Objects.equal(getSpaceOperators(), that.getSpaceOperators());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        abstractNodeHashCode(),
        getPrimaryName(),
        getType(),
        hasDefault(),
        getRegex(),
        hasConstructor(),
        getSpaceOperators());
  }
}
