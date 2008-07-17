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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gxp.compiler.alerts.SourcePosition;

import java.util.*;

/**
 * A template constructor. Corresponds to a {@code <gxp:constructor>} element.
 */
public class Constructor extends AbstractNode {
  private final ImmutableList<JavaAnnotation> javaAnnotations;
  private final ImmutableList<Parameter> parameters;

  public Constructor(SourcePosition sourcePosition,
                     String displayName,
                     List<JavaAnnotation> javaAnnotations,
                     List<Parameter> parameters) {
    super(sourcePosition, displayName);
    this.javaAnnotations = ImmutableList.copyOf(javaAnnotations);
    this.parameters = ImmutableList.copyOf(parameters);
  }

  public Constructor(Node fromNode,
                     List<JavaAnnotation> javaAnnotations,
                     List<Parameter> parameters) {
    this(fromNode.getSourcePosition(), fromNode.getDisplayName(),
         javaAnnotations, parameters);
  }

  public static Constructor empty(SourcePosition sourcePosition, String displayName) {
    return new Constructor(sourcePosition, displayName,
                           Collections.<JavaAnnotation>emptyList(),
                           Collections.<Parameter>emptyList());
  }

  public static Constructor empty(Node fromNode) {
    return empty(fromNode.getSourcePosition(), fromNode.getDisplayName());
  }

  public Constructor withParameters(List<Parameter> newParameters) {
    return Iterables.elementsEqual(parameters, newParameters)
        ? this
        : new Constructor(this, javaAnnotations, newParameters);
  }

  public Constructor transformParameters(Function<Parameter, Parameter> parameterTransformer) {
    List<Parameter> newParameters =
        Util.map(parameters, parameterTransformer);
    return withParameters(newParameters);
  }

  public List<JavaAnnotation> getJavaAnnotations() {
    return javaAnnotations;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof Constructor && equals((Constructor) that));
  }

  public boolean equals(Constructor that) {
    return equalsAbstractNode(that)
        && Objects.equal(getJavaAnnotations(), that.getJavaAnnotations())
        && Objects.equal(getParameters(), that.getParameters());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        abstractNodeHashCode(),
        getJavaAnnotations(),
        getParameters());
  }
}
