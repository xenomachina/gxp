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

function testHello() {
  new com.google.gxp.compiler.functests.instantiable.HelloGxp().write(OUT, GXP_CONTEXT);
  assertOutputEquals('Hello World!');

  new com.google.gxp.compiler.functests.instantiable.HelloGxp().getGxpClosure()
      .writeHtml(OUT, GXP_CONTEXT);
  assertOutputEquals('Hello World!');
};

function testCallGxpInterface() {
  com.google.gxp.compiler.functests.instantiable.StringParamInterfaceCallerGxp.write(
      OUT, GXP_CONTEXT, new com.google.gxp.compiler.functests.instantiable.StringParamImplGxp());
  assertOutputEquals('foo=42\nbar=84');
};

function testConstructor() {
  var s1 = 'foo';
  var s2 = 'bar';
  var expected = 'foo\nbar';

  com.google.gxp.compiler.functests.instantiable.ConstructorGxp.write(OUT, GXP_CONTEXT, s1, s2);
  assertOutputEquals(expected);

  com.google.gxp.compiler.functests.instantiable.ConstructorGxp.getGxpClosure(s1, s2)
      .writeHtml(OUT, GXP_CONTEXT);
  assertOutputEquals(expected);

  new com.google.gxp.compiler.functests.instantiable.ConstructorGxp(s1)
      .write(OUT, GXP_CONTEXT, s2);
  assertOutputEquals(expected);

  new com.google.gxp.compiler.functests.instantiable.ConstructorGxp(s1).getGxpClosure(s2)
      .writeHtml(OUT, GXP_CONTEXT);
  assertOutputEquals(expected);
};

function testConstructorCaller() {
  com.google.gxp.compiler.functests.instantiable.ConstructorCallerGxp.write(OUT, GXP_CONTEXT,
                                                                            'foo', 'bar');
  assertOutputEquals('foo\nbar\ndefault\nbar\nfoo\nbar');
};

function testInterface() {
  var s1 = 'foo';
  var s2 = 'bar';
  var expected = 'foo\nbar';

  var getGxpClosureInterface =
      new com.google.gxp.compiler.functests.instantiable.ConstructorGxp(s1);
  getGxpClosureInterface.getGxpClosure(s2).writeHtml(OUT, GXP_CONTEXT);
  assertOutputEquals(expected);

  var writeInterface = new com.google.gxp.compiler.functests.instantiable.ConstructorGxp(s1);
  writeInterface.write(OUT, GXP_CONTEXT, s2);
  assertOutputEquals(expected);
};

function testInstantiableTypeParam() {
  var l1 = ['foo', 'bar'];
  var l2 = ['baz', 'buz'];
  var expected = 'foo bar\nbaz buz';

  com.google.gxp.compiler.functests.instantiable.TypeParamGxp.write(OUT, GXP_CONTEXT, l1, l2);
  assertOutputEquals(expected);

  var i = new com.google.gxp.compiler.functests.instantiable.TypeParamGxp(l1);
  i.write(OUT, GXP_CONTEXT, l2);
  assertOutputEquals(expected);
};
