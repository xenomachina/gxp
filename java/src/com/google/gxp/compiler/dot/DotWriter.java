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

package com.google.gxp.compiler.dot;

import com.google.common.base.CharEscaper;
import com.google.common.base.CharEscaperBuilder;
import com.google.common.base.Join;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.io.CIndenter;

import java.io.*;
import java.util.*;

/**
 * A {@link GraphSink} that writes graphviz "dot" format to an {@code
 * Appendable}.
 */
public class DotWriter implements GraphSink {
  private final CIndenter out;

  public DotWriter(Appendable out) {
    this.out = new CIndenter(out);
  }

  public void digraphStart(String name) {
    out.appendLine(String.format("digraph %s {", name));
  }

  public void digraphEnd() {
    out.appendLine("}");
  }

  public void simpleNode(String nodeId, NodeShape shape, String label) {
    out.appendLine(String.format(
        "node%s [label=\"%s\",shape=%s];",
        nodeId, dotEscape(label), shape));
  }

  public void recordNode(String nodeId, Map<String, String> properties) {
    List<String> names = Lists.newArrayListWithExpectedSize(properties.size());
    List<String> values = Lists.newArrayListWithExpectedSize(properties.size());
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      names.add(dotEscape(entry.getKey()));
      values.add(dotEscape(entry.getValue()));
    }
    out.appendLine(String.format(
        "node%s [label=\"{%s} | {%s}\",shape=Mrecord];",
        nodeId,
        Join.join(" | ", names),
        Join.join(" | ", values)));
  }

  public void edge(String fromNodeId, String label, String toNodeId) {
    String suffix = (label == null)
        ? ""
        : String.format(" [label=\"%s\"]", dotEscape(label));
    out.appendLine(String.format("node%s -> node%s%s;\n",
                                 fromNodeId, toNodeId, suffix));
  }

  private static CharEscaper DOT_ESCAPER =
      new CharEscaperBuilder()
      .addEscape('\n', "\\n")
      .addEscape('\r', "\\r")
      .addEscape('\t', "\\t")
      .addEscape('\\', "\\\\")
      .addEscape('\"', "\\\"")
      .addEscape('{', "\\{")
      .addEscape('}', "\\}")
      .addEscape('<', "\\<")
      .addEscape('>', "\\>")
      .addEscape('|', "\\|")
      .addEscape(' ', "\\ ")
      .toEscaper();

  private static String dotEscape(String s) {
    return DOT_ESCAPER.escape(s);
  }
}
