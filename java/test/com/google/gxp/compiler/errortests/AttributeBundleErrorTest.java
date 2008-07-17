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

import com.google.gxp.compiler.alerts.common.UnknownAttributeError;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.reparent.InvalidTypeError;
import com.google.gxp.compiler.reparent.RequiredAttrInBundleError;
import com.google.gxp.compiler.validate.DuplicateAttributeError;
import com.google.gxp.compiler.validate.DuplicateParameterNameError;
import com.google.gxp.compiler.validate.InvalidAttrBundleError;
import com.google.gxp.compiler.validate.MismatchedAttributeValidatorsError;

/**
 * Tests of proper error reporting by the GXP compiler relating to
 * attribute bundles.
 */
public class AttributeBundleErrorTest extends BaseTestCase {

  public void testBundle_conflictingAttrs() throws Exception {
    // test conflicting bundles
    compile("<gxp:param name='bundle1' gxp:type='bundle'",
            "  from-element='div'",
            "  exclude='class,lang,title,style,align,dir,contenteditable,",
            "           unselectable,role,tabindex' />",
            "<gxp:param name='bundle2' gxp:type='bundle'",
            "  from-element='div'",
            "  exclude='onclick,ondblclick,onmousedown,onmouseup,onmouseover,",
            "           onmousemove,onmouseout,onkeypress,onkeydown,",
            "           onkeyup,onscroll,oncontextmenu' />");
    assertAlert(new DuplicateParameterNameError(pos(6,1), "id"));
    assertNoUnexpectedAlerts();

    // test bundle conflicting with standard
    compile("<gxp:param name='bundle' gxp:type='bundle'",
            "           from-element='div' />",
            "<gxp:param name='id' type='String' />");
    assertAlert(new DuplicateParameterNameError(pos(4,1), "id"));
    assertNoUnexpectedAlerts();

    // test bundle conflicting with standard (reversed from above)
    compile("<gxp:param name='id' type='String' />",
            "<gxp:param name='bundle' gxp:type='bundle'",
            "           from-element='div' />");
    assertAlert(new DuplicateParameterNameError(pos(3,1), "id"));
    assertNoUnexpectedAlerts();
  }

  public void testBundle_requiredAttribute() throws Exception {
    compile("<gxp:param name='bundle' gxp:type='bundle' from-element='img'/>");
    assertAlert(new RequiredAttrInBundleError(pos(2,1), "alt"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_conflictingAttrs() throws Exception {
    FileRef callee = createFile("callee",
                                "<gxp:param name='bundle' gxp:type='bundle'",
                                "           from-element='div' />");

    // test duplicate between standard and bundle
    FileRef caller = createFile("caller",
                                "<gxp:param name='bundle' gxp:type='bundle'",
                                "           from-element='div' />",
                                "<call:callee id='x' gxp:bundles='bundle'/>");
    compileFiles(callee, caller);
    assertAlert(new DuplicateAttributeError(pos(4, 1), "<call:callee>",
                                            "bundle", "id"));
    assertNoUnexpectedAlerts();

    // test duplicate between gxp:attr and bundle
    caller = createFile("caller",
                        "<gxp:param name='bundle' gxp:type='bundle'",
                        "           from-element='div' />",
                        "<call:callee gxp:bundles='bundle'>",
                        "  <gxp:attr name='id'>",
                        "    <gxp:eval expr='\"foo\"'/>",
                        "  </gxp:attr>",
                        "</call:callee>");
    compileFiles(callee, caller);
    assertAlert(new DuplicateAttributeError(pos(4, 1), "<call:callee>",
                                            "bundle", "id"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_invalidBundle() throws Exception {
    FileRef callee = createFile("callee",
                                "<gxp:param name='bundle' gxp:type='bundle'",
                                "           from-element='div' />");

    // test no gxp:param with that name
    FileRef caller = createFile("caller",
                                "<call:callee gxp:bundles='bundle'/>");
    compileFiles(callee, caller);
    assertAlert(new InvalidAttrBundleError(pos(2, 1), "<call:callee>",
                                           "bundle"));
    assertNoUnexpectedAlerts();

    // test gxp:param with wrong type
    caller = createFile("caller",
                        "<gxp:param name='bundle' type='String' />",
                        "<call:callee gxp:bundles='bundle'/>");
    compileFiles(callee, caller);
    assertAlert(new InvalidAttrBundleError(pos(3, 1), "<call:callee>",
                                           "bundle"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_unknownAttribute() throws Exception {
    FileRef callee = createFile("callee",
                                "<gxp:param name='bundle' gxp:type='bundle'",
                                "           from-element='div'",
                                "           exclude='align' />");

    FileRef caller = createFile("caller",
                                "<gxp:param name='bundle' gxp:type='bundle'",
                                "           from-element='div' />",
                                "<call:callee gxp:bundles='bundle' />");

    compileFiles(callee, caller);
    assertAlert(new UnknownAttributeError("<call:callee>", pos(4,1), "align"));
    assertNoUnexpectedAlerts();
  }

  public void testCall_mismatchedValidators() throws Exception {
    FileRef callee = createFile("callee",
                                "<gxp:param name='bundle' gxp:type='bundle'",
                                "           from-element='div' />");

    FileRef caller = createFile(
        "caller",
        "<gxp:param name='bundle' gxp:type='bundle' from-element='td'",
        "    exclude='abbr,axis,bgcolor,char,charoff,colspan,",
        "             headers,height,nowrap,rowspan,scope,valign,width' />",
        "<call:callee gxp:bundles='bundle' />");

    compileFiles(callee, caller);
    assertAlert(new MismatchedAttributeValidatorsError(pos(5,1),
                                                       "<call:callee>",
                                                       "align", "bundle"));
    assertNoUnexpectedAlerts();
  }

  public void testLoop_attrBundleNotAllowed() throws Exception {
    // test duplicate from bundle
    compile("<gxp:loop var='x' iterator='q'"
            + " gxp:type='bundle' from-element='b'/>");
    assertAlert(new InvalidTypeError(pos(2,1), "BundleType"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_conflictingAttrs() throws Exception {
    // test duplicate from bundle
    compile("<gxp:param name='bundle' gxp:type='bundle' from-element='div' />",
            "<div id='foo' gxp:bundles='bundle'></div>");
    assertAlert(new DuplicateAttributeError(pos(3,1), "<div>",
                                            "bundle", "id"));
    assertNoUnexpectedAlerts();

    // test gxp:attr bundle duplicate
    compile("<gxp:param name='bundle' gxp:type='bundle' from-element='div' />",
            "<div gxp:bundles='bundle'>",
            "  <gxp:attr name='id'>",
            "    foo",
            "  </gxp:attr>",
            "</div>");
    assertAlert(new DuplicateAttributeError(pos(3,1), "<div>",
                                            "bundle", "id"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_invalidBundle() throws Exception {
    // test no gxp:param with that name
    compile("<div gxp:bundles='bundle'></div>");
    assertAlert(new InvalidAttrBundleError(pos(2, 1), "<div>", "bundle"));
    assertNoUnexpectedAlerts();

    // test gxp:param with wrong type
    compile("<gxp:param name='bundle' type='String' />",
            "<div gxp:bundles='bundle'></div>");
    assertAlert(new InvalidAttrBundleError(pos(3, 1), "<div>", "bundle"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_unknownAttributes() throws Exception {
    // test bad names
    compile("<gxp:param name='bundle' gxp:type='bundle' from-element='img'",
            "           exclude='alt,align' />",
            "<div gxp:bundles='bundle'/>");

    // all of the various attributes that exist for img, but not div
    assertAlert(new UnknownAttributeError("<div>", pos(4,1), "border"));
    assertAlert(new UnknownAttributeError("<div>", pos(4,1), "height"));
    assertAlert(new UnknownAttributeError("<div>", pos(4,1), "hspace"));
    assertAlert(new UnknownAttributeError("<div>", pos(4,1), "ismap"));
    assertAlert(new UnknownAttributeError("<div>", pos(4,1), "longdesc"));
    assertAlert(new UnknownAttributeError("<div>", pos(4,1), "name"));
    assertAlert(new UnknownAttributeError("<div>", pos(4,1), "onload"));
    assertAlert(new UnknownAttributeError("<div>", pos(4,1), "src"));
    assertAlert(new UnknownAttributeError("<div>", pos(4,1), "usemap"));
    assertAlert(new UnknownAttributeError("<div>", pos(4,1), "vspace"));
    assertAlert(new UnknownAttributeError("<div>", pos(4,1), "width"));
    assertNoUnexpectedAlerts();
  }

  public void testOutputElement_mismatchedValidators() throws Exception {
    // TD align has a different pattern from DIV align
    compile("<gxp:param name='bundle' gxp:type='bundle' from-element='td'",
            "   exclude='abbr,axis,bgcolor,char,charoff,colspan,",
            "            headers,height,nowrap,rowspan,scope,valign,width' />",
            "<div gxp:bundles='bundle'/>");
    assertAlert(new MismatchedAttributeValidatorsError(pos(5,1), "<div>",
                                                       "align", "bundle"));
    assertNoUnexpectedAlerts();
  }
}
