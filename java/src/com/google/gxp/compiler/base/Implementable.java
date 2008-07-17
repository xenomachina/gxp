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

import com.google.common.base.Predicate;

/**
 * Represents an interface created in gxp. Anything that is Implementable
 * can only be called when you provide a concrete "this" instance. Any
 * template that implements an Implementable should only require the
 * parameters defined in the implementable.
 */
public interface Implementable extends Callable {
  static final String INSTANCE_PARAM_NAME = "this";
  static final Predicate<Parameter> NOT_INSTANCE_PARAM =
      new Predicate<Parameter>() {
        public boolean apply(Parameter parameter) {
          return !INSTANCE_PARAM_NAME.equals(parameter.getPrimaryName());
        }
      };
}
