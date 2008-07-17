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

package com.google.gxp.compiler.fs;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.ForwardingFileObject;
import javax.tools.JavaFileObject;

/**
 * Implementation of {@code JavaFileObject}.
 */
public class JavaFileRef extends ForwardingFileObject<FileRef> implements JavaFileObject {
  public JavaFileRef(FileRef fileRef) {
    super(fileRef);
  }

  public Modifier getAccessLevel() {
    throw new UnsupportedOperationException();
  }

  public Kind getKind() {
    return fileObject.getKind();
  }

  public NestingKind getNestingKind() {
    throw new UnsupportedOperationException();
  }

  public boolean isNameCompatible(String simpleName, Kind kind) {
    String filename = getName();
    filename = filename.substring(filename.lastIndexOf('/') + 1);

    return filename.equals(simpleName + kind.extension);
  }
}
