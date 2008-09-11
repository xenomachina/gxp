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

package com.google.gxp.compiler.errortests;

import com.google.common.base.CharEscapers;
import com.google.common.collect.ImmutableMap;
import com.google.gxp.compiler.alerts.Alert.Severity;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.SaxAlert;
import com.google.gxp.compiler.codegen.IllegalExpressionError;
import com.google.gxp.compiler.codegen.IllegalNameError;
import com.google.gxp.compiler.codegen.IllegalOperatorError;
import com.google.gxp.compiler.reparent.IllegalVariableNameError;
import com.google.gxp.compiler.java.IllegalJavaTypeError;
import com.google.gxp.testing.BaseErrorTestCase;

import java.util.*;

import org.xml.sax.SAXParseException;

/**
 * Base TestCase for errortests package. Contains a bit of extra
 * functionallity over {@code BaseErrorTestCase} for validating
 * names, java types, and java expressions.
 *
 * Typical protocol for a test is to call compile(), and then assertAlert()
 * for each Alert you expect, and finally assertNoUnexpectedAlerts().
 */
public abstract class BaseTestCase extends BaseErrorTestCase {

  /**
   * Assert a {@code SaxError} based on the given {@code SourcePosition}
   * and message.
   */
  protected final void assertParseAlert(SourcePosition pos, String message) {
    assertAlert(new SaxAlert(pos, Severity.ERROR,
                             new SAXParseException(message, null)));
  }

  private static String[] LEGAL_EXPRESSIONS = {
    "snarf + \"forbiddenOperatorInString++\"",
    "\"closed String\"",
    "\"//unclosed comment in string\"",
    "\"/* unclosed comment in string\"",
    "/* closed comment */",
    "\"illegal $ characters in string\"",
    "/* illegal % characters in comment */",
    "(List<List<Foo>>)genericCast",
    "(List<List<List<List<Foo>>>>) deepDenericCast",
    "(Pair<List<List<Foo>>, Bar>)anotherGenericCast",
  };

  private static ImmutableMap<String, String> ILLEGAL_OPERATORS
    = new ImmutableMap.Builder<String, String>()
      .put("i++", "++")
      .put("i instanceof Foo", "instanceof")
      .put("i = 10", "=")
      .put("i & 1", "&")
      .put("i >> 2", ">>")
      .put("i >>> 2", ">>>")
      .build();

  private static String[] ILLEGAL_EXPRESSIONS = {
    "\"unclosed String",
    "//unclosed comment",
    "/* unclosed comment",
    "(mismatched_parens",
    "(mismatch[)1]",
  };

  /**
   * Convenience method for when the error position should be pos(2,1)
   */
  public final void assertIllegalExpressionDetected(
      String prefix, String suffix) throws Exception {
    assertIllegalExpressionDetected(prefix, suffix, 2, 1);
  }

  /**
   * Givin a prefix and suffix, constructs and compiles GXPs containing the
   * prefix followed by an (XML-escaped) expression, followed by the suffix.
   * Several legal and illegal expressions are attempted. Legal expressions
   * should generete no alerts, while illegal expressions should generate
   * specific alerts.
   */
  public final void assertIllegalExpressionDetected(String prefix,
                                                    String suffix,
                                                    int errorLine,
                                                    int errorColumn)
      throws Exception {
    for (String legalExpr : LEGAL_EXPRESSIONS) {
      compile(prefix + CharEscapers.xmlEscaper().escape(legalExpr) + suffix);
      assertNoUnexpectedAlerts();
    }

    SourcePosition errorPos = pos(errorLine, errorColumn);

    for (Map.Entry<String, String> illegalOp : ILLEGAL_OPERATORS.entrySet()) {
      compile(prefix + CharEscapers.xmlEscaper().escape(illegalOp.getKey()) + suffix);
      assertAlert(new IllegalOperatorError(errorPos, "Java", illegalOp.getValue()));
      assertNoUnexpectedAlerts();
    }

    for (String illegalExpr : ILLEGAL_EXPRESSIONS) {
      compile(prefix + CharEscapers.xmlEscaper().escape(illegalExpr) + suffix);
      assertAlert(new IllegalExpressionError(errorPos, "Java", illegalExpr));
      assertNoUnexpectedAlerts();
    }
  }

  private static String[] ILLEGAL_VAR_NAMES = {
    "this",
    "gxp_is_a_reserved_prefix",
    "bad!name",
    "no.dots.for.you",
    "too__wideWithTheUnderscores",
    "_leadingUnderscore",
    "trailingUnderscore_",
    "1MoreToGo",
    "IWishICouldThinkOfAVariableNameThatWasExactlySixtyFiveLettersLong",
  };

  private static String[] ILLEGAL_JAVA_NAMES = {
    "boolean",
    "true",
    "null",
    "final",
  };

  private static String[] LEGAL_VAR_NAMES = {
    "x",
    "gumby",
    "Pokey",
    "STOP_YELLING",
    "B1FF",
    "the_beav",
    "worst_name_ever",
    "gxpWithoutAnUnderscoreIsFine",
    "allTheKingsHorsesAndAllTheKingsMenCouldNotPutHumptyTogetherAgain",
  };

  /**
   * Givin a prefix and suffix, constructs and compiles GXPs containing the
   * prefix followed by an (XML-escaped) name, followed by the suffix.
   * Several legal and illegal names are attempted. Legal names
   * should generetae no alerts, while illegal names should generate
   * specific alerts.
   */
  public final void assertIllegalVariableNameDetected(String attrName,
                                                      String prefix,
                                                      String suffix)
      throws Exception {
    for (String legalName : LEGAL_VAR_NAMES) {
      compile(prefix + CharEscapers.xmlEscaper().escape(legalName) + suffix);
      assertNoUnexpectedAlerts();
    }

    for (String illegalName : ILLEGAL_VAR_NAMES) {
      compile(prefix + CharEscapers.xmlEscaper().escape(illegalName) + suffix);
      SourcePosition errorPos = pos(2, 1);
      assertAlert(new IllegalVariableNameError(errorPos, illegalName));
      assertNoUnexpectedAlerts();
    }

    // TODO: update this code to better test how various names can be legal
    //       in some OutputLanguages, but illegal in others
    for (String illegalName : ILLEGAL_JAVA_NAMES) {
      compile(prefix + CharEscapers.xmlEscaper().escape(illegalName) + suffix);
      SourcePosition errorPos = pos(2, 1);
      assertAlert(new IllegalNameError(errorPos, "Java", illegalName));
      assertNoUnexpectedAlerts();
    }
  }

  private static String[] ILLEGAL_TYPES = {
    "bad!type",
    "String[10]",
    "String][",
    "String...",
    "List<Integer}",
    "List{Integer>",
    "List{List<String}>",
    "List<Intege>r",
    "List<Integer",
    "List>Integer<",
    "ListInteger>",
    "<Integer>",
    "[]",
    ".java.util.List",
    "java.util.",
    "java..util.List",
  };

  private static String[] LEGAL_TYPES = {
    "int",
    "boolean",
    "int[]",
    "int[][]",
    "List<String>",
    "List<String>[]",
    "List<Map<String, Integer>>",
    "Map<String, List<Integer>>",
    "java.lang.String",
    "java.util.List<java.lang.String>",
    "java.lang.String[]",
    "java.util.List<java.util.Map<java.lang.String, java.lang.Integer>>",
    "java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>",
    "List{String}",
    "List{String}[]",
    "List{Map{String, Integer}}",
    "Map{String, List{Integer}}",
    "java.util.List{java.lang.String}",
    "java.util.List{java.util.Map{java.lang.String, java.lang.Integer}}",
    "java.util.Map{java.lang.String, java.util.List{java.lang.Integer}}",
    " List < List < String > > [ ]",
  };

  /**
   * Givin a prefix and suffix, constructs and compiles GXPs containing the
   * prefix followed by an (XML-escaped) type, followed by the suffix.
   * Several legal and illegal types are attempted. Legal types
   * should generetae no alerts, while illegal types should generate
   * specific alerts.
   */
  public final void assertIllegalTypeDetected(String prefix,
                                              String suffix)
      throws Exception {
    for (String legalType : LEGAL_TYPES) {
      compile(prefix + CharEscapers.xmlEscaper().escape(legalType) + suffix);
      assertNoUnexpectedAlerts();
    }

    SourcePosition errorPos = pos(2, 1);
    for (String illegalType : ILLEGAL_TYPES) {
      compile(prefix + CharEscapers.xmlEscaper().escape(illegalType) + suffix);
      assertAlert(new IllegalJavaTypeError(errorPos, illegalType));
      assertNoUnexpectedAlerts();
    }
  }
}
