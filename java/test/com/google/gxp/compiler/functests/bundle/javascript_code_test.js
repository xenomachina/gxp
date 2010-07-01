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

function testAttrVariations() {
  com.google.gxp.compiler.functests.bundle.AttrVariationsGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('<option class="theclass">body</option>\n'
                     + '<option class="theclass">body</option>\n'
                     + '<option class="theclass">body</option>\n'
                     + '<option class="theclass">body</option>\n'
                     + '<option class="&lt;&gt;">body</option>\n'
                     + '<option class="&lt;&gt;">body</option>\n'
                     + '<option class="&lt;&gt;">body</option>\n'
                     + '<option class="&lt;&gt;">body</option>\n'
                     + '<option class="&lt;&gt;">body</option>');
};

function testCaller() {
  com.google.gxp.compiler.functests.bundle.CallerGxp.write(OUT, GXP_CONTEXT, 1);
  assertOutputEquals('<div id="1" class="theclass">body</div>\n'
                     + '<div id="2" class="theclass">body</div>\n'
                     + '<div id="3" onclick="alert(\'foo\'); foo();">body</div>\n'
                     + '<option selected>body</option>\n'
                     + '<option selected>body</option>\n'
                     + '<option>body</option>\n'
                     + '<div id="1" onclick="foo()" class="theclass">body</div>\n'
                     + '<img src="fluffy.gif" alt="fluffy the cat">');

  // XML Mode
  com.google.gxp.compiler.functests.bundle.CallerGxp.write(OUT, XML_GXP_CONTEXT, 1);
  assertOutputEquals('<div id="1" class="theclass">body</div>\n'
                     + '<div id="2" class="theclass">body</div>\n'
                     + '<div id="3" onclick="alert(\'foo\'); foo();">body</div>\n'
                     + '<option selected="selected">body</option>\n'
                     + '<option selected="selected">body</option>\n'
                     + '<option>body</option>\n'
                     + '<div id="1" onclick="foo()" class="theclass">body</div>\n'
                     + '<img src="fluffy.gif" alt="fluffy the cat" />');
};
