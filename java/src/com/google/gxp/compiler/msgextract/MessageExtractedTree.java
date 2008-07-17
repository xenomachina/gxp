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

package com.google.gxp.compiler.msgextract;

import com.google.common.collect.ImmutableList;
import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.ExtractedMessage;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.Tree;

import java.util.List;

/**
 * The output of {@link MessageExtractor}.
 */
public class MessageExtractedTree extends Tree<Root> {
  private final ImmutableList<ExtractedMessage> messages;

  MessageExtractedTree(SourcePosition sourcePosition, AlertSet alerts, Root root,
                       List<ExtractedMessage> messages) {
    super(sourcePosition, alerts, root);
    this.messages = ImmutableList.copyOf(messages);
  }

  public List<ExtractedMessage> getMessages() {
    return messages;
  }
}
