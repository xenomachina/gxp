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
 * Visitor for {@link ParsedElement}s.
 *
 * @param <T> return type of visitor
 */
public interface ParsedElementVisitor<T> {
  // GxpNamespace elements
  T visitAbbrElement(GxpNamespace.GxpElement node);
  T visitAttrElement(GxpNamespace.GxpElement node);
  T visitClauseElement(GxpNamespace.GxpElement node);
  T visitCondElement(GxpNamespace.GxpElement node);
  T visitConstructorElement(GxpNamespace.GxpElement node);
  T visitElifElement(GxpNamespace.GxpElement node);
  T visitElseElement(GxpNamespace.GxpElement node);
  T visitEPHElement(GxpNamespace.GxpElement node);
  T visitEvalElement(GxpNamespace.GxpElement node);
  T visitIfElement(GxpNamespace.GxpElement node);
  T visitImplementsElement(GxpNamespace.GxpElement node);
  T visitImportElement(GxpNamespace.GxpElement node);
  T visitInterfaceElement(GxpNamespace.GxpElement node);
  T visitLoopElement(GxpNamespace.GxpElement node);
  T visitMsgElement(GxpNamespace.GxpElement node);
  T visitNoMsgElement(GxpNamespace.GxpElement node);
  T visitParamElement(GxpNamespace.GxpElement node);
  T visitPHElement(GxpNamespace.GxpElement node);
  T visitTemplateElement(GxpNamespace.GxpElement node);
  T visitThrowsElement(GxpNamespace.GxpElement node);
  T visitTypeParamElement(GxpNamespace.GxpElement node);

  // CallNamespace elements
  T visitCallElement(CallNamespace.CallElement node);

  // OutputNamespace elements
  T visitParsedOutputElement(OutputNamespace.ParsedOutputElement node);

  // JavaNamespace elements
  T visitJavaAnnotateElement(JavaNamespace.JavaElement node);

  // TextElement
  T visitTextElement(TextElement node);

  // NullElement
  T visitNullElement(NullElement node);
}