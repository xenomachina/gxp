/*
 * Copyright (C) 2009 Google Inc.
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

package com.google.gxp.base.dynamic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.gxp.compiler.Configuration;
import com.google.gxp.compiler.Phase;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.codegen.CodeGeneratorFactory;
import com.google.gxp.compiler.codegen.DefaultCodeGeneratorFactory;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.fs.FileSystem;
import com.google.gxp.compiler.fs.SourcePathFileSystem;
import com.google.gxp.compiler.parser.SourceEntityResolver;
import com.google.gxp.compiler.parser.FileSystemEntityResolver;

import java.util.Set;
import java.util.SortedSet;

/**
 * An implementation of {@code Configuration} to be used to compile
 * GXPs at runtime.
 */
public class RuntimeConfiguration implements Configuration {
  private static final ImmutableSet<OutputLanguage> OUTPUT_LANGUAGES =
      ImmutableSet.of(OutputLanguage.DYNAMIC_IMPL_JAVA);
  private static final CodeGeneratorFactory CODE_GENERATOR_FACTORY =
      new DefaultCodeGeneratorFactory();
  private static final ImmutableSortedSet<Phase> DOT_PHASES = ImmutableSortedSet.of();

  private final ImmutableSet<FileRef> sourceFiles;
  private final ImmutableSet<FileRef> schemaFiles;
  private final ImmutableSet<FileRef> allowedOutputFiles;
  private final long compilationVersion;
  private final SourceEntityResolver sourceEntityResolver;
  private final AlertPolicy alertPolicy;

  public RuntimeConfiguration(FileSystem inputFileSystem,
                              FileSystem outputFileSystem,
                              Set<FileRef> sourceFiles,
                              Set<FileRef> sourceSchemas,
                              Set<FileRef> sourcePaths,
                              String outputFile,
                              long compilationVersion,
                              AlertPolicy alertPolicy) {

    SourcePathFileSystem sourcePathFs = new SourcePathFileSystem(inputFileSystem,
                                                                 sourcePaths,
                                                                 sourceFiles,
                                                                 outputFileSystem.getRoot());

    this.sourceFiles = ImmutableSet.copyOf(sourcePathFs.getSourceFileRefs());
    this.schemaFiles = ImmutableSet.copyOf(sourceSchemas);
    this.sourceEntityResolver = new FileSystemEntityResolver(sourcePathFs);
    this.allowedOutputFiles = ImmutableSet.of(sourcePathFs.parseFilename(outputFile));
    this.compilationVersion = compilationVersion;
    this.alertPolicy = Preconditions.checkNotNull(alertPolicy);
  }

  @Override
  public Set<FileRef> getSourceFiles() {
    return sourceFiles;
  }

  @Override
  public Set<FileRef> getSchemaFiles() {
    return schemaFiles;
  }

  @Override
  public Set<OutputLanguage> getOutputLanguages() {
    return OUTPUT_LANGUAGES;
  }

  @Override
  public long getCompilationVersion() {
    return compilationVersion;
  }

  @Override
  public CodeGeneratorFactory getCodeGeneratorFactory() {
    return CODE_GENERATOR_FACTORY;
  }

  @Override
  public Set<FileRef> getAllowedOutputFiles() {
    return allowedOutputFiles;
  }

  @Override
  public FileRef getDependencyFile() {
    return null;
  }

  @Override
  public FileRef getPropertiesFile() {
    return null;
  }

  @Override
  public boolean isDebugEnabled() {
    return false;
  }

  @Override
  public AlertPolicy getAlertPolicy() {
    return alertPolicy;
  }

  @Override
  public SortedSet<Phase> getDotPhases() {
    return DOT_PHASES;
  }

  @Override
  public SourceEntityResolver getEntityResolver() {
    return sourceEntityResolver;
  }
}
