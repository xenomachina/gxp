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

function testCss() {
  com.google.gxp.compiler.functests.closures.CssGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('<style type="text/css">'
                     + 'foo { font-size:10 }\n'
                     + '</style>\n'
                     + '<style type="text/css">\n'
                     + 'foo { font-size:10 }\n'
                     + '\n</style>');
};

function testJavascript() {
  com.google.gxp.compiler.functests.closures.JavascriptGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('<script type="text/javascript">'
                     + 'var foo = "bar";\n'
                     + '</script>\n'
                     + '<script type="text/javascript">\n'
                     + 'var foo = "bar";\n'
                     + '\n</script>');
};

function testPlaintext() {
  com.google.gxp.compiler.functests.closures.PlaintextGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('foo &lt; &amp; &gt;');
};

function testEvalInPlaintext() {
  com.google.gxp.compiler.functests.closures.EvalInPlaintextGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('text\n'
                     + '\u00bb\n'
                     + 'string\n'
                     + '1\n'
                     + '2\n'
                     + '3\n'
                     + '4\n'
                     + '5.5\n'
                     + '6.5\n'
                     + 'c\n'
                     + '\n'
                     + 'foo < & >');
};
