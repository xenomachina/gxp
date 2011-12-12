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

package com.google.gxp.compiler.codegen;

import com.google.gxp.compiler.alerts.ErrorAlert;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Node;

/**
 * {@code ErrorAlert} which indicates that no runtime message source was
 * specified, but one is required to compile the specified {@code Node}
 * (typically a {@code <gxp:msg>}).
 */
public class NoMessageSourceError extends ErrorAlert {
  public NoMessageSourceError(Node node) {
    this(node.getSourcePosition(), node.getDisplayName());
  }

  public NoMessageSourceError(SourcePosition sourcePosition,
                              String displayName) {
    //TODO(laurence): move google3 specific messages to google3 proper
    super(sourcePosition,
          "message source required for " + displayName
          + " but none provided.  Perhaps you forgot to add"
          + " the target parameter to the gengxp rule.");
  }
}
