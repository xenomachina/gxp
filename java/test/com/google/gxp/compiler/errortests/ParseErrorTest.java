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

package com.google.gxp.compiler.errortests;

import com.google.gxp.compiler.alerts.common.NothingToCompileError;
import com.google.gxp.compiler.parser.IgnoredXmlWarning;
import com.google.gxp.compiler.parser.NoNamespaceError;
import com.google.gxp.compiler.parser.UndefinedEntityError;
import com.google.gxp.compiler.parser.UnknownElementError;
import com.google.gxp.compiler.parser.UnknownNamespaceError;
import com.google.gxp.compiler.parser.UnsupportedExternalEntityError;

/**
 * Collection of tests of proper error reporting by the GXP compiler relating
 * to parsing and namespace resolution.
 */
public class ParseErrorTest extends BaseTestCase {
  public void testNamespace_namespacelessElement() throws Exception {
    compileNoHeader(
        "<!DOCTYPE gxp:template SYSTEM",
        "    \"http://www.corp.google.com/eng/projects/ui/xhtml.ent\">",
        "",
        "<gxp:template name='com.google.gxp.compiler.errortests."
        + "TestNamespace_namespacelessElement'",
        "              xmlns:gxp='http://google.com/2001/gxp'>",
        "<html/>",
        "</gxp:template>");
    assertAlert(new NoNamespaceError(pos(6,1)));
    assertNoUnexpectedAlerts();
  }

  public void testNamespace_processingInstruction() throws Exception {
    compileNoHeader(
        "<?snarf?>",
        "<gxp:template name='com.google.gxp.compiler.errortests."
        + "TestNamespace_processingInstruction'",
        "              xmlns='http://www.w3.org/1999/xhtml'",
        "              xmlns:gxp='http://google.com/2001/gxp'>",
        "<html/>",
        "</gxp:template>");
    assertAlert(new IgnoredXmlWarning(pos(1,1), "processing instruction"));
    assertNoUnexpectedAlerts();
  }

  public void testNamespace_undefinedAbbreviation() throws Exception {
    compile("<foo:bar></foo:bar>");
    assertParseAlert(pos(2,1), "The prefix \"foo\" for element \"foo:bar\" is"
                     + " not bound.");
    assertNoUnexpectedAlerts();
  }

  public void testNamespace_unknownAttrNamespace() throws Exception {
    compile("<div foo:class='theclass'/>");
    assertParseAlert(pos(2,1), "The prefix \"foo\" for attribute "
                     + "\"foo:class\" associated with an element type "
                     + "\"div\" is not bound.");
    assertNoUnexpectedAlerts();
  }

  public void testNamespace_unknownElement() throws Exception {
    compile("<gxp:foobar />");
    assertAlert(new UnknownElementError(pos(2,1), "<gxp:foobar>",
                                        "http://google.com/2001/gxp"));
    assertNoUnexpectedAlerts();
  }

  public void testNamespace_unknown() throws Exception {
    compileNoHeader(
        "<!DOCTYPE gxp:template SYSTEM",
        "    \"http://www.corp.google.com/eng/projects/ui/xhtml.ent\">",
        "",
        "<gxp:template name='com.google.gxp.compiler.errortests.gxp'",
        "              xmlns='http://www.w3.org/1999/xhtml'",
        "              xmlns:expr='http://google.com/2001/gxp/expressions'",
        "              xmlns:gxp='http://google.com/BAD_NAMESPACE'>",
        "",
        "<gxp:if cond='true'>",
        "  <gxp:msg>",
        "    foo",
        "    <br gxp:ph='br'/>",
        "    bar",
        "  </gxp:msg>",
        "</gxp:if>",
        "",
        "</gxp:template>");

    assertAlert(new NothingToCompileError(pos()));

    // TODO(laurence): make this kind of error less noisy
    String badNs = "http://google.com/BAD_NAMESPACE";
    // TODO(laurence): pos(7, 59) seems a bit off. Is it fixable?
    assertAlert(new UnknownNamespaceError(pos(7, 59), badNs));
    assertAlert(new UnknownNamespaceError(pos(9, 1), badNs));
    assertAlert(new UnknownNamespaceError(pos(10, 3), badNs));
    assertAlert(new UnknownNamespaceError(pos(12, 5), badNs));
    assertNoUnexpectedAlerts();
  }

  public void testParser_mismatchedStartEndTags() throws Exception {
    compile("<b></i>");
    assertParseAlert(pos(2,4), "The element type \"b\" must be terminated by"
                     + " the matching end-tag \"</b>\".");
    assertNoUnexpectedAlerts();
  }

  public void testParser_unsupportedExternalEntity() throws Exception {
    compileNoHeader(
        "<!DOCTYPE gxp:template SYSTEM \"http://www.corp.google.com/BAD\">");
    assertAlert(new UnsupportedExternalEntityError(
                    pos(1,1),
                    "PUBLIC <null> SYSTEM `http://www.corp.google.com/BAD`"));
    assertAlert(new NothingToCompileError(pos()));
    assertNoUnexpectedAlerts();
  }

  public void testParser_unterminatedElement() throws Exception {
    compile("<b>foo");
    assertParseAlert(pos(3,1), "The element type \"b\" must be terminated by"
                     + " the matching end-tag \"</b>\".");
    assertNoUnexpectedAlerts();
  }

  public void testParser_xmlParseError() throws Exception {
    compile("&idontexist;");
    assertAlert(new UndefinedEntityError(pos(2,1), "idontexist"));
    assertNoUnexpectedAlerts();
  }
}
