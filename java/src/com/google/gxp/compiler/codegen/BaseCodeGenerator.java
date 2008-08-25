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

package com.google.gxp.compiler.codegen;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.io.Characters;
import com.google.gxp.compiler.base.FormalParameter;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.Tree;

import java.io.*;

/**
 * Abstract Base Class for all {@code CodeGenerator}s.
 */
public abstract class BaseCodeGenerator<T extends Tree<Root>> implements CodeGenerator {
  protected final T tree;

  public BaseCodeGenerator(T tree) {
    this.tree = Objects.nonNull(tree);
  }

  /**
   * Loads a .format resource relative to /com/google/gxp/compiler/ and
   * returns the contents as a {@code String}.
   */
  protected static String loadFormat(String name) {
    String resourceName = "/com/google/gxp/compiler/" + name + ".format";
    InputStream stream = BaseCodeGenerator.class.getResourceAsStream(resourceName);
    try {
      if (stream == null) {
        throw new FileNotFoundException("Can't load resource " + resourceName);
      }
      return Characters.toString(new InputStreamReader(stream, Charsets.US_ASCII)).trim();
    } catch (IOException e) {
      // If this happens then something is seriously broken.
      throw new RuntimeException(e);
    }
  }

  protected static String getDefaultMethodName(FormalParameter param) {
    String s = param.getPrimaryName();
    s = Character.toUpperCase(s.charAt(0)) + s.substring(1);

    return "getDefault" + s;
  }

  protected static String getDefaultMethodName(Parameter param) {
    return getDefaultMethodName(param.getFormalParameter());
  }
}
