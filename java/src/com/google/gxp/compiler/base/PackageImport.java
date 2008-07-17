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

import com.google.common.base.Objects;
import com.google.gxp.compiler.alerts.SourcePosition;

/**
 * A GXP class import. Corresponds to a &lt;gxp:import&gt; element with a
 * {@code package} attribute.
 */
public class PackageImport extends Import {
  private final String packageName;

  public PackageImport(SourcePosition sourcePosition, String displayName,
                       String packageName) {
    super(sourcePosition, displayName);
    this.packageName = Objects.nonNull(packageName);
  }

  public PackageImport(Node fromNode, String packageName) {
    super(fromNode);
    this.packageName = Objects.nonNull(packageName);
  }

  public String getPackageName() {
    return packageName;
  }

  @Override
  public <T> T acceptVisitor(ImportVisitor<T> visitor) {
    return visitor.visitPackageImport(this);
  }

  @Override
  public String getTarget() {
    return getPackageName();
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof PackageImport) && equals((PackageImport)that);
  }

  public boolean equals(PackageImport that) {
    return getPackageName().equals(that.getPackageName());
  }
}
