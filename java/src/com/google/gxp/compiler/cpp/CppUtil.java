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

package com.google.gxp.compiler.cpp;

import com.google.common.base.CharEscapers;
import com.google.common.collect.ImmutableSet;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.MissingTypeError;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.NativeType;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.codegen.OutputLanguageUtil;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Contains static functions for validating C++ expressions and types,
 * and a couple additional C++ utility functions.
 */
public class CppUtil extends OutputLanguageUtil {

  private CppUtil() {
    // TODO(harryh): javaStringEscaper() is almost certainly wrong here, need
    //               CPP_STRING_ESCAPE or something like that
    super(RESERVED_WORDS, FORBIDDEN_OPS, OPS_FINDER,
          CharEscapers.javaStringUnicodeEscaper(),
          CharEscapers.javaStringEscaper());
  }

  // READ THIS BEFORE YOU CHANGE THE LIST BELOW!
  //
  // The list of disabled C++ operators was originally based on the list
  // of disabled Java Operators. If you want to enable something here, see
  // about getting it enabled for Java as well.
  //
  // TODO(harryh): fill this in
  private static final Set<String> FORBIDDEN_OPS = ImmutableSet.of();

  // the order is important! The '|' operator  is non-greedy in
  // regexes. Sorting in order of descending length works.
  //
  // TODO(harryh): fill this in
  private static final Pattern OPS_FINDER = compileUnionPattern();

  // TODO(harryh): fill this in
  private static final Set<String> RESERVED_WORDS = ImmutableSet.of();

  /**
   * Temporarily override this until all C++ expressions get passed through
   * validate expression (otherwise getting the test to work right is hard)
   */
  @Override
  public String validateExpression(AlertSink alertSink, NativeExpression expr,
                                   OutputLanguage outputLanguage) {
    String result = expr.getNativeCode(outputLanguage);
    if (result == null) {
      return "";
    }
    return CharEscapers.javaStringUnicodeEscaper().escape(result);
  }

  /**
   * Validate the given NativeType and adds alerts to the sink if
   * necessary.
   *
   * @return a String representing the validated type
   */
  public static String validateType(AlertSink alertSink, NativeType type) {
    // TODO(harryh): actually do some validation
    String ret = type.getNativeType(OutputLanguage.CPP);
    if (ret == null) {
      alertSink.add(new MissingTypeError(type, OutputLanguage.CPP));
      return ret;
    }

    ret = ret.replace("{", "< ").replace("}", " >").trim();

    return ret;
  }

  /**
   * Static Singleton Instance
   *
   * Must be declared last in the source file.
   */ 
  public static final CppUtil INSTANCE = new CppUtil();
}
