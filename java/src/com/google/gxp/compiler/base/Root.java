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

import com.google.gxp.compiler.schema.Schema;

import java.util.List;

/**
 * A {@code Node} that can be the Root of a GXP document.
 *
 * TODO(harryh): it's prolly somewhat sketchy that this has getImports()
 *               but I think this is OK for now.
 */
public interface Root extends Node {
  /**
   * @return the fully qualified name of this {@code Root}
   */
  TemplateName.FullyQualified getName();

  /**
   * @return the {@code Schema} of this {@code Root}'s output.
   */
  Schema getSchema();

  /**
   * Implementation of Visitor Pattern.
   */
  <T> T acceptVisitor(RootVisitor<T> visitor);

  /**
   * @return the {@link Callable} this {@link Root} exposes.
   */
  Callable getCallable();

  /**
   * @return the {@link InstanceCallable} this {@link Root} exposes.
   */
  InstanceCallable getInstanceCallable();

  /**
   * @return the {@link Implementable} this {@link Root} exposes.
   */
  Implementable getImplementable();

  List<Import> getImports();
}
