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

package com.google.gxp.compiler.alerts;

import junit.framework.TestCase;

import java.util.*;

/**
 * Tests for {@link AlertSet}.
 */
public class AlertSetTest extends TestCase {
  public void testEmpty() throws Exception {
    AlertSetBuilder builder = new AlertSetBuilder();

    AlertSet alerts = builder.buildAndClear();
    assertTrue(alerts.isEmpty());
    assertEquals(0, alerts.size());

    Iterator<Alert> it = alerts.iterator();
    assertFalse(it.hasNext());

    assertTrue(AlertSet.EMPTY.isEmpty());
  }

  public void testSimple() throws Exception {
    AlertSetBuilder builder = new AlertSetBuilder();

    builder.add(error("one"));
    builder.add(error("two"));
    AlertSet alerts = builder.buildAndClear();

    assertFalse(alerts.isEmpty());
    assertEquals(2, alerts.size());

    Iterator<Alert> it = alerts.iterator();
    assertTrue(it.hasNext());
    assertEquals(error("one"), it.next());
    assertTrue(it.hasNext());
    assertEquals(error("two"), it.next());
    assertFalse(it.hasNext());
  }

  public void testClear() throws Exception {
    AlertSetBuilder builder = new AlertSetBuilder();
    builder.add(error("one"));
    builder.clear();

    AlertSet alerts = builder.buildAndClear();
    assertTrue(alerts.isEmpty());
    assertEquals(0, alerts.size());

    Iterator<Alert> it = alerts.iterator();
    assertFalse(it.hasNext());
  }

  /**
   * Test that an AlertSetBuilder is automatically cleared after calling
   * buildAndClear, and do some sanity checks on having two AlertSets created
   * by the same AlertSetBuilder.
   */
  public void testAutoClear() throws Exception {
    AlertSetBuilder builder = new AlertSetBuilder();

    builder.add(error("one"));
    AlertSet alerts1 = builder.buildAndClear();

    AlertSet alerts0 = builder.buildAndClear();

    assertFalse(alerts1.isEmpty());
    assertTrue(alerts0.isEmpty());
    assertEquals(1, alerts1.size());
    assertEquals(0, alerts0.size());

    Iterator<Alert> it0 = alerts0.iterator();
    assertFalse(it0.hasNext());

    Iterator<Alert> it1 = alerts1.iterator();
    assertTrue(it1.hasNext());
    assertEquals(error("one"), it1.next());
    assertFalse(it1.hasNext());
  }

  public void testChaining() throws Exception {
    AlertSetBuilder builder = new AlertSetBuilder();

    builder.add(error("a"));
    builder.add(error("b"));
    builder.addAll(builder.buildAndClear());
    builder.add(error("x"));
    builder.add(error("y"));
    AlertSet alerts = builder.buildAndClear();

    assertFalse(alerts.isEmpty());
    assertEquals(4, alerts.size());

    Iterator<Alert> it = alerts.iterator();
    assertTrue(it.hasNext());
    assertEquals(error("a"), it.next());
    assertTrue(it.hasNext());
    assertEquals(error("b"), it.next());
    assertTrue(it.hasNext());
    assertEquals(error("x"), it.next());
    assertTrue(it.hasNext());
    assertEquals(error("y"), it.next());
    assertFalse(it.hasNext());
  }

  public void testDupeRemoval() throws Exception {
    AlertSetBuilder builder = new AlertSetBuilder();

    builder.add(error("a")); //dupe
    builder.add(error("b"));
    builder.add(error("c"));
    AlertSet sub1 = builder.buildAndClear();

    builder.add(error("p"));
    builder.add(error("b")); // dupe
    builder.add(error("d"));
    AlertSet sub2 = builder.buildAndClear();

    builder.add(error("a"));
    builder.add(error("z"));
    builder.addAll(sub1);
    builder.add(error("x"));
    builder.add(error("c")); // dupe
    builder.add(error("y"));
    builder.addAll(sub2);
    builder.add(error("q"));
    builder.add(error("p")); // dupe
    AlertSet alerts = builder.buildAndClear();

    assertFalse(alerts.isEmpty());
    assertEquals(9, alerts.size());

    Iterator<Alert> it = alerts.iterator();
    assertTrue(it.hasNext());
    assertEquals(error("a"), it.next());
    assertTrue(it.hasNext());
    assertEquals(error("z"), it.next());
    assertTrue(it.hasNext());
    assertEquals(error("b"), it.next());
    assertTrue(it.hasNext());
    assertEquals(error("c"), it.next());
    assertTrue(it.hasNext());
    assertEquals(error("x"), it.next());
    assertTrue(it.hasNext());
    assertEquals(error("y"), it.next());
    assertTrue(it.hasNext());
    assertEquals(error("p"), it.next());
    assertTrue(it.hasNext());
    assertEquals(error("d"), it.next());
    assertTrue(it.hasNext());
    assertEquals(error("q"), it.next());
    assertFalse(it.hasNext());
  }

  public void testHasErrors() throws Exception {
    AlertSetBuilder builder = new AlertSetBuilder();
    AlertPolicy policy = DefaultAlertPolicy.INSTANCE;

    // empty
    assertFalse(builder.buildAndClear().hasErrors(policy));

    // has warning
    builder.add(warning("foo"));
    assertFalse(builder.buildAndClear().hasErrors(policy));

    // has error
    builder.add(error("foo"));
    assertTrue(builder.buildAndClear().hasErrors(policy));

    // has multiple warnings
    builder.add(warning("foo"));
    builder.add(warning("bar"));
    builder.add(warning("baz"));
    assertFalse(builder.buildAndClear().hasErrors(policy));

    // has multiple errors
    builder.add(error("foo"));
    builder.add(error("bar"));
    builder.add(error("baz"));
    assertTrue(builder.buildAndClear().hasErrors(policy));

    // has a mix of warnings and errors
    builder.add(warning("foo"));
    builder.add(error("bar"));
    builder.add(warning("baz"));
    assertTrue(builder.buildAndClear().hasErrors(policy));
  }

  private static void assertEqualAndHashCodesMatch(Object o1, Object o2) {
    assertEquals(o1, o2);
    assertEquals(o1.hashCode(), o2.hashCode());
  }

  public void testEqualsAndHashCode() throws Exception {
    // Empty AlertSets.
    AlertSetBuilder builder = new AlertSetBuilder();
    assertEqualAndHashCodesMatch(AlertSet.EMPTY, builder.buildAndClear());

    // One Alert.
    builder.add(error("hello"));
    AlertSet a = builder.buildAndClear();

    builder.add(error("hello"));
    AlertSet b = builder.buildAndClear();

    builder.add(error("goodbye"));
    AlertSet c = builder.buildAndClear();

    assertEqualAndHashCodesMatch(a, b);
    assertFalse(AlertSet.EMPTY.equals(a));
    assertFalse(a.equals(AlertSet.EMPTY));
    assertFalse(c.equals(a));
    assertFalse(a.equals(c));

    // Two Alerts, same order.
    builder.add(error("hello"));
    builder.add(warning("goodbye"));
    a = builder.buildAndClear();

    builder.add(error("hello"));
    builder.add(warning("goodbye"));
    b = builder.buildAndClear();

    assertEqualAndHashCodesMatch(a, b);
    assertFalse(AlertSet.EMPTY.equals(a));
    assertFalse(a.equals(AlertSet.EMPTY));
    assertFalse(a.equals(c));
    assertFalse(c.equals(a));

    // Two Alerts, different order.
    builder.add(error("hello"));
    builder.add(warning("goodbye"));
    a = builder.buildAndClear();

    builder.add(warning("goodbye"));
    builder.add(error("hello"));
    b = builder.buildAndClear();

    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
    assertFalse(AlertSet.EMPTY.equals(a));
    assertFalse(a.equals(AlertSet.EMPTY));
    assertFalse(a.equals(c));
    assertFalse(c.equals(a));
  }

  public void testToString() throws Exception {
    assertEquals("AlertSet()", AlertSet.EMPTY.toString());

    AlertSetBuilder builder = new AlertSetBuilder();
    builder.add(error("one"));
    builder.add(error("two"));
    assertEquals("AlertSet("
                 + getClass().getCanonicalName()
                 + ".TestError(/<test>:0:0:0:0: one),"
                 + getClass().getCanonicalName()
                 + ".TestError(/<test>:0:0:0:0: two)"
                 + ")",
                 builder.buildAndClear().toString());
  }

  private static class TestError extends ErrorAlert {
    TestError(String message) {
      super(new SourcePosition("<test>"), message);
    }
  }

  private static ErrorAlert error(final String message) {
    return new TestError(message);
  }

  private static WarningAlert warning(final String message) {
    return new WarningAlert(new SourcePosition("<test>"), message) {};
  }
}
