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

import com.google.gxp.compiler.alerts.common.BadNodePlacementError;
import com.google.gxp.compiler.alerts.common.InvalidAttributeValueError;
import com.google.gxp.compiler.alerts.common.MissingAttributeError;
import com.google.gxp.compiler.alerts.common.MultiValueAttributeError;
import com.google.gxp.compiler.alerts.common.RequiredAttributeHasCondError;
import com.google.gxp.compiler.alerts.common.UnknownAttributeError;
import com.google.gxp.compiler.reparent.InvalidDoctypeError;

/**
 * Tests of proper error reporting by the GXP compiler relating to {@code
 * OutputElement}s.
 */
public class OutputElementErrorTest extends BaseTestCase {

  public void testOutputElement_attrElementContainsTags() throws Exception {
    compile("<a><gxp:attr name='href'><b>foo</b></gxp:attr></a>");
    // TODO(laurence): attrElement.error("Cannot contain HTML tags when used inside HTML element")
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_attrHasMultipleValues() throws Exception {
    // test regular duplicate
    compile("<div id='foo' id='bar'></div>");
    assertParseAlert(pos(2,1), "Attribute \"id\" was already specified for element \"div\".");
    assertNoUnexpectedAlerts();

    // test duplicate from gxp:attr
    compile("<div id='foo'>",
            "  <gxp:attr name='id'>",
            "    foo",
            "  </gxp:attr>",
            "</div>");
    assertAlert(new MultiValueAttributeError(pos(3,3), "<div>", "'id' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_invalidAttrValue() throws Exception {
    compile("<div align='foo'></div>");
    assertAlert(new InvalidAttributeValueError(pos(2,1), "'align' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_invalidExprAttributes() throws Exception {
    assertIllegalExpressionDetected("<img expr:alt='", "'/>");
  }

  public void testOutputElement_invalidGxpDoctype() throws Exception {
    compile("<html gxp:doctype='foo'></html>");
    assertAlert(new InvalidDoctypeError(pos(2,1), "<html>", "foo"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_nonEmptyNoEndTagElement() throws Exception {
    compile("<br>foo</br>");
    assertAlert(new BadNodePlacementError(pos(2,5), "text", "inside <br>"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_requiredAttrMissing() throws Exception {
    compile("<img src='foo.jpg'/>");
    assertAlert(new MissingAttributeError(pos(2,1), "<img>", "alt"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_requiredAttrHasConditional() throws Exception {
    compile("<img src='foo.jpg'>",
            "  <gxp:attr name='alt' cond='false'>",
            "    foo",
            "  </gxp:attr>",
            "</img>");
    assertAlert(new RequiredAttributeHasCondError(pos(2,1), "<img>", "alt"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_tagsInChildlessElement() throws Exception {
    compile("<br>foo!</br>");
    assertAlert(new BadNodePlacementError(pos(2,5), "text", "inside <br>"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_unknownAttr() throws Exception {
    // no namespace
    compile("<div foo='bar'></div>");
    assertAlert(new UnknownAttributeError("<div>", pos(2,1), "'foo' attribute"));
    assertNoUnexpectedAlerts();

    // gxp: namespace
    compile("<div gxp:foo='bar'></div>");
    assertAlert(new UnknownAttributeError("<div>", pos(2,1), "'gxp:foo' attribute"));
    assertNoUnexpectedAlerts();

    // expr: namespace
    compile("<div expr:foo='bar'></div>");
    assertAlert(new UnknownAttributeError("<div>", pos(2,1), "'foo' attribute"));
    assertNoUnexpectedAlerts();

    // <gxp:attr>
    compile("<div>",
            "  <gxp:attr name='foo'>",
            "    bar",
            "  </gxp:attr>",
            "</div>");
    assertAlert(new UnknownAttributeError("<div>", pos(3,3), "'foo' attribute"));
    assertNoUnexpectedAlerts();
  }
}
