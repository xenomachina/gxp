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
 * {@code TypeVisitor} that do the same thing for almost all types of
 * {@code Type}.
 *
 * @param <T> return type of visitor
 */
public abstract class DefaultingTypeVisitor<T> implements TypeVisitor<T> {
  /**
   * Subclasses should override this to perform the default visit operation,
   * and also override any other visit methods where the behaviour should
   * deviate from the default.
   */
  protected abstract T defaultVisitType(Type type);

  public T visitBooleanType(BooleanType type) {
    return defaultVisitType(type);
  }

  public T visitBundleType(BundleType type) {
    return defaultVisitType(type);
  }

  public T visitContentType(ContentType type) {
    return defaultVisitType(type);
  }

  public T visitInstanceType(InstanceType type) {
    return defaultVisitType(type);
  }

  public T visitNativeType(NativeType type) {
    return defaultVisitType(type);
  }

  public T visitTemplateType(TemplateType type) {
    return defaultVisitType(type);
  }
}
