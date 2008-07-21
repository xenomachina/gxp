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

package com.google.gxp.base;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.PrimitiveArrays;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Map;

/**
 * Message class manages a parameterized text string.  It can be used
 * to represent a message translation containing placeholder original
 * texts.
 */
public final class Message {
  private static final Map<String, Message> messageCache = Maps.newHashMap();
  /**
   * Get an instance of Message.
   *
   * @param msg parameterized text string
   */
  public static Message getInstance(String msg) {
    if (!messageCache.containsKey(msg)) {
      messageCache.put(msg, new Message(msg));
    }
    return messageCache.get(msg);
  }

  private final String msg;
  private final int[] paramLocations;

  private Message(String msg) {
    this.msg = msg;
    this.paramLocations = calculateParamLocations(msg);
  }

  private static final Pattern PARAM_PATTERN = Pattern.compile("%[1-9%]");

  private static int[] calculateParamLocations(String msg) {
    // find the %[1-9%] locations in a list...
    List<Integer> locs = Lists.newArrayList();
    Matcher m = PARAM_PATTERN.matcher(msg);
    int start = 0;
    while (m.find(start)) {
      locs.add(m.start());
      start = m.end();
    }

    // but save them in an array so we only unbox once
    return PrimitiveArrays.toIntArray(locs);
  }

  /**
   * Expand the message with an array of parameters.
   *
   * Parameter format strings ("%[1-9]") in the message will be
   * replaced by the supplied parameter values.  Extra parameters
   * are ignored.
   *
   * @param parameters the parameters necessary to expand the message
   * @return expanded message
   * @throws IllegalArgumentException if not enough parameters are supplied
   */
  public String toString(String... parameters) {
    if (paramLocations.length == 0) {
      return msg;
    }

    StringBuilder sb = new StringBuilder();
    int cur = 0;
    for (int pos : paramLocations) {
      sb.append(msg, cur, pos);
      char ch = msg.charAt(pos + 1);
      if (ch == '%') {
        sb.append('%');
      } else {
        int i = ch - '1';
        if (i >= parameters.length) {
          throw new IllegalArgumentException(
              "Parameter %" + ch + " not supplied for translation \"" + msg + "\"");
        }
        sb.append(parameters[i]);
      }
      cur = pos + 2;
    }
    sb.append(msg, cur, msg.length());
    return sb.toString();
  }
}
