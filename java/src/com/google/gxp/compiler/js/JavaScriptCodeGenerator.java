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

package com.google.gxp.compiler.js;

import com.google.common.base.CharEscapers;
import com.google.common.base.Function;
import com.google.common.base.Join;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.NothingToCompileError;
import com.google.gxp.compiler.base.AbbrExpression;
import com.google.gxp.compiler.base.AttrBundleParam;
import com.google.gxp.compiler.base.AttrBundleReference;
import com.google.gxp.compiler.base.BooleanConstant;
import com.google.gxp.compiler.base.BoundCall;
import com.google.gxp.compiler.base.Call;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.CallableVisitor;
import com.google.gxp.compiler.base.CallVisitor;
import com.google.gxp.compiler.base.Concatenation;
import com.google.gxp.compiler.base.Conditional;
import com.google.gxp.compiler.base.ConstructedConstant;
import com.google.gxp.compiler.base.ConvertibleToContent;
import com.google.gxp.compiler.base.DefaultingExpressionVisitor;
import com.google.gxp.compiler.base.EscapeExpression;
import com.google.gxp.compiler.base.ExampleExpression;
import com.google.gxp.compiler.base.ExceptionExpression;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.ExpressionVisitor;
import com.google.gxp.compiler.base.ExtractedMessage;
import com.google.gxp.compiler.base.FormalParameter;
import com.google.gxp.compiler.base.Implementable;
import com.google.gxp.compiler.base.InstanceCallable;
import com.google.gxp.compiler.base.Interface;
import com.google.gxp.compiler.base.IsXmlExpression;
import com.google.gxp.compiler.base.LoopExpression;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.NullRoot;
import com.google.gxp.compiler.base.ObjectConstant;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.RootVisitor;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.base.UnboundCall;
import com.google.gxp.compiler.base.UnexpectedNodeException;
import com.google.gxp.compiler.base.ValidatedCall;
import com.google.gxp.compiler.codegen.BracesCodeGenerator;
import com.google.gxp.compiler.msgextract.MessageExtractedTree;
import com.google.gxp.compiler.reparent.Attribute;
import com.google.gxp.compiler.schema.AttributeValidator;
import com.google.gxp.compiler.schema.Schema;
import com.google.transconsole.common.messages.MessageFragment;
import com.google.transconsole.common.messages.Placeholder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code CodeGenerator} which generates JavaScript code.
 */
public class JavaScriptCodeGenerator extends BracesCodeGenerator<MessageExtractedTree> {
  private final ImmutableSet<TemplateName> extraRequires;

  public JavaScriptCodeGenerator(MessageExtractedTree tree, Set<Callable> requirements) {
    super(tree);

    // In JavaScript we need to goog.require anything we depend on, so build
    // up a list of what we need.
    this.extraRequires = ImmutableSet.copyOf(
        Iterables.transform(requirements,
                            new Function<Callable, TemplateName>() {
                              public TemplateName apply(Callable requirement) {
                                return requirement.getName();
                              }
                            }));
  }

  public void generateCode(final Appendable appendable, final AlertSink alertSink) {
    alertSink.addAll(tree.getAlerts());

    root.acceptVisitor(new RootVisitor<Void>() {
      public Void visitInterface(Interface iface) {
        // TODO: what should we even do with interfaces in javascript?
        //       should we add an alert?
        return null;
      }

      public Void visitNullRoot(NullRoot nullRoot) {
        alertSink.add(new NothingToCompileError(nullRoot.getSourcePosition()));
        return null;
      }

      public Void visitTemplate(Template template) {
        createTemplateWorker(appendable, alertSink, template, extraRequires).run();
        return null;
      }
    });
  }

  private TemplateWorker createTemplateWorker(Appendable appendable,
                                              AlertSink alertSink,
                                              Template template,
                                              Set<TemplateName> extraRequires) {
    return new TemplateWorker(appendable, alertSink, template, extraRequires);
  }

  private static class TemplateWorker extends BracesCodeGenerator.Worker {
    private final Template template;
    private final ImmutableSet<TemplateName> extraRequires;
    private int varCounter = 0;

    public TemplateWorker(Appendable appendable, AlertSink alertSink, Template template,
                          Set<TemplateName> extraRequires) {
      super(appendable, alertSink);
      this.template = Preconditions.checkNotNull(template);
      this.extraRequires = ImmutableSet.copyOf(extraRequires);
    }

    public TemplateWorker createSubWorker(Appendable newAppendable) {
      return new TemplateWorker(newAppendable, alertSink, template, extraRequires);
    }

    public void run() {
      appendHeader(template);
      appendLine();
      formatLine("goog.provide('%s');", getClassName(template.getName()));
      appendRequires();
      appendLine();
      appendConstructor();
      appendLine();
      appendWriteMethod();
      appendLine();
      appendGetGxpClosureMethod(false);
      appendLine();
      appendStaticWriteMethod();
      appendLine();
      appendGetGxpClosureMethod(true);
      appendGetDefaultMethods();
      appendLine();
      appendFooter();
    }

    private void appendRequires() {
      for (TemplateName templateName : extraRequires) {
        formatLine("goog.require('%s');", getClassName(templateName));
      }
    }

    private String getWriteMethodSignature(boolean isStatic) {
      Iterable<Parameter> params = isStatic
          ? template.getAllParameters()
          : template.getParameters();

      StringBuilder sb = new StringBuilder();
      sb.append(getClassName(template.getName()));
      if (!isStatic) {
        sb.append(".prototype");
      }
      sb.append(".write = function(");
      Join.join(sb, ", ", Iterables.concat(
                    ImmutableSet.of(GXP_SIG),
                    Iterables.transform(params, parameterToName)));
      sb.append(") {");
      return sb.toString();
    }

    private String getGetGxpClosureMethodSignature(boolean isStatic) {
      Iterable<Parameter> params = isStatic
          ? template.getAllParameters()
          : template.getParameters();

      StringBuilder sb = new StringBuilder();
      sb.append(getClassName(template.getName()));
      if (!isStatic) {
        sb.append(".prototype");
      }
      sb.append(".getGxpClosure = function(");
      Join.join(sb, ", ", Iterables.transform(params, parameterToName));
      sb.append(") {");
      return sb.toString();
    }

    private void appendConstructor() {
      Iterable<Parameter> params = template.getConstructor().getParameters();

      StringBuilder sb = new StringBuilder();
      sb.append(getClassName(template.getName()));
      sb.append(" = function(");
      Join.join(sb, ", ", Iterables.transform(params, parameterToName));
      sb.append(") {");
      appendLine(sb);
      for (Parameter param : params) {
        formatLine("this.%s = %s;", param.getPrimaryName(), param.getPrimaryName());
      }
      appendLine("};");
    }

    private void appendWriteMethod() {
      Iterable<Parameter> ctorParams = template.getConstructor().getParameters();
      Iterable<Parameter> params = template.getParameters();

      // varargs + generics = spurious unchecked warning.  OK to suppress.
      @SuppressWarnings("unchecked")
      Iterable<String> methodParameters = Iterables.concat(
          ImmutableSet.of(GXP_SIG),
          Iterables.transform(ctorParams, parameterToMemberName),
          Iterables.transform(params, parameterToName));

      appendLine(getWriteMethodSignature(false));
      StringBuilder sb = new StringBuilder();
      sb.append(getClassName(template.getName()));
      sb.append(".write(");
      Join.join(sb, ", ", methodParameters);
      sb.append(");");
      appendLine(sb);
      appendLine("};");
    }

    private void appendGetGxpClosureMethod(boolean isStatic) {
      Iterable<Parameter> params = isStatic
          ? template.getAllParameters()
          : template.getParameters();

      appendLine(getGetGxpClosureMethodSignature(isStatic));
      String selfVar = createVarName("self");
      if (!isStatic) {
        formatLine("var %s = this;", selfVar);
      }
      formatLine("return new %s(function(%s) {",
                 template.getSchema().getJavaScriptType(), GXP_SIG);

      StringBuilder sb = new StringBuilder();
      sb.append(isStatic ? getClassName(template.getName()) : selfVar);
      sb.append(".write(");
      Join.join(sb, ", ", Iterables.concat(
                    ImmutableSet.of(GXP_SIG),
                    Iterables.transform(params, parameterToName)));
      sb.append(");");
      appendLine(sb);
      formatLine("return %s;", GXP_OUT_VAR);
      appendLine("});");
      appendLine("};");
    }

    private void appendStaticWriteMethod() {
      appendLine(getWriteMethodSignature(true));
      template.getContent().acceptVisitor(statementVisitor);
      appendLine("};");
    }

    private void appendGetDefaultMethods() {
      for (Parameter param : template.getParameters()) {
        Expression defaultValue = param.getDefaultValue();
        if (defaultValue != null) {
          appendLine();
          formatLine(param.getDefaultValue().getSourcePosition(),
                     "%s.GXP_DEFAULT$%s = %s;",
                     getClassName(template.getName()),
                     param.getPrimaryName(),
                     getJavaScriptExpression(param.getDefaultValue()));

          appendLine();
          String methodName = getDefaultMethodName(param);
          formatLine("%s.%s = function() {", getClassName(template.getName()), methodName);
          formatLine("return %s.GXP_DEFAULT$%s;",
                     getClassName(template.getName()), param.getPrimaryName());
          appendLine("};");
          appendLine();
          formatLine("%s.prototype.%s = function() {",
                     getClassName(template.getName()), methodName);
          formatLine("return %s.%s();", getClassName(template.getName()), methodName);
          appendLine("};");
        }
      }
    }

    private static final String GXP_OUT_VAR = "gxp$out";
    private static final String GXP_CONTEXT_VAR = "gxp_context";
    private static final String GXP_SIG = Join.join(", ", GXP_OUT_VAR, GXP_CONTEXT_VAR);

    /**
     * Creates a unique (to this Worker) variable name.
     *
     * @param token string which is included in variable name. This can be used
     * to (slightly) increase readability of the generated code.
     */
    protected final String createVarName(String token) {
      return "gxp$" + token + "$" + varCounter++;
    }

    private String getClassName(TemplateName name) {
      return name.toString();
    }

    private String getWriteMethodName(Schema schema) {
      String name = schema.getName();
      return "gxp$write" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private Function<Parameter, String> parameterToName =
      new Function<Parameter, String>() {
        public String apply(Parameter param) {
          return param.getPrimaryName();
        }
      };

    private Function<Parameter, String> parameterToMemberName =
      new Function<Parameter, String>() {
        public String apply(Parameter param) {
          return "this." + param.getPrimaryName();
        }
      };

    ////////////////////////////////////////////////////////////////////////////////
    // Utility Functions used by Visitors
    ////////////////////////////////////////////////////////////////////////////////

    protected void writeExpression(SourcePosition pos, String expr) {
      formatLine(pos, "%s.append(%s);", GXP_OUT_VAR, expr);
    }

    // TODO(harryh): find out what this should be
    private static final int MAX_JAVASCRIPT_STRING_LENGTH = 65534;

    protected void writeString(SourcePosition pos, String s) {
      int length = s.length();
      if (length != 0) {
        int curPos = 0;
        while (length - curPos > MAX_JAVASCRIPT_STRING_LENGTH) {
          writeExpression(pos, JavaScriptUtil.toJavaScriptStringLiteral(
                              s.substring(curPos, curPos + MAX_JAVASCRIPT_STRING_LENGTH)));
          curPos += MAX_JAVASCRIPT_STRING_LENGTH;
        }
        writeExpression(pos,
                        JavaScriptUtil.toJavaScriptStringLiteral(s.substring(curPos, length)));
      }
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
          return getClassName(callable.getName());
        }

        public String visitInstanceCallable(InstanceCallable callable) {
          return instantiatedGxps.peek();
        }
      });
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
              : getJavaScriptExpression(parameter.getType().getDefaultValue());

          if (value == null) {
            fParams.add(defaultFetchString);
          } else if (value.getCondition() != null) {
            String s = "(" +
                getJavaScriptExpression(value.getCondition()) +
                " ? " + getJavaScriptExpression(value.getValue()) + " : " +
                defaultFetchString + ")";
            fParams.add(s);
          } else {
            fParams.add(getJavaScriptExpression(value.getValue()));
          }
        }
      }
      return fParams;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Visitors
    ////////////////////////////////////////////////////////////////////////////////

    private final StatementVisitor statementVisitor = getStatementVisitor();

    private final ExpressionVisitor<String> toExpressionVisitor =
        new ToExpressionVisitor();

    private final ExpressionVisitor<String> toEscapableExpressionVisitor =
        getToEscapableExpressionVisitor();

    protected StatementVisitor getStatementVisitor() {
      return new StatementVisitor();
    }

    protected ToEscapableExpressionVisitor getToEscapableExpressionVisitor() {
      return new ToEscapableExpressionVisitor();
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

      ////////////////////////////////////////////////////////////////////////////////
      // Expression Visitors
      ////////////////////////////////////////////////////////////////////////////////

      @Override
      public Void visitAbbrExpression(AbbrExpression abbr) {
        appendLine("(function() {");
        formatLine(abbr.getSourcePosition(),
                   "var %s = %s;",
                   JavaScriptUtil.validateName(alertSink, abbr, abbr.getName()),
                   getJavaScriptExpression(abbr.getValue()));
        abbr.getContent().acceptVisitor(this);
        appendLine("})();");
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
                   prefix + getJavaScriptExpression(predicate) + ") {");
        clause.getExpression().acceptVisitor(this);
      }

      @Override
      public Void visitConvertibleToContent(ConvertibleToContent value) {
        value.getSubexpression().acceptVisitor(this);
        return null;
      }

      @Override
      public Void visitEscapeExpression(EscapeExpression value) {
        formatLine(value.getSourcePosition(), "%s.%s(%s);",
                   getEscapableExpression(value.getSubexpression()),
                   getWriteMethodName(value.getSchema()), GXP_SIG);
        return null;
      }

      @Override
      public Void visitExampleExpression(ExampleExpression value) {
        return value.getSubexpression().acceptVisitor(this);
      }

      @Override
      public Void visitExceptionExpression(ExceptionExpression value) {
        String excClass;
        switch (value.getKind()) {
          case NOT_SUPPORTED_IN_SGML_MODE:
            excClass = "Error";
            break;
          default:
            throw new AssertionError("Unsupported ExceptionExpression.Kind: " + value.getKind());
        }
        appendLine(value.getSourcePosition(),
                   "throw new " + excClass + "("
                   + JavaScriptUtil.toJavaScriptStringLiteral(value.getMessage()) + ");");
        return null;
      }

      @Override
      public Void visitExtractedMessage(ExtractedMessage msg) {
        String paramVar = createVarName("params");
        if (!msg.getParameters().isEmpty()) {
          StringBuilder sb = new StringBuilder("var ");
          sb.append(paramVar);
          sb.append(" = [");
          Join.join(sb, ", ", Iterables.transform(msg.getParameters(),
                                                  expressionToEscapedString));
          sb.append("];");
          appendLine(sb);
        }
        Set<Placeholder> placeholders = Sets.newHashSet();
        String msgVar = "MSG_" + msg.getTcMessage().getId();
        StringBuilder sb = new StringBuilder("var ");
        sb.append(msgVar);
        sb.append(" = goog.getMsg(\"");
        for (MessageFragment fragment : msg.getTcMessage()) {
          if (fragment instanceof Placeholder) {
            placeholders.add((Placeholder) fragment);
            sb.append("{$");
            sb.append(fragment.getPresentation());
            sb.append("}");
          } else {
            sb.append(CharEscapers.JAVASCRIPT_ESCAPER.escape(fragment.getPresentation()));
          }
        }
        sb.append('"');
        Iterator<Placeholder> phIter = placeholders.iterator();
        if (phIter.hasNext()) {
          sb.append(", {");
          appendLine(msg.getSourcePosition(), sb);
          sb = new StringBuilder();
          while(phIter.hasNext()) {
            Placeholder placeholder = phIter.next();
            sb.append(JavaScriptUtil.toJavaScriptStringLiteral(placeholder.getPresentation()));
            sb.append(": ");
            sb.append(evalPlaceholder(placeholder.getOriginal(), paramVar));
            if (phIter.hasNext()) {
              sb.append(",");
            }
            appendLine(msg.getSourcePosition(), sb);
            sb = new StringBuilder();
          }
          sb.append("}");
        }
        sb.append(");");
        appendLine(msg.getSourcePosition(), sb);
        writeExpression(msg.getSourcePosition(), msgVar);
        return null;
      }

      private final Pattern PARAM_PATTERN = Pattern.compile("%([1-9%])");

      private String evalPlaceholder(String original, String paramVar) {
        List<String> parts = Lists.newArrayList();
        Matcher m = PARAM_PATTERN.matcher(original);
        int cur = 0;
        while (m.find(cur)) {
          parts.add(JavaScriptUtil.toJavaScriptStringLiteral(original.substring(cur, m.start())));
          char ch = original.charAt(m.start() + 1);
          if (m.group(1).equals("%")) {
            parts.add("'%'");
          } else {
            parts.add(paramVar + "[" + (Integer.parseInt(m.group(1)) - 1) + "]");
          }
          cur = m.end();
        }
        parts.add(JavaScriptUtil.toJavaScriptStringLiteral(
                      original.substring(cur, original.length())));

        return Join.join("+", parts);
      }

      @Override
      public Void visitLoopExpression(LoopExpression loop) {
        if (loop.getIterator() != null) {
          // TODO(harryh): figure out what to do in this case
          return null;
        }

        appendLine("(function() {");
        Expression delimiter = loop.getDelimiter();
        String boolVar = createVarName("bool");
        if (!delimiter.alwaysEmpty()) {
          formatLine("var %s = false;", boolVar);
        }
        String keyVar = createVarName("key");
        String iterableVar = createVarName("iterable");
        formatLine("%s = %s;", iterableVar, getJavaScriptExpression(loop.getIterable()));
        formatLine(loop.getSourcePosition(), "for (var %s in %s) {", keyVar, iterableVar);
        formatLine("%s = %s[%s];",
                   JavaScriptUtil.validateName(alertSink, loop, loop.getVar()),
                   iterableVar, keyVar);
        writeConditionalDelim(delimiter, boolVar);
        loop.getSubexpression().acceptVisitor(this);
        appendLine("}");
        appendLine("})();");
        return null;
      }

      private void writeConditionalDelim(Expression delimiter, String boolVar) {
        if (!delimiter.alwaysEmpty()) {
          formatLine("if (%s) {", boolVar);
          delimiter.acceptVisitor(this);
          appendLine("} else {");
          formatLine("%s = true;", boolVar);
          appendLine("}");
        }
      }

      // TODO(harrh): delete this when unsafe-eval goes away. This is needed
      //              for unsafe-eval of attribute bundle parameters
      @Override
      public Void visitNativeExpression(NativeExpression value) {
        // TODO
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

      ////////////////////////////////////////////////////////////////////////////////
      // Call Visitors
      ////////////////////////////////////////////////////////////////////////////////

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
              formatLine(call.getSourcePosition(), "%s = %s;",
                         instantiatedGxps.peek(),
                         thisAttr.getValue().acceptVisitor(toExpressionVisitor));
            }
            return true;
          }
        });

        sb.append(getCalleeName(callee));
        sb.append(".write(");
        sb.append(GXP_SIG);
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
        return null;
      }
    }

    protected String getJavaScriptExpression(Expression value) {
      return value.acceptVisitor(toExpressionVisitor);
    }

    protected String getEscapedString(Expression value) {
      StringBuilder sb = new StringBuilder();
      sb.append(getJavaScriptExpression(value));
      sb.append(".");
      sb.append(getWriteMethodName(value.getSchema()));
      sb.append("(new goog.string.StringBuffer(), ");
      sb.append(GXP_CONTEXT_VAR);
      sb.append(").toString()");
      return sb.toString();
    }

    protected Function<Expression, String> expressionToEscapedString =
      new Function<Expression, String>() {
        public String apply(Expression value) {
          return getEscapedString(value);
        }
      };

    private String getEscapableExpression(Expression value) {
      return value.acceptVisitor(toEscapableExpressionVisitor);
    }

    /**
     * Converts an Expression into a JavaScript expression that evaluates to
     * a String containing that Expression's value. This differs from
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

      ////////////////////////////////////////////////////////////////////////////////
      // Expression Visitors
      ////////////////////////////////////////////////////////////////////////////////

      @Override
      public String visitAttrBundleReference(AttrBundleReference value) {
        return value.getName();
      }

      @Override
      public String visitEscapeExpression(EscapeExpression value) {
        StringBuilder sb = new StringBuilder();
        sb.append(getEscapableExpression(value.getSubexpression()));
        sb.append(".");
        sb.append(getWriteMethodName(value.getSchema()));
        sb.append("(new goog.string.StringBuffer(), ");
        sb.append(GXP_CONTEXT_VAR);
        sb.append(").toString()");

        return sb.toString();
      }

      @Override
      public String visitExtractedMessage(ExtractedMessage msg) {
        return getEscapedString(msg);
      }

      @Override
      public String visitNativeExpression(NativeExpression value) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(JavaScriptUtil.validateExpression(alertSink, value));
        sb.append(')');
        return sb.toString();
      }

      ////////////////////////////////////////////////////////////////////////////////
      // Call Visitors
      ////////////////////////////////////////////////////////////////////////////////

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
        return "TODO3";
      }
    }

    private class ToExpressionVisitor
        extends DefaultingExpressionVisitor<String>
        implements CallVisitor<String> {

      @Override
      public String defaultVisitExpression(Expression value) {
        throw new UnexpectedNodeException(value);
      }

      ////////////////////////////////////////////////////////////////////////////////
      // Expression Visitors
      ////////////////////////////////////////////////////////////////////////////////

      @Override
      public String visitAbbrExpression(AbbrExpression value) {
        return toAnonymousClosure(value);
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

        StringBuilder sb = new StringBuilder("new goog.gxp.base.GxpAttrBundle.Builder(");
        sb.append('"');
        sb.append(getWriteMethodName(bundle.getSchema()));
        sb.append('"');
        for (String includeAttr : bundle.getIncludeAttrs()) {
          sb.append(", ");
          sb.append(JavaScriptUtil.toJavaScriptStringLiteral(includeAttr));
        }
        sb.append(")");
        for (Map.Entry<AttributeValidator, Attribute> entry : bundle.getAttrs().entrySet()) {
          AttributeValidator validator = entry.getKey();
          Expression condition = entry.getValue().getCondition();
          Expression value = entry.getValue().getValue();

          sb.append(".attr(");
          sb.append(JavaScriptUtil.toJavaScriptStringLiteral(validator.getName()));
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
      public String visitConcatenation(Concatenation value) {
        return toAnonymousClosure(value);
      }

      @Override
      public String visitConditional(Conditional value) {
        return toAnonymousClosure(value);
      }

      @Override
      public String visitConstructedConstant(ConstructedConstant node) {
        return "TODO5";
      }

      @Override
      public String visitConvertibleToContent(ConvertibleToContent value) {
        return toAnonymousClosure(value);
      }

      @Override
      public String visitEscapeExpression(EscapeExpression value) {
        return value.getSubexpression().acceptVisitor(this);
      }

      @Override
      public String visitExampleExpression(ExampleExpression value) {
        return value.getSubexpression().acceptVisitor(this);
      }

      @Override
      public String visitExtractedMessage(ExtractedMessage msg) {
        return toAnonymousClosure(msg);
      }

      @Override
      public String visitLoopExpression(LoopExpression value) {
        return toAnonymousClosure(value);
      }

      @Override
      public String visitIsXmlExpression(IsXmlExpression ixe) {
        return GXP_CONTEXT_VAR + ".isUsingXmlSyntax()";
      }

      @Override
      public String visitNativeExpression(NativeExpression value) {
        return JavaScriptUtil.validateExpression(alertSink, value);
      }

      @Override
      public String visitObjectConstant(ObjectConstant node) {
        return JavaScriptUtil.toJavaScriptStringLiteral(node.getValue());
      }

      @Override
      public String visitStringConstant(StringConstant value) {
        return "TODO7";
      }

      ////////////////////////////////////////////////////////////////////////////////
      // Call Visitors
      ////////////////////////////////////////////////////////////////////////////////

      @Override
      public String visitCall(Call value) {
        return value.acceptCallVisitor(this);
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
      public String visitValidatedCall(final ValidatedCall call) {
        final Callable callee = call.getCallee();
        final StringBuilder sb = new StringBuilder();

        callee.acceptCallableVisitor(new CallableVisitor<Void>() {
          public Void visitCallable(Callable callable) {
            sb.append(callee.getName().toString());
            sb.append(".getGxpClosure(");
            sb.append(Join.join(", ", getCallArguments(callee, call.getAttributes())));
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
    }

    private String toAnonymousClosure(Expression value) {
      StringBuilder sb = new StringBuilder();
      TemplateWorker subWorker = createSubWorker(sb);
      subWorker.toAnonymousClosureImpl(value);
      sb.append("})");
      return sb.toString();
    }

    private void toAnonymousClosureImpl(Expression value) {
      formatLine(value.getSourcePosition(), "new %s(function(%s) {",
                 value.getSchema().getJavaScriptType(), GXP_SIG);

      value.acceptVisitor(statementVisitor);
      formatLine("return %s;", GXP_OUT_VAR);
    }
  }
}
