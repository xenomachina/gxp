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
import com.google.gxp.compiler.alerts.AlertCounter;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.IOError;
import com.google.gxp.compiler.alerts.common.ProgressAlert;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.codegen.CodeGenerator;
import com.google.gxp.compiler.codegen.CodeGeneratorFactory;
import com.google.gxp.compiler.fs.FileRef;

import java.io.*;
import java.nio.charset.UnmappableCharacterException;
import java.util.*;

/**
 * Represents a fine-grained unit of work when compiling. A {@code
 * CompilationTask} compiles a single {@code CompilationUnit} into a single
 * output file.
 */
public final class CompilationTask {
  private final CompilationUnit compilationUnit;
  private final CodeGeneratorFactory codeGeneratorFactory;
  private final OutputLanguage language;
  private final FileRef outputFileRef;

  CompilationTask(CompilationUnit compilationUnit,
                  CodeGeneratorFactory codeGeneratorFactory,
                  OutputLanguage language,
                  FileRef outputFileRef) {
    this.compilationUnit = Preconditions.checkNotNull(compilationUnit);
    this.codeGeneratorFactory = Preconditions.checkNotNull(codeGeneratorFactory);
    this.language = Preconditions.checkNotNull(language);
    this.outputFileRef = Preconditions.checkNotNull(outputFileRef);
  }

  public FileRef getOutputFileRef() {
    return outputFileRef;
  }

  public CompilationUnit getCompilationUnit() {
    return compilationUnit;
  }

  /**
   * Generates output for the specified {@code CompilationUnit} in the
   * specified {@code OutputLanguage}.
   */
  void execute(AlertSink alertSink, AlertPolicy alertPolicy) {
    SourcePosition outputPosition = new SourcePosition(outputFileRef);
    alertSink.add(new ProgressAlert(outputPosition, "Generating"));
    CodeGenerator codeGenerator =
        codeGeneratorFactory.getCodeGenerator(language, compilationUnit);

    AlertCounter counter = new AlertCounter(alertSink, alertPolicy);

    StringBuilder sb = new StringBuilder();
    try {
      codeGenerator.generateCode(sb, counter);

      if (counter.getErrorCount() == 0) {
        Writer writer = outputFileRef.openWriter(Charsets.US_ASCII);
        try {
          writer.write(sb.toString());
        } finally {
          writer.close();
        }
      }
    } catch (UnmappableCharacterException uce) {
      // These are caused by coding errors, not user error.
      throw new AssertionError(uce);
    } catch (IOException iox) {
      FileRef sourceFileRef = compilationUnit.getSourceFileRef();
      alertSink.add(new IOError(sourceFileRef, iox));
    }
    alertSink.add(new ProgressAlert(outputPosition, "Generate finished"));
  }
}
