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

package com.google.gxp.compiler.parser;

import com.google.common.collect.Iterables;
import com.google.gxp.compiler.alerts.Alert;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.schema.BuiltinSchemaFactory;

import junit.framework.TestCase;

/**
 * Tests for {@link NamespaceSet}.
 */
public class NamespaceSetTest extends TestCase {
  private final NamespaceSet nsSet =
      new NamespaceSet(BuiltinSchemaFactory.INSTANCE);

  public void testExactMappings() throws Exception {
    AlertSetBuilder alertSetBuilder = new AlertSetBuilder();
    SourcePosition sourcePosition = new SourcePosition("<test>");
    assertTrue(nsSet.get(alertSetBuilder, sourcePosition,
                         "http://google.com/2001/gxp")
               instanceof GxpNamespace);
    assertTrue(alertSetBuilder.buildAndClear().isEmpty());
    assertTrue(nsSet.get(alertSetBuilder, sourcePosition,
                         "http://google.com/2001/gxp/expressions")
               instanceof ExprNamespace);
    assertTrue(alertSetBuilder.buildAndClear().isEmpty());
    assertTrue(nsSet.get(alertSetBuilder, sourcePosition,
                         "http://google.com/2001/gxp/call")
               instanceof CallNamespace);
    assertTrue(alertSetBuilder.buildAndClear().isEmpty());
    assertTrue(nsSet.get(alertSetBuilder, sourcePosition,
                         "http://www.w3.org/1999/xhtml")
               instanceof OutputNamespace);
    assertTrue(alertSetBuilder.buildAndClear().isEmpty());
  }

  public void testFailedMapping() throws Exception {
    AlertSetBuilder alertSetBuilder = new AlertSetBuilder();
    nsSet.get(alertSetBuilder, new SourcePosition("<test>"),
              "http://google.com/i/dont/exist");
    Alert alert = Iterables.getOnlyElement(alertSetBuilder.buildAndClear());
    assertTrue(alert instanceof UnknownNamespaceError);
  }
}
