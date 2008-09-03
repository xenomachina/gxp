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

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.IOError;
import com.google.gxp.compiler.alerts.common.ProgressAlert;
import com.google.gxp.compiler.base.ExtractedMessage;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.base.Util;
import com.google.gxp.compiler.codegen.CodeGeneratorFactory;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.parser.Parser;
import com.google.gxp.compiler.servicedir.OnDemandServiceDirectory;
import com.google.gxp.compiler.servicedir.ServiceDirectory;
import com.google.transconsole.common.messages.MessageBundle;
import com.google.transconsole.common.messages.PropertiesBundleWriter;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.UnmappableCharacterException;
import java.util.*;

/**
 * A set of {@code CompilationUnit}s, and the logic for compiling (subsets of)
 * them.
 */
public class CompilationSet {
  private final Map<TemplateName.FullyQualified, CompilationUnit> compilationUnits;
  private final Parser parser;
  private final CodeGeneratorFactory codeGeneratorFactory;
  private final CompilationManager manager;
  private final ServiceDirectory serviceDirectory;
  private final long compilationVersion;
  private final FileRef propertiesFile;

  /**
   * Builds a {@code CompilationSet}.
   */
  public static final class Builder {
    // Required parameters to build CompilationSet.
    private final Parser parser;
    private final CodeGeneratorFactory codeGeneratorFactory;
    private final CompilationManager manager;

    // Optional parameters to build CompilationSet.
    private long compilationVersion = 0;
    private FileRef propertiesFile = null;

    /**
     * Creates a {@code CompilationSet}.
     *
     * @param parser the {@code Parser} that will be used for parsing source
     *     files
     * @param codeGeneratorFactory the {@code CodeGeneratorFactory} that will be
     *     used for constructing {@code CodeGenerator}s, which in turn will be
     *     used to generate output
     */
    public Builder(Parser parser, CodeGeneratorFactory codeGeneratorFactory,
                   CompilationManager manager) {
      this.parser = Preconditions.checkNotNull(parser);
      this.codeGeneratorFactory = Preconditions.checkNotNull(codeGeneratorFactory);
      this.manager = Preconditions.checkNotNull(manager);
    }

    /**
     * Builds the {@code CompilationSet}.
     *
     * @param inputFileRefs the {@code FileRef}s for the source files. A {@code
     *     CompilationUnit} will be created for each element.
     */
    public CompilationSet build(Iterable<FileRef> inputFileRefs) {
      return new CompilationSet(this, inputFileRefs);
    }

    /**
     * Builds the {@code CompilationSet}.
     *
     * @param inputFileRefs the {@code FileRef}s for the source files. A {@code
     *     CompilationUnit} will be created for each element.
     */
    public CompilationSet build(FileRef... inputFileRefs) {
      return build(Arrays.asList(inputFileRefs));
    }

    public Builder setCompilationVersion(long compilationVersion) {
      this.compilationVersion = compilationVersion;
      return this;
    }

    public Builder setPropertiesFile(FileRef propertiesFile) {
      this.propertiesFile = propertiesFile;
      return this;
    }
  }

  private CompilationSet(Builder builder, Iterable<FileRef> inputFileRefs) {
    this.parser = builder.parser;
    this.codeGeneratorFactory = builder.codeGeneratorFactory;
    this.manager = builder.manager;
    this.compilationVersion = builder.compilationVersion;
    this.propertiesFile = builder.propertiesFile;

    this.serviceDirectory = new OnDemandServiceDirectory(this);
    this.compilationUnits = createCompilationUnits(serviceDirectory,
                                                   inputFileRefs);
  }

  private Map<TemplateName.FullyQualified, CompilationUnit>
      createCompilationUnits(ServiceDirectory serviceDirectory,
                             Iterable<FileRef> inputFileRefs) {
    ImmutableMap.Builder<TemplateName.FullyQualified, CompilationUnit>
        mapBuilder = ImmutableMap.builder();
    for (FileRef inputFileRef : inputFileRefs) {
      CompilationUnit unit =
          new CompilationUnit(serviceDirectory, parser, inputFileRef,
                              compilationVersion);
      mapBuilder.put(unit.getTemplateName(), unit);
    }
    return mapBuilder.build();
  }

  /**
   * @return the {@code CompilationUnit}s that make up this {@code
   * CompilationSet}.
   */
  public final List<CompilationUnit> getCompilationUnits() {
    return ImmutableList.copyOf(compilationUnits.values());
  }

  /**
   * @return the {@code CompilationUnit} with the specified TemplateName
   * CompilationSet}.
   */
  public final CompilationUnit getCompilationUnit(TemplateName.FullyQualified templateName) {
    return compilationUnits.get(templateName);
  }

  /**
   * Compiles {@code CompilationUnits} into the specified {@code
   * OutputLanguage}s. Only outputs that {@code allowedOutputPredicate} returns
   * {@code true} for will actually be generated.
   */
  public void compile(AlertSink alertSink, AlertPolicy alertPolicy,
                      Iterable<OutputLanguage> outputLanguages,
                      Predicate<FileRef> allowedOutputPredicate) {
    Set<CompilationUnit> extractMessagesFrom = Sets.newHashSet();
    List<CompilationTask> sourceNotChanged = Lists.newArrayList();
    for (CompilationUnit cUnit : getCompilationUnits()) {
      FileRef sourceFileRef = cUnit.getSourceFileRef();
      SourcePosition sourcePosition = new SourcePosition(sourceFileRef);

      for (OutputLanguage language : outputLanguages) {
        String suffix = language.getSuffix();
        if (language.suffixIncludesVersion()) {
          suffix = String.format(suffix, compilationVersion);
        }
        FileRef outputFileRef = sourceFileRef.removeExtension().addSuffix(suffix);

        if (allowedOutputPredicate.apply(outputFileRef)) {
          extractMessagesFrom.add(cUnit);
          CompilationTask task =
              new CompilationTask(cUnit, codeGeneratorFactory, language,
                                  outputFileRef);
          // if the output file does not exist (last modified = 0) or if the source has been
          // modified since the last time that the output has been generated, or if the source has
          // changed, then we need to recompile the target
          if (outputFileRef.getLastModified() < sourceFileRef.getLastModified()
              || manager.sourceChanged(task)) {
            task.execute(alertSink, alertPolicy);
          } else {
            alertSink.add(new ProgressAlert(sourcePosition, "Skipped (source unchanged)"));
            sourceNotChanged.add(task);
          }
        } else {
          alertSink.add(new ProgressAlert(sourcePosition, "Skipped (output supressed)"));
        }
      }
    }

    // For each task we didn't execute, check to see if any of the interfaces
    // it depends on have changed (which could happen as a result of
    // recompiling one of the things it depends on).
    // TODO(laurence): see whether it's possible to combine these two loops by
    // having usedInterfacesChanged not change its value.
    for (CompilationTask task : sourceNotChanged) {
      if (manager.usedInterfacesChanged(task)) {
        FileRef sourceFileRef = task.getCompilationUnit().getSourceFileRef();
        SourcePosition sourcePosition = new SourcePosition(sourceFileRef);
        alertSink.add(new ProgressAlert(sourcePosition, "Reconsidered; callees have changed"));
        task.execute(alertSink, alertPolicy);
      }
    }

    // Optionally write out a java properties file that contains
    // strings extracted from <gxp:msg>s
    if (propertiesFile != null && !extractMessagesFrom.isEmpty()) {
      SourcePosition outputPosition = new SourcePosition(propertiesFile);
      alertSink.add(new ProgressAlert(outputPosition, "Generating"));

      List<ExtractedMessage> messages = Lists.newArrayList();
      for (CompilationUnit cUnit : extractMessagesFrom) {
        messages.addAll(cUnit.getMessageExtractedTree().getMessages());
      }

      MessageBundle messageBundle = Util.bundleMessages(alertSink, messages);
      PropertiesBundleWriter pbw = new PropertiesBundleWriter(messageBundle);
      try {
        Writer writer = propertiesFile.openWriter(Charsets.US_ASCII);
        try {
          pbw.write(writer);
        } finally {
          writer.close();
        }
      } catch (UnmappableCharacterException uce) {
        // These are caused by coding errors, not user error.
        throw new AssertionError(uce);
      } catch (IOException iox) {
        alertSink.add(new IOError(propertiesFile, iox));
      }
      alertSink.add(new ProgressAlert(outputPosition, "Generate finished"));
    }
  }

  /**
   * Convenience method which compiles allowing all outputs.
   */
  public final void compile(AlertSink alertSink, AlertPolicy alertPolicy,
                            Iterable<OutputLanguage> outputLanguages) {
    compile(alertSink, alertPolicy, outputLanguages, Predicates.<FileRef>alwaysTrue());
  }
}
