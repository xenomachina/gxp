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

import com.google.gxp.compiler.alerts.common.MissingAttributeError;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.reparent.MoreThanOneConstructorError;
import com.google.gxp.compiler.validate.DuplicateParameterNameError;

/**
 * Error tests related to instantiable GXPs.
 */
public class InstantiableErrorTest extends BaseTestCase {
  public void testCallConstructor_missingAttribute() throws Exception {
    FileRef callee = createFile("callee",
                                "<gxp:constructor>",
                                "  <gxp:param name='s1' type='String' />",
                                "</gxp:constructor>",
                                "<gxp:param name='s2' type='String' />");
    FileRef caller = createFile("caller", "<call:callee/>");
    compileFiles(callee, caller);

    assertAlert(new MissingAttributeError(pos(2,1), "<call:callee>", "s1"));
    assertAlert(new MissingAttributeError(pos(2,1), "<call:callee>", "s2"));
    assertNoUnexpectedAlerts();
  }

  public void testConstructor_doubleConstructor() throws Exception {
    compile("<gxp:constructor></gxp:constructor>",
            "<gxp:constructor></gxp:constructor>");
    assertAlert(new MoreThanOneConstructorError(pos(3,1), "<gxp:constructor>"));
    assertNoUnexpectedAlerts();
  }

  public void testConstructor_duplicateParamName() throws Exception {
    compile("<gxp:constructor>",
            "  <gxp:param name='foo' type='String' />",
            "</gxp:constructor>",
            "",
            "<gxp:param name='foo' type='String' />");
    assertAlert(new DuplicateParameterNameError(pos(6,1), "foo"));
    assertNoUnexpectedAlerts();
  }
}
