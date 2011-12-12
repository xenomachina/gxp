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

package com.google.gxp.compiler.java;

import static com.google.gxp.compiler.base.OutputLanguage.JAVA;

import com.google.common.base.CharEscapers;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.AbbrExpression;
import com.google.gxp.compiler.base.AttrBundleParam;
import com.google.gxp.compiler.base.AttrBundleReference;
import com.google.gxp.compiler.base.BooleanConstant;
import com.google.gxp.compiler.base.BoundCall;
import com.google.gxp.compiler.base.Call;
import com.google.gxp.compiler.base.CallVisitor;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.CallableVisitor;
import com.google.gxp.compiler.base.Concatenation;
import com.google.gxp.compiler.base.Conditional;
import com.google.gxp.compiler.base.ConstructedConstant;
import com.google.gxp.compiler.base.ConvertibleToContent;
import com.google.gxp.compiler.base.DefaultingExpressionVisitor;
import com.google.gxp.compiler.base.EscapeExpression;
import com.google.gxp.compiler.base.ExampleExpression;
import com.google.gxp.compiler.base.ExceptionExpression;
import com.google.gxp.compiler.base.ExhaustiveExpressionVisitor;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.ExpressionVisitor;
import com.google.gxp.compiler.base.ExtractedMessage;
import com.google.gxp.compiler.base.FormalParameter;
import com.google.gxp.compiler.base.Implementable;
import com.google.gxp.compiler.base.InstanceCallable;
import com.google.gxp.compiler.base.IsXmlExpression;
import com.google.gxp.compiler.base.JavaAnnotation;
import com.google.gxp.compiler.base.LoopExpression;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.ObjectConstant;
import com.google.gxp.compiler.base.OutputLanguage;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.ThrowsDeclaration;
import com.google.gxp.compiler.base.UnboundCall;
import com.google.gxp.compiler.base.UnexpectedNodeException;
import com.google.gxp.compiler.base.ValidatedCall;
import com.google.gxp.compiler.codegen.DuplicateMessageNameError;
import com.google.gxp.compiler.codegen.LoopMissingBothIterableAndIteratorError;
import com.google.gxp.compiler.codegen.MissingExpressionError;
import com.google.gxp.compiler.codegen.NoMessageSourceError;
import com.google.gxp.compiler.msgextract.MessageExtractedTree;
import com.google.gxp.compiler.reparent.Attribute;
import com.google.gxp.compiler.schema.AttributeValidator;
import com.google.gxp.compiler.schema.ContentFamilyVisitor;
import com.google.gxp.compiler.schema.Schema;
import com.google.transconsole.common.messages.Message;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@code CodeGenerator} that generates Java code.
 */
public class JavaCodeGenerator extends BaseJavaCodeGenerator<MessageExtractedTree> {

  /**
   * @param tree the MessageExtractedTree to compile.
   * @param runtimeMessageSource the message source to use at runtime, or null
   * if none was provided. This is a (typically dotted) prefix used when
   * loading message resources at runtime.
   */
  public JavaCodeGenerator(MessageExtractedTree tree, String runtimeMessageSource) {
    super(tree, runtimeMessageSource);
  }

  @Override
  protected TemplateWorker createTemplateWorker(Appendable appendable,
                                                AlertSink alertSink,
                                                Template template,
                                                String runtimeMessageSource) {
    return new TemplateWorker(appendable, alertSink, template, runtimeMessageSource);
  }

  /**
   * Helper class which exists mainly so we don't have to pass the CIndenter
   * and AlertSink everywhere manually.
   */
  protected static class TemplateWorker extends BaseJavaCodeGenerator.TemplateWorker {
    private int varCounter = 0;
    private final String runtimeMessageSource;

    protected final Set<Schema> anonymousSchemas = Sets.newTreeSet();

    TemplateWorker(Appendable appendable, AlertSink alertSink,
                   Template template, String runtimeMessageSource) {
      super(appendable, alertSink, template);
      this.runtimeMessageSource = runtimeMessageSource;
    }

    public TemplateWorker createSubWorker(Appendable newAppendable) {
      return new TemplateWorker(newAppendable, alertSink, template, runtimeMessageSource);
    }

    /**
     * Creates a unique (to this Worker) variable name.
     *
     * @param token string which is included in variable name. This can be used
     * to (slightly) increase readability of the generated code.
     */
    protected final String createVarName(String token) {
      return "gxp$" + token + "$" + varCounter++;
    }

    @Override
    protected String getBaseClassName() {
      return "com.google.gxp.base.GxpTemplate";
    }

    @Override
    protected void appendClass() {
      appendAnnotations(template.getJavaAnnotations(JavaAnnotation.Element.CLASS));
      appendClassDecl();
      appendLine();
      if (runtimeMessageSource != null) {
        formatLine("private static final String GXP$MESSAGE_SOURCE = %s;",
                   JAVA.toStringLiteral(runtimeMessageSource));
        appendNamedMessageDefinitions();
        appendLine();
      }
      appendWriteMethod();
      appendDefaultAccessors();
      appendParamConstructors();
      appendAnonymousInterfaces();
      appendGetArgListMethod();
      appendGetGxpClosureMethod(true);
      appendInterface();
      appendInstance();
      appendLine("}");
    }

    protected void appendClassDecl() {
      formatLine(template.getSourcePosition(), "public class %s extends %s {",
                 getClassName(template.getName()), getBaseClassName());
    }

    private void appendNamedMessageDefinitions() {
      template.getContent().acceptVisitor(new DuplicateMessageNameVisitor());
      template.getContent().acceptVisitor(new NamedExtractedMessageVisitor());
    }

    private String getAnonymousJavaType(Schema schema) {
      return "Anonymous" + getBaseName(schema.getJavaType());
    }

    /**
     * Generates private abstract static classes for all the anonymous closures
     * needed by this template
     */
    private void appendAnonymousInterfaces() {
      for (Schema schema : anonymousSchemas) {
        appendLine();
        appendLine("private abstract static class " + getAnonymousJavaType(schema));
        appendLine("    extends GxpTemplate.AnonymousGxpClosure");
        appendLine("    implements " + schema.getJavaType() + " {");
        appendLine("}");
      }
    }

    /**
     * Generates private constants for default values and accessor methods for
     * retrieving them.
     */
    private void appendDefaultAccessors() {
      for (Parameter param : template.getAllParameters()) {
        Expression defaultValue = param.getDefaultValue();
        if (defaultValue != null) {
          String paramType = toJavaType(param.getType());
          appendLine();
          formatLine(param.getDefaultValue().getSourcePosition(),
                     "private static final %s GXP_DEFAULT$%s = %s;",
                     paramType, param.getPrimaryName(),
                     getJavaExpression(param.getDefaultValue()));

          appendLine();
          String methodName = getDefaultMethodName(param);
          formatLine("public static %s %s() {", paramType, methodName);
          formatLine("return GXP_DEFAULT$%s;", param.getPrimaryName());
          appendLine("}");
        }
      }
    }

    /**
     * Generates methods for constructing object parameters from strings.
     */
    private void appendParamConstructors() {
      for (Parameter param : template.getParameters()) {
        if (param.getConstructor() != null) {
          appendLine();
          formatLine(param.getSourcePosition(),
                     "public static %s %s(String %s) {",
                     toJavaType(param.getType()),
                     getConstructorMethodName(param),
                     param.getPrimaryName());
          appendLine(param.getSourcePosition(), "return "
                     + param.getConstructor().acceptVisitor(toExpressionVisitor) + ";");
          appendLine("}");
        }
      }
    }

    private List<String> getCallArguments(Callable callee, Map<String, Attribute> callerAttrs) {
      List<String> fParams = Lists.newArrayList();
      String calleeName = getCalleeName(callee);
      for (FormalParameter parameter : callee.getParameters()) {
        String paramName = parameter.getPrimaryName();
        if (!Implementable.INSTANCE_PARAM_NAME.equals(paramName)) {
          Attribute value = callerAttrs.get(paramName);

          String defaultFetchString = (parameter.getType().getDefaultValue() == null)
              ? calleeName + "." + getDefaultMethodName(parameter) + "()"
              : getJavaExpression(parameter.getType().getDefaultValue());

          if (value == null) {
            fParams.add(defaultFetchString);
          } else if (value.getCondition() != null) {
            String s = "(" +
                getJavaExpression(value.getCondition()) +
                " ? " + getJavaExpression(value.getValue()) + " : " +
                defaultFetchString + ")";
            fParams.add(s);
          } else {
            fParams.add(getJavaExpression(value.getValue()));
          }
        }
      }
      return fParams;
    }

    @Override
    protected void appendWriteMethodBody() {
      appendLine("final java.util.Locale gxp_locale = gxp_context.getLocale();");
      List<ThrowsDeclaration> throwsDecls = template.getThrowsDeclarations();
      if (!throwsDecls.isEmpty()) {
        appendLine("try {");
      }

      template.getContent().acceptVisitor(statementVisitor);

      if (!throwsDecls.isEmpty()) {
        String runtimeExceptionVar = createVarName("runtimeException");
        String causeVar = createVarName("cause");
        formatLine("} catch (com.google.gxp.base.GxpRuntimeException %s) {", runtimeExceptionVar);
        formatLine("final java.lang.Throwable %s = %s.getCause();", causeVar, runtimeExceptionVar);
        for (ThrowsDeclaration throwsDecl : throwsDecls) {
          String excType = throwsDecl.getExceptionType();
          formatLine(throwsDecl.getSourcePosition(),
                     "if (%s instanceof %s) throw (%s) %s;",
                     causeVar, excType, excType, causeVar);
        }
        formatLine("throw %s;", runtimeExceptionVar);
        appendLine("}");
      }
    }

    protected void writeExpression(SourcePosition sourcePosition, String expr) {
      appendLine(sourcePosition, GXP_OUT_VAR + ".append(" + expr + ");");
    }

    private static final int MAX_JAVA_STRING_LENGTH = 65534;

    // TODO(danignatoff): something else does this too. Merge.
    protected void writeString(SourcePosition pos, String s) {
      int length = s.length();
      if (length != 0) {
        int curPos = 0;
        while (length - curPos > MAX_JAVA_STRING_LENGTH) {
          writeExpression(pos, JAVA.toStringLiteral(
                              s.substring(curPos, curPos + MAX_JAVA_STRING_LENGTH)));
          curPos += MAX_JAVA_STRING_LENGTH;
        }
        writeExpression(pos, JAVA.toStringLiteral(s.substring(curPos, length)));
      }
    }

    private final StatementVisitor statementVisitor = getStatementVisitor();
    private final ExpressionVisitor<String> toExpressionVisitor =
        new ToExpressionVisitor();

    private final ExpressionVisitor<String> toEscapableExpressionVisitor =
        getToEscapableExpressionVisitor();

    protected StatementVisitor getStatementVisitor() {
      return new StatementVisitor();
    }

    protected final Deque<String> instantiatedGxps = new ArrayDeque<String>();

    /**
     * @return either the fully qualified static name of the GXP if we're
     * doing a static call, or the variable name of the instantiated GXP
     * if we're doing an instance call.
     */
    protected String getCalleeName(Callable callee) {
      return callee.acceptCallableVisitor(new CallableVisitor<String>() {
        public String visitCallable(Callable callable) {
          return callable.getName().toString();
        }

        public String visitInstanceCallable(InstanceCallable callable) {
          return instantiatedGxps.peek();
        }
      });
    }

    /**
     * A visitor that creates Alerts when it encounters a duplicate Java message name.
     */
    private class DuplicateMessageNameVisitor extends ExhaustiveExpressionVisitor {

      private Set<String> names = Sets.newHashSet();

      @Override
      public Expression visitExtractedMessage(ExtractedMessage msg) {
        if (isNamed(msg)) {
          String name = msg.getName(OutputLanguage.JAVA);
          if (names.contains(name)) {
            alertSink.add(new DuplicateMessageNameError(msg, name));
          } else {
            names.add(name);
          }
        }
        return super.visitExtractedMessage(msg);
      }
    }

    /**
     * A visitor that outputs MessageReference definitions to {@code out} based on the named
     * ExtractedMessages that it visits.
     */
    private class NamedExtractedMessageVisitor extends ExhaustiveExpressionVisitor {

      private static final String MESSAGE_REFERENCE_CLASSNAME_PREFIX
          = "com.google.gxp.base.MessageReference";

      @Override
      public Expression visitExtractedMessage(ExtractedMessage msg) {
        if (isNamed(msg)) {
          validateName(msg);
          outputNamedMessage(msg);
        }
        return super.visitExtractedMessage(msg);
      }

      private void validateName(ExtractedMessage msg) {
        JAVA.validateName(alertSink, msg, msg.getName(OutputLanguage.JAVA));
      }

      private void outputNamedMessage(ExtractedMessage msg) {
        // TODO: include msg.getParameters().size() in class name?
        formatLine(msg.getSourcePosition(), "public static final %s %s",
                   MESSAGE_REFERENCE_CLASSNAME_PREFIX,
                   msg.getName(OutputLanguage.JAVA));
        formatLine("    = new %s(GXP$MESSAGE_SOURCE, %sL);",
                   MESSAGE_REFERENCE_CLASSNAME_PREFIX,
                   msg.getTcMessage().getId());
      }
    }

    private boolean isNamed(ExtractedMessage msg) {
      String name = msg.getName(OutputLanguage.JAVA);
      return name != null && name.trim().length() > 0;
    }

    /**
     * A visitor that outputs statements to {@code out} based on the nodes that
     * it visits.
     */
    protected class StatementVisitor extends DefaultingExpressionVisitor<Void>
        implements CallVisitor<Void> {

      @Override
      public Void defaultVisitExpression(Expression node) {
        throw new UnexpectedNodeException(node);
      }

      @Override
      public Void visitExceptionExpression(ExceptionExpression value) {
        String excClass;
        switch (value.getKind()) {
          case NOT_SUPPORTED_IN_SGML_MODE:
            excClass = "java.lang.IllegalStateException";
            break;
          default:
            throw new AssertionError("Unsupported ExceptionExpression.Kind: " + value.getKind());
        }
        appendLine(value.getSourcePosition(),
                   "throw new " + excClass + "("
                   + JAVA.toStringLiteral(value.getMessage()) + ");");
        return null;
      }

      @Override
      public Void visitStringConstant(StringConstant value) {
        if (value.getSchema() == null) {
          throw new AssertionError();
        }
        writeString(value.getSourcePosition(), value.evaluate());
        return null;
      }

      @Override
      public Void visitEscapeExpression(EscapeExpression value) {
        formatLine(value.getSourcePosition(), "%s.INSTANCE.append(%s, %s, %s);",
                   value.getSchema().getJavaAppender(), GXP_OUT_VAR, GXP_CONTEXT_VAR,
                   getEscapableExpression(value.getSubexpression()));
        return null;
      }

      @Override
      public Void visitExampleExpression(ExampleExpression value) {
        return value.getSubexpression().acceptVisitor(this);
      }

      @Override
      public Void visitConvertibleToContent(ConvertibleToContent value) {
        value.getSubexpression().acceptVisitor(this);
        return null;
      }

      @Override
      public Void visitConcatenation(Concatenation value) {
        for (Expression subValue : value.getValues()) {
          subValue.acceptVisitor(this);
        }
        return null;
      }

      @Override
      public Void visitConditional(Conditional value) {
        Iterator<Conditional.Clause> clauses = value.getClauses().iterator();
        if (clauses.hasNext()) {
          appendIf("if (", clauses.next());
          while (clauses.hasNext()) {
            appendIf("} else if (", clauses.next());
          }
          Expression elseExpression = value.getElseExpression();
          if (!elseExpression.alwaysEmpty()) {
            appendLine("} else {");
            elseExpression.acceptVisitor(this);
          }
          appendLine("}");
        } else {
          throw new AssertionError("No clauses in Conditional!");
        }
        return null;
      }

      private void appendIf(String prefix, Conditional.Clause clause) {
        Expression predicate = clause.getPredicate();
        appendLine(predicate.getSourcePosition(),
                   prefix + getJavaExpression(predicate) + ") {");
        clause.getExpression().acceptVisitor(this);
      }

      private void writeConditionalDelim(Expression delimiter,
                                         String boolVar) {
        if (!delimiter.alwaysEmpty()) {
          formatLine("if (%s) {", boolVar);
          delimiter.acceptVisitor(this);
          appendLine("} else {");
          formatLine("%s = true;", boolVar);
          appendLine("}");
        }
      }

      @Override
      public Void visitLoopExpression(LoopExpression loop) {
        // start outer scope for temporary variables
        appendLine("{");
        Expression delimiter = loop.getDelimiter();
        String boolVar = createVarName("bool");
        if (!delimiter.alwaysEmpty()) {
          formatLine("boolean %s = false;", boolVar);
        }
        String tmpKeyVar = null, keyVar = null;
        if (loop.getKey() != null) {
          tmpKeyVar = createVarName("key");
          keyVar = JAVA.validateName(alertSink, loop, loop.getKey());
          formatLine("int %s = 0;", tmpKeyVar);
        }
        if (loop.getIterator() != null && loop.getIterator().canEvaluateAs(JAVA)) {
          String iterVar = createVarName("iter");
          Expression iter = loop.getIterator();
          formatLine(iter.getSourcePosition(),
                     "final java.util.Iterator<? extends %s> %s = %s;",
                     JavaUtil.toReferenceType(toJavaType(loop.getType())),
                     iterVar,
                     getJavaExpression(iter));

          // the loop itself
          formatLine(loop.getSourcePosition(),
                     "while (%s.hasNext()) {",
                     iterVar);
          String itemExpr = JavaUtil.unbox(iterVar + ".next()",
                                           toJavaType(loop.getType()));
          formatLine(loop.getType().getSourcePosition(),
                     "final %s %s = %s;",
                     toJavaType(loop.getType()),
                     JAVA.validateName(alertSink, loop, loop.getVar()),
                     itemExpr);
          writeConditionalDelim(delimiter, boolVar);
          if (keyVar != null) {
            formatLine("final int %s = %s++;", keyVar, tmpKeyVar);
          }
          loop.getSubexpression().acceptVisitor(this);
          appendLine("}");
        } else if (loop.getIterable() != null && loop.getIterable().canEvaluateAs(JAVA)) {
          formatLine(loop.getSourcePosition(), "for (final %s %s : %s) {",
                     toJavaType(loop.getType()),
                     JAVA.validateName(alertSink, loop, loop.getVar()),
                     getJavaExpression(loop.getIterable()));
          writeConditionalDelim(delimiter, boolVar);
          if (keyVar != null) {
            formatLine("final int %s = %s++;", keyVar, tmpKeyVar);
          }
          loop.getSubexpression().acceptVisitor(this);
          appendLine("}");
        } else {
          // if we only have an expression for 1 of iterable/iterator then we
          // add a MissingExpressionAlert
          if (loop.getIterable() == null && loop.getIterator() != null) {
            alertSink.add(new MissingExpressionError(loop.getIterator(), JAVA));
          } else if (loop.getIterator() == null && loop.getIterable() != null) {
            alertSink.add(new MissingExpressionError(loop.getIterable(), JAVA));
          } else {
            alertSink.add(new LoopMissingBothIterableAndIteratorError(loop, JAVA));
          }
        }
        // close outer scope
        appendLine("}");
        return null;
      }

      @Override
      public Void visitAbbrExpression(AbbrExpression abbr) {
        appendLine("{");
        formatLine(abbr.getSourcePosition(),
                   "final %s %s = %s;",
                   toJavaType(abbr.getType()),
                   JAVA.validateName(alertSink, abbr, abbr.getName()),
                   getJavaExpression(abbr.getValue()));
        abbr.getContent().acceptVisitor(this);
        appendLine("}");
        return null;
      }

      @Override
      public Void visitCall(Call value) {
        return value.acceptCallVisitor(this);
      }

      public Void visitUnboundCall(UnboundCall call) {
        throw new UnexpectedNodeException(call);
      }

      public Void visitBoundCall(BoundCall call) {
        throw new UnexpectedNodeException(call);
      }

      public Void visitValidatedCall(final ValidatedCall call) {
        final Callable callee = call.getCallee();
        final Map<String, Attribute> params = call.getAttributes();
        final StringBuilder sb = new StringBuilder();

        boolean isInstance = callee.acceptCallableVisitor(new CallableVisitor<Boolean>() {
          public Boolean visitCallable(Callable callable) {
            return false;
          }

          public Boolean visitInstanceCallable(InstanceCallable callable) {
            instantiatedGxps.push(createVarName("inst"));
            Attribute thisAttr = params.get(Implementable.INSTANCE_PARAM_NAME);

            appendLine("{");
            if (thisAttr != null) {
              formatLine(call.getSourcePosition(), "%s %s = %s;",
                         toJavaType(callable.getInstanceType()),
                         instantiatedGxps.peek(),
                         thisAttr.getValue().acceptVisitor(toExpressionVisitor));
            }
            return true;
          }
        });

        sb.append(getCalleeName(callee));
        sb.append(".write(");
        sb.append(GXP_OUT_VAR);
        sb.append(", ");
        sb.append(GXP_CONTEXT_VAR);
        for (String param : getCallArguments(callee, params)) {
          sb.append(", ");
          sb.append(param);
        }
        sb.append(");");
        appendLine(call.getSourcePosition(), sb);

        if (isInstance) {
          instantiatedGxps.pop();
          appendLine("}");
        }

        // TODO(laurence): use mangled method name for greater safety?

        return null;
      }

      @Override
      public Void visitExtractedMessage(ExtractedMessage msg) {
        if (runtimeMessageSource == null) {
          alertSink.add(new NoMessageSourceError(msg));
        }
        Message tcMessage = msg.getTcMessage();
        formatLine("// MSG %s=%s",
                   tcMessage.getId(),
                   CharEscapers.javaStringUnicodeEscaper().escape(
                       tcMessage.getOriginal().replace("\n", " ")));

        StringBuilder sb = new StringBuilder("GxpTemplate.getMessage(GXP$MESSAGE_SOURCE, ");
        sb.append("gxp_context.getLocale(), ");
        sb.append(tcMessage.getId());
        sb.append("L");
        for (Expression param : msg.getParameters()) {
          sb.append(", ");
          sb.append(getEscapedString(param));
        }
        sb.append(")");
        String getMessage = sb.toString();

        writeExpression(msg.getSourcePosition(), getMessage);
        return null;
      }
    }

    protected final String getJavaExpression(Expression value) {
      return value.acceptVisitor(toExpressionVisitor);
    }

    protected final String getEscapedString(Expression value) {
      StringBuilder sb = new StringBuilder(value.getSchema().getJavaAppender());
      sb.append(".INSTANCE.append(new java.lang.StringBuilder(), ");
      sb.append(GXP_CONTEXT_VAR);
      sb.append(", ");
      sb.append(getJavaExpression(value));
      sb.append(").toString()");
      return sb.toString();
    }

    private String getEscapableExpression(Expression value) {
      return value.acceptVisitor(toEscapableExpressionVisitor);
    }

    protected ToEscapableExpressionVisitor getToEscapableExpressionVisitor() {
      return new ToEscapableExpressionVisitor();
    }

    /**
     * Converts an Expression into a Java expression that evaluates to a String
     * containing that Expression's value. This differs from
     * ToExpressionVisitor in that ToExpressionVisitor will sometimes return
     * closure types (eg: HtmlClosure), rather than Strings. Note that this is
     * only ever called on Expressions that are the child of an
     * EscapeExpression, so only types that can appear as the child of an
     * EscapeExpression need to be handled.
     */
    protected class ToEscapableExpressionVisitor
        extends DefaultingExpressionVisitor<String>
        implements CallVisitor<String> {

      @Override
      public String defaultVisitExpression(Expression value) {
        throw new UnexpectedNodeException(value);
      }

      @Override
      public String visitAttrBundleReference(AttrBundleReference value) {
        return value.getName();
      }

      @Override
      public String visitNativeExpression(NativeExpression value) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(JAVA.validateExpression(alertSink, value));
        sb.append(')');
        return sb.toString();
      }

      @Override
      public String visitEscapeExpression(EscapeExpression value) {
        StringBuilder sb = new StringBuilder();
        sb.append(value.getSchema().getJavaAppender());
        sb.append(".INSTANCE.append(new StringBuilder(), ");
        sb.append(GXP_CONTEXT_VAR);
        sb.append(", ");
        sb.append(getEscapableExpression(value.getSubexpression()));
        sb.append(")");

        return sb.toString();
      }

      @Override
      public String visitExtractedMessage(ExtractedMessage msg) {
        if (runtimeMessageSource == null) {
          alertSink.add(new NoMessageSourceError(msg));
        }
        Message tcMessage = msg.getTcMessage();
        StringBuilder sb = new StringBuilder("GxpTemplate.getMessage(GXP$MESSAGE_SOURCE, ");
        sb.append("gxp_context.getLocale(), ");
        sb.append(String.format("/* \"%s\" */ ",
                                CharEscapers.javaStringUnicodeEscaper().escape(
                                    tcMessage.getOriginal()
                                      .replace("\n", " ")
                                      .replace("\\*/", "*/"))));
        sb.append(tcMessage.getId());
        sb.append("L");
        for (Expression param : msg.getParameters()) {
          sb.append(", ");
          sb.append(getEscapedString(param));
        }
        sb.append(")");
        return sb.toString();
      }

      @Override
      public String visitCall(Call call) {
        return call.acceptCallVisitor(this);
      }

      @Override
      public String visitBoundCall(BoundCall call) {
        throw new UnexpectedNodeException(call);
      }

      @Override
      public String visitUnboundCall(UnboundCall call) {
        throw new UnexpectedNodeException(call);
      }

      @Override
      public String visitValidatedCall(ValidatedCall call) {
        StringBuilder sb = new StringBuilder(GXP_CONTEXT_VAR);
        sb.append(".getString(");
        sb.append(getJavaExpression(call));
        sb.append(")");
        return sb.toString();
      }
    }

    private class ToExpressionVisitor
        extends DefaultingExpressionVisitor<String>
        implements CallVisitor<String> {

      @Override
      public String defaultVisitExpression(Expression value) {
        throw new UnexpectedNodeException(value);
      }

      @Override
      public String visitAttrBundleParam(AttrBundleParam bundle) {
        // optimization where a single bundle is being passed without
        // restrictions to another.  Just pass the bundle along without
        // using a GxpAttrBundle.Builder
        if (bundle.getIncludeAttrs().isEmpty() && bundle.getAttrs().isEmpty()
            && bundle.getSubBundles().size() == 1) {
          return bundle.getSubBundles().get(0);
        }

        StringBuilder sb = new StringBuilder("new GxpAttrBundle.Builder<");
        sb.append(bundle.getSchema().getJavaType());
        sb.append(">(");
        List<String> includeAttrs = Lists.newArrayList();
        for (String includeAttr : bundle.getIncludeAttrs()) {
          includeAttrs.add(JAVA.toStringLiteral(includeAttr));
        }
        COMMA_JOINER.appendTo(sb, includeAttrs);
        sb.append(')');
        for (Map.Entry<AttributeValidator, Attribute> entry : bundle.getAttrs().entrySet()) {
          AttributeValidator validator = entry.getKey();
          Expression condition = entry.getValue().getCondition();
          Expression value = entry.getValue().getValue();

          sb.append(".attr(");
          sb.append(JAVA.toStringLiteral(validator.getName()));
          sb.append(", ");
          sb.append(validator.isFlagSet(AttributeValidator.Flag.BOOLEAN)
                      ? value.acceptVisitor(this)
                      : toAnonymousClosure(value));
          if (condition != null) {
            sb.append(", ");
            sb.append(condition.acceptVisitor(this));
          }
          sb.append(")");
        }
        for (String subBundle : bundle.getSubBundles()) {
          sb.append(".addBundle(");
          sb.append(subBundle);
          sb.append(")");
        }
        sb.append(".build()");
        return sb.toString();
      }

      @Override
      public String visitBooleanConstant(BooleanConstant value) {
        return value.getValue().toString();
      }

      @Override
      public String visitStringConstant(StringConstant value) {
        if (value.getSchema() == null) {
          throw new AssertionError();
        }
        return value.getSchema().getContentFamily().acceptVisitor(
            STRING_CONSTANT_VISITOR, value);
      }

      private ContentFamilyVisitor<StringConstant, String>
        STRING_CONSTANT_VISITOR =
        new ContentFamilyVisitor<StringConstant, String>() {
          public String visitCss(StringConstant value) {
            return toAnonymousClosure(value);
          }

          public String visitJavaScript(StringConstant value) {
            return toAnonymousClosure(value);
          }

          public String visitMarkup(StringConstant value) {
            return toAnonymousClosure(value);
          }

          public String visitPlaintext(StringConstant value) {
            String s = CharEscapers.javaStringEscaper().escape(
                value.evaluate());
            return "\"" + s + "\"";
          }
        };

      // TODO(harryh): validate the value when it is a primitive type
      @Override
      public String visitObjectConstant(ObjectConstant node) {
        String value = node.getValue();
        String type = toJavaType(node.getType());
        if (!JavaUtil.isPrimitiveType(type)) {
          value = type + ".valueOf(\""
              + CharEscapers.javaStringEscaper().escape(value) + "\")";
        } else {
          if (!JavaUtil.isValidPrimitive(value, type)) {
            alertSink.add(new IllegalJavaPrimitiveError(node, value, type));
          }
        }

        return value;
      }

      @Override
      public String visitConstructedConstant(ConstructedConstant node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getCalleeName(node.getCallee()));
        sb.append(".");
        sb.append(getConstructorMethodName(node.getParam()));
        sb.append("(\"");
        sb.append(node.getValue());
        sb.append("\")");

        return sb.toString();
      }

      @Override
      public String visitEscapeExpression(EscapeExpression value) {
        // TODO(laurence): perhaps we shouldn't be generating these
        // EscapeExpressions at all?
        return value.getSubexpression().acceptVisitor(this);
      }

      @Override
      public String visitExampleExpression(ExampleExpression value) {
        return value.getSubexpression().acceptVisitor(this);
      }

      @Override
      public String visitNativeExpression(NativeExpression value) {
        return JAVA.validateExpression(alertSink, value);
      }

      @Override
      public String visitConvertibleToContent(ConvertibleToContent value) {
        return toAnonymousClosure(value);
      }

      @Override
      public String visitConcatenation(Concatenation value) {
        return toAnonymousClosure(value);
      }

      @Override
      public String visitConditional(Conditional value) {
        // TODO(laurence): use "?:" operator instead?
        return toAnonymousClosure(value);
      }

      @Override
      public String visitLoopExpression(LoopExpression value) {
        return toAnonymousClosure(value);
      }

      @Override
      public String visitCall(Call value) {
        return value.acceptCallVisitor(this);
      }

      public String visitBoundCall(BoundCall call) {
        throw new UnexpectedNodeException(call);
      }

      public String visitUnboundCall(UnboundCall call) {
        throw new UnexpectedNodeException(call);
      }

      public String visitValidatedCall(final ValidatedCall call) {
        final Callable callee = call.getCallee();
        final StringBuilder sb = new StringBuilder();

        callee.acceptCallableVisitor(new CallableVisitor<Void>() {
          public Void visitCallable(Callable callable) {
            sb.append(callee.getName().toString());
            sb.append(".getGxpClosure(");
            COMMA_JOINER.appendTo(sb, getCallArguments(callee, call.getAttributes()));
            sb.append(")");
            return null;
          }

          public Void visitInstanceCallable(InstanceCallable callable) {
            // we can't use getGxpClosure in the instance call case, because we have to
            // return a single expression, so we can't set an inst variable like we
            // do in StatementVisitor#visitValidatedCall
            sb.append(toAnonymousClosure(call));
            return null;
          }
        });

        return sb.toString();
      }

      @Override
      public String visitAbbrExpression(AbbrExpression value) {
        return toAnonymousClosure(value);
      }

      @Override
      public String visitExtractedMessage(ExtractedMessage msg) {
        return toAnonymousClosure(msg);
      }

      @Override
      public String visitIsXmlExpression(IsXmlExpression ixe) {
        return "gxp_context.isForcingXmlSyntax()";
      }
    }

    private String toAnonymousClosure(Expression value) {
      StringBuilder sb = new StringBuilder();

      TemplateWorker subWorker = createSubWorker(sb);
      subWorker.toAnonymousClosureImpl(value);
      anonymousSchemas.addAll(subWorker.anonymousSchemas);

      // XXX We do this here to avoid the newline appended by CIndenter.append.
      sb.append("}");
      return sb.toString();
    }

    private void toAnonymousClosureImpl(Expression value) {
      // add the schema of this value to the Set of schemas we need
      // interface definitions for
      anonymousSchemas.add(value.getSchema());

      appendLine(value.getSourcePosition(),
                 "new " + getAnonymousJavaType(value.getSchema()) + "() {");

      StringBuilder sb = new StringBuilder();
      appendJavaThrowsDeclaration(sb, template.getThrowsDeclarations());
      appendLine("@Override");
      formatLine(value.getSourcePosition(),
                 "protected void writeImpl(%s) %s {",
                 GXP_SIG, sb.toString());

      // Fill in writeImpl method.
      value.acceptVisitor(statementVisitor);

      // End writeImpl method.
      appendLine("}");
    }
  }
}
