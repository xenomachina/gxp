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

import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.ExtractedMessage;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.msgextract.MessageExtractedTree;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@code CodeGenerator} which generates Java code in dynamic
 * impl mode.
 */
public class DynamicImplJavaCodeGenerator extends JavaCodeGenerator {
  private final long compilationVersion;

  public DynamicImplJavaCodeGenerator(MessageExtractedTree tree,
                                      long compilationVersion) {
    super(tree, null);
    this.compilationVersion = compilationVersion;
  }

  protected TemplateWorker createTemplateWorker(Appendable appendable,
                                                AlertSink alertSink,
                                                Template template,
                                                String runtimeMessageSource) {
    return new TemplateWorker(appendable, alertSink, template,
                              compilationVersion);
  }

  protected static class TemplateWorker
      extends JavaCodeGenerator.TemplateWorker {
    private final long compilationVersion;

    TemplateWorker(Appendable appendable, AlertSink alertSink,
                   Template template, long compilationVersion) {
      super(appendable, alertSink, template, null);
      this.compilationVersion = compilationVersion;
    }

    @Override
    public TemplateWorker createSubWorker(Appendable newAppendable) {
      return new TemplateWorker(newAppendable, alertSink, template,
                                compilationVersion);
    }

    @Override
    protected String getClassName(TemplateName templateName) {
      return super.getClassName(templateName) + "$Impl" + compilationVersion;
    }

    @Override
    protected String getBaseClassName() {
      return "com.google.gxp.base.dynamic.ImplGxpTemplate";
    }

    private Pattern PARAM_PATTERN = Pattern.compile("%[1-9%]");

    @Override
    protected StatementVisitor getStatementVisitor() {
      return new StatementVisitor();
    }

    private class StatementVisitor extends JavaCodeGenerator.TemplateWorker.StatementVisitor {

      @Override
      public Void visitExtractedMessage(ExtractedMessage msg) {
        String org = msg.getTcMessage().getOriginal();
        List<Expression> params = msg.getParameters();
        List<String> paramVars = Lists.newArrayListWithExpectedSize(params.size());

        for (Expression param : params) {
          String varName = createVarName("ph");
          paramVars.add(varName);
          formatLine(param.getSourcePosition(),
                     "String %s = %s.INSTANCE.append(new StringBuilder(), gxp_context, %s)"
                     + ".toString();",
                     varName, msg.getSchema().getJavaAppender(), getJavaExpression(param));
        }

        Matcher m = PARAM_PATTERN.matcher(org);
        int start = 0;
        while (m.find(start)) {
          if (m.start() != start) {
            writeString(msg.getSourcePosition(), org.substring(start, m.start()));
          }
          String s = m.group().substring(1);
          if (s.equals("%")) {
            writeString(msg.getSourcePosition(), "%");
          } else {
            String var = paramVars.get(Integer.parseInt(s) - 1);
            writeExpression(msg.getSourcePosition(), var);
          }
          start = m.end();
        }
        if (org.length() > start) {
          writeString(msg.getSourcePosition(), org.substring(start));
        }

        return null;
      }
    }

    @Override
    protected ToEscapableExpressionVisitor getToEscapableExpressionVisitor() {
      return new ToEscapableExpressionVisitor();
    }

    private class ToEscapableExpressionVisitor
        extends JavaCodeGenerator.TemplateWorker.ToEscapableExpressionVisitor {

      @Override
      public String visitExtractedMessage(ExtractedMessage msg) {
        StringBuilder sb = new StringBuilder("formatGxpMessage(");
        sb.append(JAVA.toStringLiteral(msg.getTcMessage().getOriginal()));
        for (Expression param : msg.getParameters()) {
          sb.append(", ");
          sb.append(msg.getSchema().getJavaAppender());
          sb.append(".INSTANCE.append(new StringBuilder(), gxp_context, ");
          sb.append(getJavaExpression(param));
          sb.append(").toString()");
        }
        sb.append(')');
        return sb.toString();
      }
    }

    @Override
    protected void appendGetArgListMethod() {
      // Impl classes don't need this functionality
    }

    @Override
    protected void appendGetGxpClosureMethod(boolean isStatic) {
      // Impl classes don't need this functionality
    }

    @Override
    protected void appendInterface() {
      // Impl classes don't need this functionality
    }

    @Override
    protected void appendInstance() {
      // Impl classes don't need this functionality
    }
  }
}
