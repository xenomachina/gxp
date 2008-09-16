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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.gxp.compiler.alerts.Alert.Severity;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.SaxAlert;
import com.google.gxp.compiler.base.OutputLanguage;
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

  private static class IllegalOperatorExpression {
    private final String expression;
    private final String operator;
    private final ImmutableList<OutputLanguage> outputLanguages;

    public IllegalOperatorExpression(String expression, String operator,
                                     OutputLanguage... outputLanguages) {
      this.expression = expression;
      this.operator = operator;
      this.outputLanguages = ImmutableList.of(outputLanguages);
    }

    public String getExpression() {
      return expression;
    }

    public String getOperator() {
      return operator;
    }

    public ImmutableList<OutputLanguage> getOutputLanguages() {
      return outputLanguages;
    }
  }

  private static ImmutableList<IllegalOperatorExpression> ILLEGAL_OPERATORS
      = ImmutableList.of(
          new IllegalOperatorExpression("i++", "++", OutputLanguage.JAVA),
          new IllegalOperatorExpression("i instanceof Foo", "instanceof", OutputLanguage.JAVA),
          new IllegalOperatorExpression("i = 10", "=", OutputLanguage.JAVA),
          new IllegalOperatorExpression("i & 1", "&", OutputLanguage.JAVA),
          new IllegalOperatorExpression("i >> 2", ">>", OutputLanguage.JAVA),
          new IllegalOperatorExpression("i >>> 2", ">>>", OutputLanguage.JAVA));

  private static ImmutableMap<String, Collection<OutputLanguage>> ILLEGAL_EXPRESSIONS =
    new ImmutableMultimap.Builder<String, OutputLanguage>()
      .putAll("\"unclosed String", OutputLanguage.JAVA)
      .putAll("//unclosed comment", OutputLanguage.JAVA)
      .putAll("/* unclosed comment", OutputLanguage.JAVA)
      .putAll("(mismatched_parens", OutputLanguage.JAVA)
      .putAll("(mismatch[)1]", OutputLanguage.JAVA)
      .build().asMap();

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

    for (IllegalOperatorExpression illegalOp : ILLEGAL_OPERATORS) {
      compile(prefix + CharEscapers.xmlEscaper().escape(illegalOp.getExpression()) + suffix);
      for (OutputLanguage outputLanguage : illegalOp.getOutputLanguages()) {
        assertAlert(new IllegalOperatorError(errorPos, outputLanguage.getDisplay(),
                                             illegalOp.getOperator()));
      }
      assertNoUnexpectedAlerts();
    }

    for (Map.Entry<String, Collection<OutputLanguage>> illegalExpr :
             ILLEGAL_EXPRESSIONS.entrySet()) {
      compile(prefix + CharEscapers.xmlEscaper().escape(illegalExpr.getKey()) + suffix);
      for (OutputLanguage outputLanguage : illegalExpr.getValue()) {
        assertAlert(new IllegalExpressionError(errorPos, outputLanguage.getDisplay(),
                                               illegalExpr.getKey()));
      }
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

  private static ImmutableMap<String, Collection<OutputLanguage>> ILLEGAL_OUTPUT_LANGUAGE_NAMES =
    new ImmutableMultimap.Builder<String, OutputLanguage>()
      .putAll("for", OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT)
      .putAll("boolean", OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT)
      .putAll("true", OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT)
      .putAll("assert", OutputLanguage.JAVA)
      .putAll("delete", OutputLanguage.JAVASCRIPT)
      .build().asMap();

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

    for (Map.Entry<String, Collection<OutputLanguage>> illegalName :
             ILLEGAL_OUTPUT_LANGUAGE_NAMES.entrySet()) {
      compile(prefix + CharEscapers.xmlEscaper().escape(illegalName.getKey()) + suffix);
      SourcePosition errorPos = pos(2, 1);
      for (OutputLanguage outputLanguage : illegalName.getValue()) {
        assertAlert(new IllegalNameError(errorPos, outputLanguage.getDisplay(),
                                         illegalName.getKey()));
      }
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
