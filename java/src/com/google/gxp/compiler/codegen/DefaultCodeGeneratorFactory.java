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

import com.google.gxp.compiler.CompilationUnit;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.DefaultAlertPolicy;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.base.OutputLanguageVisitor;
import com.google.gxp.compiler.cpp.CppCodeGenerator;
import com.google.gxp.compiler.cpp.CppHeaderCodeGenerator;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.java.DynamicImplJavaCodeGenerator;
import com.google.gxp.compiler.java.DynamicStubJavaCodeGenerator;
import com.google.gxp.compiler.java.JavaCodeGenerator;
import com.google.gxp.compiler.js.JavaScriptCodeGenerator;
import com.google.gxp.compiler.xmb.XmbCodeGenerator;

import java.util.Collection;

/**
 * Default implementation of {@code CodeGeneratorFactory}. Note that instances
 * of this class <em>are</em> mutable. In particular, the individual "options"
 * (which are typically only used for constructing certain types of
 * {@code CodeGenerator}s) can be modified via setters.
 */
public class DefaultCodeGeneratorFactory implements CodeGeneratorFactory {
  private String runtimeMessageSource = null;
  private boolean dynamicModeEnabled = false;
  private Collection<FileRef> sourceFiles = null;
  private Collection<FileRef> schemaFiles = null;
  private Collection<FileRef> sourcePaths = null;
  private AlertPolicy alertPolicy = DefaultAlertPolicy.INSTANCE;

  public String getRuntimeMessageSource() {
    return runtimeMessageSource;
  }

  /**
   * Sets the "message source" to use at runtime, or null if none was supplied.
   * In Java this corresponds to the baseName for the {@link
   * java.util.ResourceBundle ResourceBundle}.
   *
   * <p>Note that not setting this (that is, leaving it set to null) may
   * prevent the compilation of GXPs that contain {@code <gxp:msg>} elements.
   *
   * @see com.google.i18n.MessageBundle
   */
  public void setRuntimeMessageSource(String runtimeMessageSource) {
    this.runtimeMessageSource = runtimeMessageSource;
  }

  public boolean isDynamicModeEnabled() {
    return dynamicModeEnabled;
  }

  /**
   * Sets whether compiled code should attempt to dynamically reload and
   * recompile GXPs at runtime.
   */
  public void setDynamicModeEnabled(boolean dynamicModeEnabled) {
    this.dynamicModeEnabled = dynamicModeEnabled;
  }

  /**
   * Sets the source files used when compiling this set of GXPs.  Needed
   * when creating runtime stubs.
   */
  public void setSourceFiles(Collection<FileRef> sourceFiles) {
    this.sourceFiles = sourceFiles;
  }

  /**
   * Sets the schema files used when compiling this set of GXPs.  Needed
   * when creating runtime stubs.  This is only the user-provided schemas,
   * not the built-in ones.
   */
  public void setSchemaFiles(Collection<FileRef> schemaFiles) {
    this.schemaFiles = schemaFiles;
  }

  /**
   * Sets the source paths used when compiling this set of GXPs.  Needed
   * when creating runtime stubs.
   */
  public void setSourcePaths(Collection<FileRef> sourcePaths) {
    this.sourcePaths = sourcePaths;
  }

  /**
   * Sets the {@link AlertPolicy} used when compiling this set of GXPs.  Needed
   * when creating runtime stubs.
   */
  public void setAlertPolicy(AlertPolicy alertPolicy) {
    this.alertPolicy = alertPolicy;
  }

  public CodeGenerator getCodeGenerator(OutputLanguage outputLanguage,
                                        CompilationUnit compilationUnit) {
    return outputLanguage.acceptVisitor(visitor, compilationUnit);
  }

  private final OutputLanguageVisitor<CompilationUnit, CodeGenerator>
      visitor = new OutputLanguageVisitor<CompilationUnit, CodeGenerator>() {
          public CodeGenerator visitCpp(CompilationUnit cUnit) {
            return new CppCodeGenerator(cUnit.getMessageExtractedTree());
          }

          public CodeGenerator visitCppHeader(CompilationUnit cUnit) {
            return new CppHeaderCodeGenerator(cUnit.getMessageExtractedTree(),
                                              cUnit.getBoundTree().getRequirements());
          }

          public CodeGenerator visitJava(CompilationUnit cUnit) {
            if (dynamicModeEnabled) {
              return new DynamicStubJavaCodeGenerator(
                  cUnit.getMessageExtractedTree(),
                  sourceFiles, schemaFiles, sourcePaths, alertPolicy);
            } else {
              return new JavaCodeGenerator(cUnit.getMessageExtractedTree(),
                                           runtimeMessageSource);
            }
          }

          public CodeGenerator visitDynamicImplJava(CompilationUnit cUnit) {
            return new DynamicImplJavaCodeGenerator(
                cUnit.getMessageExtractedTree(),
                cUnit.getCompilationVersion());
          }

          public CodeGenerator visitJavaScript(CompilationUnit cUnit) {
            return new JavaScriptCodeGenerator(cUnit.getMessageExtractedTree(),
                                               cUnit.getBoundTree().getRequirements());
          }

          public CodeGenerator visitXmb(CompilationUnit cUnit) {
            return new XmbCodeGenerator(cUnit.getMessageExtractedTree());
          }
      };
}
