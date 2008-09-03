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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A GXP parameter. Corresponds to a &lt;gxp:param&gt; element.
 */
public class Parameter extends AbstractNode {
  private final FormalParameter formal;
  private final ImmutableList<JavaAnnotation> javaAnnotations;
  private final Expression defaultValue;
  private final boolean hasDefaultFlag;
  private final Expression constructor;
  private final boolean hasConstructorFlag;
  private final Expression comment;

  public Parameter(FormalParameter formal, List<JavaAnnotation> javaAnnotations,
                   Expression defaultValue, boolean hasDefaultFlag,
                   Expression constructor, boolean hasConstructorFlag,
                   Expression comment) {
    super(formal);
    this.formal = Preconditions.checkNotNull(formal);
    this.javaAnnotations = ImmutableList.copyOf(javaAnnotations);
    this.defaultValue = defaultValue;
    this.hasDefaultFlag = hasDefaultFlag;
    this.constructor = constructor;
    this.hasConstructorFlag = hasConstructorFlag;
    this.comment = Preconditions.checkNotNull(comment);
  }

  public Parameter(FormalParameter formal) {
    this(formal, Collections.<JavaAnnotation>emptyList(),
         /* default value */ null, /* default flag */ false,
         /* constructor */ null, /* constructor flag */ false,
         new StringConstant(formal.getSourcePosition(), null, ""));
  }

  public Parameter withDefaultValue(Expression newDefaultValue) {
    if (Objects.equal(newDefaultValue, defaultValue)) {
      return this;
    } else {
      Preconditions.checkNotNull(defaultValue);
      Preconditions.checkNotNull(newDefaultValue);
      return new Parameter(formal, javaAnnotations, newDefaultValue, hasDefaultFlag,
                           constructor, hasConstructorFlag, comment);
    }
  }

  public Parameter withComment(Expression newComment) {
    return newComment.equals(comment)
        ? this
        : new Parameter(formal, javaAnnotations, defaultValue, hasDefaultFlag,
                        constructor, hasConstructorFlag, newComment);
  }

  public static final Function<Parameter, FormalParameter> GET_FORMAL =
        new Function<Parameter, FormalParameter>() {
      public FormalParameter apply(Parameter from) {
        return from.getFormalParameter();
      }
    };  

  public FormalParameter getFormalParameter() {
    return formal;
  }

  /**
   * @return the primary name of this {@code Parameter}.  This is
   * always what is specified in the name parameter in the gxp.
   */
  public String getPrimaryName() {
    return formal.getPrimaryName();
  }

  /**
   * @return the entire list of names this {@code Parameter} can accept
   */
  public Set<String> getNames() {
    return formal.getNames();
  }

  public boolean consumesContent() {
    return formal.consumesContent();
  }

  public List<JavaAnnotation> getJavaAnnotations() {
    return javaAnnotations;
  }

  public Type getType() {
    return formal.getType();
  }

  public Expression getDefaultValue() {
    return defaultValue;
  }

  public boolean hasDefaultFlag() {
    return hasDefaultFlag;
  }

  public Expression getConstructor() {
    return constructor;
  }

  public boolean hasConstructorFlag() {
    return hasConstructorFlag;
  }

  public Pattern getRegex() {
    return formal.getRegex();
  }

  public boolean regexMatches(ObjectConstant oc) {
    return formal.regexMatches(oc);
  }

  public SpaceOperatorSet getSpaceOperators() {
    return formal.getSpaceOperators();
  }

  // TODO(laurence): something needs to verify that this eventually turns into
  // a StringConstant containing HTML.
  public Expression getComment() {
    return comment;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof Parameter && equals((Parameter) that));
  }

  public boolean equals(Parameter that) {
    return equalsAbstractNode(that)
        && Objects.equal(getFormalParameter(), that.getFormalParameter())
        && Objects.equal(getJavaAnnotations(), that.getJavaAnnotations())
        && Objects.equal(getDefaultValue(), that.getDefaultValue())
        && Objects.equal(hasDefaultFlag(), that.hasDefaultFlag())
        && Objects.equal(getConstructor(), that.getConstructor())
        && Objects.equal(hasConstructorFlag(), that.hasConstructorFlag())
        && Objects.equal(getComment(), that.getComment());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        abstractNodeHashCode(),
        getFormalParameter(),
        getJavaAnnotations(),
        getDefaultValue(),
        hasDefaultFlag(),
        getConstructor(),
        hasConstructorFlag(),
        getComment());
  }
}
