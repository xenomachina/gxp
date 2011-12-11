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

import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.codegen.OutputLanguageUtil;
import com.google.gxp.compiler.cpp.CppUtil;
import com.google.gxp.compiler.java.JavaUtil;
import com.google.gxp.compiler.js.JavaScriptUtil;
import com.google.gxp.compiler.scala.ScalaUtil;

/**
 * A language that we can generate code into.
 */
public enum OutputLanguage {
  CPP("C++", ".cc", CppUtil.INSTANCE) {
    @Override
    public <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg) {
      return visitor.visitCpp(arg);
    }
  },

  CPP_HEADER("C++ header", ".h", CppUtil.INSTANCE) {
    @Override
    public <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg) {
      return visitor.visitCppHeader(arg);
    }
  },

  JAVA("Java", ".java", JavaUtil.INSTANCE) {
    @Override
    public <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg) {
      return visitor.visitJava(arg);
    }
  },

  DYNAMIC_IMPL_JAVA("Dynamic Java", "$Impl%d.java", JavaUtil.INSTANCE) {
    @Override
    public <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg) {
      return visitor.visitDynamicImplJava(arg);
    }
  },

  JAVASCRIPT("JavaScript", ".js", JavaScriptUtil.INSTANCE) {
    @Override
    public <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg) {
      return visitor.visitJavaScript(arg);
    }
  },

  SCALA("Scala", ".scala", ScalaUtil.INSTANCE) {
    @Override
    public <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg) {
      return visitor.visitScala(arg);
    }
  },

  XMB("Xmb", ".xmb", null) {
    @Override
    public <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg) {
      return visitor.visitXmb(arg);
    }
  };

  private final String display;
  private final String suffix;
  private final boolean suffixIncludesVersion;
  private final OutputLanguageUtil outputLanguageUtil;

  private OutputLanguage(String display, String suffix, OutputLanguageUtil outputLanguageUtil) {
    this.display = display;
    this.suffix = suffix;
    this.suffixIncludesVersion = suffix.contains("%d");
    this.outputLanguageUtil = outputLanguageUtil;
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
  public String getSuffix(long compilationVersion) {
    return suffixIncludesVersion
        ? String.format(suffix, compilationVersion)
        : suffix;
  }

  public String validateExpression(AlertSink alertSink, NativeExpression expr) {
    return outputLanguageUtil.validateExpression(alertSink, expr, this);
  }

  public String validateName(AlertSink alertSink, Node node, String name) {
    return outputLanguageUtil.validateName(alertSink, node, name, this);
  }

  public String toStringLiteral(String s) {
    return outputLanguageUtil.toStringLiteral(s);
  }

  public abstract <K,V> V acceptVisitor(OutputLanguageVisitor<K,V> visitor, K arg);
}
