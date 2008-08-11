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

package com.google.gxp.compiler.js;

import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.codegen.BracesCodeGenerator;
import com.google.gxp.compiler.msgextract.MessageExtractedTree;

/**
 * {@code CodeGenerator} which generates JavaScript code.
 */
public class JavaScriptCodeGenerator extends BracesCodeGenerator<MessageExtractedTree> {
  public JavaScriptCodeGenerator(MessageExtractedTree tree) {
    super(tree);
  }

  public void generateCode(final Appendable appendable, final AlertSink alertSink) {
    // TODO: some actual code here
  }
}
