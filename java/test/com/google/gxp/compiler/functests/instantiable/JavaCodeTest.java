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

package com.google.gxp.compiler.functests.instantiable;

import com.google.common.collect.Lists;
import com.google.gxp.testing.BaseFunctionalTestCase;

import java.util.List;

/**
 * Functional tests of instantiable templates and related functionallity.
 */
public class JavaCodeTest extends BaseFunctionalTestCase {
  public void testHello() throws Exception {
    new HelloGxp.Instance().write(out, gxpContext);
    assertOutputEquals("Hello World!");

    new HelloGxp.Instance().getGxpClosure().write(out, gxpContext);
    assertOutputEquals("Hello World!");
  }

  public void testGxpInterface() throws Exception {
    assertTrue(new StringParamImplGxp.Instance() instanceof StringParamInterfaceGxp);
  }

  public void testCallGxpInterface() throws Exception {
    StringParamInterfaceCallerGxp.write(out, gxpContext, new StringParamImplGxp.Instance());
    assertOutputEquals("foo=42\nbar=84");
  }

  public void testConstructor() throws Exception {
    String s1 = "foo";
    String s2 = "bar";
    String expected = "foo\nbar";

    ConstructorGxp.write(out, gxpContext, s1, s2);
    assertOutputEquals(expected);

    ConstructorGxp.getGxpClosure(s1, s2).write(out, gxpContext);
    assertOutputEquals(expected);

    new ConstructorGxp.Instance(s1).write(out, gxpContext, s2);
    assertOutputEquals(expected);

    new ConstructorGxp.Instance(s1).getGxpClosure(s2).write(out, gxpContext);
    assertOutputEquals(expected);
  }

  public void testConstructorCaller() throws Exception {
    ConstructorCallerGxp.write(out, gxpContext, "foo", "bar");
    assertOutputEquals("foo\nbar\ndefault\nbar\nfoo\nbar");
  }

  public void testInterface() throws Exception {
    String s1 = "foo";
    String s2 = "bar";
    String expected = "foo\nbar";

    GetGxpClosureInterface getGxpClosureInterface = new ConstructorGxp.Instance(s1);
    getGxpClosureInterface.getGxpClosure(s2).write(out, gxpContext);
    assertOutputEquals(expected);

    WriteInterface writeInterface = new ConstructorGxp.Instance(s1);
    writeInterface.write(out, gxpContext, s2);
    assertOutputEquals(expected);
  }

  public void testTypeParam() throws Exception {
    List<String> l1 = Lists.newArrayList("foo", "bar");
    List<String> l2 = Lists.newArrayList("baz", "buz");
    String expected = "foo bar\nbaz buz";

    TypeParamGxp.write(out, gxpContext, l1, l2);
    assertOutputEquals(expected);

    TypeParamInterface<String> i = new TypeParamGxp.Instance<String>(l1);
    i.write(out, gxpContext, l2);
    assertOutputEquals(expected);
  }
}
