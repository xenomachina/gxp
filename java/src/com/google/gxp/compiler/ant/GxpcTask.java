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

package com.google.gxp.compiler.ant;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.Configuration;
import com.google.gxp.compiler.Compiler;
import com.google.gxp.compiler.Phase;
import com.google.gxp.compiler.InvalidConfigException;
import com.google.gxp.compiler.alerts.Alert.Severity;
import com.google.gxp.compiler.alerts.AlertCounter;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.ConfigurableAlertPolicy;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.codegen.CodeGeneratorFactory;
import com.google.gxp.compiler.codegen.DefaultCodeGeneratorFactory;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.fs.FileSystem;
import com.google.gxp.compiler.fs.SourcePathFileSystem;
import com.google.gxp.compiler.fs.SystemFileSystem;
import com.google.gxp.compiler.i18ncheck.UnextractableContentAlert;
import com.google.gxp.compiler.parser.FileSystemEntityResolver;
import com.google.gxp.compiler.parser.SourceEntityResolver;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * An Ant Task that can be used to invoke gxpc.
 */
public class GxpcTask extends Task implements Configuration {
  private static final String[] DEFAULT_INCLUDES = { "**/*.gxp" };

  private final FileSystem fs;
  private final FileRef cwd;
  // TODO(harryh): replace this with something that scans fs so
  //               we can write some real tests
  private final FileScanner fileScanner = new DirectoryScanner();

  private ImmutableSet<FileRef> sourceFiles;
  private ImmutableSet<FileRef> schemaFiles;
  private ImmutableSet<OutputLanguage> outputLanguages;
  private DefaultCodeGeneratorFactory codeGeneratorFactory;
  private FileRef propertiesFile;
  private AlertPolicy alertPolicy;
  private ImmutableSortedSet<Phase> dotPhases;
  private SourceEntityResolver sourceEntityResolver;

  private String srcpaths;
  private String schemas = null;
  private String destdir;
  private String target;
  private boolean dynamic = false;
  private boolean i18nwarn = false;

  public GxpcTask(FileSystem fs, FileRef cwd) {
    this.fs = Preconditions.checkNotNull(fs);
    this.cwd = Preconditions.checkNotNull(cwd);
    fileScanner.setIncludes(DEFAULT_INCLUDES);
  }

  public GxpcTask() {
    this(SystemFileSystem.INSTANCE,
         SystemFileSystem.INSTANCE.parseFilename(System.getProperty("user.dir")));
  }

  public void execute() throws BuildException {
    configure();
    AlertSink alertSink = new LoggingAlertSink(getAlertPolicy(), this);
    AlertCounter counter = new AlertCounter(alertSink, getAlertPolicy());

    try {
      new Compiler(this).call(counter);
    } catch (InvalidConfigException e) {
      throw new BuildException(e);
    }

    if (counter.getErrorCount() > 0) {
      throw new BuildException("Compile failed; see the compiler error output for details.");
    }
  }

  public void configure() throws BuildException {
    if (fileScanner.getBasedir() == null) {
      throw new BuildException("Attribute 'srcdir' was not set.");
    }

    fileScanner.scan();

    String baseDir = fileScanner.getBasedir().getPath() + File.separator;

    List<FileRef> underlyingInputFiles = Lists.newArrayList();
    for (String includedFile : fileScanner.getIncludedFiles()) {
      underlyingInputFiles.add(fs.parseFilename(baseDir + includedFile));
    }

    if (destdir == null) {
      log("Attribute 'destdir' was not set, the current working directory will be used.",
          Project.MSG_WARN);
    }
    FileRef outputDir = (destdir == null)
        ? cwd
        : fs.parseFilename(destdir);

    Set<FileRef> sourcePaths = (srcpaths == null)
        ? ImmutableSet.<FileRef>of()
        : ImmutableSet.copyOf(fs.parseFilenameList(srcpaths));

    SourcePathFileSystem sourcePathFs = new SourcePathFileSystem(fs,
                                                                 sourcePaths,
                                                                 underlyingInputFiles,
                                                                 outputDir);

    sourceFiles = ImmutableSet.copyOf(sourcePathFs.getSourceFileRefs());

    // Compute Schema Files
    schemaFiles = (schemas == null)
        ? ImmutableSet.<FileRef>of()
        : ImmutableSet.copyOf(fs.parseFilenameList(schemas));

    // Compute Output Languages
    outputLanguages = ImmutableSet.of(OutputLanguage.JAVA);

    // Compute Properties File
    propertiesFile = (target != null)
        ? outputDir.join("/" + target.replace(".", "/") + "_en.properties")
        : null;

    // Compute Alert Policy
    alertPolicy = computeAlertPolicy();

    // Compute Dot Phases
    dotPhases = computeDotPhases();

    // Compute SourceEntityResolver
    // use the sourcePathFs so that gxp:///foo/bar is resolved in a way that
    // includes both source files and genfiles
    sourceEntityResolver = new FileSystemEntityResolver(sourcePathFs);

    // Compute CodeGeneratorFactory (Always do this last)
    codeGeneratorFactory = new DefaultCodeGeneratorFactory();
    codeGeneratorFactory.setRuntimeMessageSource(target);
    codeGeneratorFactory.setDynamicModeEnabled(dynamic);
    codeGeneratorFactory.setSourceFiles(getSourceFiles());
    codeGeneratorFactory.setSchemaFiles(getSchemaFiles());
    codeGeneratorFactory.setSourcePaths(sourcePaths);
    codeGeneratorFactory.setAlertPolicy(getAlertPolicy());
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Setters
  ////////////////////////////////////////////////////////////////////////////////

  public void setSrcpaths(String srcpaths) {
    this.srcpaths = srcpaths;
  }

  public void setSrcdir(String srcdir) {
    fileScanner.setBasedir(srcdir);
  }

  public void setIncludes(String includes) {
    fileScanner.setIncludes(includes.split(","));
  }

  public void setExcludes(String excludes) {
    fileScanner.setExcludes(excludes.split(","));
  }

  public void setDestdir(String destdir) {
    this.destdir = destdir;
  }

  public void setSchemas(String schemas) {
    this.schemas = schemas;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public void setDynamic(boolean dynamic) {
    this.dynamic = dynamic;
  }

  public void setI18nwarn(boolean i18nwarn) {
    this.i18nwarn = i18nwarn;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Getters (Configuration implementation)
  ////////////////////////////////////////////////////////////////////////////////

  public Set<FileRef> getSourceFiles() {
    return sourceFiles;
  }

  public Set<FileRef> getSchemaFiles() {
    return schemaFiles;
  }

  public Set<OutputLanguage> getOutputLanguages() {
    return outputLanguages;
  }

  public long getCompilationVersion() {
    return 0;
  }

  public CodeGeneratorFactory getCodeGeneratorFactory() {
    return codeGeneratorFactory;
  }

  public Set<FileRef> getAllowedOutputFiles() {
    return ImmutableSet.<FileRef>of();
  }

  public FileRef getDependencyFile() {
    return null;
  }

  public FileRef getPropertiesFile() {
    return propertiesFile;
  }

  public boolean isDebugEnabled() {
    return false;
  }

  public AlertPolicy getAlertPolicy() {
    return alertPolicy;
  }

  public SortedSet<Phase> getDotPhases() {
    return dotPhases;
  }

  public SourceEntityResolver getEntityResolver() {
    return sourceEntityResolver;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Private Functionallity
  ////////////////////////////////////////////////////////////////////////////////

  // TODO(laurence): add more general support for AlertPolicy configuration
  private AlertPolicy computeAlertPolicy() {
    ConfigurableAlertPolicy result = new ConfigurableAlertPolicy();
    if (i18nwarn) {
      result.setSeverity(UnextractableContentAlert.class, Severity.WARNING);
    }
    result.setTreatWarningsAsErrors(true);
    return result;
  }

  private static ImmutableSortedSet<Phase> computeDotPhases() {
    return ImmutableSortedSet.<Phase>of();
  }
}
