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
 * Abstract base class useful for creating implementations of
 * {@code ImportVisitor} that do the same thing for almost all types of
 * {@code Import}.
 *
 * @param <T> return type of visitor
 */
public abstract class DefaultingImportVisitor<T> implements ImportVisitor<T> {
  /**
   * Subclasses should override this to perform the default visit operation,
   * and also override any other visit methods where the behavior should
   * deviate from the default.
   */
  protected abstract T defaultVisitImport(Import imp);

  public T visitClassImport(ClassImport imp) {
    return defaultVisitImport(imp);
  }

  public T visitPackageImport(PackageImport imp) {
    return defaultVisitImport(imp);
  }

  public T visitCppFileImport(CppFileImport imp) {
    return defaultVisitImport(imp);
  }

  public T visitCppLibraryImport(CppLibraryImport imp) {
    return defaultVisitImport(imp);
  }
}
