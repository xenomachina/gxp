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

import com.google.common.base.Preconditions;

import java.io.IOException;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

/**
 * Implementation of {@code JavaFileManager} for use by the gxp compiler.
 */
public class JavaFileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {
  private final FileSystem fileSystem;

  public JavaFileManagerImpl(JavaFileManager javaFileManager, FileSystem fileSystem) {
    super(javaFileManager);
    this.fileSystem = Preconditions.checkNotNull(fileSystem);
  }

  /**
   * @return a {@code JavaFileRef} from {@code fileSystem}.
   */
  private JavaFileRef getJavaFileRef(String className, Kind kind) {
    String filename = "/" + className.replace('.', '/') + kind.extension;
    return new JavaFileRef(fileSystem.parseFilename(filename));
  }

  public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind)
      throws IOException {
    return (location.equals(StandardLocation.SOURCE_PATH)
            || location.equals(StandardLocation.CLASS_OUTPUT))
        ? getJavaFileRef(className, kind)
        : super.getJavaFileForInput(location, className, kind);
  }

  public JavaFileObject getJavaFileForOutput(Location location, String className,
                                             JavaFileObject.Kind kind, FileObject sibling)
      throws IOException {
    return (location.equals(StandardLocation.CLASS_OUTPUT))
        ? getJavaFileRef(className, kind)
        : super.getJavaFileForOutput(location, className, kind, sibling);
  }

  public String inferBinaryName(Location location, JavaFileObject file) {
    // I'm not entirely sure why the StandardFileManager doesn't do this right
    if (location.equals(StandardLocation.CLASS_OUTPUT)) {
      // looks like: /com/google/gxp/compiler/dynamictests/HelloWorld$Impl1.class
      String name = file.getName();
      name = name.substring(1, name.length() - 6).replace("/", ".");
      // now looks like: com.google.gxp.compiler.dynamictests.HelloWorld$Impl1
      return name;
    }
    return super.inferBinaryName(location, file);
  }
}
