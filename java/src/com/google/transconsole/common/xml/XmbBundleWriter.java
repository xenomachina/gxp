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

package com.google.transconsole.common.xml;

import com.google.common.base.Preconditions;
import com.google.transconsole.common.messages.Message;
import com.google.transconsole.common.messages.MessageBundle;

import java.io.IOException;

/**
 * Writes a MessageBundle object into the XMB file format.
 */
public class XmbBundleWriter {

  private final MessageBundle bundle;

  private final static String HEADER =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<!DOCTYPE messagebundle SYSTEM \"/home/build/nonconf/google3/i18n/messagebundle.dtd\">\n" +
    "<messagebundle>\n";

  private final static String FOOTER = "</messagebundle>\n";

  /**
   * Creates an XmbBundleWriter object and binds it to a MessageBundle.
   *
   * @param bundle MessageBundle to write
   */
  public XmbBundleWriter(MessageBundle bundle) {
    this.bundle = Preconditions.checkNotNull(bundle);
  }

  /**
   * Writes the contents of the bundle to the Appendable object.
   *
   * @param out Appendable object (file, string buffer, etc.)
   */
  public void write(Appendable out) throws IOException {
    writeHeader(out);
    writeBody(out);
    writeFooter(out);
  }

  protected void writeHeader(Appendable out) throws IOException {
    out.append(HEADER);
  }

  protected void writeFooter(Appendable out) throws IOException {
    out.append(FOOTER);
  }

  protected void writeBody(Appendable out) throws IOException {
    for (Message m : bundle) {
      out.append(m.toXml());
      out.append('\n');
    }
  }
}
