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

package com.google.gxp.compiler.base;

/**
 * A language that we can generate code into.
 */
public enum OutputLanguage {
  CPP("C++", ".cc") {
    @Override
    public <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg) {
      return visitor.visitCpp(arg);
    }
  },

  CPP_HEADER("C++ header", ".h") {
    @Override
    public <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg) {
      return visitor.visitCppHeader(arg);
    }
  },

  JAVA("Java", ".java") {
    @Override
    public <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg) {
      return visitor.visitJava(arg);
    }
  },

  DYNAMIC_IMPL_JAVA("Dynamic Java", "$Impl%d.java") {
    @Override
    public <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg) {
      return visitor.visitDynamicImplJava(arg);
    }
  },

  XMB("Xmb", ".xmb") {
    @Override
    public <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg) {
      return visitor.visitXmb(arg);
    }
  };

  private final String display;
  private final String suffix;
  private final boolean suffixIncludesVersion;

  private OutputLanguage(String display, String suffix) {
    this.display = display;
    this.suffix = suffix;
    this.suffixIncludesVersion = suffix.contains("%d");
  }

  /**
   * @return this display String for this {@code OutputLanguage}.
   */
  public String getDisplay() {
    return display;
  }

  /**
   * @return the suffix associated with this {@code OutputLanguage}.
   */
  public String getSuffix() {
    return suffix;
  }

  public boolean suffixIncludesVersion() {
    return suffixIncludesVersion;
  }

  public abstract <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg);
}
