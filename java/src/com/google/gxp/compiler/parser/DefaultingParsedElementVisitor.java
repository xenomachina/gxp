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

package com.google.gxp.compiler.parser;

/**
 * Defaulting visitor for {@link ParsedElement}s.
 *
 * @param <T> return type of visitor
 */
public abstract class DefaultingParsedElementVisitor<T>
    implements ParsedElementVisitor<T> {

  protected abstract T defaultVisitElement(ParsedElement node);

  // GxpNamespace Elements

  public T visitAbbrElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitAttrElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitClauseElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitCondElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitConstructorElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitElifElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitElseElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitEPHElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitEvalElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitIfElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitImplementsElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitImportElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitInterfaceElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitLoopElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitMsgElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitNoMsgElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitParamElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitPHElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitTemplateElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitThrowsElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  public T visitTypeParamElement(GxpNamespace.GxpElement node) {
    return defaultVisitElement(node);
  }

  // CallNamespace elements
  public T visitCallElement(CallNamespace.CallElement node) {
    return defaultVisitElement(node);
  }

  // OutputNamespace elements
  public T visitParsedOutputElement(OutputNamespace.ParsedOutputElement node) {
    return defaultVisitElement(node);
  }

  // CppNamespace elements
  public T visitCppIncludeElement(CppNamespace.CppElement node) {
    return defaultVisitElement(node);
  }

  // JavaNamespace elements
  public T visitJavaAnnotateElement(JavaNamespace.JavaElement node) {
    return defaultVisitElement(node);
  }

  // TextElement
  public T visitTextElement(TextElement node) {
    return defaultVisitElement(node);
  }

  // NullElement
  public T visitNullElement(NullElement node) {
    return defaultVisitElement(node);
  }
}
