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

package com.google.gxp.compiler.parser;

import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Forest;

import java.util.*;

/**
 * The parse tree. Technically, actually a forest. While it should ideally only
 * have only one root element, malformed documents may have multiple roots.
 * This essentially parallels a DOM document node.
 */
public class ParseTree extends Forest<ParsedElement> {
  public ParseTree(SourcePosition pos, AlertSet alerts, List<ParsedElement> children) {
    super(pos, alerts, children);
  }
}
