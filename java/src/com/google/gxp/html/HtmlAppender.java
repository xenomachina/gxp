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

package com.google.gxp.html;

import com.google.common.base.CharEscaper;
import com.google.common.base.CharEscapers;
import com.google.common.base.Preconditions;
import com.google.gxp.base.GxpContext;
import com.google.gxp.base.MarkupAppender;
import com.google.gxp.css.CssClosure;
import com.google.gxp.js.JavascriptClosure;
import com.google.gxp.text.PlaintextClosure;
import com.google.i18n.Localizable;

import java.io.IOException;

/**
 * Appender for content-type: text/html
 */
public class HtmlAppender extends MarkupAppender<HtmlClosure> {

  public static final HtmlAppender INSTANCE = new HtmlAppender();
  private HtmlAppender() { }

  //////////////////////////////////////////////////////////////////////
  // CharEscaper
  //////////////////////////////////////////////////////////////////////

  @Override
  protected CharEscaper getCharEscaper(GxpContext gxpContext) {
    return CharEscapers.HTML_ESCAPE;
  }

  //////////////////////////////////////////////////////////////////////
  // Closures
  //////////////////////////////////////////////////////////////////////

  /**
   * Appends a {@code CssClosure} to out. Surrounds it with a <style> tag.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, CssClosure closure)
      throws IOException {
    Preconditions.checkNotNull(closure);
    out.append("<style type=\"text/css\">\n");
    closure.write(out, gxpContext);
    out.append("\n</style>");
    return out;
  }

  /**
   * Appends a {@code JavascriptClosure} to out. Surrounds it with a
   * <script> tag.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, JavascriptClosure closure)
      throws IOException {
    Preconditions.checkNotNull(closure);
    out.append("<script type=\"text/javascript\">\n");
    closure.write(out, gxpContext);
    out.append("\n</script>");
    return out;
  }

  /**
   * Appends a {@code PlaintextClosure} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, PlaintextClosure closure)
      throws IOException {
    Preconditions.checkNotNull(closure);
    closure.write(CharEscapers.escape(out, getCharEscaper(gxpContext)), gxpContext);
    return out;
  }

  //////////////////////////////////////////////////////////////////////
  // Objects
  //////////////////////////////////////////////////////////////////////

  public <A extends Appendable> A append(A out, GxpContext gxpContext, Object value)
      throws IOException {
    Preconditions.checkNotNull(value);
    if (value instanceof Localizable) {
      append(out, gxpContext, (Localizable)value);
    } else if (value instanceof PlaintextClosure) {
      append(out, gxpContext, (PlaintextClosure)value);
    } else if (value instanceof CssClosure) {
      append(out, gxpContext, (CssClosure)value);
    } else if (value instanceof JavascriptClosure) {
      append(out, gxpContext, (JavascriptClosure)value);
    } else if (value instanceof HtmlClosure) {
      append(out, gxpContext, (HtmlClosure)value);
    } else {
      append(out, gxpContext, value.toString());
    }
    return out;
  }
}
