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

function testMultilingualEval() {
  com.google.gxp.compiler.functests.multilingual.EvalGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('You are running: JavaScript\n'
                     + 'Is this C++? No\n'
                     + 'Is this Java? No\n'
                     + 'Is this JavaScript? Yes');
};

function testMultilingualAbbr() {
  com.google.gxp.compiler.functests.multilingual.AbbrGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('You are running: JavaScript');
};

function testMultilingualCond() {
  com.google.gxp.compiler.functests.multilingual.CondGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('You are running:\nJavaScript');
};

function testMultilingualIfElse() {
  com.google.gxp.compiler.functests.multilingual.IfElseGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('You are running:\nJavaScript');
};

function testMultilingualLoop() {
  com.google.gxp.compiler.functests.multilingual.LoopGxp.write(OUT, GXP_CONTEXT, 3);
  assertOutputEquals('* * * * * | * * * * *');
};

function testMultilingualLoopDelimiter() {
  com.google.gxp.compiler.functests.multilingual.LoopDelimiterGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('|JavaScript|');
};

function testMultilingualOutputElement() {
  com.google.gxp.compiler.functests.multilingual.OutputElementGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('<div class="JavaScript"></div>');
};

function testMultilingualCaller() {
  com.google.gxp.compiler.functests.multilingual.CallerGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('You are running: JavaScript\n'
                     + 'Is this C++? No\n'
                     + 'Is this Java? No\n'
                     + 'Is this JavaScript? Yes\n'
                     + 'You are running: JavaScript');
};
