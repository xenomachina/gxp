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

package com.google.gxp.compiler.reparent;

import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.common.MissingAttributeError;
import com.google.gxp.compiler.alerts.common.MultiValueAttributeError;
import com.google.gxp.compiler.alerts.common.UnknownAttributeError;
import com.google.gxp.compiler.base.AbstractNode;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.GxpcTestCase;
import com.google.gxp.compiler.parser.NullNamespace;

/**
 * Tests for {@link AttributeMap}.
 */
public class AttributeMapTest extends GxpcTestCase {
  private final AlertSetBuilder actualAlerts = new AlertSetBuilder();
  private final AlertSetBuilder expectedAlerts = new AlertSetBuilder();

  private final Node fromNode = new AbstractNode(pos(), "<ignored>"){};
  private final AttributeMap attrMap = new AttributeMap(actualAlerts, fromNode);

  public void tearDown() throws Exception {
    assertEquals(expectedAlerts.buildAndClear(), actualAlerts.buildAndClear());
  }

  public void testNotFoundWithFallback() throws Exception {
    expectedAlerts.add(new MissingAttributeError(fromNode, "foo"));

    assertEquals("bar", attrMap.get("foo", "bar"));
    attrMap.reportUnusedAttributes();
  }

  public void testNotFoundWithoutFallback() throws Exception {
    expectedAlerts.add(new MissingAttributeError(fromNode, "foo"));

    assertEquals(null, attrMap.get("foo", null));
    attrMap.reportUnusedAttributes();
  }

  public void testFound() throws Exception {
    attrMap.add(attr(NullNamespace.INSTANCE, "foo", str("quux")));
    assertEquals("quux", attrMap.get("foo", "bar"));
    attrMap.reportUnusedAttributes();
  }

  public void testUnused() throws Exception {
    Attribute attr = attr(NullNamespace.INSTANCE, "foo", str("quux"));
    expectedAlerts.add(new UnknownAttributeError(fromNode, attr));

    attrMap.add(attr);
    attrMap.reportUnusedAttributes();
  }

  public void testMultiValue() throws Exception {
    Attribute attr;

    attr = attr(NullNamespace.INSTANCE, "foo", str("quux"));
    attrMap.add(attr);

    attr = attr(NullNamespace.INSTANCE, "foo", str("baz"));
    attrMap.add(attr);
    attrMap.get("foo", "bar");

    expectedAlerts.add(new MultiValueAttributeError(fromNode, attr));

    attrMap.reportUnusedAttributes();
  }

  public void testOptionalNotFoundWithFallback() throws Exception {
    assertEquals("bar", attrMap.getOptional("foo", "bar"));
    attrMap.reportUnusedAttributes();
  }

  public void testOptionalFound() throws Exception {
    attrMap.add(attr(NullNamespace.INSTANCE, "foo", str("quux")));
    assertEquals("quux", attrMap.getOptional("foo", "bar"));
    attrMap.reportUnusedAttributes();
  }
}
