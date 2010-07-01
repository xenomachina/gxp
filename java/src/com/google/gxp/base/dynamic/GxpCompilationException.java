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

import com.google.common.base.Preconditions;
import com.google.gxp.base.GxpContext;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.html.HtmlClosure;

import java.io.IOException;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Exception thrown when there is an error durring runtime gxp compilation.
 *
 * TODO(harryh): consider renaming this class (and the inner classes) as
 *               they're currently a little confusing.
 */
public abstract class GxpCompilationException extends RuntimeException implements HtmlClosure {

  private final HtmlClosure htmlClosure;

  protected GxpCompilationException(HtmlClosure htmlClosure) {
    this.htmlClosure = Preconditions.checkNotNull(htmlClosure);
  }

  public void write(Appendable out, GxpContext gxpContext) throws IOException {
    htmlClosure.write(out, gxpContext);
  }

  public static class Gxp extends GxpCompilationException {

    public Gxp(AlertPolicy alertPolicy, AlertSet alertSet) {
      super(GxpCompilationError.getGxpClosure(alertPolicy, alertSet));
    }
  }

  public static class Java extends GxpCompilationException {

    public Java(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
      super(JavaCompilationError.getGxpClosure(diagnostics));
    }
  }

  public static class GxpParamChange extends GxpCompilationException {

    public GxpParamChange(IllegalArgumentException iae) {
      super(GxpParamChangeError.getGxpClosure(iae));
    }
  }

  public static class Throw extends GxpCompilationException {

    public Throw(Throwable throwable) {
      super(ThrowableError.getGxpClosure(throwable));
    }
  }
}
