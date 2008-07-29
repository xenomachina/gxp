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

package com.google.gxp.compiler.alerts.common;

import com.google.gxp.compiler.alerts.ErrorAlert;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.parser.Namespace;
import com.google.gxp.compiler.reparent.Attribute;

/**
 * An {@link ErrorAlert} which indicates that the specified attribute already
 * has a value assigned to it.
 */
public class MultiValueAttributeError extends ErrorAlert {
  /**
   * @param parentNode the attribute's parent {@code Node}.
   * @param attr the attribute
   */
  public MultiValueAttributeError(Node parentNode,
                                  Attribute attr) {
    this(attr.getSourcePosition(), parentNode.getDisplayName(),
         formatAttrName(attr.getNamespace(), attr.getName()));
  }

  private static String formatAttrName(Namespace attrNs,
                                       String attrLocalName) {
    String nsUri = attrNs.getUri();
    return "'" + ((nsUri == null) ? "" : ("[" + nsUri + "]")) + attrLocalName
        + "' attribute";
  }

  public MultiValueAttributeError(SourcePosition sourcePosition,
                                  String parentName, String attrName) {
    super(sourcePosition, attrName + " in " + parentName
          + " already has a value.");
  }
}
