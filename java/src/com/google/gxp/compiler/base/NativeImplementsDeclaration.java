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

import com.google.common.base.Preconditions;
import com.google.gxp.compiler.alerts.SourcePosition;

/**
 * Represents a declaration that the enclosing template implements a native
 * language (commonly java) interface.
 */
public class NativeImplementsDeclaration extends ImplementsDeclaration {
  private NativeType nativeType;

  public NativeImplementsDeclaration(SourcePosition sourcePosition,
                                     String displayName,
                                     NativeType nativeType) {
    super(sourcePosition, displayName);
    this.nativeType = Preconditions.checkNotNull(nativeType);
  }

  public NativeImplementsDeclaration(Node fromNode, NativeType nativeType) {
      this(fromNode.getSourcePosition(), fromNode.getDisplayName(), nativeType);
  }

  public NativeType getNativeType() {
    return nativeType;
  }

  public <T> T acceptImplementsVisitor(ImplementsVisitor<T> v) {
    return v.visitNativeImplementsDeclaration(this);
  }
}
