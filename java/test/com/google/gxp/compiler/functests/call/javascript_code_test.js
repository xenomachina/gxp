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

function testBasicCall() {
  com.google.gxp.compiler.functests.call.BasicCallerGxp.write(OUT, GXP_CONTEXT, 0);
  assertOutputEquals('<i><b>0</b></i>');

  com.google.gxp.compiler.functests.call.BasicCallerGxp.write(OUT, GXP_CONTEXT, 12345);
  assertOutputEquals('<i><b>12345</b></i>');
};

function testBooleanParam() {
  com.google.gxp.compiler.functests.call.BooleanParamCallerGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('yes\n' + 'no\n' + 'yes\n' + 'no');
};

function testCallAsExpression() {
  com.google.gxp.compiler.functests.call.CallAsExpressionGxp.write(OUT, GXP_CONTEXT, 'x val');
  assertOutputEquals(' X: x val\nY: default y \n X: x val\nY: default y ');
};

function testCallAsEscapableExpression() {
  com.google.gxp.compiler.functests.call.CallAsEscapableExpressionGxp.write(OUT, GXP_CONTEXT,
                                                                            'x val');
  assertOutputEquals(
      '<div onclick="alert(\'x val\'); alert(\'default y\');"></div>\n'
      + '<div onclick="alert(\'x val\'); alert(\'default y\');"></div>');
};

function testCallingContentParamsWithDefaults() {
  com.google.gxp.compiler.functests.call.DefaultContentCallerGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('<h1>Hello, World!</h1>\n'
                     + '<h1><i>Untitled</i></h1>\n'
                     + '<h1><i>Untitled</i></h1>\n'
                     + '<h1>Goodbye, World?</h1>');
};

function testCtorParamMethods() {
  var inst = new com.google.gxp.compiler.functests.call.CtorParamCalleeGxp();

  assertEquals(com.google.gxp.compiler.functests.call.CtorParamCalleeGxp.constructS("foo"), "FOO");
  assertEquals(inst.constructS("bar"), "BAR");
};

function testCtorParam() {
  com.google.gxp.compiler.functests.call.CtorParamCallerGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('foo\n'
                     + 'bar\n'
                     + 'BAZ\n'
                     + 'BUZ');
};

function testCondAttrCaller() {
  com.google.gxp.compiler.functests.call.CondAttrCallerGxp.write(OUT, GXP_CONTEXT, true);
  assertOutputEquals('<div id="optionalId">ImaDiv</div>');

  com.google.gxp.compiler.functests.call.CondAttrCallerGxp.write(OUT, GXP_CONTEXT, false);
  assertOutputEquals('<div>ImaDiv</div>');
};

function testCondAttrDefaultParamCaller() {
  com.google.gxp.compiler.functests.call.CondAttrDefaultParamCallerGxp.write(OUT, GXP_CONTEXT, 3);
  assertOutputEquals('7');

  com.google.gxp.compiler.functests.call.CondAttrDefaultParamCallerGxp.write(OUT, GXP_CONTEXT, 20);
  assertOutputEquals('20');
};

function testDefaultParamCaller() {
  com.google.gxp.compiler.functests.call.DefaultParamCallerGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('7\n8\n7\n7\n8\n7');
};

function testDefaultParamMethods() {
  var inst = new com.google.gxp.compiler.functests.call.DefaultParamCalleeGxp();

  var expected = 7;
  assertEquals(expected,
               com.google.gxp.compiler.functests.call.DefaultParamCalleeGxp.getDefaultI());
  assertEquals(expected, inst.getDefaultI());
};

function testNewStyleCall() {
  com.google.gxp.compiler.functests.call.NewStyleCallerGxp.write(OUT, GXP_CONTEXT, 0);
  assertOutputEquals('<i><b>0</b></i>');

  com.google.gxp.compiler.functests.call.NewStyleCallerGxp.write(OUT, GXP_CONTEXT, 12345);
  assertOutputEquals('<i><b>24690</b></i>');
};

function testNewStyleStringCall() {
  com.google.gxp.compiler.functests.call.NewStyleStringCallerGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('<i>foo</i>');
};

function testRegex() {
  com.google.gxp.compiler.functests.call.RegexCallerGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('foo');
};

function testWriteWithObjMethod() {
  com.google.gxp.compiler.functests.call.DefaultParamCalleeGxp.writeWithObj(
      OUT, GXP_CONTEXT, {i:1});
  assertOutputEquals('1');

  com.google.gxp.compiler.functests.call.DefaultParamCalleeGxp.writeWithObj(OUT, GXP_CONTEXT, {});
  assertOutputEquals('7');

  var inst = new com.google.gxp.compiler.functests.call.DefaultParamCalleeGxp();
  inst.writeWithObj(OUT, GXP_CONTEXT, {i:1});
  assertOutputEquals('1');

  inst.writeWithObj(OUT, GXP_CONTEXT, {});
  assertOutputEquals('7');
};
