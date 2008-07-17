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

package com.google.gxp.base.dynamic;

import com.google.common.base.CharEscapers;
import com.google.gxp.base.GxpContext;
import com.google.gxp.html.HtmlClosure;

import java.io.*;

/**
 * {@code HtmlClosure} that outputs a subset of a file (passed
 * in as a {@code Reader}) given its name and a line to center on.
 */
public class FilePrinter implements HtmlClosure {
  private static final int SURROUND_LINES = 5;

  private Reader fileReader;
  private long startLine, endLine, errorLine;

  public FilePrinter(Reader fileReader, long errorLine) {
    this.fileReader = fileReader;
    this.errorLine = errorLine;
    this.startLine = Math.max(0, errorLine - SURROUND_LINES);
    this.endLine = errorLine + SURROUND_LINES;
  }

  public void write(Appendable out, GxpContext gxpContext) throws IOException {
    BufferedReader br = new BufferedReader(fileReader);
    String line;
    long lineNumber = 1;
    while ((line = br.readLine()) != null && lineNumber <= endLine) {
      if (startLine <= lineNumber) {
        if (lineNumber == errorLine) {
          out.append("<span class=\"error\">");
        }
        out.append(String.format("%4d: ", lineNumber));
        CharEscapers.HTML_ESCAPE.escape(out).append(line);
        if (lineNumber == errorLine) {
          out.append("</span>");
        }
        out.append('\n');
      }
      lineNumber++;
    }
  }
}
