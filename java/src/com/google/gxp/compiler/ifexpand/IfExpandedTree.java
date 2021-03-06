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

package com.google.gxp.compiler.ifexpand;

import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Forest;
import com.google.gxp.compiler.parser.ParsedElement;

import java.util.*;

/**
 * The output of {@link IfExpander}.
 */
public class IfExpandedTree extends Forest<ParsedElement> {
  public IfExpandedTree(SourcePosition sourcePosition, AlertSet alerts,
                        List<ParsedElement> children) {
    super(sourcePosition, alerts, children);
  }
}
