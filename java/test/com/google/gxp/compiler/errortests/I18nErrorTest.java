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
import com.google.gxp.compiler.alerts.common.InvalidMessageError;
import com.google.gxp.compiler.codegen.CodeGeneratorFactory;
import com.google.gxp.compiler.codegen.DefaultCodeGeneratorFactory;
import com.google.gxp.compiler.java.NoMessageSourceError;
import com.google.gxp.compiler.msgextract.TooManyDynamicPlaceholdersError;
import com.google.gxp.compiler.phpivot.EmptyPlaceholderError;
import com.google.gxp.compiler.phpivot.EphMissingPhError;
import com.google.gxp.compiler.phpivot.PhMissingEphError;
import com.google.gxp.compiler.phpivot.PlaceholderRequiresExampleError;

/**
 * Tests of proper error reporting by the GXP compiler relating to i18n
 * features.
 */
public class I18nErrorTest extends BaseTestCase {
  private final DefaultCodeGeneratorFactory codeGeneratorFactory;

  public I18nErrorTest() {
    codeGeneratorFactory = new DefaultCodeGeneratorFactory();
    codeGeneratorFactory.setRuntimeMessageSource("com.google.foo.bar");
  }

  @Override
  protected CodeGeneratorFactory getCodeGeneratorFactory() {
    return codeGeneratorFactory;
  }

  public void testMsg_dynamicContentOutsidePlaceholder() throws Exception {
    compile("<gxp:msg><gxp:eval expr='1'/></gxp:msg>");
    assertAlert(new BadNodePlacementError(pos(2, 10), "<gxp:eval>",
                                          "inside <gxp:msg>"));
    assertNoUnexpectedAlerts();
  }

  public void testMsg_insideMsg() throws Exception {
    compile("<gxp:msg>foo <b><gxp:msg>bar</gxp:msg></b> baz</gxp:msg>");
    assertAlert(new BadNodePlacementError(pos(2, 17), "<gxp:msg>",
                                          "inside <gxp:msg>"));
    assertNoUnexpectedAlerts();
  }

  public void testMsg_insideNoMsg() throws Exception {
    compile("<gxp:nomsg>foo <b><gxp:msg>bar</gxp:msg></b> baz</gxp:nomsg>");
    assertAlert(new BadNodePlacementError(pos(2, 19), "<gxp:msg>",
                                          "inside <gxp:nomsg>"));
    assertNoUnexpectedAlerts();
  }

  public void testMsg_noMessageSource() throws Exception {
    codeGeneratorFactory.setRuntimeMessageSource(null);

    // No messages, no problem.
    compile("hello, world!");
    assertNoUnexpectedAlerts();

    // Message without message source. Problem.
    compile("<gxp:msg>hello, world!</gxp:msg>");
    assertAlert(new NoMessageSourceError(pos(2, 1), "<gxp:msg>"));
    assertNoUnexpectedAlerts();
  }

  public void testMsg_messageBundleError() throws Exception {
    // identical messages are okay
    compile("<gxp:msg>",
            "foo <gxp:ph name='x'/>bar<gxp:eph/> baz",
            "</gxp:msg>",
            "<gxp:msg>",
            "foo <gxp:ph name='x'/>bar<gxp:eph/> baz",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();

    // different messages with the same ID are not
    compile("<gxp:msg>",
            "foo <gxp:ph name='x'/>bar<gxp:eph/> baz",
            "</gxp:msg>",
            "<gxp:msg>",
            "foo <gxp:ph name='x'/>quux<gxp:eph/> baz",
            "</gxp:msg>");
    assertAlert(new InvalidMessageError(
        pos(5, 1), "Cannot merge messages with different content."));
    assertNoUnexpectedAlerts();
  }

  public void testMsg_tooManyDynamicPlaceholders() throws Exception {
    // straightforward case: one dynamic parameter per placeholder
    compile("<gxp:msg>",
            "<gxp:ph name='ph1' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph2' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph3' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph4' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph5' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph6' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph7' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph8' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph9' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph10' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new TooManyDynamicPlaceholdersError(pos(12, 34)));
    assertNoUnexpectedAlerts();

    // multiple dynamic parameters per placeholder
    compile("<gxp:msg>",
            "<gxp:ph name='ph1' example='1'/><gxp:eval expr='x'/>"
            + "<gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph2' example='1'/><gxp:eval expr='x'/>"
            + "<gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph3' example='1'/><gxp:eval expr='x'/>"
            + "<gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph4' example='1'/><gxp:eval expr='x'/>"
            + "<gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph5' example='1'/><gxp:eval expr='x'/>"
            + "<gxp:eval expr='x'/><gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new TooManyDynamicPlaceholdersError(pos(7, 53)));
    assertNoUnexpectedAlerts();

    // more than 9 placeholders, but not too many dynamic
    compile("<gxp:msg>",
            "<gxp:ph name='ph1' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph2'/>static<gxp:eph/>",
            "<gxp:ph name='ph3' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph4'/>static<gxp:eph/>",
            "<gxp:ph name='ph5' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph6'/>static<gxp:eph/>",
            "<gxp:ph name='ph7' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph8' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph9' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='ph10' example='1'/><gxp:eval expr='x'/><gxp:eph/>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testNoMsg_insideMsg() throws Exception {
    compile("<gxp:msg><gxp:nomsg>foo</gxp:nomsg></gxp:msg>");
    assertAlert(new BadNodePlacementError(pos(2, 10), "<gxp:nomsg>",
                                          "inside <gxp:msg>"));
    assertNoUnexpectedAlerts();
  }

  public void testNoMsg_insideNoMsg() throws Exception {
    compile("<gxp:nomsg><gxp:nomsg>foo</gxp:nomsg></gxp:nomsg>");
    assertAlert(new BadNodePlacementError(pos(2, 12), "<gxp:nomsg>",
                                          "inside <gxp:nomsg>"));
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholder_collision() throws Exception {
    // static content differs
    compile("<gxp:msg>",
            "<gxp:ph name='x'/>foo<gxp:eph/>",
            "<gxp:ph name='x'/>bar<gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new InvalidMessageError(
        pos(4, 1), "Conflicting declarations of X within message"));
    assertNoUnexpectedAlerts();

    // static the same
    compile("<gxp:msg>",
            "<gxp:ph name='x'/>foo<gxp:eph/>",
            "<gxp:ph name='x'/>foo<gxp:eph/>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();

    // dynamic content differs (even if they look the same, NativeExpressions
    // potentially eveluate to different values, so are considered "different")
    compile("<gxp:msg>",
            "<gxp:ph name='x' example='5'/><gxp:eval expr='x'/><gxp:eph/>",
            "<gxp:ph name='x' example='5'/><gxp:eval expr='x'/><gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new InvalidMessageError(
        pos(4, 1), "Conflicting declarations of X within message"));
    assertNoUnexpectedAlerts();

    // dynamic content the same (<br/> is dynamic because of XML mode, but is
    // guaranteed to be consistent within a given scope)
    compile("<gxp:msg>",
            "<gxp:ph name='x' example='5'/><br/><gxp:eph/>",
            "<gxp:ph name='x' example='5'/><br/><gxp:eph/>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholder_empty() throws Exception {
    compile("<gxp:msg><gxp:ph name='foo'/><gxp:eph/></gxp:msg>");
    assertAlert(new EmptyPlaceholderError(pos(2, 10)));
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholder_insideNoMsg() throws Exception {
    compile("<gxp:nomsg>",
            "<gxp:ph name='foo'/>foo<gxp:eph/>",
            "</gxp:nomsg>");
    assertAlert(new BadNodePlacementError(pos(3, 1), "<gxp:ph>",
                                          "inside <gxp:nomsg>"));
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholder_insideMsgInsideNoMsg() throws Exception {
    compile("<gxp:nomsg>",
            "foo <b>",
            "<gxp:msg>",
            "bar <gxp:ph name='verb'/>baz<gxp:eph/> quux",
            "</gxp:msg>",
            "</b> zarf",
            "</gxp:nomsg>");
    assertAlert(new BadNodePlacementError(pos(4, 1), "<gxp:msg>",
                                          "inside <gxp:nomsg>"));
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholder_outsideMsg() throws Exception {
    compile("<gxp:ph name='foo'/>foo<gxp:eph/>");
    assertAlert(new BadNodePlacementError(pos(2, 1), "<gxp:ph>", "here"));
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholderEnd_inDynamicContent() throws Exception {
    compile("<gxp:msg>",
            "foo",
            "<gxp:ph name='one'/>",
            "bar",
            "<gxp:if cond='moonIsFull'>",
            "baz",
            "<gxp:eph/>",
            "quux",
            "</gxp:if>",
            "zarf",
            "</gxp:msg>");
    assertAlert(new PhMissingEphError(pos(4, 1), "<gxp:ph>"));
    assertAlert(new EphMissingPhError(pos(8, 1), "<gxp:eph>"));
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholderEnd_missingPlaceholderStart() throws Exception {
    // with preceeding text
    compile("<gxp:msg>foo <gxp:eph/></gxp:msg>");
    assertAlert(new EphMissingPhError(pos(2, 14), "<gxp:eph>"));
    assertNoUnexpectedAlerts();

    // without preceeding text
    compile("<gxp:msg><gxp:eph/></gxp:msg>");
    assertAlert(new EphMissingPhError(pos(2, 10), "<gxp:eph>"));
    assertNoUnexpectedAlerts();

    // with preceeding ph/eph pair
    compile("<gxp:msg>",
            "<gxp:ph name='foo' example='bar'/><gxp:eval expr='1'/><gxp:eph/>",
            "<gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new EphMissingPhError(pos(4, 1), "<gxp:eph>"));
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholderEnd_nonEmpty() throws Exception {
    compile("<gxp:msg><gxp:ph name='foo'/>x<gxp:eph>text</gxp:eph></gxp:msg>");
    assertAlert(new BadNodePlacementError(pos(2, 40), "text",
                                          "inside <gxp:eph>"));
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholderStart_exampleEmpty() throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph example='' name='foo'/>",
            "<gxp:eval expr='x'/>",
            "<gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new InvalidAttributeValueError(pos(3, 1),
                                               "'example' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholderStart_exampleMissing() throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='foo'/>",
            "<gxp:eval expr='x'/>",
            "<gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new PlaceholderRequiresExampleError(pos(3, 1), "<gxp:ph>"));
    assertNoUnexpectedAlerts();

    compile("<gxp:msg>",
            "<gxp:ph name='foo'/>",
            "<gxp:if cond='x'>yes<gxp:else/>no</gxp:if>",
            "<gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new PlaceholderRequiresExampleError(pos(3, 1), "<gxp:ph>"));
    assertNoUnexpectedAlerts();

    // Static content doesn't need an example.
    compile("<gxp:msg>",
            "<gxp:ph name='foo'/>static<gxp:eph/>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();

    // Tags that differ in XML mode aren't static, but we can style synthesize
    // examples for them.
    compile("<gxp:msg>",
            "<gxp:ph name='foo'/><br/><gxp:eph/>",
            "</gxp:msg>");
    assertNoUnexpectedAlerts();

    // no:msg elements use their content as an example.
    compile("<gxp:msg>"
            + "<gxp:ph name='p'/>"
            + "<gxp:nomsg>foo</gxp:nomsg>"
            + "<gxp:eph/>"
            + "</gxp:msg>");
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholderStart_invalidName() throws Exception {
    compile("<gxp:msg><gxp:ph name='ham on rye'/>x<gxp:eph/></gxp:msg>");
    assertAlert(new InvalidMessageError(
        pos(2, 10), "Invalid placeholder specification: only caps, digits,"
                    + " and underscores allowed in presentation"));
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholderStart_missingEnd() throws Exception {
    compile("<gxp:msg><gxp:ph name='foo'/>x</gxp:msg>");
    assertAlert(new PhMissingEphError(pos(2, 10), "<gxp:ph>"));
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholderStart_nonEmpty() throws Exception {
    compile("<gxp:msg>",
            "  <gxp:ph name='foo'>text</gxp:ph>x<gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new BadNodePlacementError(pos(3, 22), "text",
                                          "inside <gxp:ph>"));
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholderStart_outsideMsg() throws Exception {
    compile("<gxp:ph name='foo' />");
    assertAlert(new BadNodePlacementError(pos(2, 1), "<gxp:ph>", "here"));
    assertNoUnexpectedAlerts();
  }

  public void testPlaceholder_transconsoleError() throws Exception {
    compile("<gxp:msg>",
            "<gxp:ph name='cola'/>coke<gxp:eph/>",
            "<gxp:ph name='cola'/>pepsi<gxp:eph/>",
            "</gxp:msg>");
    assertAlert(new InvalidMessageError(
        pos(4, 1), "Conflicting declarations of COLA within message"));
    assertNoUnexpectedAlerts();
  }
}