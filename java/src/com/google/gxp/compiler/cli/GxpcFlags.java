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

package com.google.gxp.compiler.cli;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.gxp.compiler.Configuration;
import com.google.gxp.compiler.Phase;
import com.google.gxp.compiler.alerts.Alert.Severity;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.ConfigurableAlertPolicy;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.codegen.CodeGeneratorFactory;
import com.google.gxp.compiler.codegen.DefaultCodeGeneratorFactory;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.fs.FileSystem;
import com.google.gxp.compiler.fs.SourcePathFileSystem;
import com.google.gxp.compiler.i18ncheck.UnextractableContentAlert;
import com.google.gxp.compiler.parser.FileSystemEntityResolver;
import com.google.gxp.compiler.parser.SourceEntityResolver;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.*;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * The GXP compiler, "gxpc". The command line interface for generating code and
 * XMB files from GXP files.
 */
class GxpcFlags implements Configuration {
  private final CommandLine commandLine;
  private final ImmutableSet<FileRef> sourceFiles;
  private final ImmutableSet<FileRef> schemaFiles;
  private final ImmutableSet<OutputLanguage> outputLanguages;
  private final DefaultCodeGeneratorFactory codeGeneratorFactory;
  private final ImmutableSet<FileRef> allowedOutputFiles;
  private final FileRef dependencyFile;
  private final FileRef propertiesFile;
  private final boolean isVerboseEnabled;
  private final boolean isDebugEnabled;
  private final AlertPolicy alertPolicy;
  private final ImmutableSortedSet<Phase> dotPhases;
  private final SourceEntityResolver sourceEntityResolver;

  /**
   * Creates an instance of the compiler based on command-line arguments.
   *
   * @param fs underlying {@code FileSystem} that filenames in {@code args}
   * refer to
   * @param defaultDir default directory for source and output dirs
   * @param args command-line arguments. See <code>HELP_*</code> variables
   * defined in {@link GxpcFlags} for accepted flags
   * @throws Flags.UsageError if there is an error parsing the command line
   * arguments
   */
  GxpcFlags(FileSystem fs, FileRef defaultDir, String... args)
      throws CmdLineException, IOException {

    // If there is only one argument, and it starts with an '@', then treat it
    // as an options file, relative to the current working directory.
    if ((args.length == 1) && (args[0].startsWith("@"))) {
      FileRef optionsFile = defaultDir.join(args[0].substring(1));
      Reader in = optionsFile.openReader(Charsets.UTF_8);
      List<String> lines = CharStreams.readLines(in);
      in.close();
      List<String> parsedTokens = Lists.newArrayList();
      for (String line : lines) {
        for (String token : line.trim().split("\\s+")) {
          if (token.length() > 0) {
            parsedTokens.add(token);
          }
        }
      }
      args = parsedTokens.toArray(new String[parsedTokens.size()]);
    }

    commandLine = new CommandLine(args);

    Set<FileRef> underlyingInputFiles = getFileRefs(fs, commandLine.trailingArgs);

    FileRef outputDir = (commandLine.FLAG_dir == null)
        ? defaultDir : fs.parseFilename(commandLine.FLAG_dir);

    List<FileRef> sourcePaths = (commandLine.FLAG_source == null)
        ? Collections.singletonList(defaultDir)
        : fs.parseFilenameList(commandLine.FLAG_source);

    SourcePathFileSystem sourcePathFs = new SourcePathFileSystem(fs,
                                                                 sourcePaths,
                                                                 underlyingInputFiles,
                                                                 outputDir);

    sourceFiles = ImmutableSet.copyOf(sourcePathFs.getSourceFileRefs());
    schemaFiles = getFileRefs(fs, commandLine.FLAG_schema);

    // Compute Output Languages
    Set<OutputLanguage> tmpOutputLanguages = EnumSet.noneOf(OutputLanguage.class);
    for (String outputLanguage : commandLine.FLAG_output_language) {
      tmpOutputLanguages.add(OutputLanguage.valueOf(outputLanguage.toUpperCase()));
    }
    outputLanguages = ImmutableSet.copyOf(tmpOutputLanguages);

    allowedOutputFiles = getFileRefs(sourcePathFs, commandLine.FLAG_output);

    alertPolicy = computeAlertPolicy(commandLine.FLAG_warn, commandLine.FLAG_error);

    // Compute Dependency File
    dependencyFile = (commandLine.FLAG_depend == null)
        ? null : fs.parseFilename(commandLine.FLAG_depend);

    // Compute Properties File
    propertiesFile = (commandLine.FLAG_output_properties
                      && commandLine.FLAG_message_source != null)
        ? outputDir.join(
            "/" + commandLine.FLAG_message_source.replace(".", "/") + "_en.properties")
        : null;

    isVerboseEnabled = commandLine.FLAG_verbose;
    isDebugEnabled = commandLine.FLAG_g;

    // Compute Dot Phases
    dotPhases = computeDotPhases(commandLine.getParser(), commandLine.FLAG_dot);

    // Compute SourceEntityResolver
    // use the sourcePathFs so that gxp:///foo/bar is resolved in a way that
    // includes both source files and genfiles
    sourceEntityResolver = new FileSystemEntityResolver(sourcePathFs);

    // Compute CodeGeneratorFactory (Always do this last)
    codeGeneratorFactory = new DefaultCodeGeneratorFactory();
    codeGeneratorFactory.setRuntimeMessageSource(commandLine.FLAG_message_source);
    codeGeneratorFactory.setDynamicModeEnabled(commandLine.FLAG_dynamic);
    codeGeneratorFactory.setSourceFiles(getSourceFiles());
    codeGeneratorFactory.setSchemaFiles(getSchemaFiles());
    codeGeneratorFactory.setSourcePaths(sourcePaths);
    codeGeneratorFactory.setAlertPolicy(getAlertPolicy());
  }

  public boolean showHelp() {
    return commandLine.FLAG_help;
  }

  public void printHelp(Appendable out) throws IOException {
    out.append(commandLine.getUsage());
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Supplemental Computation Functions
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * Iterate over an {@code Iterable} of flags treating each as a filename in the
   * supplied {@link FileSystem}.
   *
   * @return an {@link ImmutableSet} of the results.
   */
  private static ImmutableSet<FileRef> getFileRefs(FileSystem fs, Iterable<String> filenames) {
    Set<FileRef> result = Sets.newHashSet();
    for (String filename : filenames) {
      result.add(fs.parseFilename(filename));
    }
    return ImmutableSet.copyOf(result);
  }

  // TODO(laurence): add more general support for AlertPolicy configuration
  private static final AlertPolicy computeAlertPolicy(List<String> warnFlags,
                                                      List<String> errorFlags) {
    ConfigurableAlertPolicy result = new ConfigurableAlertPolicy();
    configureAlertPolicy(result, warnFlags,  Severity.WARNING);
    configureAlertPolicy(result, errorFlags, Severity.ERROR);
    if (warnFlags.contains("error")) {
      result.setTreatWarningsAsErrors(true);
    }

    return result;
  }

  private static final void configureAlertPolicy(ConfigurableAlertPolicy alertPolicy,
                                                 List<String> flags,
                                                 Severity severity) {
    if (flags.contains("i18n")) {
      alertPolicy.setSeverity(UnextractableContentAlert.class, severity);
    }
  }

  private static ImmutableSortedSet<Phase> computeDotPhases(CmdLineParser parser,
                                                            List<String> phaseNames)
      throws CmdLineException {
    SortedSet<Phase> result = Sets.newTreeSet();
    if (phaseNames.contains("*")) {
      result.addAll(Arrays.asList(Phase.values()));
    } else {
      for (String phaseName : phaseNames) {
        phaseName = phaseName.trim();
        if (phaseName.length() > 0) {
          phaseName = phaseName.toUpperCase().replace("-", "_");
          Phase phase;
          try {
            phase = Phase.valueOf(phaseName);
          } catch (IllegalArgumentException iax) {
            throw new CmdLineException(parser, "illegal phase name in --dot flag: " + phaseName);
          }
          result.add(phase);
        }
      }
    }
    return ImmutableSortedSet.copyOfSorted(result);
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Configuration Implementation
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
    return allowedOutputFiles;
  }

  public FileRef getDependencyFile() {
    return dependencyFile;
  }

  public FileRef getPropertiesFile() {
    return propertiesFile;
  }

  public boolean isVerboseEnabled() {
    return isVerboseEnabled;
  }

  public boolean isDebugEnabled() {
    return isDebugEnabled;
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
  // Command Line
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * Bucket for holding the output of flags parsing. It doesn't do any fancy
   * processing of the flags.
   */
  private static class CommandLine {
    private final CmdLineParser parser;

    private CommandLine(String[] args) throws CmdLineException {
      parser = new CmdLineParser(this);
      parser.parseArgument(args);
    }

    public CmdLineParser getParser() {
      return parser;
    }

    public String getUsage() {
      StringWriter sw = new StringWriter();
      sw.append("Usage: gxpc [flags...] [args...]\n");
      parser.printUsage(sw, null);
      return sw.toString();
    }

    @Argument
    public List<String> trailingArgs = Lists.newArrayList();

    @Option(name = "--help",
            usage = "display this help message")
    public boolean FLAG_help = false;

    @Option(name = "--dir",
            usage = "output directory")
    public String FLAG_dir = null;

    @Option(name = "--schema",
            usage = "a schema file used for compilation; can be repeated.")
    public List<String> FLAG_schema = Lists.newArrayList();

    @Option(name = "--output_language",
            usage = "output files for this language; can be repeated.")
    public List<String> FLAG_output_language = Lists.newArrayList();

    @Option(name = "--output",
            usage = "output this file; can be repeated. If not specified,\n"
                  + "all files will be output.")
    public List<String> FLAG_output = Lists.newArrayList();

    @Option(name = "--warn",
            usage = "Sets warning options. VAL can be one of:\n"
                  + "i18n (enable i18n warnings),\n"
                  + "error (warnings are errors)")
    public List<String> FLAG_warn = Lists.newArrayList();

    @Option(name = "--error",
            usage = "Sets error options. VAL can be one of:\n"
                  + "i18n (enable i18n errors)")
    public List<String> FLAG_error = Lists.newArrayList();

    @Option(name = "--source",
            usage = "base directory for source")
    public String FLAG_source = null;

    @Option(name = "--dynamic",
            usage = "indicate dynamic mode")
    public boolean FLAG_dynamic = false;

    @Option(name = "--output_properties",
            usage = "indicates that gxpc should output a properties file")
    public boolean FLAG_output_properties = false;

    @Option(name = "--message_source",
            usage = "Message source for retrieving messages at runtime.\n"
                  + "eg: com.google.foo.bar_messages")
    public String FLAG_message_source = null;

    @Option(name = "--depend",
            usage = "location of dependency info file; enables dependency\n"
                  + "checking")
    public String FLAG_depend = null;

    @Option(name = "--verbose",
            usage = "enable verbose mode")
    public boolean FLAG_verbose = false;

    @Option(name = "--g",
            usage = "include debugging comments in HTML output")
    public boolean FLAG_g = false;

    @Option(name = "--dot",
            usage = "phase to produce graphviz \"dot\" output for;\n"
                  + "can be repeated (useful for debugging compiler)")
    public List<String> FLAG_dot = Lists.newArrayList();
  }
}
