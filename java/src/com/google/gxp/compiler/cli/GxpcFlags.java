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

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
class GxpcFlags implements GxpcConfiguration {
  private final FileSystem fs;
  private final SourcePathFileSystem sourcePathFs;
  private final Set<FileRef> inputFiles;
  private final Set<FileRef> schemaFiles;
  private final FileRef outputDir;
  private final List<FileRef> sourcePaths;
  private final SortedSet<Phase> dotPhases;

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
  GxpcFlags(FileSystem fs, Appendable stderr, FileRef defaultDir, String... args)
      throws CmdLineException {
    this.fs = fs;

    List<String> trailingArgs = CommandLine.init(stderr, args);

    List<FileRef> underlyingInputFiles = Lists.newArrayList();
    for (String arg : trailingArgs) {
      underlyingInputFiles.add(fs.parseFilename(arg));
    }

    Set<FileRef> tmpSchemaFiles = Sets.newHashSet();
    for (String schema : CommandLine.FLAG_schema) {
      tmpSchemaFiles.add(fs.parseFilename(schema));
    }
    schemaFiles = ImmutableSet.copyOf(tmpSchemaFiles);

    outputDir = (CommandLine.FLAG_dir == null)
        ? defaultDir : fs.parseFilename(CommandLine.FLAG_dir);

    sourcePaths = ImmutableList.copyOf((CommandLine.FLAG_source == null)
        ? Collections.singletonList(defaultDir)
        : sourcePath(fs, CommandLine.FLAG_source));

    sourcePathFs = new SourcePathFileSystem(fs,
                                            sourcePaths,
                                            underlyingInputFiles,
                                            outputDir);

    dotPhases = computeDotPhases(CommandLine.FLAG_dot);

    inputFiles = ImmutableSet.copyOf(sourcePathFs.getSourceFileRefs());
  }

  /**
   * @return source path, with longest elements first.
   */
  private static List<FileRef> sourcePath(FileSystem fs, String sourceFlag) {
    List<FileRef> result = Lists.newArrayList();
    for (FileRef sourceName : fs.parseFilenameList(sourceFlag)) {
      result.add(sourceName);
    }
    sortByDecreasingLength(result);

    return result;
  }

  private static void sortByDecreasingLength(List<FileRef> list) {
    Collections.sort(list,
        new Comparator<FileRef>() {
          public int compare(FileRef f1, FileRef f2) {
            int len1 = f1.getName().length();
            int len2 = f2.getName().length();
            return (len1 > len2) ? -1 : (len1 == len2 ? 0 : 1);
          }
        });
  }

  public Set<FileRef> getSourceFiles() {
    return inputFiles;
  }

  public Set<FileRef> getSchemaFiles() {
    return schemaFiles;
  }

  public Set<OutputLanguage> getOutputLanguages() {
    Set<OutputLanguage> result = EnumSet.noneOf(OutputLanguage.class);
    for (String outputLanguage : CommandLine.FLAG_output_language) {
      result.add(OutputLanguage.valueOf(outputLanguage.toUpperCase()));
    }

    return Collections.unmodifiableSet(result);
  }

  public CodeGeneratorFactory getCodeGeneratorFactory() {
    DefaultCodeGeneratorFactory result =  new DefaultCodeGeneratorFactory();
    result.setRuntimeMessageSource(CommandLine.FLAG_message_source);
    result.setDynamicModeEnabled(CommandLine.FLAG_dynamic);
    result.setSourceFiles(inputFiles);
    result.setSchemaFiles(schemaFiles);
    result.setSourcePaths(sourcePaths);
    result.setAlertPolicy(getAlertPolicy());
    return result;
  }

  public Set<FileRef> getAllowedOutputFileRefs() {
    Set<FileRef> result = Sets.newHashSet();
    for (String outputFlag : CommandLine.FLAG_output) {
      FileRef fnam = sourcePathFs.parseFilename(outputFlag);
      result.add(fnam);
    }
    return Collections.unmodifiableSet(result);
  }

  public FileRef getDependencyFile() {
    return (CommandLine.FLAG_depend == null)
        ? null : fs.parseFilename(CommandLine.FLAG_depend);
  }

  public FileRef getPropertiesFile() {
    return (CommandLine.FLAG_output_properties && CommandLine.FLAG_message_source != null)
        ? outputDir.join(
            "/" + CommandLine.FLAG_message_source.replace(".", "/") + "_en.properties")
        : null;
  }

  public boolean isVerboseEnabled() {
    return CommandLine.FLAG_verbose;
  }

  public boolean isDebugEnabled() {
    return CommandLine.FLAG_g;
  }

  public AlertPolicy getAlertPolicy() {
    return alertPolicySupplier.get();
  }

  public SourceEntityResolver getEntityResolver() {
    // use the sourcePathFs so that gxp:///foo/bar is resolved in a way that
    // includes both source files and genfiles
    return new FileSystemEntityResolver(sourcePathFs);
  }

  // TODO(laurence): add more general support for AlertPolicy configuration
  private final Supplier<AlertPolicy> alertPolicySupplier =
      Suppliers.memoize(new Supplier<AlertPolicy>() {
        public AlertPolicy get() {
          ConfigurableAlertPolicy result = new ConfigurableAlertPolicy();
          Set<String> warnFlags = Sets.newHashSet(CommandLine.FLAG_warn);
          if (warnFlags.contains("i18n")) {
            result.setSeverity(UnextractableContentAlert.class,
                               Severity.WARNING);
          }
          if (warnFlags.contains("error")) {
            result.setTreatWarningsAsErrors(true);
          }
          return result;
        }
      });

  public SortedSet<Phase> getDotPhases() {
    return dotPhases;
  }

  private static SortedSet<Phase> computeDotPhases(List<String> phaseNames)
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
            throw new CmdLineException(
                "illegal phase name in --dot flag: " + phaseName);
          }
          result.add(phase);
        }
      }
    }
    return Collections.unmodifiableSortedSet(result);
  }

  /**
   * Bucket for holding the output of flags parsing. It doesn't do any fancy
   * processing of the flags.
   */
  private static class CommandLine {
    private CommandLine() {
    }

    public static List<String> init(Appendable stderr, String[] args) throws CmdLineException {
      resetFlags();
      CmdLineParser parser = new CmdLineParser(new CommandLine());
      parser.parseArgument(args);

      if (FLAG_help) {
        // TODO(harryh): really shouldn't use System.exit at this level of the code
        StringWriter sw = new StringWriter();
        parser.printUsage(sw, null);
        try {
          stderr.append("Usage: gxpc [flags...] [args...]\n");
          stderr.append(sw.toString());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        System.exit(0);
      }

      return trailingArgs;
    }

    private static void resetFlags() {
      trailingArgs = Lists.newArrayList();
      FLAG_help = false;
      FLAG_dir = null;
      FLAG_schema = Lists.newArrayList();
      FLAG_output_language = Lists.newArrayList();
      FLAG_output = Lists.newArrayList();
      FLAG_warn = Lists.newArrayList();
      FLAG_source = null;
      FLAG_dynamic = false;
      FLAG_output_properties = false;
      FLAG_message_source = null;
      FLAG_depend = null;
      FLAG_verbose = false;
      FLAG_g = false;
      FLAG_dot = Lists.newArrayList();
    }

    @Argument
    private static List<String> trailingArgs;

    @Option(name = "--help",
            usage = "display this help message")
    private static boolean FLAG_help;

    @Option(name = "--dir",
            usage = "output directory")
    public static String FLAG_dir;

    @Option(name = "--schema",
            usage = "a schema file used for compilation; can be repeated.")
    public static List<String> FLAG_schema;

    @Option(name = "--output_language",
            usage = "output files for this language; can be repeated.")
    public static List<String> FLAG_output_language;

    @Option(name = "--output",
            usage = "output this file; can be repeated. If not specified,\n"
                  + "all files will be output.")
    public static List<String> FLAG_output;

    @Option(name = "--warn",
            usage = "Sets warning options. VAL can be one of:\n"
                  + "i18n (enable i18n warnings),\n"
                  + "errors (warnings are errors)")
    public static List<String> FLAG_warn;

    @Option(name = "--source",
            usage = "base directory for source")
    public static String FLAG_source;

    @Option(name = "--dynamic",
            usage = "indicate dynamic mode")
    public static boolean FLAG_dynamic;

    @Option(name = "--output_properties",
            usage = "indicates that gxpc should output a properties file")
    public static boolean FLAG_output_properties;
    
    @Option(name = "--message_source",
            usage = "Message source for retrieving messages at runtime.\n"
                  + "eg: com.google.foo.bar_messages")
    public static String FLAG_message_source;

    @Option(name = "--depend",
            usage = "location of dependency info file; enables dependency\n"
                  + "checking")
    public static String FLAG_depend;

    @Option(name = "--verbose",
            usage = "enable verbose mode")
    public static boolean FLAG_verbose;

    @Option(name = "--g",
            usage = "include debugging comments in HTML output")
    public static boolean FLAG_g;

    @Option(name = "--dot",
            usage = "phase to produce graphviz \"dot\" output for;\n"
                  + "can be repeated (useful for debugging compiler)")
    public static List<String> FLAG_dot;
  }
}
