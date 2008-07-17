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

import com.google.common.collect.Sets;
import com.google.gxp.compiler.parser.NoNamespaceError;
import com.google.gxp.compiler.parser.UnknownNamespaceError;

import java.util.LinkedHashSet;

import junit.framework.TestCase;

/**
 * Tests for {@link UniquifyingAlertSink}.
 */
public class UniquifyingAlertSinkTest extends TestCase {
  private static class Counter implements AlertSink {
    public int count = 0;

    public void add(Alert alert) {
      count++;
    }

    public void addAll(AlertSet alertSet) {
      count += alertSet.size();
    }
  }

  private static SourcePosition pos(String sourceName, int line, int column) {
    return new SourcePosition(sourceName, line, column);
  }

  private static Alert[] alerts = {
    new UnknownNamespaceError(pos("foo", 1, 1), "bar"),
    new UnknownNamespaceError(pos("foo", 1, 1), "bar"), // equals() to above
    new UnknownNamespaceError(pos("foo", 2, 2), "bar"),
    new UnknownNamespaceError(pos("foo", 2, 2), "baz"),
    new NoNamespaceError(pos("foo", 1, 1)),
    new NoNamespaceError(pos("foo", 1, 1)), // equals() to above
    new NoNamespaceError(pos("foo", 2, 2)),
    new NoNamespaceError(pos("bar", 2, 2)),
  };

  private static LinkedHashSet<Alert> set = Sets.newLinkedHashSet();
  private static AlertSet aset = new AlertSet(set);
  static {
    for (int i = 0; i < alerts.length; i++) {
      set.add(alerts[i]);
    }
  }

  public void testUniquifyingAlertSink() throws Exception {
    Counter c = new Counter();
    UniquifyingAlertSink sink = new UniquifyingAlertSink(c);

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < alerts.length; j++) {
        sink.add(alerts[j]);
      }
      assertEquals(6, c.count);
    }

    sink.addAll(aset);
    assertEquals(6, c.count);
  }
}
