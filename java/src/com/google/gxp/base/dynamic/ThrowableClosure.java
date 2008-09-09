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
import com.google.common.base.Preconditions;
import com.google.gxp.base.GxpContext;
import com.google.gxp.html.HtmlClosure;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A {@code HtmlClosure} that will print the stack trace of a
 * {@code Throwable}. In general, you'll want to wrap this inside
 * of a pair of &lt;pre&gt; tags.
 */
public class ThrowableClosure implements HtmlClosure {
  private final Throwable t;

  public ThrowableClosure(Throwable t) {
    this.t = Preconditions.checkNotNull(t);
  }

  public void write(Appendable out, GxpContext gxpContext) throws IOException {
    StringWriter sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    CharEscapers.asciiHtmlEscaper().escape(out).append(sw.toString());
  }
}
