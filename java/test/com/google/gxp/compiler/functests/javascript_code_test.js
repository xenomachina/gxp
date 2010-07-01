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

/**
 * Functional tests of javascript code generation.
 */

function testHelloWorld() {
  com.google.gxp.compiler.functests.HelloGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('hello, world!');
};

var HTML_STRICT_DOCTYPE =
    '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"'
    + ' "http://www.w3.org/TR/html4/strict.dtd">';

var XHTML_STRICT_DOCTYPE =
    '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"'
    + ' "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">';

var XHTML_MOBILE_DOCTYPE =
    '<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN"'
    + ' "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">';

function testDoctype() {
  com.google.gxp.compiler.functests.DoctypeGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals(HTML_STRICT_DOCTYPE + '<html></html>');
};

function testSomeTags() {
  com.google.gxp.compiler.functests.SomeTagsGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('foo <b>bar</b> <img src="baz.gif" alt="baz">');
};

function testIntEval() {
  com.google.gxp.compiler.functests.IntEvalGxp.write(OUT, GXP_CONTEXT, 5);
  assertOutputEquals('returned 5 results');

  com.google.gxp.compiler.functests.IntEvalGxp.write(OUT, GXP_CONTEXT, 12345678);
  assertOutputEquals('returned 12345678 results');

  com.google.gxp.compiler.functests.IntEvalGxp.write(OUT, GXP_CONTEXT, -8);
  assertOutputEquals('returned -8 results');

  com.google.gxp.compiler.functests.IntEvalGxp.write(OUT, GXP_CONTEXT, 0);
  assertOutputEquals('returned 0 results');
};

function testIntExpr() {
  com.google.gxp.compiler.functests.IntExprGxp.write(OUT, GXP_CONTEXT, 5);
  assertOutputEquals('click <a href="5">here</a>.');

  com.google.gxp.compiler.functests.IntExprGxp.write(OUT, GXP_CONTEXT, 12345678);
  assertOutputEquals('click <a href="12345678">here</a>.');

  com.google.gxp.compiler.functests.IntExprGxp.write(OUT, GXP_CONTEXT, -8);
  assertOutputEquals('click <a href="-8">here</a>.');

  com.google.gxp.compiler.functests.IntExprGxp.write(OUT, GXP_CONTEXT, 0);
  assertOutputEquals('click <a href="0">here</a>.');
};

function testStringEval() {
  com.google.gxp.compiler.functests.StringEvalGxp.write(OUT, GXP_CONTEXT, 'abc123');
  assertOutputEquals('foo abc123 bar');

  com.google.gxp.compiler.functests.StringEvalGxp.write(OUT, GXP_CONTEXT, '  abc123  ');
  assertOutputEquals('foo   abc123   bar');

  com.google.gxp.compiler.functests.StringEvalGxp.write(OUT, GXP_CONTEXT, '');
  assertOutputEquals('foo  bar');

  var s = 'xyz<>&\u4321\u00a0"\'123';
  com.google.gxp.compiler.functests.StringEvalGxp.write(OUT, GXP_CONTEXT, s);
  assertOutputEquals('foo xyz&lt;&gt;&amp;\u4321\u00a0&quot;\'123 bar');

  var caughtException = false;
  try {
    com.google.gxp.compiler.functests.StringEvalGxp.write(OUT, GXP_CONTEXT, null);
  } catch (e) {
    caughtException = true;
  }
  OUT.clear();
  assertTrue(caughtException);
};

function testShortEval() {
  com.google.gxp.compiler.functests.ShortEvalGxp.write(OUT, GXP_CONTEXT, 'abc123');
  assertOutputEquals('foo abc123 bar');
}

function testStringExpr() {
  com.google.gxp.compiler.functests.StringExprGxp.write(OUT, GXP_CONTEXT, 'abc123');
  assertOutputEquals('click <a href="abc123">here</a>.');

  com.google.gxp.compiler.functests.StringExprGxp.write(OUT, GXP_CONTEXT, '  abc123  ');
  assertOutputEquals('click <a href="  abc123  ">here</a>.');

  com.google.gxp.compiler.functests.StringExprGxp.write(OUT, GXP_CONTEXT, '');
  assertOutputEquals('click <a href="">here</a>.');

  var s = 'xyz<>&\u4321\u00a0"\'123';
  com.google.gxp.compiler.functests.StringExprGxp.write(OUT, GXP_CONTEXT, s);
  assertOutputEquals('click <a href="xyz&lt;&gt;&amp;\u4321\u00a0&quot;\'123">here</a>.');

  var caughtException = false;
  try {
    com.google.gxp.compiler.functests.StringExprGxp.write(OUT, GXP_CONTEXT, null);
  } catch (e) {
    caughtException = true;
  }
  OUT.clear();
  assertTrue(caughtException);
};

function testSpacePreservation() {
  com.google.gxp.compiler.functests.SpacePreservationGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('foo\n'
                     + 'bar\n'
                     + 'baz\n'
                     + '<script type="text/javascript">\n'
                     + '  alert(\'hello\');\n'
                     + '</script>\n'
                     + '<style type="text/css">\n'
                     + '  .header {\n'
                     + '    color: red;\n'
                     + '  }\n'
                     + '</style>\n'
                     + 'quux\n'
                     + 'zarf\n'
                     + 'zot!');
};

function testEvalInScript() {
  com.google.gxp.compiler.functests.EvalInScriptGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('<script type="text/javascript">\n'
                     + '  var x = 1234567.1;\n'
                     + '  var y = \'don\\047t worry, be \\074em\\076happy\\074/em\\076\';\n'
                     + '</script>');
};

function testJsMsgEscape() {
  com.google.gxp.compiler.functests.JsMsgEscapeGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('<script type="text/javascript">\n'
                     + '  var foo = \'foo\';\n'
                     + '  var bar = \'bar\';\n'
                     + '</script>\n'
                     + '<div onclick="alert(\'foo \\042 \\047\');"></div>');
};

function testIfBasic() {
  com.google.gxp.compiler.functests.IfBasicGxp.write(OUT, GXP_CONTEXT, 1);
  assertOutputEquals('That number is less than 5.');

  com.google.gxp.compiler.functests.IfBasicGxp.write(OUT, GXP_CONTEXT, 7);
  assertOutputEquals('');
};

function testIfElse() {
  com.google.gxp.compiler.functests.IfElseGxp.write(OUT, GXP_CONTEXT, 1);
  assertOutputEquals('That number is\nless than\n5.');

  com.google.gxp.compiler.functests.IfElseGxp.write(OUT, GXP_CONTEXT, 7);
  assertOutputEquals('That number is\ngreater than or equal to\n5.');
};

function testIfElseIf() {
  com.google.gxp.compiler.functests.IfElseIfGxp.write(OUT, GXP_CONTEXT, 1);
  assertOutputEquals('That number is\nless than\n5.');

  com.google.gxp.compiler.functests.IfElseIfGxp.write(OUT, GXP_CONTEXT, 5);
  assertOutputEquals('That number is\nequal to\n5.');

  com.google.gxp.compiler.functests.IfElseIfGxp.write(OUT, GXP_CONTEXT, 7);
  assertOutputEquals('That number is\ngreater than or equal to\n5.');
};

function testCond() {
  com.google.gxp.compiler.functests.CondGxp.write(OUT, GXP_CONTEXT, 1);
  assertOutputEquals('That number is\nless than\n5.');

  com.google.gxp.compiler.functests.CondGxp.write(OUT, GXP_CONTEXT, 5);
  assertOutputEquals('That number is\nequal to\n5.');

  com.google.gxp.compiler.functests.CondGxp.write(OUT, GXP_CONTEXT, 7);
  assertOutputEquals('That number is\ngreater than or equal to\n5.');
};

function testAbbr() {
  com.google.gxp.compiler.functests.AbbrGxp.write(OUT, GXP_CONTEXT, 1);
  assertOutputEquals('a1\nb1\nc&lt;*&gt;');

  com.google.gxp.compiler.functests.AbbrGxp.write(OUT, GXP_CONTEXT, 2);
  assertOutputEquals('a2\nb4\nc&lt;* *&gt;');

  com.google.gxp.compiler.functests.AbbrGxp.write(OUT, GXP_CONTEXT, 3);
  assertOutputEquals('a3\nb9\nc&lt;* * *&gt;');
};

function testAbbrCall() {
  com.google.gxp.compiler.functests.AbbrCallGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals('foo hello bar');
};

/**
 * Various google libraries modify the Array prototype, so we do the
 * same to test that <gxp:loop>s over Arrays aren't affected by these
 * modifications.
 */
//Array.prototype.peek = function() {
//  return this[this.length - 1];
//};

function testLoop() {
  com.google.gxp.compiler.functests.LoopGxp.write(OUT, GXP_CONTEXT, 0);
  assertOutputEquals('<b></b>');

  com.google.gxp.compiler.functests.LoopGxp.write(OUT, GXP_CONTEXT, 1);
  assertOutputEquals('<b>!</b>');

  com.google.gxp.compiler.functests.LoopGxp.write(OUT, GXP_CONTEXT, 5);
  assertOutputEquals('<b>! ! ! ! !</b>');

  com.google.gxp.compiler.functests.LoopGxp.write(OUT, GXP_CONTEXT, 7);
  assertOutputEquals('<b>! ! ! ! ! ! !</b>');
};

function testLoopDelimiter() {
  com.google.gxp.compiler.functests.LoopDelimiterGxp.write(OUT, GXP_CONTEXT, 0);
  assertOutputEquals('<b></b>\n<b></b>\n<b></b>\n<b></b>\n<b></b>\n<b></b>');

  com.google.gxp.compiler.functests.LoopDelimiterGxp.write(OUT, GXP_CONTEXT, 1);
  assertOutputEquals('<b>!</b>\n<b>!</b>\n<b>!</b>\n<b>!</b>\n<b>!</b>\n<b>!</b>');

  com.google.gxp.compiler.functests.LoopDelimiterGxp.write(OUT, GXP_CONTEXT, 3);
  assertOutputEquals('<b>!!!</b>\n<b>!&amp;!&amp;!</b>\n<b>!,!,!</b>\n<b>!,!,!</b>'
                     + '\n<b>!,!,!</b>\n<b>!,!,!</b>');
};

function testLoopKey() {
  com.google.gxp.compiler.functests.LoopKeyGxp.write(OUT, GXP_CONTEXT,
                                                     ['Alice', 'Bob', 'Charlie']);
  assertOutputEquals('0:Alice 1:Bob 2:Charlie');
};

function testIterableLoop() {
  com.google.gxp.compiler.functests.IterableLoopGxp.write(OUT, GXP_CONTEXT, []);
  assertOutputEquals('<ul></ul>');

  com.google.gxp.compiler.functests.IterableLoopGxp.write(OUT, GXP_CONTEXT, ['McCloud']);
  assertOutputEquals('<ul><li>McCloud</li></ul>');

  com.google.gxp.compiler.functests.IterableLoopGxp.write(OUT, GXP_CONTEXT,
                                                          ['one', 'two', 'three']);
  assertOutputEquals('<ul><li>one</li> <li>two</li> <li>three</li></ul>');
};

function testContentParamCalls() {
  com.google.gxp.compiler.functests.ContentParamCallerGxp.write(OUT, GXP_CONTEXT, 0,
                                                                'hello, world!');
  assertOutputEquals('<div id="html">'
                     + '<b id="0">hello, world!</b>'
                     + '</div>\n'
                     + '<div id="html-oc">'
                     + '<b id="0">1 &lt; 2 &amp; &quot;O&#39;Foozle&quot;</b>'
                     + '</div>');

  com.google.gxp.compiler.functests.ContentParamCallerGxp.write(OUT, GXP_CONTEXT, 12345,
                                                                'a < b > c " d & e \' f \\ g');
  assertOutputEquals('<div id="html">'
                     + '<b id="12345">a &lt; b &gt; c &quot; d &amp; e \' f \\ g</b>'
                     + '</div>\n'
                     + '<div id="html-oc">'
                     + '<b id="12345">1 &lt; 2 &amp; &quot;O&#39;Foozle&quot;</b>'
                     + '</div>');
};

function testCssContentParam() {
  com.google.gxp.compiler.functests.CssContentParamGxp.write(
      OUT, GXP_CONTEXT, goog.gxp.css.CssClosures.fromCss('foo'));
  assertOutputEquals('foo');
}; 

function testContentType() {
  com.google.gxp.compiler.functests.ContentTypeGxp.write(
      OUT, GXP_CONTEXT, com.google.gxp.compiler.functests.ContentTypeGxp.getDefaultP());
  assertOutputEquals('param\nabbr');
};

function testContainerCall() {
  com.google.gxp.compiler.functests.ContainerCallerGxp.write(OUT, GXP_CONTEXT, 0);
  assertOutputEquals('<i><b id="0">Foo, <b>bar</b> &amp; baz.</b></i>');

  com.google.gxp.compiler.functests.ContainerCallerGxp.write(OUT, GXP_CONTEXT, 12345);
  assertOutputEquals('<i><b id="12345">Foo, <b>bar</b> &amp; baz.</b></i>');
};

function testEvalInContainerCall() {
  com.google.gxp.compiler.functests.EvalInContainerCallerGxp.write(OUT, GXP_CONTEXT,
                                                                   'snoo snah snuh');
  var expected = '<i><b id="42">snoo snah snuh</b></i>';
  assertOutputEquals(expected + '\n' + expected);

  com.google.gxp.compiler.functests.EvalInContainerCallerGxp.write(OUT, GXP_CONTEXT,
                                                                   'he said "1 < 2 & 5 > 3"');
  expected = '<i><b id="42">'
      + 'he said &quot;1 &lt; 2 &amp; 5 &gt; 3&quot;'
      + '</b></i>';
  assertOutputEquals(expected + '\n' + expected);
};

function testEvalInNonContentAttrElement() {
  com.google.gxp.compiler.functests.EvalInNonContentAttrElementGxp.write(OUT, GXP_CONTEXT,
                                                                         'snoo snah snuh');
  assertOutputEquals('<i>foo SNOO SNAH SNUH bar</i>');

  com.google.gxp.compiler.functests.EvalInNonContentAttrElementGxp.write(OUT, GXP_CONTEXT,
                                                                         'he said "1 < 2 & 5 > 3"');
  assertOutputEquals('<i>foo HE SAID &quot;1 &lt; 2 &amp; 5 &gt; 3&quot; bar</i>');
};

function testBasicAttr() {
  com.google.gxp.compiler.functests.BasicAttrGxp.write(OUT, GXP_CONTEXT, 0);
  assertOutputEquals('<img src="0lives.png" alt="You have 0 lives left.">');

  com.google.gxp.compiler.functests.BasicAttrGxp.write(OUT, GXP_CONTEXT, 5);
  assertOutputEquals('<img src="5lives.png" alt="You have 5 lives left.">');
};

function testCondAttr() {
  com.google.gxp.compiler.functests.CondAttrGxp.write(OUT, GXP_CONTEXT, 1);
  assertOutputEquals('<div class="1-lives">You have 1 lives left.</div>');

  com.google.gxp.compiler.functests.CondAttrGxp.write(OUT, GXP_CONTEXT, 0);
  assertOutputEquals('<div class="0-lives">You have 0 lives left.</div>');

  com.google.gxp.compiler.functests.CondAttrGxp.write(OUT, GXP_CONTEXT, -1);
  assertOutputEquals('<div>You have -1 lives left.</div>');
};

function throwException(msg) {
  var e = new Error(msg);
  throw e;
};

function testThrows() {
  com.google.gxp.compiler.functests.ThrowsGxp.write(OUT, GXP_CONTEXT, false);
  assertOutputEquals('No problem.');

  var caughtException = false;
  try {
    com.google.gxp.compiler.functests.ThrowsGxp.write(OUT, GXP_CONTEXT, true);
  } catch (e) {
    caughtException = true;
    assertEquals("A problem!", e.message);
  }
  OUT.clear();
  assertTrue(caughtException);
};

function testThrowsInAnonymousClosure() {
  com.google.gxp.compiler.functests.ThrowsInAnonymousClosureGxp.write(OUT, GXP_CONTEXT, false);
  assertOutputEquals('<div id="outer"><b id="5"><div id="inner">No problem.</div></b></div>');

  var caughtException = false;
  try {
    com.google.gxp.compiler.functests.ThrowsInAnonymousClosureGxp.write(OUT, GXP_CONTEXT, true);
  } catch (e) {
    caughtException = true;
    assertEquals("A problem!", e.message);
  }
  OUT.clear();
  assertTrue(caughtException);
};

function testTypeParam() {
  com.google.gxp.compiler.functests.TypeParamGxp.write(OUT, GXP_CONTEXT, 3, [1, 2, 3, 4, 5]);
  assertOutputEquals('1 2 <b>3</b> 4 5');

  com.google.gxp.compiler.functests.TypeParamGxp.write(OUT, GXP_CONTEXT, 'bar',
                                                       ['foo', 'bar', 'baz', 'quux', 'zarf']);
  assertOutputEquals('foo <b>bar</b> baz quux zarf');
};

function testTypeParamExtends() {
  com.google.gxp.compiler.functests.TypeParamExtendsGxp.write(OUT, GXP_CONTEXT, 3, [1, 2, 3, 4, 5]);
  assertOutputEquals('1 2 <b>3</b> 4 5');

  com.google.gxp.compiler.functests.TypeParamExtendsGxp.write(OUT, GXP_CONTEXT, 2.2,
                                                              [1.1, 2.2, 3.3, 4.4, 5.5]);
  assertOutputEquals('1.1 <b>2.2</b> 3.3 4.4 5.5');
};

function testPrettyGenerics() {
  var list = [[1,2],[3,4]];
  com.google.gxp.compiler.functests.PrettyGenericsGxp.write(OUT, GXP_CONTEXT, list);
  assertOutputEquals('1 2 3 4');
};

function testBooleanAttrib() {
  com.google.gxp.compiler.functests.BooleanAttribGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals(
      '<input type="radio" name="key1" value="value1" checked>\n'
      + '<input type="radio" name="key1" value="value2">\n'
      + '<input type="checkbox" name="key2" value="value" checked>\n'
      + '<input type="checkbox" name="key2" value="value" checked>');
};

function testSpaceControl() {
  com.google.gxp.compiler.functests.SpaceControlGxp.write(OUT, GXP_CONTEXT);
  // The words on each line tell you the values of ispace and espace on the
  // "b" element, respectively. "inherit" means not set, ie: inherited from
  // the gxp:template, which has ispace and espace both set to remove in this
  // test.
  assertOutputEquals(
      '<b>1inherit1inherit</b>'
      + '<b>\n  2inherit2preserve\n</b>'
      + '<b>3inherit3remove</b>'
      + '<b>\n4inherit4collapse\n</b>'
      + '<b> 5inherit5normalize </b>'
      + '<b>6preserve\n  6inherit</b>'
      + '<b>\n  7preserve\n  7preserve\n</b>'
      + '<b>8preserve\n  8remove</b>'
      + '<b>\n9preserve\n  9collapse\n</b>'
      + '<b> 10preserve\n  10normalize </b>'
      + '<b>11remove11inherit</b>'
      + '<b>\n  12remove12preserve\n</b>'
      + '<b>13remove13remove</b>'
      + '<b>\n14remove14collapse\n</b>'
      + '<b> 15remove15normalize </b>'
      + '<b>16collapse\n16inherit</b>'
      + '<b>\n  17collapse\n17preserve\n</b>'
      + '<b>18collapse\n18remove</b>'
      + '<b>\n19collapse\n19collapse\n</b>'
      + '<b> 20collapse\n20normalize </b>'
      + '<b>21normalize 21inherit</b>'
      + '<b>\n  22normalize 22preserve\n</b>'
      + '<b>23normalize 23remove</b>'
      + '<b>\n24normalize 24collapse\n</b>'
      + '<b> 25normalize 25normalize </b>'

      // nested CollapseExpressions
      + '<b>\n26collapse\n26collapse\n</b>'
      + '<b><i> 27normalize 27normalize </i></b>'
      + '<b><i> 28collapse\n28normalize </i></b>'

      // calls (parameters and nested content)
      + '<b id="0">29remove29remove</b>'
      + '<b id="0"> 30normalize 30normalize </b>'
      + '<b> 31preserve\n  31normalize </b>'
      + '<b> 32preserve\n  32normalize </b>'

      // special elements
      + '33normalize 33remove' // gxp:msg
      + '<script type="text/javascript">'
      + '\n  34preserve\n  34preserve\n</script>'
      + '<style type="text/css">\n  35preserve\n  35preserve\n</style>'
      + '<pre>\n  36preserve\n  36preserve\n</pre>'
      + '<textarea rows="10" cols="10">'
      + '\n  37preserve\n  37preserve\n</textarea>'
      + '38normalize 38remove'
      + '<div class="39normalize 39remove"></div>'
      + '<div class=" 40remove40normalize "></div>'
      );
};

function testSgml() {
  com.google.gxp.compiler.functests.SgmlXmlDifferencesGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals(
      HTML_STRICT_DOCTYPE
      + '<html>'
      + '<div id="snarf">'
      + '<img alt="hello, world!" ismap>\n'
      + '<br>'
      + '</div>\n'
      + 'foo <br> bar <br> baz'
      + '</html>');
};

function testXml() {
  com.google.gxp.compiler.functests.SgmlXmlDifferencesGxp.write(OUT, XML_GXP_CONTEXT);
  assertOutputEquals(
      '<?xml version="1.0" ?>\n'
      + XHTML_STRICT_DOCTYPE
      + '<html xmlns="http://www.w3.org/1999/xhtml">'
      + '<div id="snarf">'
      + '<img alt="hello, world!" ismap="ismap" />\n'
      + '<br />'
      + '</div>\n'
      + 'foo <br /> bar <br /> baz'
      + '</html>');
};

function testXmlOnlyDoctypeInXml() {
  com.google.gxp.compiler.functests.XmlOnlyDoctypeGxp.write(OUT, XML_GXP_CONTEXT);
  assertOutputEquals(
      '<?xml version="1.0" ?>\n'
      + XHTML_MOBILE_DOCTYPE
      + '<html xmlns="http://www.w3.org/1999/xhtml"></html>');
};

function testXmlOnlyDoctypeInSgml() {
  var caughtException = false;
  try {
    com.google.gxp.compiler.functests.XmlOnlyDoctypeGxp.write(OUT, GXP_CONTEXT);
  } catch (e) {
    caughtException = true;
    assertEquals("Doctype 'mobile' incompatible with non-XML syntax", e.message);
  }
  OUT.clear();
  assertTrue(caughtException);
};

function repeat(s, count) {
  var result = '';
  for (var i = 0; i < count; i++) {
    result = result + s;
  }
  return result;
};

function testReallyBig() {
  com.google.gxp.compiler.functests.ReallyBigGxp.write(OUT, GXP_CONTEXT);
  assertOutputEquals(repeat(repeat('1234567890', 6) + '1234', 16 * 65));
};

function testJavascriptAttr() {
  com.google.gxp.compiler.functests.JavascriptAttrGxp.write(OUT, GXP_CONTEXT, 'foo < > \' "');

  assertOutputEquals(
      '<div onclick="\'alert(\\047foo \\074 \\076 \\047 \\042\\047);\'">1</div>\n'
      + '<div onclick="alert(\'foo \\074 \\076 \\047 \\042\');">2</div>\n'
      + '<div onclick="alert(\'foo \\074 \\076 \\047 \\042\');">3</div>\n'
      + '<div onclick="alert(\'foo \\074 \\076 \\047 \\042\');">4</div>\n'
      + '<div onclick="alert(\'foo \\074 \\076 \\047 \\042\');">5</div>');
};
