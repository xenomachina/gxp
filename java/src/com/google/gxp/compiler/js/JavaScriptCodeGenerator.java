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

import com.google.common.base.Function;
import com.google.common.base.Join;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.NothingToCompileError;
import com.google.gxp.compiler.base.AbbrExpression;
import com.google.gxp.compiler.base.BooleanConstant;
import com.google.gxp.compiler.base.BoundCall;
import com.google.gxp.compiler.base.Call;
import com.google.gxp.compiler.base.CallVisitor;
import com.google.gxp.compiler.base.Concatenation;
import com.google.gxp.compiler.base.Conditional;
import com.google.gxp.compiler.base.ConvertibleToContent;
import com.google.gxp.compiler.base.DefaultingExpressionVisitor;
import com.google.gxp.compiler.base.EscapeExpression;
import com.google.gxp.compiler.base.ExampleExpression;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.ExpressionVisitor;
import com.google.gxp.compiler.base.ExtractedMessage;
import com.google.gxp.compiler.base.Interface;
import com.google.gxp.compiler.base.IsXmlExpression;
import com.google.gxp.compiler.base.LoopExpression;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.NullRoot;
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
import com.google.gxp.compiler.schema.Schema;
import com.google.transconsole.common.messages.Message;

import java.util.Iterator;

/**
 * {@code CodeGenerator} which generates JavaScript code.
 */
public class JavaScriptCodeGenerator extends BracesCodeGenerator<MessageExtractedTree> {
  public JavaScriptCodeGenerator(MessageExtractedTree tree) {
    super(tree);
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
        createTemplateWorker(appendable, alertSink, template).run();
        return null;
      }
    });
  }

  private TemplateWorker createTemplateWorker(Appendable appendable,
                                              AlertSink alertSink,
                                              Template template) {
    return new TemplateWorker(appendable, alertSink, template);
  }

  private static class TemplateWorker extends BracesCodeGenerator.Worker {
    private final Template template;
    private int varCounter = 0;

    public TemplateWorker(Appendable appendable, AlertSink alertSink, Template template) {
      super(appendable, alertSink);
      this.template = Objects.nonNull(template);
    }

    public void run() {
      appendHeader(template);
      appendLine();
      formatLine("goog.provide('%s');", getClassName(template.getName()));
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
      appendLine();
      appendFooter();
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
      appendLine("return {");

      formatLine("%s : function(%s) {", getWriteMethodName(template.getSchema()), GXP_SIG);
      StringBuilder sb = new StringBuilder();
      sb.append(isStatic ? getClassName(template.getName()) : selfVar);
      sb.append(".write(");
      Join.join(sb, ", ", Iterables.concat(
                    ImmutableSet.of(GXP_SIG),
                    Iterables.transform(params, parameterToName)));
      sb.append(");");
      appendLine(sb);
      appendLine("}");
      appendLine("};");
      appendLine("};");
    }

    private void appendStaticWriteMethod() {
      appendLine(getWriteMethodSignature(true));
      template.getContent().acceptVisitor(statementVisitor);
      appendLine("};");
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
      return "write" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
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

    ////////////////////////////////////////////////////////////////////////////////
    // Visitors
    ////////////////////////////////////////////////////////////////////////////////

    private final StatementVisitor statementVisitor = getStatementVisitor();
    private final ExpressionVisitor<String> toExpressionVisitor =
        new ToExpressionVisitor();

    protected StatementVisitor getStatementVisitor() {
      return new StatementVisitor();
    }

    /**
     * A visitor that outputs statements to {@code out} based on the nodes that
     * it visits.
     */
    protected class StatementVisitor extends DefaultingExpressionVisitor<Void>
        implements CallVisitor<Void> {

      @Override
      public Void defaultVisitExpression(Expression node) {
        // TODO(harryh): put this back when we fill in the other
        //               expected expressions
        // throw new UnexpectedNodeException(node);
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
                   prefix + getJavaScriptExpression(predicate) + ") {");
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
        Expression delimiter = loop.getDelimiter();
        String boolVar = createVarName("bool");
        if (!delimiter.alwaysEmpty()) {
          formatLine("var %s = false;", boolVar);
        }
        if (loop.getIterator() != null) {
          // TODO(harryh): what shall we do here?
        } else {
          formatLine(loop.getSourcePosition(), "for (%s in %s) {",
                     JavaScriptUtil.validateName(alertSink, loop, loop.getVar()),
                     getJavaScriptExpression(loop.getIterable()));
          writeConditionalDelim(delimiter, boolVar);
          loop.getSubexpression().acceptVisitor(this);
          appendLine("}");
        }
        return null;
      }

      @Override
      public Void visitAbbrExpression(AbbrExpression abbr) {
        appendLine("{");
        formatLine(abbr.getSourcePosition(),
                   "var %s = %s;",
                   JavaScriptUtil.validateName(alertSink, abbr, abbr.getName()),
                   getJavaScriptExpression(abbr.getValue()));
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
        return null;
      }

      @Override
      public Void visitExtractedMessage(ExtractedMessage msg) {
        Message tcMessage = msg.getTcMessage();
        return null;
      }
    }

    protected final String getJavaScriptExpression(Expression value) {
      return value.acceptVisitor(toExpressionVisitor);
    }

    private class ToExpressionVisitor
        extends DefaultingExpressionVisitor<String>
        implements CallVisitor<String> {

      @Override
      public String defaultVisitExpression(Expression value) {
        // TODO(harryh): uncomment this when other expected expression functions
        //               are filled in
        // throw new UnexpectedNodeException(value);
        return "TODO";
      }

      @Override
      public String visitBooleanConstant(BooleanConstant value) {
        return value.getValue().toString();
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
        JavaScriptUtil.validateExpression(alertSink, value);
        return value.getNativeCode();
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
        return "TODO";
      }

      @Override
      public String visitIsXmlExpression(IsXmlExpression ixe) {
        return GXP_CONTEXT_VAR + ".isUsingXmlSyntax()";
      }
    }
  }
}
