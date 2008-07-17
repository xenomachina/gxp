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

import java.io.Serializable;
import java.util.List;

/**
 * Something that can be "called", eg: with a {@code <call:*>} element.
 */
public interface Callable extends Serializable {
  /**
   * @return the fully qualified name of this {@code Callable}
   */
  TemplateName.FullyQualified getName();

  /**
   * @return the {@code Schema} of this {@code Callable}'s output.
   */
  Schema getSchema();

  /**
   * @return the {@code FormalParameter}s of this {@code Callable} in the order
   * in which they were declared
   */
  List<FormalParameter> getParameters();

  /**
   * @return the {@code FormalParameter} with the specified name from this
   * {@code Callable}
   */
  FormalParameter getParameter(String name);

  /**
   * @return a {@code FormalParameter} based on it's primary name
   */
  public FormalParameter getParameterByPrimary(String paramName);

  /**
   * @return the {@code FormalParameter} for content, or null if there is no
   * content parameter.
   */
  FormalParameter getContentConsumingParameter();

  <T> T acceptCallableVisitor(CallableVisitor<T> visitor);
}
