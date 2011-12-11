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

/**
 * Visitor for {@link OutputLanguage}s. This interface deviates from the
 * typical visitor interface in that the visit methods take a parameter other
 * than the visited object. (the visited object is implied by the method)
 *
 * @param <K> parameter type for visitor
 * @param <V> return type of visitor
 */
public interface OutputLanguageVisitor<K, V> {
  V visitCpp(K arg);
  V visitCppHeader(K arg);
  V visitJava(K arg);
  V visitDynamicImplJava(K arg);
  V visitJavaScript(K arg);
  V visitScala(K arg);
  V visitXmb(K arg);
}
