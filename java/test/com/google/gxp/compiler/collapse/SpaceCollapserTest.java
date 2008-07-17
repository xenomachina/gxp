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

package com.google.gxp.compiler.collapse;

import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.FormalTypeParameter;
import com.google.gxp.compiler.base.Import;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.ThrowsDeclaration;
import com.google.gxp.compiler.bind.BoundTree;
import com.google.gxp.compiler.GxpcTestCase;

import java.util.*;

/**
 * Tests for {@code SpaceCollapser}.
 */
public class SpaceCollapserTest extends GxpcTestCase {
  private static final SpaceCollapser COLLAPSER = new SpaceCollapser();

  // We don't care so much about source positions for these tests, so make
  // pos() return a fixed value for each test.
  private final SourcePosition POS = super.pos();
  public SourcePosition pos() {
    return POS;
  }

  public void testCollapse(Expression input, Expression expected) {
    Template root = template("bar.Baz",
                             htmlSchema(),
                             Collections.<Import>emptyList(),
                             Collections.<ThrowsDeclaration>emptyList(),
                             Collections.<Parameter>emptyList(),
                             Collections.<FormalTypeParameter>emptyList(),
                             collapse(input, null, null));
    BoundTree inTree = new BoundTree(pos(), AlertSet.EMPTY, root,
                                     Collections.<Callable>emptySet());
    SpaceCollapsedTree outTree = COLLAPSER.apply(inTree);
    Root newRoot = outTree.getRoot();
    assertTrue(newRoot + " is not a Template",
               newRoot instanceof Template);
    Template newTemplate = (Template)newRoot;
    assertEquals(expected, newTemplate.getContent());
  }

  public void testBasic() throws Exception {
    Expression input = str(" hello world ");
    testCollapse(input, str("hello world"));
  }

  public void testBasicWithNewlines() throws Exception {
    Expression input = str("\n hello world \n");
    testCollapse(input, str("hello world"));
  }
}
