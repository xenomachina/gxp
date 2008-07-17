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

package com.google.gxp.compiler;

import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Concatenation;
import com.google.gxp.compiler.base.EscapeExpression;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.parser.Namespace;
import com.google.gxp.compiler.parser.NullNamespace;
import com.google.gxp.compiler.parser.ParsedAttribute;
import com.google.gxp.compiler.schema.Schema;

import static com.google.testing.util.MoreAsserts.*;

/**
 * Tests of GxpcTestCase.
 */
public class GxpcTestCaseTest extends GxpcTestCase {

  public void testList() throws Exception {
    assertContentsInOrder(list(1, 2, 3), 1, 2, 3);
  }

  public void testPos() throws Exception {
    SourcePosition pos1 = pos();
    SourcePosition pos2 = pos();
    assertNotEqual(pos1, pos2);
    assertNotEqual(pos1.getSourceName(), pos2.getSourceName());
    assertNotEqual(pos1.getLine(), pos2.getLine());
    assertNotEqual(pos1.getColumn(), pos2.getColumn());
  }

  public void testAttr() throws Exception {
    Namespace namespace = NullNamespace.INSTANCE;
    String name = "foo";
    String value = "bar";

    ParsedAttribute result = parsedAttr(namespace, name, value);

    assertEquals(namespace, result.getNamespace());
    assertEquals(name, result.getName());
    assertEquals(value, result.getValue());
  }

  public void testStr() throws Exception {
    String value = "Hello, world!";
    StringConstant result = str(value);
    assertEquals(value, result.evaluate());
  }

  public void testEscape() throws Exception {
    Expression subexpression = expr("\"foo < bar\"");
    Schema schema = htmlSchema();

    EscapeExpression result =
        (EscapeExpression) escape(schema, subexpression);

    assertEquals(subexpression, result.getSubexpression());
    assertEquals(schema, result.getSchema());
  }

  public void testExpr() throws Exception {
    String nativeCode = "6 * 9";
    NativeExpression result = expr(nativeCode);
    assertEquals(nativeCode, result.getNativeCode());
  }

  public void testConcat() throws Exception {
    Expression value0 = expr("4 + 12");
    Expression value1 = str("slurm");
    Concatenation result = (Concatenation) concat(null, value0, value1);
    assertEquals(value0, result.getValues().get(0));
    assertEquals(value1, result.getValues().get(1));
  }

  // TODO(laurence): Perhaps I should test more of this stuff. Since it's only
  // for use in tests it shouldn't be too critcal to test it all that throughly
  // as I believe most bugs will tend to expose themselves in the tests that
  // use TestUtil.
}
