/*
 * Copyright (C) 2007 Google Inc.
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

package com.google.gxp.base;

import com.google.common.collect.ImmutableSet;

import java.util.*;

/**
 * An attribute bundle builder. Used to construct an AttrBundle.
 */
public class GxpAttrBundleBuilder<T extends MarkupClosure> {
  private final Set<String> includeAttrs;
  private HashMap<String, T> attrs = new HashMap<String, T>();
  private HashSet<String> booleanAttrs = new HashSet<String>();

  // in this case all attributes from added bundles will be accepted
  public GxpAttrBundleBuilder() {
    this.includeAttrs = null;
  }

  // in this case only specified attributes will be accepted from bundles
  public GxpAttrBundleBuilder(String... includeAttrs) {
    this.includeAttrs = ImmutableSet.of(includeAttrs);
  }

  public GxpAttrBundleBuilder<T> attr(String attr, boolean cond, T value) {
    return cond ? attr(attr, value) : this;
  }

  public GxpAttrBundleBuilder<T> attr(String attr, T value) {
    attrs.put(attr, value);
    return this;
  }

  public GxpAttrBundleBuilder<T> booleanAttr(String attr, boolean cond, boolean include) {
    return cond ? booleanAttr(attr, include) : this;
  }

  public GxpAttrBundleBuilder<T> booleanAttr(String attr, boolean include) {
    if (include) {
      booleanAttrs.add(attr);
    }
    return this;
  }

  public GxpAttrBundleBuilder<T> addBundle(GxpAttrBundle<T> bundle) {
    if (includeAttrs == null) {
      attrs.putAll(bundle.getAttrs());
      booleanAttrs.addAll(bundle.getBooleanAttrs());
    } else {
      for (Map.Entry<String, T> attr : bundle.getAttrs().entrySet()) {
        if (includeAttrs.contains(attr.getKey())) {
          attrs.put(attr.getKey(), attr.getValue());
        }
      }
      for (String attr : bundle.getBooleanAttrs()) {
        if (includeAttrs.contains(attr)) {
          booleanAttrs.add(attr);
        }
      }
    }
    return this;
  }

  public GxpAttrBundle<T> getBundle() {
    HashMap<String, T> tmpAttrs = attrs;
    HashSet<String> tmpBooleanAttrs = booleanAttrs;
    attrs = new HashMap<String, T>();
    booleanAttrs = new HashSet<String>();
    return new GxpAttrBundle<T>(tmpAttrs, tmpBooleanAttrs);
  }
}
