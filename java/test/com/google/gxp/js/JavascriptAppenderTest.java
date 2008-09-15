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

package com.google.gxp.js;

import com.google.gxp.testing.BaseFunctionalTestCase;


import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Tests for {@code JavascriptAppender}.
 */
public class JavascriptAppenderTest extends BaseFunctionalTestCase {
  public void testPrimitives() throws Exception {
    // byte
    JavascriptAppender.INSTANCE.append(out, gxpContext, (byte)10);
    assertOutputEquals("10");

    // short
    JavascriptAppender.INSTANCE.append(out, gxpContext, (short)2048);
    assertOutputEquals("2048");

    // int
    JavascriptAppender.INSTANCE.append(out, gxpContext, -12345678);
    assertOutputEquals("-12345678");

    // long
    JavascriptAppender.INSTANCE.append(out, gxpContext, 16777216512L);
    assertOutputEquals("16777216512");

    // float
    JavascriptAppender.INSTANCE.append(out, gxpContext, 38.125f);
    assertOutputEquals("38.125");

    // double
    JavascriptAppender.INSTANCE.append(out, gxpContext, 938.05625d);
    assertOutputEquals("938.05625");

    // char
    JavascriptAppender.INSTANCE.append(out, gxpContext, 'a');
    assertOutputEquals("\"a\"");

    JavascriptAppender.INSTANCE.append(out, gxpContext, '"');
    assertOutputEquals("\"\\x22\"");

    JavascriptAppender.INSTANCE.append(out, gxpContext, '\'');
    assertOutputEquals("\"\\x27\"");

    // boolean
    JavascriptAppender.INSTANCE.append(out, gxpContext, true);
    assertOutputEquals("true");

    JavascriptAppender.INSTANCE.append(out, gxpContext, false);
    assertOutputEquals("false");
  }

  public void testWriteBoolean() throws Exception {
    JavascriptAppender.INSTANCE.append(out, gxpContext, Boolean.TRUE);
    assertOutputEquals("true");

    JavascriptAppender.INSTANCE.append(out, gxpContext, Boolean.FALSE);
    assertOutputEquals("false");
  }

  public void testWriteString() throws Exception {
    JavascriptAppender.INSTANCE.append(out, gxpContext, "foo ' bar \" baz");
    assertOutputEquals("\"foo \\x27 bar \\x22 baz\"");

    JavascriptAppender.INSTANCE.append(out, gxpContext, (CharSequence)null);
    assertOutputEquals("null");
  }

  public void testWriteJSONArray() throws Exception {
    JSONArray json = new JSONArray();
    json.put(1);
    json.put(true);
    json.put(2.2);
    json.put("foo");
    JavascriptAppender.INSTANCE.append(out, gxpContext, json);
    assertOutputEquals("[1,true,2.2,\"foo\"]");

    json = null;
    try {
      JavascriptAppender.INSTANCE.append(out, gxpContext, json);
      fail("expected NullPointerException");
    } catch (NullPointerException e) {
      // good
    }
  }

  public void testWriteJSONObject() throws Exception {
    JSONObject json = new JSONObject();
    json.put("k1", 1);
    json.put("k2", true);
    json.put("k3", 2.2);
    JavascriptAppender.INSTANCE.append(out, gxpContext, json);
    assertOutputEquals("{\"k3\":2.2,\"k1\":1,\"k2\":true}");

    json = null;
    try {
      JavascriptAppender.INSTANCE.append(out, gxpContext, json);
      fail("expected NullPointerException");
    } catch (NullPointerException e) {
      // good
    }
  }
}
