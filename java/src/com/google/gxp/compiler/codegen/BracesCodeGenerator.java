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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.Tree;
import com.google.gxp.compiler.io.CIndenter;

/**
 * Abstract Base Class for all {@link CodeGenerator}s generating languages
 * with block deliniated by curly braces.  Currently this is only C++ and Java.
 */
public abstract class BracesCodeGenerator<T extends Tree<Root>> extends BaseCodeGenerator<T> {
  protected final Root root;

  protected BracesCodeGenerator(T tree) {
    super(tree);
    this.root = tree.getRoot();
  }

  protected abstract static class Worker {
    protected final CIndenter out;
    protected final AlertSink alertSink;

    protected Worker(Appendable out, AlertSink alertSink, String... halfIndentMarkers) {
      this.out = new CIndenter(out, halfIndentMarkers);
      this.alertSink = Objects.nonNull(alertSink);
    }

    protected SourcePosition getDefaultSourcePosition() {
      return null;
    }

    private static final String COMMENT_FORMAT = "// %s: L%d, C%d";

    protected String makeTailComment(SourcePosition pos) {
      return String.format(COMMENT_FORMAT, pos.getSourceName(), pos.getLine(), pos.getColumn());
    }

    protected final void appendLine() {
      appendLine(null, "");
    }

    protected final void appendLine(CharSequence line) {
      appendLine(getDefaultSourcePosition(), line);
    }

    /**
     * Appends a line of code.
     *
     * @param pos the {@code SourcePosition} of the original source
     * code that generated this line of code for the purpose of annotating the
     * generated code, or null if no such annotation is desired
     * @param line the line of code
     */
    protected final void appendLine(SourcePosition pos, CharSequence line) {
      // BUGBUG(harryh): i check to see if line contains SourceName to avoid
      // double commenting lines where we called toAnonymousClosure() and then
      // used the resulting String in another line.  This is a gross hack, but
      // it's the best I can come up with right now
      String tailComment = (pos != null && !line.toString().contains(pos.getSourceName()))
          ? makeTailComment(pos) : "";
      out.appendLine(line, tailComment);
    }

    /**
     * Appends a line of code with formatting.
     *
     * @param lineFormat the format string for the line of code
     * @param args the arguments to the format
     */
    protected final void formatLine(String lineFormat, Object... args) {
      formatLine(getDefaultSourcePosition(), lineFormat, args);
    }

    /**
     * Appends a line of code with formatting.
     *
     * @param sourcePosition the {@code SourcePosition} of the original source
     * @param lineFormat the format string for the line of code
     * @param args the arguments to the format
     */
    protected final void formatLine(SourcePosition sourcePosition,
                                    String lineFormat, Object... args) {
      appendLine(sourcePosition, String.format(lineFormat, args));
    }

    private static final String BANNER_FORMAT = loadFormat("codegen/banner");

    protected void appendBanner() {
      appendLine(null, BANNER_FORMAT);
    }

    protected void appendHeader(Root root) {
      appendBanner();
    }

    protected void appendFooter() {
      appendBanner();
    }

    protected Function<Parameter, String> paramToCallName =
      new Function<Parameter, String>() {
        public String apply(Parameter param) {
          return param.getPrimaryName();
        }
      };
  }
}
