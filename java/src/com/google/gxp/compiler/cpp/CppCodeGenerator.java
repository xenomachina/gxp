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

import static com.google.gxp.compiler.base.OutputLanguage.CPP;

import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
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
import com.google.gxp.compiler.base.ExceptionExpression;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.ExpressionVisitor;
import com.google.gxp.compiler.base.ExtractedMessage;
import com.google.gxp.compiler.base.Interface;
import com.google.gxp.compiler.base.IsXmlExpression;
import com.google.gxp.compiler.base.LoopExpression;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.base.UnboundCall;
import com.google.gxp.compiler.base.UnexpectedNodeException;
import com.google.gxp.compiler.base.ValidatedCall;
import com.google.gxp.compiler.msgextract.MessageExtractedTree;

import java.util.Iterator;

/**
 * C++ {@code CodeGenerator}.
 */
public class CppCodeGenerator extends BaseCppCodeGenerator<MessageExtractedTree> {

  public CppCodeGenerator(MessageExtractedTree tree) {
    super(tree);
  }

  protected InterfaceWorker createInterfaceWorker(Appendable out,
                                                  AlertSink alertSink,
                                                  Interface iface) {
    return new InterfaceWorker(out, alertSink, iface);
  }

  private static class InterfaceWorker extends BaseCppCodeGenerator.InterfaceWorker {
    public InterfaceWorker(Appendable out, AlertSink alertSink, Interface iface) {
      super(out, alertSink, iface);
    }

    protected void appendClass() {
    }
  }

  protected TemplateWorker createTemplateWorker(Appendable out,
                                                AlertSink alertSink,
                                                Template template) {
    return new TemplateWorker(out, alertSink, template);
  }

  private static class TemplateWorker extends BaseCppCodeGenerator.TemplateWorker {

    public TemplateWorker(Appendable out, AlertSink alertSink, Template template) {
      super(out, alertSink, template);
    }

    protected void appendClass() {
      appendInclude(template.getSourcePosition(), template.getName());
      appendLine();
      appendWriteMethod();
      appendLine();
      appendGetGxpClosureMethod();
    }

    protected void appendInclude(SourcePosition pos, TemplateName templateName) {
      String filename = templateName.toString().replace('.', '/');
      formatLine(pos, "#include \"%s.h\"", filename);
    }

    protected void appendWriteMethod() {
      appendLine(getWriteMethodSignature(true, true) + " {");
      template.getContent().acceptVisitor(statementVisitor);
      appendLine("}");
    }

    protected void appendGetGxpClosureMethod() {
      appendLine(getGetGxpClosureMethodSignature(true, true) + " {");
      appendLine("return NULL;");
      appendLine("}");
    }

    protected void writeExpression(SourcePosition pos, String expr) {
      appendLine(pos, GXP_OUT_VAR + "->Append(" + expr + ");");
    }

    // TODO(harryh): figure out what this should be
    private static final int MAX_CPP_STRING_LENGTH = 65534;

    protected void writeString(SourcePosition pos, String s) {
      int length = s.length();
      if (length != 0) {
        int curPos = 0;
        while (length - curPos > MAX_CPP_STRING_LENGTH) {
          writeExpression(pos, CPP.toStringLiteral(
                              s.substring(curPos, curPos + MAX_CPP_STRING_LENGTH)));
          curPos += MAX_CPP_STRING_LENGTH;
        }
        writeExpression(pos, CPP.toStringLiteral(s.substring(curPos, length)));
      }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Functions for getting various visitors
    ////////////////////////////////////////////////////////////////////////////////

    private final StatementVisitor statementVisitor = getStatementVisitor();

    private final ExpressionVisitor<String> toExpressionVisitor =
        getToExpressionVisitor();

    private final ExpressionVisitor<String> toEscapableExpressionVisitor =
        getToEscapableExpressionVisitor();

    protected StatementVisitor getStatementVisitor() {
      return new StatementVisitor();
    }

    protected ToExpressionVisitor getToExpressionVisitor() {
      return new ToExpressionVisitor();
    }

    protected ToEscapableExpressionVisitor getToEscapableExpressionVisitor() {
      return new ToEscapableExpressionVisitor();
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Functions for manipulating Expressions
    ////////////////////////////////////////////////////////////////////////////////

    protected String getCppExpression(Expression value) {
      return value.acceptVisitor(toExpressionVisitor);
    }

    protected String getEscapableExpression(Expression value) {
      return value.acceptVisitor(toEscapableExpressionVisitor);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Visitors
    ////////////////////////////////////////////////////////////////////////////////

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
      public Void visitAbbrExpression(AbbrExpression abbr) {
        // TODO(harryh): implement
        return null;
      }

      @Override
      public Void visitCall(Call value) {
        return value.acceptCallVisitor(this);
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
                   prefix + getCppExpression(predicate) + ") {");
        clause.getExpression().acceptVisitor(this);
      }

      @Override
      public Void visitConvertibleToContent(ConvertibleToContent value) {
        value.getSubexpression().acceptVisitor(this);
        return null;
      }

      @Override
      public Void visitEscapeExpression(EscapeExpression value) {
        // TODO(harryh): implement
        return null;
      }

      @Override
      public Void visitExceptionExpression(ExceptionExpression value) {
        // TODO(harryh): implement
        return null;
      }

      @Override
      public Void visitExampleExpression(ExampleExpression value) {
        return value.getSubexpression().acceptVisitor(this);
      }

      @Override
      public Void visitExtractedMessage(ExtractedMessage msg) {
        // TODO(harryh): implement
        return null;
      }

      @Override
      public Void visitLoopExpression(LoopExpression loop) {
        // TODO(harryh): implement
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
      public Void visitBoundCall(BoundCall call) {
        throw new UnexpectedNodeException(call);
      }

      @Override
      public Void visitUnboundCall(UnboundCall call) {
        throw new UnexpectedNodeException(call);
      }

      @Override
      public Void visitValidatedCall(ValidatedCall call) {
        // TODO(harryh): implement
        return null;
      }
    }

    /**
     * Converts an Expression into a C++ expression that evaluates to a String
     * containing that Expression's value. This differs from ToExpressionVisitor
     * in that ToExpressionVisitor will sometimes return closure types
     * (eg: HtmlClosure), rather than Strings.
     *
     * Note that this is only ever called on Expressions that are the child of
     * an EscapeExpression, so only types that can appear as the child of an
     * EscapeExpression need to be handled.
     */
    protected class ToEscapableExpressionVisitor extends DefaultingExpressionVisitor<String> {
      @Override
      public String defaultVisitExpression(Expression value) {
        throw new UnexpectedNodeException(value);
      }

      @Override
      public String visitNativeExpression(NativeExpression value) {
        return "(" + CPP.validateExpression(alertSink, value) + ")";
      }

      @Override
      public String visitEscapeExpression(EscapeExpression value) {
        // TODO(harryh): implement
        return "";
      }

      @Override
      public String visitExtractedMessage(ExtractedMessage msg) {
        // TODO(harryh): implement
        return "";
      }
    }

    /**
     * Converts an Expression into a C++ expression
     */
    protected class ToExpressionVisitor extends DefaultingExpressionVisitor<String> {
      @Override
      public String defaultVisitExpression(Expression value) {
        throw new UnexpectedNodeException(value);
      }

      @Override
      public String visitBooleanConstant(BooleanConstant value) {
        // TODO(harryh): implement
        return "";
      }

      @Override
      public String visitEscapeExpression(EscapeExpression value) {
        return value.getSubexpression().acceptVisitor(this);
      }

      @Override
      public String visitIsXmlExpression(IsXmlExpression ixe) {
        return "gxp_context.IsForcingXmlSyntax()";
      }

      @Override
      public String visitNativeExpression(NativeExpression value) {
        return "(" + CPP.validateExpression(alertSink, value) + ")";
      }
    }
  }
}
