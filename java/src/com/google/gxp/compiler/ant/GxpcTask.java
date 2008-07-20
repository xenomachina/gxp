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

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.alerts.Alert.Severity;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.ConfigurableAlertPolicy;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.cli.Gxpc;
import com.google.gxp.compiler.cli.GxpcConfiguration;
import com.google.gxp.compiler.cli.Phase;
import com.google.gxp.compiler.codegen.CodeGeneratorFactory;
import com.google.gxp.compiler.codegen.DefaultCodeGeneratorFactory;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.fs.FileSystem;
import com.google.gxp.compiler.fs.SourcePathFileSystem;
import com.google.gxp.compiler.fs.SystemFileSystem;
import com.google.gxp.compiler.i18ncheck.UnextractableContentAlert;
import com.google.gxp.compiler.parser.FileSystemEntityResolver;
import com.google.gxp.compiler.parser.SourceEntityResolver;

import java.io.IOException;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Task;

/**
 * An Ant Task that can be used to invoke gxpc.
 */
public class GxpcTask extends Task implements GxpcConfiguration {
  private static final String[] DEFAULT_INCLUDES = { "**/*.gxp" };

  private final FileSystem fs = SystemFileSystem.INSTANCE;
  private SourcePathFileSystem sourcePathFs;
  private Set<FileRef> sourceFiles;
  private Set<FileRef> schemaFiles = Sets.newHashSet();
  private List<FileRef> sourcePaths;
  private FileRef outputDir;
  private SortedSet<Phase> dotPhases;

  private final FileScanner fileScanner = new DirectoryScanner();
  private String srcpaths;
  private String destdir;
  private String target;
  private boolean dynamic = false;
  private boolean i18nwarn = true;

  public GxpcTask() {
    fileScanner.setIncludes(DEFAULT_INCLUDES);
  }

  public void execute() throws BuildException {
    fileScanner.scan();
    String baseDir = fileScanner.getBasedir().getPath() + "/";

    List<FileRef> underlyingInputFiles = Lists.newArrayList();
    for (String includedFile : fileScanner.getIncludedFiles()) {
      underlyingInputFiles.add(fs.parseFilename(baseDir + includedFile));
    }

    if (destdir == null) {
      destdir = System.getProperty("user.dir");
    }
    outputDir = fs.parseFilename(destdir);

    sourcePaths = Lists.newArrayList();
    for (FileRef sourcePath : fs.parseFilenameList(srcpaths)) {
      sourcePaths.add(sourcePath);
    }
    Collections.sort(sourcePaths, FILEREF_LENGTH_COMPARATOR);

    sourcePathFs = new SourcePathFileSystem(fs,
                                            sourcePaths,
                                            underlyingInputFiles,
                                            outputDir);
    dotPhases = computeDotPhases();

    sourceFiles = Collections.unmodifiableSet(sourcePathFs.getSourceFileRefs());

    try {
      Gxpc.main(this, System.err);
    } catch (IOException e) {
      throw new BuildException(e);
    }
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
    for (String schema : schemas.split(",")) {
      schemaFiles.add(fs.parseFilename(schema));
    }
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
  // Getters (GxpcConfiguration implementation)
  ////////////////////////////////////////////////////////////////////////////////

  public Set<FileRef> getSourceFiles() {
    return sourceFiles;
  }

  public Set<FileRef> getSchemaFiles() {
    return schemaFiles;
  }

  public Set<OutputLanguage> getOutputLanguages() {
    Set<OutputLanguage> result = EnumSet.noneOf(OutputLanguage.class);
    result.add(OutputLanguage.JAVA);
    return Collections.unmodifiableSet(result);
  }

  public CodeGeneratorFactory getCodeGeneratorFactory() {
    DefaultCodeGeneratorFactory result =  new DefaultCodeGeneratorFactory();
    result.setRuntimeMessageSource(target);
    result.setDynamicModeEnabled(dynamic);
    result.setSourceFiles(getSourceFiles());
    result.setSchemaFiles(getSchemaFiles());
    result.setSourcePaths(sourcePaths);
    return result;
  }

  public Set<FileRef> getAllowedOutputFileRefs() {
    Set<FileRef> result = Sets.newHashSet();
    return Collections.unmodifiableSet(result);
  }

  public FileRef getDependencyFile() {
    return null;
  }

  public FileRef getPropertiesFile() {
    return (target != null)
        ? outputDir.join("/" + target.replace(".", "/") + "_en.properties")
        : null;
  }

  public boolean isVerboseEnabled() {
    return false;
  }

  public boolean isDebugEnabled() {
    return false;
  }

  public AlertPolicy getAlertPolicy() {
    return alertPolicySupplier.get();
  }

  public SortedSet<Phase> getDotPhases() {
    return dotPhases;
  }

  public SourceEntityResolver getEntityResolver() {
    return new FileSystemEntityResolver(sourcePathFs);
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Private Functionallity
  ////////////////////////////////////////////////////////////////////////////////

  // TODO(laurence): add more general support for AlertPolicy configuration
  private final Supplier<AlertPolicy> alertPolicySupplier =
      Suppliers.memoize(new Supplier<AlertPolicy>() {
        public AlertPolicy get() {
          ConfigurableAlertPolicy result = new ConfigurableAlertPolicy();
          if (i18nwarn) {
            result.setSeverity(UnextractableContentAlert.class,
                               Severity.WARNING);
          }
          if (true) {
            result.setTreatWarningsAsErrors(true);
          }
          return result;
        }
      });

  private static SortedSet<Phase> computeDotPhases() {
    SortedSet<Phase> result = Sets.newTreeSet();
    return Collections.unmodifiableSortedSet(result);
  }

  private static final Comparator<FileRef> FILEREF_LENGTH_COMPARATOR = new Comparator<FileRef>() {
    public int compare(FileRef f1, FileRef f2) {
      int len1 = f1.getName().length();
      int len2 = f2.getName().length();
      return (len1 > len2) ? -1 : (len1 == len2 ? 0 : 1);
    }
  };
}
