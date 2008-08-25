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

package com.google.gxp.compiler.functests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gxp.css.CssClosures;
import com.google.gxp.testing.BaseFunctionalTestCase;

import java.io.*;
import java.util.*;

import static com.google.gxp.testing.MoreAsserts.*;

/**
 * Functional tests for {@link com.google.gxp.compiler.java.JavaCodeGenerator
 * JavaCodeGenerator}.
 */
public class JavaCodeTest extends BaseFunctionalTestCase {
  public void testHello() throws Exception {
    HelloGxp.write(out, gxpContext);
    assertOutputEquals("hello, world!");
  }

  private static final String HTML_STRICT_DOCTYPE =
      "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\""
      + " \"http://www.w3.org/TR/html4/strict.dtd\">";

  private static final String XHTML_STRICT_DOCTYPE =
      "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
      + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";

  private static final String XHTML_MOBILE_DOCTYPE =
      "<!DOCTYPE html PUBLIC \"-//WAPFORUM//DTD XHTML Mobile 1.0//EN\""
      + " \"http://www.wapforum.org/DTD/xhtml-mobile10.dtd\">";

  public void testDoctype() throws Exception {
    DoctypeGxp.write(out, gxpContext);
    assertOutputEquals(HTML_STRICT_DOCTYPE + "<html></html>");
  }

  public void testSomeTags() throws Exception {
    SomeTagsGxp.write(out, gxpContext);
    assertOutputEquals("foo <b>bar</b> <img src=\"baz.gif\" alt=\"baz\">");
  }

  public void testIntEval() throws Exception {
    IntEvalGxp.write(out, gxpContext, 5);
    assertOutputEquals("returned 5 results");

    IntEvalGxp.write(out, gxpContext, 12345678);
    assertOutputEquals("returned 12345678 results");

    IntEvalGxp.write(out, gxpContext, -8);
    assertOutputEquals("returned -8 results");

    IntEvalGxp.write(out, gxpContext, 0);
    assertOutputEquals("returned 0 results");
  }

  public void testIntExpr() throws Exception {
    IntExprGxp.write(out, gxpContext, 5);
    assertOutputEquals("click <a href=\"5\">here</a>.");

    IntExprGxp.write(out, gxpContext, 12345678);
    assertOutputEquals("click <a href=\"12345678\">here</a>.");

    IntExprGxp.write(out, gxpContext, -8);
    assertOutputEquals("click <a href=\"-8\">here</a>.");

    IntExprGxp.write(out, gxpContext, 0);
    assertOutputEquals("click <a href=\"0\">here</a>.");
  }

  public void testStringEval() throws Exception {
    StringEvalGxp.write(out, gxpContext, "abc123");
    assertOutputEquals("foo abc123 bar");

    StringEvalGxp.write(out, gxpContext, "  abc123  ");
    assertOutputEquals("foo   abc123   bar");

    StringEvalGxp.write(out, gxpContext, "");
    assertOutputEquals("foo  bar");

    String s = "xyz<>&\u4321\u00a0\"'123";
    StringEvalGxp.write(out, gxpContext, s);
    assertOutputEquals("foo xyz&lt;&gt;&amp;&#17185;&nbsp;&quot;&#39;123 bar");

    try {
      StringEvalGxp.write(out, gxpContext, null);
      fail("should throw NPE");
    } catch (NullPointerException e) {
      // good
    }
  }

  public void testStringExpr() throws Exception {
    StringExprGxp.write(out, gxpContext, "abc123");
    assertOutputEquals("click <a href=\"abc123\">here</a>.");

    StringExprGxp.write(out, gxpContext, "  abc123  ");
    assertOutputEquals("click <a href=\"  abc123  \">here</a>.");

    StringExprGxp.write(out, gxpContext, "");
    assertOutputEquals("click <a href=\"\">here</a>.");

    String s = "xyz<>&\u4321\u00a0\"'123";
    StringExprGxp.write(out, gxpContext, s);
    assertOutputEquals("click <a href=\"xyz&lt;&gt;&amp;&#17185;&nbsp;&quot;&#39;123\">here</a>.");

    try {
      StringExprGxp.write(out, gxpContext, null);
      fail("should throw NPE");
    } catch (NullPointerException e) {
      // good
    }
  }

  public void testSpacePreservation() throws Exception {
    SpacePreservationGxp.write(out, gxpContext);
    assertOutputEquals("foo\n"
                       + "bar\n"
                       + "baz\n"
                       + "<script type=\"text/javascript\">\n"
                       + "  alert('hello');\n"
                       + "</script>\n"
                       + "<style type=\"text/css\">\n"
                       + "  .header {\n"
                       + "    color: red;\n"
                       + "  }\n"
                       + "</style>\n"
                       + "quux\n"
                       + "zarf\n"
                       + "zot!");
  }

  public void testEvalInScript() throws Exception {
    EvalInScriptGxp.write(out, gxpContext);
    assertOutputEquals("<script type=\"text/javascript\">\n"
                       + "  var x = 1234567.0;\n"
                       + "  var y = \"don\\x27t worry, be \\x3cem\\x3ehappy\\x3c/em\\x3e\";\n"
                       + "</script>");
  }

  public void testJsMsgEscape() throws Exception {
    JsMsgEscapeGxp.write(out, gxpContext);
    assertOutputEquals("<script type=\"text/javascript\">\n"
                       + "  var foo = \"foo\";\n"
                       + "  var bar = \"bar\";\n"
                       + "</script>\n"
                       + "<div onclick=\"alert(&quot;foo \\x22 \\x27&quot;);\"></div>");
  }

  public void testIfBasic() throws Exception {
    IfBasicGxp.write(out, gxpContext, 1);
    assertOutputEquals("That number is less than 5.");

    IfBasicGxp.write(out, gxpContext, 7);
    assertOutputEquals("");
  }

  public void testIfElse() throws Exception {
    IfElseGxp.write(out, gxpContext, 1);
    assertOutputEquals("That number is\nless than\n5.");

    IfElseGxp.write(out, gxpContext, 7);
    assertOutputEquals("That number is\ngreater than or equal to\n5.");
  }

  public void testIfElseIf() throws Exception {
    IfElseIfGxp.write(out, gxpContext, 1);
    assertOutputEquals("That number is\nless than\n5.");

    IfElseIfGxp.write(out, gxpContext, 5);
    assertOutputEquals("That number is\nequal to\n5.");

    IfElseIfGxp.write(out, gxpContext, 7);
    assertOutputEquals("That number is\ngreater than or equal to\n5.");
  }

  public void testCond() throws Exception {
    CondGxp.write(out, gxpContext, 1);
    assertOutputEquals("That number is\nless than\n5.");

    CondGxp.write(out, gxpContext, 5);
    assertOutputEquals("That number is\nequal to\n5.");

    CondGxp.write(out, gxpContext, 7);
    assertOutputEquals("That number is\ngreater than or equal to\n5.");
  }

  public void testAbbr() throws Exception {
    AbbrGxp.write(out, gxpContext, 1);
    assertOutputEquals("a1\nb1\nc&lt;*&gt;");

    AbbrGxp.write(out, gxpContext, 2);
    assertOutputEquals("a2\nb4\nc&lt;* *&gt;");

    AbbrGxp.write(out, gxpContext, 3);
    assertOutputEquals("a3\nb9\nc&lt;* * *&gt;");
  }

  public void testAbbrCall() throws Exception {
    AbbrCallGxp.write(out, gxpContext);
    assertOutputEquals("foo hello bar");
  }

  public void testLoop() throws Exception {
    LoopGxp.write(out, gxpContext, 0);
    assertOutputEquals("<b></b>");

    LoopGxp.write(out, gxpContext, 1);
    assertOutputEquals("<b>!</b>");

    LoopGxp.write(out, gxpContext, 5);
    assertOutputEquals("<b>! ! ! ! !</b>");

    LoopGxp.write(out, gxpContext, 7);
    assertOutputEquals("<b>! ! ! ! ! ! !</b>");
  }

  public void testLoopDelimiter() throws Exception {
    LoopDelimiterGxp.write(out, gxpContext, 0);
    assertOutputEquals("<b></b>\n<b></b>\n<b></b>\n<b></b>\n<b></b>\n<b></b>");

    LoopDelimiterGxp.write(out, gxpContext, 1);
    assertOutputEquals("<b>!</b>\n<b>!</b>\n<b>!</b>\n<b>!</b>\n<b>!</b>\n<b>!</b>");

    LoopDelimiterGxp.write(out, gxpContext, 3);
    assertOutputEquals("<b>!!!</b>\n<b>!&amp;!&amp;!</b>\n<b>!,!,!</b>\n<b>!,!,!</b>"
                       + "\n<b>!,!,!</b>\n<b>!,!,!</b>");
  }

  public void testIterableLoop() throws Exception {
    IterableLoopGxp.write(out, gxpContext, Collections.<String>emptyList());
    assertOutputEquals("<ul></ul>");

    IterableLoopGxp.write(out, gxpContext,
                          Collections.singletonList("McCloud"));
    assertOutputEquals("<ul><li>McCloud</li></ul>");

    IterableLoopGxp.write(out, gxpContext,
                          ImmutableList.of("one", "two", "three"));
    assertOutputEquals("<ul><li>one</li> <li>two</li> <li>three</li></ul>");
  }

  public void testContentParamCalls() throws Exception {
    ContentParamCallerGxp.write(out, gxpContext, 0,
                                "hello, world!");
    assertOutputEquals("<div id=\"html\">"
                       + "<b id=\"0\">hello, world!</b>"
                       + "</div>\n"
                       + "<div id=\"html-oc\">"
                       + "<b id=\"0\">1 &lt; 2 &amp; &quot;O&#39;Foozle&quot;</b>"
                       + "</div>");

    ContentParamCallerGxp.write(out, gxpContext, 12345,
                               "a < b > c \" d & e ' f \\ g");
    assertOutputEquals("<div id=\"html\">"
                       + "<b id=\"12345\">a &lt; b &gt; c &quot; d &amp; e &#39; f \\ g</b>"
                       + "</div>\n"
                       + "<div id=\"html-oc\">"
                       + "<b id=\"12345\">1 &lt; 2 &amp; &quot;O&#39;Foozle&quot;</b>"
                       + "</div>");
  }

  public void testCssContentParam() throws Exception {
    CssContentParamGxp.write(out, gxpContext, CssClosures.fromCss("foo"));
    assertOutputEquals("foo");
  }

  public void testContentType() throws Exception {
    ContentTypeGxp.write(out, gxpContext, ContentTypeGxp.getDefaultP());
    assertOutputEquals("param\nabbr");
  }

  public void testContainerCall() throws Exception {
    ContainerCallerGxp.write(out, gxpContext, 0);
    assertOutputEquals("<i><b id=\"0\">Foo, <b>bar</b> &amp; baz.</b></i>");

    ContainerCallerGxp.write(out, gxpContext, 12345);
    assertOutputEquals("<i><b id=\"12345\">Foo, <b>bar</b> &amp; baz.</b></i>");
  }

  public void testEvalInContainerCall() throws Exception {
    EvalInContainerCallerGxp.write(out, gxpContext, "snoo snah snuh");
    String expected = "<i><b id=\"42\">snoo snah snuh</b></i>";
    assertOutputEquals(expected + "\n" + expected);

    EvalInContainerCallerGxp.write(out, gxpContext,
                                   "he said \"1 < 2 & 5 > 3\"");
    expected = "<i><b id=\"42\">"
        + "he said &quot;1 &lt; 2 &amp; 5 &gt; 3&quot;"
        + "</b></i>";
    assertOutputEquals(expected + "\n" + expected);
  }

  public void testEvalInNonContentAttrElement() throws Exception {
    EvalInNonContentAttrElementGxp.write(out, gxpContext,
                                         "snoo snah snuh");
    assertOutputEquals("<i>foo SNOO SNAH SNUH bar</i>");

    EvalInNonContentAttrElementGxp.write(out, gxpContext,
                                         "he said \"1 < 2 & 5 > 3\"");
    assertOutputEquals(
        "<i>foo HE SAID &quot;1 &lt; 2 &amp; 5 &gt; 3&quot; bar</i>");
  }

  public void testBasicAttr() throws Exception {
    BasicAttrGxp.write(out, gxpContext, 0);
    assertOutputEquals("<img src=\"0lives.png\" alt=\"You have 0 lives left.\">");

    BasicAttrGxp.write(out, gxpContext, 5);
    assertOutputEquals("<img src=\"5lives.png\" alt=\"You have 5 lives left.\">");
  }

  public void testCondAttr() throws Exception {
    CondAttrGxp.write(out, gxpContext, 1);
    assertOutputEquals("<div class=\"1-lives\">You have 1 lives left.</div>");

    CondAttrGxp.write(out, gxpContext, 0);
    assertOutputEquals("<div class=\"0-lives\">You have 0 lives left.</div>");

    CondAttrGxp.write(out, gxpContext, -1);
    assertOutputEquals("<div>You have -1 lives left.</div>");
  }

  // Note: it's important to be specific about the exceptions declared here to
  // ensure that the generated code is declaring that it throws only these, and
  // no others,
  public void testThrows() throws IOException, ArbitraryCheckedException {
    ThrowsGxp.write(out, gxpContext, false);
    assertOutputEquals("No problem.");

    try {
      ThrowsGxp.write(out, gxpContext, true);
      fail("expected ArbitraryCheckedException to be thrown.");
    } catch (ArbitraryCheckedException exc) {
      assertEquals("A problem!", exc.getMessage());
    }
  }

  // Again, it's important to be specific about the exceptions declared here.
  public void testThrowsInAnonymousClosure()
      throws IOException, ArbitraryCheckedException {
    ThrowsInAnonymousClosureGxp.write(out, gxpContext, false);
    assertOutputEquals("<div id=\"outer\"><b id=\"5\"><div id=\"inner\">"
                       + "No problem.</div></b></div>");

    try {
      ThrowsInAnonymousClosureGxp.write(out, gxpContext, true);
      fail("expected ArbitraryCheckedException to be thrown.");
    } catch (ArbitraryCheckedException exc) {
      assertEquals("A problem!", exc.getMessage());
    }
  }

  public static String throwException(String msg)
      throws ArbitraryCheckedException {
    throw new ArbitraryCheckedException(msg);
  }

  /**
   * An arbitrary checked exception, used for testing checked exceptions thrown
   * from withing GXPs.
   */
  public static class ArbitraryCheckedException extends Exception {
    private static final long serialVersionUID = 0xb100d;

    private ArbitraryCheckedException(String msg) {
      super(msg);
    }
  }

  public void testTypeParam() throws Exception {
    TypeParamGxp.write(out, gxpContext, 3, ImmutableList.of(1, 2, 3, 4, 5));
    assertOutputEquals("1 2 <b>3</b> 4 5");

    TypeParamGxp.write(out, gxpContext, "bar",
                       ImmutableList.of("foo", "bar", "baz", "quux", "zarf"));
    assertOutputEquals("foo <b>bar</b> baz quux zarf");
  }

  public void testTypeParamExtends() throws Exception {
    TypeParamExtendsGxp.write(out, gxpContext, 3, ImmutableList.of(1, 2, 3, 4, 5));
    assertOutputEquals("1 2 <b>3</b> 4 5");

    TypeParamExtendsGxp.write(out, gxpContext, 2.2,
                              ImmutableList.of(1.1, 2.2, 3.3, 4.4, 5.5));
    assertOutputEquals("1.1 <b>2.2</b> 3.3 4.4 5.5");
  }

  public void testPrettyGenerics() throws Exception {
    List<List<Integer>> list = Lists.newArrayList();
    list.add(ImmutableList.of(1, 2));
    list.add(ImmutableList.of(3, 4));
    PrettyGenericsGxp.write(out, gxpContext, list);
    assertOutputEquals("1 2 3 4");
  }

  public void testBooleanAttrib() throws Exception {
    BooleanAttribGxp.write(out, gxpContext);
    assertOutputEquals(
        "<input type=\"radio\" name=\"key1\" value=\"value1\" checked>\n"
        + "<input type=\"radio\" name=\"key1\" value=\"value2\">\n"
        + "<input type=\"checkbox\" name=\"key2\" value=\"value\" checked>\n"
        + "<input type=\"checkbox\" name=\"key2\" value=\"value\" checked>");
  }

  public void testSpaceControl() throws Exception {
    SpaceControlGxp.write(out, gxpContext);
    // The words on each line tell you the values of ispace and espace on the
    // "b" element, respectively. "inherit" means not set, ie: inherited from
    // the gxp:template, which has ispace and espace both set to remove in this
    // test.
    assertOutputEquals(
        "<b>1inherit1inherit</b>"
        + "<b>\n  2inherit2preserve\n</b>"
        + "<b>3inherit3remove</b>"
        + "<b>\n4inherit4collapse\n</b>"
        + "<b> 5inherit5normalize </b>"
        + "<b>6preserve\n  6inherit</b>"
        + "<b>\n  7preserve\n  7preserve\n</b>"
        + "<b>8preserve\n  8remove</b>"
        + "<b>\n9preserve\n  9collapse\n</b>"
        + "<b> 10preserve\n  10normalize </b>"
        + "<b>11remove11inherit</b>"
        + "<b>\n  12remove12preserve\n</b>"
        + "<b>13remove13remove</b>"
        + "<b>\n14remove14collapse\n</b>"
        + "<b> 15remove15normalize </b>"
        + "<b>16collapse\n16inherit</b>"
        + "<b>\n  17collapse\n17preserve\n</b>"
        + "<b>18collapse\n18remove</b>"
        + "<b>\n19collapse\n19collapse\n</b>"
        + "<b> 20collapse\n20normalize </b>"
        + "<b>21normalize 21inherit</b>"
        + "<b>\n  22normalize 22preserve\n</b>"
        + "<b>23normalize 23remove</b>"
        + "<b>\n24normalize 24collapse\n</b>"
        + "<b> 25normalize 25normalize </b>"

        // nested CollapseExpressions
        + "<b>\n26collapse\n26collapse\n</b>"
        + "<b><i> 27normalize 27normalize </i></b>"
        + "<b><i> 28collapse\n28normalize </i></b>"

        // calls (parameters and nested content)
        + "<b id=\"0\">29remove29remove</b>"
        + "<b id=\"0\"> 30normalize 30normalize </b>"
        + "<b> 31preserve\n  31normalize </b>"
        + "<b> 32preserve\n  32normalize </b>"

        // special elements
        + "33normalize 33remove" // gxp:msg
        + "<script type=\"text/javascript\">"
        + "\n  34preserve\n  34preserve\n</script>"
        + "<style type=\"text/css\">\n  35preserve\n  35preserve\n</style>"
        + "<pre>\n  36preserve\n  36preserve\n</pre>"
        + "<textarea rows=\"10\" cols=\"10\">"
        + "\n  37preserve\n  37preserve\n</textarea>"
        + "38normalize 38remove"
        + "<div class=\"39normalize 39remove\"></div>"
        + "<div class=\" 40remove40normalize \"></div>"
        );
  }

  public void testSgml() throws Exception {
    SgmlXmlDifferencesGxp.write(out, gxpContext);
    assertOutputEquals(
        HTML_STRICT_DOCTYPE
        + "<html>"
        + "<div id=\"snarf\">"
        + "<img alt=\"hello, world!\" ismap>\n"
        + "<br>"
        + "</div>\n"
        + "foo <br> bar <br> baz"
        + "</html>");
  }

  public void testXml() throws Exception {
    SgmlXmlDifferencesGxp.write(out, xmlGxpContext);
    assertOutputEquals(
        "<?xml version=\"1.0\" ?>\n"
        + XHTML_STRICT_DOCTYPE
        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
        + "<div id=\"snarf\">"
        + "<img alt=\"hello, world!\" ismap=\"ismap\" />\n"
        + "<br />"
        + "</div>\n"
        + "foo <br /> bar <br /> baz"
        + "</html>");
  }

  public void testXmlOnlyDoctypeInXml() throws Exception {
    XmlOnlyDoctypeGxp.write(out, xmlGxpContext);
    assertOutputEquals(
        "<?xml version=\"1.0\" ?>\n"
        + XHTML_MOBILE_DOCTYPE
        + "<html xmlns=\"http://www.w3.org/1999/xhtml\"></html>");
  }

  public void testXmlOnlyDoctypeInSgml() throws Exception {
    try {
      XmlOnlyDoctypeGxp.write(out, gxpContext);
      fail("Expected GxpRuntimeException to be thrown!");
    } catch (IllegalStateException exc) {
      assertEquals(
          "Doctype 'mobile' incompatible with non-XML syntax",
          exc.getMessage());
    }
  }

  private String repeat(String s, int count) {
    StringBuilder sb = new StringBuilder(s.length() * count);
    for (int i = 0; i < count; i++) {
      sb.append(s);
    }
    return sb.toString();
  }

  public void testReallyBig() throws Exception {
    ReallyBigGxp.write(out, gxpContext);
    assertOutputEquals(repeat(repeat("1234567890", 6) + "1234", 16 * 65));
  }

  public void testJavascriptAttr() throws Exception {
    JavascriptAttrGxp.write(out, gxpContext, "foo < > ' \"");

    assertOutputEquals(
        "<div onclick=\"&quot;alert(\\x27foo \\x3c \\x3e \\x27 \\x22\\x27);&quot;\">1</div>\n"
        + "<div onclick=\"alert(&quot;foo \\x3c \\x3e \\x27 \\x22&quot;);\">2</div>\n"
        + "<div onclick=\"alert(&quot;foo \\x3c \\x3e \\x27 \\x22&quot;);\">3</div>\n"
        + "<div onclick=\"alert(&quot;foo \\x3c \\x3e \\x27 \\x22&quot;);\">4</div>\n"
        + "<div onclick=\"alert(&quot;foo \\x3c \\x3e \\x27 \\x22&quot;);\">5</div>");
  }

  //////////////////////////////////////////////////////////////////////
  // Tests of Interface, Instance, and getArgList()
  //////////////////////////////////////////////////////////////////////

  public void testInterface() throws Exception {
    HelloGxp.Interface i = new HelloGxp.Instance();
    i.write(out, gxpContext);
    assertOutputEquals("hello, world!");

    i.getGxpClosure().write(out, gxpContext);
    assertOutputEquals("hello, world!");
  }

  public void testGetArgList() throws Exception {
    assertContentsInOrder("getArgList() failure:", StringEvalGxp.getArgList(),
                          "s");
  }
}
