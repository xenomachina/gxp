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

import com.google.gxp.compiler.alerts.AlertSink;

import java.io.IOException;

/**
 * Something that generates code. A {@code CodeGenerator} is typically obtained
 * by calling {@link
 * com.google.gxp.compiler.CompilationUnit#getCodeGenerator(OutputLanguage)}
 */
public interface CodeGenerator {
  /**
   * Generates code to the specified {@code Appendable}.
   *
   * @param appendable the place code should be generated
   * @param sink the alert sink for
   * {@link com.google.gxp.compiler.alerts.Alert}s
   * encountered while generating the code.
   */
  void generateCode(Appendable appendable, AlertSink sink) throws IOException;
}
