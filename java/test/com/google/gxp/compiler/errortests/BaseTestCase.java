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
import com.google.gxp.compiler.codegen.IllegalTypeError;
import com.google.gxp.compiler.reparent.IllegalVariableNameError;
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
  };

  private static class IllegalOperatorExpression {
    private final String expression;
    private final String operator;
    private final ImmutableList<OutputLanguage> outputLanguages;

    public IllegalOperatorExpression(String expression, String operator,
                                     OutputLanguage... outputLanguages) {
      this.expression = expression;
      this.operator = operator;
      this.outputLanguages = ImmutableList.copyOf(outputLanguages);
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
          new IllegalOperatorExpression("i++", "++",
                                        OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA),
          new IllegalOperatorExpression("i instanceof Foo", "instanceof",
                                        OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA),
          new IllegalOperatorExpression("i = 10", "=",
                                        OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA),
          new IllegalOperatorExpression("i & 1", "&",
                                        OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA),
          new IllegalOperatorExpression("i >> 2", ">>",
                                        OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA),
          new IllegalOperatorExpression("i >>> 2", ">>>",
                                        OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA),

          // here are a couple of expressions with java casts.  they are legal in
          // java but illegal in javascript
          new IllegalOperatorExpression("(List<List<Foo>>)genericCast", ">>",
                                        OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA),
          new IllegalOperatorExpression("(List<List<List<List<Foo>>>>) deepDenericCast", ">>>",
                                        OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA),
          new IllegalOperatorExpression("(Pair<List<List<Foo>>, Bar>)anotherGenericCast", ">>",
                                        OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA),

          // here are a few illegal JS operators, that are not illegal in JS
          // (though they aren't valid so will result in a java compile error)
          new IllegalOperatorExpression("x typeof y", "typeof",
                                        OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA),
          new IllegalOperatorExpression("x in y", "in",
                                        OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA));

  private static ImmutableMap<String, Collection<OutputLanguage>> ILLEGAL_EXPRESSIONS =
    new ImmutableMultimap.Builder<String, OutputLanguage>()
      .putAll("\"unclosed String",
              OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA)
      .putAll("//unclosed comment",
              OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA)
      .putAll("/* unclosed comment",
              OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA)
      .putAll("(mismatched_parens",
              OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA)
      .putAll("(mismatch[)1]",
              OutputLanguage.JAVA, OutputLanguage.JAVASCRIPT, OutputLanguage.SCALA)
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

  private static class TestType {
    private final String type;
    private final ImmutableList<OutputLanguage> illegalIn;

    public TestType(String type, OutputLanguage... illegalIn) {
      this.type = type;
      this.illegalIn = ImmutableList.copyOf(illegalIn);
    }

    public String getType() {
      return type;
    }

    public ImmutableList<OutputLanguage> getIllegalIn() {
      return illegalIn;
    }
  }

  private static ImmutableList<TestType> TEST_TYPES
      = ImmutableList.of(
          new TestType("bad!type", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("String[10]", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("String][", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("String...", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("List<Integer}", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("List{Integer>", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("List{List<String}>", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("List<Intege>r", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("List<Integer", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("List>Integer<", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("ListInteger>", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("<Integer>", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("[]", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType(".java.util.List", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("java.util.", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("java..util.List", OutputLanguage.JAVA, OutputLanguage.SCALA),
          new TestType("int"),
          new TestType("boolean"),
          new TestType("int[]"),
          new TestType("int[][]"),
          new TestType("List<String>", OutputLanguage.SCALA),
          new TestType("List<String>[]", OutputLanguage.SCALA),
          new TestType("List<Map<String, Integer>>", OutputLanguage.SCALA),
          new TestType("Map<String, List<Integer>>", OutputLanguage.SCALA),
          new TestType("java.lang.String"),
          new TestType("java.util.List<java.lang.String>", OutputLanguage.SCALA),
          new TestType("java.lang.String[]", OutputLanguage.SCALA),
          new TestType("java.util.List<java.util.Map<java.lang.String, java.lang.Integer>>", OutputLanguage.SCALA),
          new TestType("java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>", OutputLanguage.SCALA),
          new TestType("List{String}"),
          new TestType("List{String}[]"),
          new TestType("List{Map{String, Integer}}"),
          new TestType("Map{String, List{Integer}}"),
          new TestType("java.util.List{java.lang.String}"),
          new TestType("java.util.List{java.util.Map{java.lang.String, java.lang.Integer}}"),
          new TestType("java.util.Map{java.lang.String, java.util.List{java.lang.Integer}}"),
          new TestType(" List < List < String > > [ ]", OutputLanguage.SCALA));

  /**
   * Givin a prefix and suffix, constructs and compiles GXPs containing the
   * prefix followed by an (XML-escaped) type, followed by the suffix.
   * Several legal and illegal types are attempted. Legal types
   * should generetae no alerts, while illegal types should generate
   * specific alerts.
   */
  public final void assertIllegalTypeDetected(String prefix, String suffix) throws Exception {
    for (TestType testType : TEST_TYPES) {
      compile(prefix + CharEscapers.xmlEscaper().escape(testType.getType()) + suffix);
      SourcePosition errorPos = pos(2, 1);
      for (OutputLanguage outputLanguage : testType.getIllegalIn()) {
        assertAlert(new IllegalTypeError(errorPos, outputLanguage.getDisplay(), testType.getType()));
      }
      assertNoUnexpectedAlerts();
    }
  }
}
