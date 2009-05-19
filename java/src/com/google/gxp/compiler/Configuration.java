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

package com.google.gxp.compiler;

import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.codegen.CodeGeneratorFactory;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.parser.SourceEntityResolver;

import java.util.Set;
import java.util.SortedSet;

/**
 * Configuration for the GXP Compiler.
 */
public interface Configuration {
  /**
   * @return the set of source files to compile.
   */
  Set<FileRef> getSourceFiles();

  /**
   * @return the set of schema files used during compilation.  These are
   *         user provided schemas.  The {@code Set} does not include any
   *         built in schemas.
   */
  Set<FileRef> getSchemaFiles();

  /**
   * @return the set of {@code OutputLanguage}s to generate.
   */
  Set<OutputLanguage> getOutputLanguages();

  /**
   * @return the compilation version to use if we are executing
   * a runtime compilation.
   */
  long getCompilationVersion();

  /**
   * @return the {@code CodeGeneratorFactory} to use.
   */
  CodeGeneratorFactory getCodeGeneratorFactory();

  /**
   * @return The set of output files we're allowed to generate, or empty if all
   * possible outputs are allowed. Note that it's an error to allow outputs
   * that aren't actually possible.
   */
  Set<FileRef> getAllowedOutputFiles();

  /**
   * @return where dependency information can be cached, or null.
   */
  FileRef getDependencyFile();

  /**
   * @return where to write a properties file, or null.
   */
  FileRef getPropertiesFile();

  /**
   * @return whether generated code should emit comments.
   */
  boolean isDebugEnabled();

  /**
   * @return the {@link AlertPolicy} to use
   */
  AlertPolicy getAlertPolicy();

  /**
   * @return the set of phases to generate dot graphs for.
   */
  SortedSet<Phase> getDotPhases();

  /**
   * @return an entity resolver used to resolve entities in DOCTYPEs, xmlns
   *   declarations, and other external entity references in the
   *   @link {#getSourceFiles source files}.
   */
  SourceEntityResolver getEntityResolver();
}
