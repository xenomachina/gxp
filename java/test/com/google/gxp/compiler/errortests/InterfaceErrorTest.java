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

import com.google.gxp.compiler.alerts.common.InvalidAttributeValueError;
import com.google.gxp.compiler.alerts.common.NothingToCompileError;
import com.google.gxp.compiler.reparent.UnknownContentTypeError;
import com.google.gxp.compiler.validate.DuplicateParameterNameError;
import com.google.gxp.compiler.validate.InterfaceParamHasConstructorError;
import com.google.gxp.compiler.validate.InterfaceParamHasDefaultValueError;

/**
 * Collection of tests of proper error reporting by the GXP compiler relating
 * to {@code <gxp:interface>} and its special children ({@code <gxp:param>},
 * {@code <gxp:throws>}, etc.)
 */
public class InterfaceErrorTest extends BaseTestCase {
  public void testInterface_unknownContentType() throws Exception {
    compileNoHeader(
        "<!DOCTYPE gxp:interface SYSTEM \"http://www.corp.google.com/eng/projects/ui/xhtml.ent\">",
        "",
        "<gxp:interface name='com.google.gxp.compiler.errortests."
        + "TestInterface_unknownContentType'",
        "               xmlns:gxp='http://google.com/2001/gxp'",
        "               content-type='text/bad'>",
        "</gxp:interface>");
    assertAlert(new UnknownContentTypeError(pos(5,40), "text/bad"));
    assertAlert(new NothingToCompileError(pos()));
    assertNoUnexpectedAlerts();
  }

  public void testParam_badHasConstructor() throws Exception {
    compileInterface("<gxp:param name='foo' type='int' has-constructor='yes' />");
    assertAlert(new InvalidAttributeValueError(pos(2,1), "'has-constructor' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testParam_badHasDefault() throws Exception {
    compileInterface("<gxp:param name='foo' type='int' has-default='yes' />");
    assertAlert(new InvalidAttributeValueError(pos(2,1), "'has-default' attribute"));
    assertNoUnexpectedAlerts();
  }

  public void testParam_duplicateName() throws Exception {
    // standard param
    compileInterface("<gxp:param name='foo' type='String' />",
                     "<gxp:param name='foo' type='String' />");
    assertAlert(new DuplicateParameterNameError(pos(3,1), "foo"));
    assertNoUnexpectedAlerts();
  }

  public void testParam_hasConstructor() throws Exception {
    compileInterface("<gxp:param name='foo' type='int' constructor='foo' />");
    assertAlert(new InterfaceParamHasConstructorError(pos(2,1)));
    assertNoUnexpectedAlerts();
  }

  public void testParam_hasDefaultValue() throws Exception {
    compileInterface("<gxp:param name='foo' type='int' default='12' />");
    assertAlert(new InterfaceParamHasDefaultValueError(pos(2,1)));
    assertNoUnexpectedAlerts();
  }
}
