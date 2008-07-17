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

package com.google.gxp.compiler.servicedir;

import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.Implementable;
import com.google.gxp.compiler.base.InstanceCallable;
import com.google.gxp.compiler.base.TemplateName;

/**
 * A directory of services available from (typically) other compilation units.
 */
public interface ServiceDirectory {
  /**
   * Finds a Callable resource based on the specified TemplateName.  Note that
   * implementations are not required to support unqualified {@code
   * TemplateName}s.
   *
   * @param templateName name of {@code Callable} to look up.
   * @return {@code Callable} referred to by the specified {@code
   * TemplateName}
   * @throws IllegalArgumentException if {@code templateName} is unqualified
   * and the implementation doesn't support unqualified {@code TemplateName}s.
   */
  Callable getCallable(TemplateName templateName);

  /**
   * Finds an {@code InstanceCallable} based in the specified {@code
   * TemplateName}. Note that implementations are not required to support
   * unqualified {@code TemplateName}s.
   *
   * @param templateName name of {@code InstanceCallable} to look up.
   * @return {@code InstanceCallable} referred to by the specified {@code
   * TemplateName}
   * @throws IllegalArgumentException if {@code templateName} is unqualified
   * and the implementation doesn't support unqualified {@code TemplateName}s.
   */
  InstanceCallable getInstanceCallable(TemplateName templateName);

  /**
   * Finds a {@link Implementable} based on the specified TemplateName.  Note that
   * implementations are not required to support unqualified {@code
   * TemplateName}s.
   *
   * @param interfaceName name of {@code Implementable} to look up.
   * @return {@code Implementable} referred to by the specified {@code
   * TemplateName}, or null if it cannot find one
   * @throws IllegalArgumentException if {@code templateName} is unqualified
   * and the implementation doesn't support unqualified {@code TemplateName}s.
   */
  Implementable getImplementable(TemplateName interfaceName);
}
