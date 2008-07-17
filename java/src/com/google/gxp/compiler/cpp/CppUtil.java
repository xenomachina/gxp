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
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.MissingTypeError;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.NativeType;
import com.google.gxp.compiler.base.OutputLanguage;

/**
 * Contains static functions for validating C++ expressions and types,
 * and a couple additional C++ utility functions.
 */
public class CppUtil {
  /**
   * Validate the given NativeExpression and adds {@link Alert}s to the
   * {@link AlertSink} if necessary.
   */
  public static void validateExpression(AlertSink alertSink, NativeExpression expr) {
    // TODO(harryh): actually do some validation
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

    ret = ret.trim();

    return ret;
  }

  //////////////////////////////////////////////////////////////////////
  // String manipulation
  //////////////////////////////////////////////////////////////////////

  // TODO(harryh): JAVA_STRING_ESCAPE is almost certainly wrong here, need
  //               CPP_STRING_ESCAPE or something like that
  public static String toCppStringLiteral(String s) {
    return "\"" + CharEscapers.JAVA_STRING_ESCAPE.escape(s) + "\"";
  }
}
