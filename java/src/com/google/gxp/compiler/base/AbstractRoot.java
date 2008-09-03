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
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.schema.Schema;

import java.util.*;

/**
 * An abstract implementation of {@code Root}.
 */
public abstract class AbstractRoot<T extends AbstractRoot> extends AbstractNode implements Root {
  private final TemplateName.FullyQualified name;
  private final Schema schema;
  private final ImmutableList<JavaAnnotation> javaAnnotations;
  private final ImmutableList<Parameter> parameters;
  private final ImmutableList<Import> imports;
  private final ImmutableList<ThrowsDeclaration> throwsDeclarations;
  private final ImmutableList<FormalTypeParameter> formalTypeParameters;

  protected AbstractRoot(SourcePosition pos,
                         String displayName,
                         TemplateName.FullyQualified name,
                         Schema schema,
                         List<JavaAnnotation> javaAnnotations,
                         List<Import> imports,
                         List<ThrowsDeclaration> throwsDeclarations,
                         List<Parameter> parameters,
                         List<FormalTypeParameter> formalTypeParameters) {
    super(pos, displayName);
    this.name = Preconditions.checkNotNull(name);
    this.schema = Preconditions.checkNotNull(schema);
    this.javaAnnotations = ImmutableList.copyOf(javaAnnotations);
    this.imports = ImmutableList.copyOf(imports);
    this.throwsDeclarations = ImmutableList.copyOf(throwsDeclarations);
    this.parameters = ImmutableList.copyOf(parameters);
    this.formalTypeParameters = ImmutableList.copyOf(formalTypeParameters);
  }

  protected abstract T self();

  protected abstract T withParameters(List<Parameter> newParameters);

  public T transformParameters(Function<Parameter, Parameter> parameterTransformer) {
    List<Parameter> newParameters = Util.map(getParameters(), parameterTransformer);

    return Iterables.elementsEqual(getParameters(), newParameters)
        ? self()
        : withParameters(newParameters);
  }

  public TemplateName.FullyQualified getName() {
    return name;
  }

  public Schema getSchema() {
    return schema;
  }

  protected List<JavaAnnotation> getJavaAnnotations() {
    return javaAnnotations;
  }

  /**
   * @return all {@link JavaAnnotation}s that are annotating a specific item.
   */
  public Iterable<JavaAnnotation> getJavaAnnotations(final JavaAnnotation.Element element) {
    return Iterables.filter(javaAnnotations, new Predicate<JavaAnnotation>() {
      public boolean apply(JavaAnnotation javaAnnotation) {
        return Objects.equal(javaAnnotation.getElement(), element);
      }
    });
  }

  public List<Import> getImports() {
    return imports;
  }

  public List<ThrowsDeclaration> getThrowsDeclarations() {
    return throwsDeclarations;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  public Parameter getParameterByPrimary(String paramName) {
    for (Parameter parameter : getParameters()) {
      if (paramName.equals(parameter.getPrimaryName())) {
        return parameter;
      }
    }
    return null;
  }

  public List<FormalTypeParameter> getFormalTypeParameters() {
    return formalTypeParameters;
  }
}
