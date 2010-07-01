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

function testOneMsg() {
  com.google.gxp.compiler.functests.i18n.OneMsgGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals("<b>hello, world!</b>");
};

function testNestedMsgs() {
  com.google.gxp.compiler.functests.i18n.NestedMsgsGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('baz &lt; bot');
};

function testMessageMetaChars() {
  com.google.gxp.compiler.functests.i18n.MessageMetaCharsGxp.write(OUT, GXP_CONTEXT, 98.5);
  assertOutputEquals('110% was required but you only gave 98.5%!');
};

function testMsgInCss() {
  com.google.gxp.compiler.functests.i18n.MsgInCssGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('<style type="text/css">\n'
                     + '  body {\n'
                     + '    font-family: "arial";\n'
                     + '  }\n'
                     + '</style>');
};

function testMsgInPlaintext() {
  com.google.gxp.compiler.functests.i18n.MsgInPlaintextGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('hello world\n'
                     + 'funny chars: < > \' "\n'
                     + 'funny chars in eval: < > \' "');
};

function testMsgInScript() {
  com.google.gxp.compiler.functests.i18n.MsgInScriptGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('<script type="text/javascript">\n'
                     + '  var x = \'hello world\';\n'
                     + '  var y = \'\\074b\\076bold\\074/b\\076\';\n'
                     + '</script>');
};

function testSimplePlaceholder() {
  com.google.gxp.compiler.functests.i18n.SimplePlaceholderGxp.write(OUT, GXP_CONTEXT, 'cruel');
  assertOutputEquals('hello <b>cruel</b> world!');

  com.google.gxp.compiler.functests.i18n.SimplePlaceholderGxp.write(OUT, GXP_CONTEXT, 'kind');
  assertOutputEquals('hello <b>kind</b> world!');

  com.google.gxp.compiler.functests.i18n.SimplePlaceholderGxp.write(OUT, GXP_CONTEXT, '<&>');
  assertOutputEquals('hello <b>&lt;&amp;&gt;</b> world!');
};

function testPlaceholder() {
  com.google.gxp.compiler.functests.i18n.PlaceholderGxp.write(OUT, GXP_CONTEXT, 'bob');
  assertOutputEquals('bob');
};

function testPhAttribute() {
  com.google.gxp.compiler.functests.i18n.PhAttributeGxp.write(OUT, GXP_CONTEXT,
                                                              'http://www.google.com');
  assertOutputEquals('click ' 
                     + '<a href="http://www.google.com" class="foo" name="bar">'
                     + 'here</a>\n'
                     + 'foo <br> bar\n'
                     + '<a name="foo"></a>\n'
                     + 'bob');
};

function testMsgNamespace() {
  com.google.gxp.compiler.functests.i18n.MsgNamespaceGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('<div id="foo"></div>\n<b>bar</b>\n<b>baz</b>');
};

function testNoMsgNamespace() {
  com.google.gxp.compiler.functests.i18n.NoMsgNamespaceGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('<img alt="foo">\n<b>bar</b>\n<b>baz</b>');
};

function testAttrInsideMsg() {
  com.google.gxp.compiler.functests.i18n.AttrInsideMsgGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('<div class="foo"></div>');
};
