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

package com.google.gxp.compiler.dynamictests;

import com.google.gxp.base.GxpContext;
import com.google.gxp.html.HtmlClosure;

import java.io.IOException;

/**
 * A test {@code HtmlClosure} that is package private.
 */
class PackagePrivateClosure implements HtmlClosure {
  public void write(Appendable out, GxpContext gxp_context) throws IOException {
    out.append("private data");
  }
}
