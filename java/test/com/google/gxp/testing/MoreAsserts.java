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

package com.google.gxp.testing;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains additional assertion methods not found in JUnit.
 */
public final class MoreAsserts {

  private MoreAsserts() { }

  /**
   * Asserts that {@code actual} is not equal {@code unexpected}, according
   * to both {@code ==} and {@link Object#equals}.
   */
  public static void assertNotEqual(Object unexpected, Object actual) {
    if (Objects.equal(unexpected, actual)) {
      Assert.fail("expected not to be <" + unexpected + ">");
    }
  }

  /**
   * Asserts that array {@code actual} is the same size and every element equals
   * those in array {@code expected}.
   */
  public static void assertEquals(byte[] expected, byte[] actual) {
    if (expected.length != actual.length) {
      Assert.fail("expected array length:<" + expected.length 
                  + "> but was:<" + actual.length + '>');
    }
    for (int i = 0; i < expected.length; i++) {
      if (expected[i] != actual[i]) {
        Assert.fail("expected array element[" + i + "]:<"
                    + expected[i] + "> but was:<" + actual[i] + '>');
      }
    }
  }

  /**
   * Asserts that {@code expectedRegex} matches any substring of {@code actual}
   * and fails with {@code message} if it does not.  The Matcher is returned in
   * case the test needs access to any captured groups.  Note that you can also
   * use this for a literal string, by wrapping your expected string in
   * {@link Pattern#quote}.
   */
  public static MatchResult assertContainsRegex(String expectedRegex, String actual) {
    if (actual == null) {
      Assert.fail("expected to contain regex:<" + expectedRegex + "> but was null");
    }
    Matcher matcher = Pattern.compile(expectedRegex).matcher(actual);;
    if (!matcher.find()) {
      Assert.fail("expected to contain regex:<" + expectedRegex + "> but was " + actual);
    }
    return matcher;
  }

  /**
   * Asserts that {@code actual} contains precisely the elements
   * {@code expected}, and in the same order.
   */
  public static void assertContentsInOrder(String message, Iterable<?> actual, Object... expected) {
    Assert.assertEquals(message, Arrays.asList(expected), Lists.newArrayList(actual));
  }

  /**
   * Asserts that {@code actual} contains precisely the elements
   * {@code expected}, and in the same order.
   */
  public static void assertContentsInOrder(Iterable<?> actual, Object... expected) {
    assertContentsInOrder((String)null, actual, expected);
  }

  /**
   * Asserts that {@code actual} contains precisely the elements
   * {@code expected}, in any order.  Both collections may contain
   * duplicates, and this method will only pass if the quantities are
   * exactly the same.
   */
  public static void assertContentsAnyOrder(String message,
                                            Iterable<?> actual, Object... expected) {
    Assert.assertEquals(message,
                        Multisets.newHashMultiset(expected), Multisets.newHashMultiset(actual));
  }

  /**
   * Variant of {@link #assertContentsAnyOrder(String,Iterable,Object...)}
   * using a generic message.
   */
  public static void assertContentsAnyOrder(Iterable<?> actual, Object... expected) {
    assertContentsAnyOrder((String) null, actual, expected);
  }
}
