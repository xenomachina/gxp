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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.schema.Schema;

import java.util.List;

/**
 * A GXP template. Corresponds to a {@code <gxp:template>} element.
 */
public final class Template extends AbstractRoot<Template> {
  private final Constructor constructor;
  private final ImmutableList<Parameter> allParameters;
  private final ImmutableList<ImplementsDeclaration> implementsDeclarations;
  private final Expression content;
  private final Callable callable;
  private final InstanceCallable instanceCallable;

  public Template(SourcePosition sourcePosition,
                  String displayName,
                  TemplateName.FullyQualified name,
                  Schema schema,
                  List<JavaAnnotation> javaAnnotations,
                  Constructor constructor,
                  List<Import> imports,
                  List<ImplementsDeclaration> implementsDeclarations,
                  List<ThrowsDeclaration> throwsDeclarations,
                  List<Parameter> parameters,
                  List<FormalTypeParameter> formalTypeParameters,
                  Expression content) {
    super(sourcePosition, displayName, name, schema, javaAnnotations,
          imports, throwsDeclarations, parameters, formalTypeParameters);
    this.constructor = Preconditions.checkNotNull(constructor);
    this.allParameters = ImmutableList.copyOf(
        Iterables.concat(constructor.getParameters(), getParameters()));
    this.implementsDeclarations = ImmutableList.copyOf(implementsDeclarations);
    this.content = Preconditions.checkNotNull(content);

    this.callable = new TemplateCallable(name, schema, getAllParameters());

    List<Parameter> params = Lists.newArrayList();
    params.addAll(getParameters());
    params.add(new Parameter(new FormalParameter(sourcePosition, Implementable.INSTANCE_PARAM_NAME,
                                                 Implementable.INSTANCE_PARAM_NAME,
                                                 new InstanceType(this, name))));
    this.instanceCallable = new TemplateInstanceCallable(name, schema, params);
  }

  public Template(Node fromNode,
                  TemplateName.FullyQualified name,
                  Schema schema,
                  List<JavaAnnotation> javaAnnotations,
                  Constructor constructor,
                  List<Import> imports,
                  List<ImplementsDeclaration> implementsDeclarations,
                  List<ThrowsDeclaration> throwsDeclarations,
                  List<Parameter> parameters,
                  List<FormalTypeParameter> formalTypeParameters,
                  Expression content) {
    this(fromNode.getSourcePosition(), fromNode.getDisplayName(),
         name, schema, javaAnnotations, constructor, imports,
         implementsDeclarations, throwsDeclarations, parameters,
         formalTypeParameters, content);
  }

  protected Template self() {
    return this;
  }

  protected Template withParameters(List<Parameter> newParameters) {
    return new Template(this, getName(), getSchema(), getJavaAnnotations(),
                        constructor, getImports(), getImplementsDeclarations(),
                        getThrowsDeclarations(), newParameters,
                        getFormalTypeParameters(), content);
  }

  public Template withContent(Expression newContent) {
    return newContent.equals(content)
        ? this
        : new Template(this, getName(), getSchema(), getJavaAnnotations(),
                       constructor, getImports(), getImplementsDeclarations(),
                       getThrowsDeclarations(), getParameters(),
                       getFormalTypeParameters(), newContent);
  }

  public Template withConstructor(Constructor newConstructor) {
    return constructor.equals(newConstructor)
        ? this
        : new Template(this, getName(), getSchema(), getJavaAnnotations(),
                       newConstructor, getImports(), getImplementsDeclarations(),
                       getThrowsDeclarations(), getParameters(),
                       getFormalTypeParameters(), content);
  }

  public Template withImplementsDeclarations(List<ImplementsDeclaration> newImplDec) {
    return implementsDeclarations.equals(newImplDec)
        ? this
        : new Template(this, getName(), getSchema(), getJavaAnnotations(),
                       getConstructor(), getImports(), newImplDec,
                       getThrowsDeclarations(), getParameters(),
                       getFormalTypeParameters(), content);
  }

  public <T> T acceptVisitor(RootVisitor<T> visitor) {
    return visitor.visitTemplate(this);
  }

  public Constructor getConstructor() {
    return constructor;
  }

  public List<Parameter> getAllParameters() {
    return allParameters;
  }

  public List<ImplementsDeclaration> getImplementsDeclarations() {
    return implementsDeclarations;
  }

  public Expression getContent() {
    return content;
  }

  public Callable getCallable() {
    return callable;
  }

  public InstanceCallable getInstanceCallable() {
    return instanceCallable;
  }

  public Implementable getImplementable() {
    return null;
  }

  /**
   * The {@code Callable} exposed by a {@code Template}.
   */
  @SuppressWarnings("serial") // let java pick the serialVersionUID
  private static class TemplateCallable extends AbstractCallable {
    public TemplateCallable(TemplateName.FullyQualified name, Schema schema,
                            List<Parameter> parameters) {
      super(name, schema, parameters);
    }

    public <T> T acceptCallableVisitor(CallableVisitor<T> visitor) {
      return visitor.visitCallable(this);
    }

    @Override
    public boolean equals(Object that) {
      return this == that
          || (that instanceof TemplateCallable && equals((TemplateCallable) that));
    }

    public boolean equals(TemplateCallable that) {
      return equalsAbstractCallable(that);
    }

    @Override
    public int hashCode() {
      return abstractCallableHashCode();
    }
  }

  /**
   * The {@code InstanceCallable} exposed by a {@code Template}.
   */
  @SuppressWarnings("serial") // let java pick the serialVersionUID
  private static class TemplateInstanceCallable
      extends AbstractCallable implements InstanceCallable {

    public TemplateInstanceCallable(TemplateName.FullyQualified name, Schema schema,
                                    List<Parameter> parameters) {
      super(name, schema, parameters);
    }

    public Type getInstanceType() {
      return getParameter(Implementable.INSTANCE_PARAM_NAME).getType();
    }

    public <T> T acceptCallableVisitor(CallableVisitor<T> visitor) {
      return visitor.visitInstanceCallable(this);
    }

    @Override
    public boolean equals(Object that) {
      return this == that
          || (that instanceof TemplateInstanceCallable && equals((TemplateInstanceCallable) that));
    }

    public boolean equals(TemplateInstanceCallable that) {
      return equalsAbstractCallable(that);
    }

    @Override
    public int hashCode() {
      return abstractCallableHashCode();
    }
  }
}
