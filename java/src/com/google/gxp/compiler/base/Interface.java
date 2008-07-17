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

import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.schema.Schema;

import java.util.List;

/**
 * A GXP interface. Corresponds to a {@code <gxp:interface>} element.
 */
public final class Interface extends AbstractRoot<Interface> {
  private final InterfaceCallable interfaceCallable;

  public Interface(SourcePosition sourcePosition,
                   String displayName,
                   TemplateName.FullyQualified name,
                   Schema schema,
                   List<JavaAnnotation> javaAnnotations,
                   List<Import> imports,
                   List<ThrowsDeclaration> throwsDeclarations,
                   List<Parameter> parameters,
                   List<FormalTypeParameter> formalTypeParameters) {
    super(sourcePosition, displayName, name, schema, javaAnnotations, imports,
          throwsDeclarations, parameters, formalTypeParameters);
    interfaceCallable = new InterfaceCallable(name, schema, parameters);
  }

  public Interface(Node fromNode,
                   TemplateName.FullyQualified name,
                   Schema schema,
                   List<JavaAnnotation> javaAnnotations,
                   List<Import> imports,
                   List<ThrowsDeclaration> throwsDeclarations,
                   List<Parameter> parameters,
                   List<FormalTypeParameter> formalTypeParameters) {
    this(fromNode.getSourcePosition(), fromNode.getDisplayName(), name, schema,
         javaAnnotations, imports, throwsDeclarations, parameters,
         formalTypeParameters);
  }

  protected Interface self() {
    return this;
  }

  protected Interface withParameters(List<Parameter> newParameters) {
    return new Interface(this, getName(), getSchema(), getJavaAnnotations(),
                         getImports(), getThrowsDeclarations(), newParameters,
                         getFormalTypeParameters());
  }

  public <T> T acceptVisitor(RootVisitor<T> visitor) {
    return visitor.visitInterface(this);
  }

  public Callable getCallable() {
    // Strictly speaking, this should probably return null, but if we did that
    // then <call:SomeInterface/> would result in a CallableNotFoundError
    // when what we really want is a MissingAttributeError for the missing
    // this parameter.  See the code in Binder that decides whether to get a
    // Callable or InstanceCallable based on the presence of a this parameter
    return interfaceCallable;
  }

  public InstanceCallable getInstanceCallable() {
    return interfaceCallable;
  }

  public Implementable getImplementable() {
    return interfaceCallable;
  }

  /**
   * The {@code InstanceCallable} exposed by a {@code Template}.
   */
  @SuppressWarnings("serial") // let java pick the serialVersionUID
  private static class InterfaceCallable
      extends AbstractCallable implements Implementable, InstanceCallable  {

    public InterfaceCallable(TemplateName.FullyQualified name, Schema schema,
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
          || (that instanceof InterfaceCallable && equals((InterfaceCallable) that));
    }

    public boolean equals(InterfaceCallable that) {
      return equalsAbstractCallable(that);
    }

    @Override
    public int hashCode() {
      return abstractCallableHashCode();
    }
  }
}
