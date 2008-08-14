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

package com.google.gxp.html;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gxp.base.GxpContext;
import com.google.gxp.testing.BaseFunctionalTestCase;
import com.google.gxp.testing.TestLocalizable;
import com.google.i18n.Localizable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

/**
 * Tests for {@code HtmlClosures}
 */
public class HtmlClosuresTest extends BaseFunctionalTestCase {
  public void testEmpty() throws Exception {
    HtmlClosures.EMPTY.write(out, gxpContext);
    assertOutputEquals("");
  }

  public void testFromHtml() throws Exception {
    HtmlClosures.fromHtml("foo < bar > baz & quux").write(out, gxpContext);
    assertOutputEquals("foo < bar > baz & quux");

    try {
      HtmlClosures.fromHtml(null);
      fail("should have thrown NullPointerException");
    } catch (NullPointerException e) {
      // good
    }
  }

  public void testFromPlaintext() throws Exception {
    HtmlClosures.fromPlaintext("foo < bar > baz & qux \" quux ' quuux").write(out, gxpContext);
    assertOutputEquals("foo &lt; bar &gt; baz &amp; qux &quot; quux &#39; quuux");

    HtmlClosures.fromPlaintext("\n").write(out, gxpContext);
    assertOutputEquals("\n");

    try {
      HtmlClosures.fromPlaintext(null);
      fail("should have thrown NullPointerException");
    } catch (NullPointerException e) {
      // good
    }
  }

  public void testFromLocalizable() throws Exception {
    Localizable l = new TestLocalizable();

    HtmlClosures.fromLocalizable(l).write(out, createGxpContext(Locale.US, false));
    assertOutputEquals("[toString(en_US)]");

    HtmlClosures.fromLocalizable(l).write(out, createGxpContext(Locale.UK, false));
    assertOutputEquals("[toString(en_GB)]");

    // test proper escaping
    l = new Localizable() {
        public String toString(Locale locale) {
          return "foo < bar > baz & qux \" quux ' quuux";
        }
      };

    HtmlClosures.fromLocalizable(l).write(out, createGxpContext(Locale.UK, false));
    assertOutputEquals("foo &lt; bar &gt; baz &amp; qux &quot; quux &#39; quuux");

    try {
      HtmlClosures.fromLocalizable(null);
      fail("should have thrown NullPointerException");
    } catch (NullPointerException e) {
      // good
    }
  }

  public void testFromFile() throws Exception {
    // create tmp file
    File tmpDir = Files.createTempDir();
    File file = new File(tmpDir, "tmp");
    FileOutputStream fis = new FileOutputStream(file);
    fis.write("foo".getBytes("UTF-8"));
    fis.close();

    // run tests
    HtmlClosures.fromFile(file, Charset.forName("UTF-8")).write(out, gxpContext);
    assertOutputEquals("foo");

    try {
      HtmlClosures.fromFile(null, Charset.forName("UTF-8"));
      fail("should have thrown NullPointerException");
    } catch (NullPointerException e) {
      // good
    }

    try {
      HtmlClosures.fromFile(file, null);
      fail("should have thrown NullPointerException");
    } catch (NullPointerException e) {
      // good
    }

    // cleanup tmp file
    file.delete();
    tmpDir.delete();
  }

  public void testFromReader() throws Exception {
    String content = "foo < bar > baz & qux \" quux ' quuux";
    Reader reader = new StringReader(content);

    HtmlClosure closure = HtmlClosures.fromReader(reader);

    // first call succeeds
    closure.write(out, gxpContext);
    assertOutputEquals(content);

    // 2nd call resets back to the beginning
    closure.write(out, gxpContext);
    assertOutputEquals(content);

    try {
      HtmlClosures.fromReader(null);
      fail("should have thrown NullPointerException");
    } catch (NullPointerException e) {
      // good
    }
  }

  private static final HtmlClosure[] CLOSURE_ARRAY = new HtmlClosure[0];

  public void testConcat_empty() throws Exception {
    List<HtmlClosure> closures = Lists.newArrayList();

    HtmlClosures.concat(closures).write(out, gxpContext);
    assertOutputEquals("");

    HtmlClosures.concat(closures.toArray(CLOSURE_ARRAY)).write(out, gxpContext);
    assertOutputEquals("");
  }

  public void testConcat_single() throws Exception {
    List<HtmlClosure> closures = Lists.newArrayList(HtmlClosures.fromHtml("foo"));

    HtmlClosures.concat(closures).write(out, gxpContext);
    assertOutputEquals("foo");

    HtmlClosures.concat(closures.toArray(CLOSURE_ARRAY)).write(out, gxpContext);
    assertOutputEquals("foo");
  }

  public void testConcat_multiple() throws Exception {
    List<HtmlClosure> closures = Lists.newArrayList(HtmlClosures.fromHtml("foo"),
                                                    HtmlClosures.fromHtml("bar"),
                                                    HtmlClosures.fromHtml("baz"));

    HtmlClosures.concat(closures).write(out, gxpContext);
    assertOutputEquals("foobarbaz");

    HtmlClosures.concat(closures.toArray(CLOSURE_ARRAY)).write(out, gxpContext);
    assertOutputEquals("foobarbaz");
  }

  public void testConcat_throwsIOException() throws Exception {
    final HtmlClosure IOExceptionThrower = new HtmlClosure() {
        public void write(Appendable out, GxpContext gxpContext) throws IOException {
          throw new IOException();
        }
      };

    List<HtmlClosure> closures = Lists.newArrayList(IOExceptionThrower);

    try {
      HtmlClosures.concat(closures).write(out, gxpContext);
      fail("should have thrown IOException");
    } catch (IOException e) {
      // good
    }

    try {
      HtmlClosures.concat(closures.toArray(CLOSURE_ARRAY)).write(out, gxpContext);
      fail("should have thrown IOException");
    } catch (IOException e) {
      // good
    }
  }

  public void testConcat_throwsNullPointerException() throws Exception {
    try {
      HtmlClosures.concat((Iterable<HtmlClosure>) null);
      fail("should have thrown null pointer exception");
    } catch (NullPointerException e) {
      // good
    }

    try {
      HtmlClosures.concat(Lists.<HtmlClosure>newArrayList((HtmlClosure) null));
      fail("should have thrown null pointer exception");
    } catch (NullPointerException e) {
      // good
    }

    try {
      HtmlClosures.concat((HtmlClosure[]) null);
      fail("should have thrown null pointer exception");
    } catch (NullPointerException e) {
      // good
    }

    try {
      HtmlClosures.concat((HtmlClosure) null);
      fail("should have thrown null pointer exception");
    } catch (NullPointerException e) {
      // good
    }
  }

  public void testConcat_changeList() throws Exception {
    List<HtmlClosure> closures = Lists.newArrayList(HtmlClosures.fromHtml("foo"),
                                                    HtmlClosures.fromHtml("bar"));
    HtmlClosure closure = HtmlClosures.concat(closures);
    closures.add(HtmlClosures.fromHtml("baz"));
    closure.write(out, gxpContext);
    assertOutputEquals("foobar");

    HtmlClosure[] closures_array = { HtmlClosures.fromHtml("foo"),
                                     HtmlClosures.fromHtml("bar") };
    closure = HtmlClosures.concat(closures_array);
    closures_array[1] = HtmlClosures.fromHtml("baz");
    closure.write(out, gxpContext);
    assertOutputEquals("foobar");
  }
}
