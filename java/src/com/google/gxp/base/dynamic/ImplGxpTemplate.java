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

import com.google.gxp.base.GxpTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the super class of all runtime compiled gxp templates.  It
 * contains extra functionality necessary for the rendering of these
 * templates.
 */
public class ImplGxpTemplate extends GxpTemplate {

  private static Pattern PARAM_PATTERN = Pattern.compile("%[1-9%]");

  protected static String formatGxpMessage(String org, String... params) {
    StringBuilder sb = new StringBuilder();
    Matcher m = PARAM_PATTERN.matcher(org);
    int start = 0;
    while (m.find(start)) {
      if (m.start() != start) {
        sb.append(org.substring(start, m.start()));
      }
      String s = m.group().substring(1);
      if (s.equals("%")) {
        sb.append('%');
      } else {
        sb.append(params[Integer.parseInt(s) -  1]);
      }
      start = m.end();
    }
    if (org.length() > start) {
      sb.append(org.substring(start));
    }
    return sb.toString();
  }
}
