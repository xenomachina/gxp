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
 * A visitor over {@link Namespace}s.
 *
 * @param <T> return type of visitor
 */
public interface NamespaceVisitor<T> {
  T visitCallNamespace(CallNamespace ns);
  T visitCppNamespace(CppNamespace ns);
  T visitExprNamespace(ExprNamespace ns);
  T visitGxpNamespace(GxpNamespace ns);
  T visitJavaNamespace(JavaNamespace ns);
  T visitMsgNamespace(MsgNamespace ns);
  T visitNoMsgNamespace(NoMsgNamespace ns);
  T visitNullNamespace(NullNamespace ns);
  T visitOutputNamespace(OutputNamespace ns);
}
