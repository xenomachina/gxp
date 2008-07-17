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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Iterator;

/**
 * A read-only set of Alerts. (While this class could, theoretically, implement
 * {Set&lt;Alert&gt;}, there isn't really a compelling reason to do this at
 * this time.
 */
public final class AlertSet implements Iterable<Alert> {
  public static final AlertSet EMPTY = new AlertSet(ImmutableSet.<Alert>of());

  // ImmutableSet retains the ordering of its contents
  private final ImmutableSet<Alert> alerts;

  /**
   * Creates AlertSet containing specified Alerts.
   *
   * <p>This constructor is package private as only AlertSetBuilder should call
   * it.
   */
  AlertSet(Iterable<Alert> alerts) {
    this.alerts = ImmutableSet.copyOf(alerts);
  }

  /**
   * @return the number of Alerts in this AlertSet.
   */
  public int size() {
    return alerts.size();
  }

  /**
   * @return whether this AlertSet is empty.
   */
  public boolean isEmpty() {
    return alerts.isEmpty();
  }

  /**
   * @return an iterator over the Alerts in this AlertSet.
   */
  public Iterator<Alert> iterator() {
    return alerts.iterator();
  }

  /**
   * @return whether there are any errors in this AlertSet according
   * to the supplied {@code AlertPolicy}.
   */
  public boolean hasErrors(AlertPolicy policy) {
    for (Alert alert : alerts) {
      if (policy.getSeverity(alert) == Alert.Severity.ERROR) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || ((that instanceof AlertSet) && equals((AlertSet) that));
  }

  public boolean equals(AlertSet that) {
    return Iterables.elementsEqual(this, that);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(alerts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("AlertSet(");
    Iterator<Alert> it = iterator();
    while (it.hasNext()) {
      Alert alert = it.next();
      sb.append(alert.getClass().getCanonicalName());
      sb.append("(");
      sb.append(alert.toString());
      sb.append(")");
      if (it.hasNext()) {
        sb.append(",");
      }
    }
    sb.append(")");
    return sb.toString();
  }
}
