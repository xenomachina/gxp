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

package com.google.gxp.compiler.base;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.InvalidMessageError;
import com.google.transconsole.common.messages.InvalidMessageBundleException;
import com.google.transconsole.common.messages.MessageBundle;

import java.util.*;

/**
 * Utility functions for things that are done frequently in the GXP compiler.
 */
public class Util {
  private Util() {
    throw new AssertionError("Don't instantiate me!");
  }

  /**
   * Given a function and a list, returns a list such that each element of the
   * result is the corresponding element of the input list mapped through the
   * input function.  Unlike Lists.transform the result is <em>not</em> a lazy
   * view, and null elements in the result are disallowed.  This is very
   * similar to the map function in Python and Scheme, except the parameters
   * are reversed for consistency with Lists.transform.
   *
   * @throws NullPointerException if any of resulting elements would be null.
   */
  public static <K, V> ImmutableList<V> map(List<K> fromList,
                                            Function<? super K, ? extends V> function) {
    return ImmutableList.copyOf(Lists.transform(fromList, function));
  }

  // TODO(laurence): don't hardcode these. They currently don't do anything
  // except stop NullPointerExceptions, but these are reasonable defaults for
  // when they actually do something. "AdWordsSelect" is the name of the
  // "shared" translation console project for, er... historical reasons.
  private static final String PROJECT_ID  = "AdWordsSelect";
  private static final String LANGUAGE_ID = "en-US";

  /**
   * Build a {@code MessageBundle} from a {@code List} of {@code ExtractedMessage}s.
   */
  public static MessageBundle bundleMessages(AlertSink alertSink,
                                             List<ExtractedMessage> messages) {
    MessageBundle messageBundle = new MessageBundle(PROJECT_ID, LANGUAGE_ID);
    for (ExtractedMessage msg : messages) {
      try {
        messageBundle.addMessage(msg.getTcMessage());
      } catch (InvalidMessageBundleException e) {
        alertSink.add(new InvalidMessageError(msg.getSourcePosition(), e.getCause()));
      }
    }
    return messageBundle;
  }
}
