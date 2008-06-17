/*
 * Copyright (C) 2007 Google Inc.
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

package com.google.gxp.base;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An attribute bundle.  Used by the GXP compiler for bundling up a bunch
 * of attributes into a single item.  Currently supports regular and boolean
 * attributes.
 */
public class GxpAttrBundle<T extends MarkupClosure> {
  private final Map<String, T> attrs;
  private final Set<String> booleanAttrs;

  GxpAttrBundle(Map<String, T> attrs, Set<String> booleanAttrs) {
    this.attrs = Collections.unmodifiableMap(attrs);
    this.booleanAttrs = Collections.unmodifiableSet(booleanAttrs);
  }

  public void write(Appendable out, GxpContext gxpContext) throws IOException {
    for (Map.Entry<String, T> attr : attrs.entrySet()) {
      out.append(' ');
      out.append(attr.getKey());
      out.append("=\"");
      attr.getValue().write(out, gxpContext);
      out.append('"');
    }
    for (String booleanAttr : booleanAttrs) {
      out.append(' ');
      out.append(booleanAttr);
      if (gxpContext.isUsingXmlSyntax()) {
        out.append("=\"");
        out.append(booleanAttr);
        out.append('"');
      }
    }
  }

  Map<String, T> getAttrs() {
    return attrs;
  }

  Set<String> getBooleanAttrs() {
    return booleanAttrs;
  }
}
