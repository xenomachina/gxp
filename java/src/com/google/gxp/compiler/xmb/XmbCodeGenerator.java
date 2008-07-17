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

package com.google.gxp.compiler.xmb;

import com.google.common.base.CharEscaper;
import com.google.common.base.Charsets;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.base.Util;
import com.google.gxp.compiler.codegen.BaseCodeGenerator;
import com.google.gxp.compiler.msgextract.MessageExtractedTree;
import com.google.transconsole.common.messages.MessageBundle;
import com.google.transconsole.common.xml.XmbBundleWriter;

import java.io.IOException;

/**
 * A {@link CodeGenerator} which generates XMB (XML Message Bundle) files.
 */
public class XmbCodeGenerator extends BaseCodeGenerator<MessageExtractedTree> {
  private static final CharEscaper XML_ASCII_ESCAPER = new XmlCharsetEscaper(Charsets.US_ASCII);

  /**
   * @param tree the MessageExtractedTree to compile.
   */
  public XmbCodeGenerator(MessageExtractedTree tree) {
    super(tree);
  }

  public void generateCode(Appendable out, AlertSink alertSink) throws IOException {
    alertSink.addAll(tree.getAlerts());

    MessageBundle messageBundle = Util.bundleMessages(alertSink, tree.getMessages());
    XmbBundleWriter bundleWriter = new XmbBundleWriter(messageBundle);
    bundleWriter.write(XML_ASCII_ESCAPER.escape(out));
  }
}
