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

package com.google.gxp.base;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * An attribute bundle.  Used by the GXP compiler for bundling up a bunch
 * of attributes into a single item.  Currently supports regular and boolean
 * attributes.
 */
public class GxpAttrBundle<T extends MarkupClosure> {
  private final ImmutableMap<String, T> attrs;
  private final ImmutableSet<String> booleanAttrs;

  GxpAttrBundle(Map<String, T> attrs, Set<String> booleanAttrs) {
    this.attrs = ImmutableMap.copyOf(attrs);
    this.booleanAttrs = ImmutableSet.copyOf(booleanAttrs);
  }

  public void write(Appendable out, GxpContext gxpContext) throws IOException {
    for (Map.Entry<String, T> attr : attrs.entrySet()) {
      out.append(' ');
      out.append(attr.getKey());
      out.append("=\"");
      attr.getValue().write(out, gxpContext);
      out.append('"');
    }
    for (String booleanAttr : booleanAttrs) {
      out.append(' ');
      out.append(booleanAttr);
      if (gxpContext.isUsingXmlSyntax()) {
        out.append("=\"");
        out.append(booleanAttr);
        out.append('"');
      }
    }
  }

  private Map<String, T> getAttrs() {
    return attrs;
  }

  private Set<String> getBooleanAttrs() {
    return booleanAttrs;
  }

  /**
   * An attribute bundle builder.
   */
  public static class Builder<T extends MarkupClosure> {
    private final ImmutableSet<String> includeAttrs;
    private final Map<String, T> attrs = Maps.newHashMap();
    private final Set<String> booleanAttrs = Sets.newHashSet();

    // in this case all attributes from added bundles will be accepted
    public Builder() {
      this.includeAttrs = null;
    }

    // in this case only specified attributes will be accepted from bundles
    public Builder(String... includeAttrs) {
      this.includeAttrs = ImmutableSet.of(includeAttrs);
    }

    public Builder<T> attr(String name, T value, boolean cond) {
      return cond ? attr(name, value) : this;
    }

    public Builder<T> attr(String name, T value) {
      attrs.put(name, value);
      return this;
    }

    public Builder<T> attr(String name, boolean include, boolean cond) {
      return cond ? attr(name, include) : this;
    }

    public Builder<T> attr(String name, boolean include) {
      if (include) {
        booleanAttrs.add(name);
      }
      return this;
    }

    public Builder<T> addBundle(GxpAttrBundle<T> bundle) {
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

    public GxpAttrBundle<T> build() {
      return new GxpAttrBundle<T>(attrs, booleanAttrs);
    }
  }
}
