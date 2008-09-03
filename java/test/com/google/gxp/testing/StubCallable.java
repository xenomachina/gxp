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

package com.google.gxp.testing;

import com.google.common.base.Preconditions;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.CallableVisitor;
import com.google.gxp.compiler.base.FormalParameter;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.schema.Schema;

import java.util.List;

/**
 * Stub implementation of {@link Callable}.
 */
public class StubCallable implements Callable {
  private final String name;

  public StubCallable(String name) {
    this.name = Preconditions.checkNotNull(name);
  }

  public String toString() {
    return name;
  }

  public TemplateName.FullyQualified getName() {
    throw new UnsupportedOperationException();
  }

  public Schema getSchema() {
    throw new UnsupportedOperationException();
  }

  public List<FormalParameter> getParameters() {
    throw new UnsupportedOperationException();
  }

  public FormalParameter getParameter(String paramName) {
    throw new UnsupportedOperationException();
  }

  public FormalParameter getParameterByPrimary(String paramName) {
    throw new UnsupportedOperationException();
  }

  public FormalParameter getContentConsumingParameter() {
    throw new UnsupportedOperationException();
  }

  public <T> T acceptCallableVisitor(CallableVisitor<T> visitor) {
    throw new UnsupportedOperationException();
  }
}
