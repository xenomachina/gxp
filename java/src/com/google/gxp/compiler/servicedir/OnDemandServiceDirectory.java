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

import com.google.common.base.Objects;
import com.google.common.collect.ReferenceCache;
import com.google.gxp.compiler.CompilationSet;
import com.google.gxp.compiler.CompilationUnit;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.Implementable;
import com.google.gxp.compiler.base.InstanceCallable;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.TemplateName;

/**
 * A {@link ServiceDirectory} that only computes {@code Callable}s as they
 * are requested.
 */
public class OnDemandServiceDirectory implements ServiceDirectory {
  private final CompilationSet compilationSet;
  private final ReferenceCache<TemplateName.FullyQualified, Root> signatures =
    new ReferenceCache<TemplateName.FullyQualified, Root>() {
      private static final long serialVersionUID = 1;
      @Override
      public Root create(TemplateName.FullyQualified templateName) {
        CompilationUnit compilationUnit = compilationSet.getCompilationUnit(templateName);
        return (compilationUnit == null)
            ? null
            : compilationUnit.getReparentedTree().getRoot();
      }
    };

  public OnDemandServiceDirectory(CompilationSet compilationSet) {
    this.compilationSet = Objects.nonNull(compilationSet);
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalArgumentException if {@code templateName} is not fully
   * qualified (ie: does not have a package name)
   */
  public Callable getCallable(TemplateName templateName) {
    validateTemplateName(templateName);
    Root root = signatures.get(templateName);
    return (root == null) ? null : root.getCallable();
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalArgumentException if {@code templateName} is not fully
   * qualified (ie: does not have a package name)
   */
  public InstanceCallable getInstanceCallable(TemplateName templateName) {
    validateTemplateName(templateName);
    Root root = signatures.get(templateName);
    return (root == null) ? null : root.getInstanceCallable();
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalArgumentException if {@code templateName} is not fully
   * qualified (ie: does not have a package name)
   */
  public Implementable getImplementable(TemplateName templateName) {
    validateTemplateName(templateName);
    Root root = signatures.get(templateName);
    return (root == null) ? null : root.getImplementable();
  }

  /**
   * @throws IllegalArgumentException if {@code templateName} is not fully
   * qualified (ie: does not have a package name)
   */
  private static void validateTemplateName(TemplateName templateName) {
    if (templateName.getPackageName() == null) {
      throw new IllegalArgumentException("templateName must be fully qualified");
    }
  }
}
