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

package com.google.gxp.css;

import com.google.gxp.testing.BaseFunctionalTestCase;

import java.net.URI;

/**
 * Tests for {@code CssAppender}.
 */
public class CssAppenderTest extends BaseFunctionalTestCase {
  public void testWriteUri() throws Exception {
    CssAppender.INSTANCE.append(out, gxpContext, new URI("http://www.via.com.tw"));
    assertOutputEquals("url(\"http://www.via.com.tw\")");

    CssAppender.INSTANCE.append(out, gxpContext, new URI("http://news.com/id/203,189,20201.html"));
    assertOutputEquals("url(\"http://news.com/id/203\\,189\\,20201.html\")");

    CssAppender.INSTANCE.append(out, gxpContext, new URI("/some(image).gif"));
    assertOutputEquals("url(\"/some\\(image\\).gif\")");
  }

  public void testWriteString() throws Exception {
    CssAppender.INSTANCE.append(out, gxpContext, "hello, world!");
    assertOutputEquals("\"hello, world!\"");

    CssAppender.INSTANCE.append(out, gxpContext, "one\ntwo\nthree");
    assertOutputEquals("\"one\\a two\\a three\"");

    CssAppender.INSTANCE.append(out, gxpContext, "What are \"scare quotes\"?");
    assertOutputEquals("\"What are \\\"scare quotes\\\"?\"");

    CssAppender.INSTANCE.append(out, gxpContext, "Don't mess with CSS!");
    assertOutputEquals("\"Don\\'t mess with CSS!\"");

    CssAppender.INSTANCE.append(out, gxpContext, "c:\\windows\\fubar.dll");
    assertOutputEquals("\"c:\\\\windows\\\\fubar.dll\"");
  }

  public void testAppendDouble() throws Exception {
    CssAppender.INSTANCE.append(out, gxpContext, 1.5);
    assertOutputEquals("1.5");

    CssAppender.INSTANCE.append(out, gxpContext, 0.5);
    assertOutputEquals("0.5");

    CssAppender.INSTANCE.append(out, gxpContext, 0);
    assertOutputEquals("0");

    CssAppender.INSTANCE.append(out, gxpContext, 0.0);
    assertOutputEquals("0");

    CssAppender.INSTANCE.append(out, gxpContext, -0);
    assertOutputEquals("0");

    CssAppender.INSTANCE.append(out, gxpContext, -0.3);
    assertOutputEquals("-0.3");

    CssAppender.INSTANCE.append(out, gxpContext, -3.51);
    assertOutputEquals("-3.51");

    CssAppender.INSTANCE.append(out, gxpContext, 1.0);
    assertOutputEquals("1");

    CssAppender.INSTANCE.append(out, gxpContext, 1f);
    assertOutputEquals("1");

    CssAppender.INSTANCE.append(out, gxpContext, 1);
    assertOutputEquals("1");
  }
}
