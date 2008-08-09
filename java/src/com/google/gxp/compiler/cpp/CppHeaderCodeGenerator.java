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

import com.google.common.base.Function;
import com.google.common.base.Join;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.Constructor;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.Interface;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.msgextract.MessageExtractedTree;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * C++ Header {@code CodeGenerator}.
 */
public class CppHeaderCodeGenerator extends BaseCppCodeGenerator<MessageExtractedTree> {
  private final ImmutableSet<TemplateName> extraIncludess;

  public CppHeaderCodeGenerator(MessageExtractedTree tree, Set<Callable> requirements) {
    super(tree);

    // unlike java, you have to include the header file for anything you depend on,
    // you can't just fully qualify the class name, so build up a list of extra includes
    this.extraIncludess = ImmutableSet.copyOf(
        Iterables.transform(requirements,
                            new Function<Callable, TemplateName>() {
                              public TemplateName apply(Callable requirement) {
                                return requirement.getName();
                              }
                            }));
  }

  protected InterfaceWorker createInterfaceWorker(Appendable out,
                                                  AlertSink alertSink,
                                                  Interface iface) {
    return new InterfaceWorker(out, alertSink, iface, extraIncludess);
  }

  private static class InterfaceWorker extends BaseCppCodeGenerator.InterfaceWorker {
    private final ImmutableSet<TemplateName> extraIncludess;

    public InterfaceWorker(Appendable out, AlertSink alertSink, Interface iface,
                           Set<TemplateName> extraIncludess) {
      super(out, alertSink, iface);
      this.extraIncludess = ImmutableSet.copyOf(extraIncludess);
    }

    protected void appendClass() {
      TemplateName ifaceName = iface.getName();

      appendIfdefGuardStart(iface);
      appendLine();
      appendLine("#include \"gxp/base/base.h\"");
      appendImports(iface, extraIncludess);
      appendLine();
      appendNamespacesOpen(ifaceName);
      formatLine(iface.getSourcePosition(), "class %s {", getClassName(ifaceName));
      appendLine("public:");
      appendLine("};");
      appendNamespacesClose(ifaceName);
      appendLine();
      appendIfdefGuardEnd(iface);
    }

  }

  protected TemplateWorker createTemplateWorker(Appendable out,
                                                AlertSink alertSink,
                                                Template template) {
    return new TemplateWorker(out, alertSink, template, extraIncludess);
  }

  private static class TemplateWorker extends BaseCppCodeGenerator.TemplateWorker {
    private final ImmutableSet<TemplateName> extraIncludess;

    public TemplateWorker(Appendable out, AlertSink alertSink, Template template,
                          Set<TemplateName> extraIncludess) {
      super(out, alertSink, template);
      this.extraIncludess = ImmutableSet.copyOf(extraIncludess);
    }

    protected void appendClass() {
      SourcePosition pos = template.getSourcePosition();
      TemplateName templateName = template.getName();

      appendIfdefGuardStart(template);
      appendLine();
      appendLine("#include \"gxp/base/base.h\"");
      appendImports(template, extraIncludess);
      appendLine();
      appendNamespacesOpen(templateName);
      formatLine(pos, "class %s : public GxpTemplate {", getClassName(templateName));
      appendLine("public:");
      appendLine(pos, getWriteMethodSignature(false, true) + ";");
      appendLine();
      appendLine(pos, getGetGxpClosureMethodSignature(false, true) + ";");
      appendLine();
      appendExtraWriteMethods(true);
      appendLine();
      appendInterface();
      appendLine();
      appendInstance();
      appendLine("};");
      appendNamespacesClose(templateName);
      appendLine();
      appendIfdefGuardEnd(template);
    }

    protected void appendExtraWriteMethods(boolean isStatic) {
      SourcePosition pos = template.getSourcePosition();
      Iterable<Parameter> params = isStatic
          ? template.getAllParameters()
          : template.getParameters();

      for (ExtraOutType extraOutType : extraOutTypes) {
        appendLine(pos,
                   getWriteMethodSignature(extraOutType.getOutType(), false, isStatic) + " {");
        formatLine(pos, "%s gxp_wrapper(%s);", extraOutType.getOutWrapper(), GXP_OUT_VAR);
        StringBuilder sb = new StringBuilder("Write(");
        Join.join(sb, ", ", Iterables.concat(
            Collections.singleton("&gxp_wrapper, " + GXP_CONTEXT_VAR),
            Iterables.transform(params, paramToCallName)));
        sb.append(");");
        appendLine(pos, sb);
        appendLine("}");
      }
    }

    protected void appendInterface() {
      SourcePosition pos = template.getSourcePosition();

      appendLine("//");
      appendLine("// Interface that defines a strategy for writing this GXP");
      appendLine("//");
      appendCppFormalTypeParameters(true, template.getFormalTypeParameters());
      appendLine("class Interface {");
      appendLine("public:");
      appendLine(pos, "virtual ~Interface() {}");
      appendLine(pos, "virtual " + getWriteMethodSignature(false, false) + " = 0;");
      appendLine(pos, "virtual " + getGetGxpClosureMethodSignature(false, false) + " = 0;");
      appendLine();
      appendExtraWriteMethods(false);
      appendLine("};");
    }

    protected void appendInstance() {
      StringBuilder sb;
      Constructor constructor = template.getConstructor();
      List<Parameter> cParams = constructor.getParameters();
      SourcePosition pos = constructor.getSourcePosition();

      appendLine("//");
      appendLine("// Instantiable instance of this GXP");
      appendLine("//");
      appendCppFormalTypeParameters(true, template.getFormalTypeParameters());
      sb = new StringBuilder("class Instance : public Interface");
      appendCppFormalTypeParameters(sb, false, template.getFormalTypeParameters());
      sb.append(" {");
      appendLine(pos, sb);
      appendLine("public:");
      sb = new StringBuilder("Instance(");
      Join.join(sb, ", ", Iterables.transform(cParams, parameterToCallName));
      sb.append(")");
      if (!cParams.isEmpty()) {
        sb.append("\n  : ");
        Join.join(sb, ", ", Iterables.transform(cParams, parameterToInitializer));
      }
      sb.append(" {");
      appendLine(sb);
      appendLine("}");

      appendLine();
      appendLine(getWriteMethodSignature(false, false) + " {");
      sb = new StringBuilder(getClassName(template.getName()));
      sb.append("::Write(");
      Join.join(sb, ", ", Iterables.concat(
          Collections.singleton(GXP_OUT_VAR + ", " + GXP_CONTEXT_VAR),
          Iterables.transform(template.getAllParameters(), paramToCallName)));
      sb.append(");");
      appendLine(pos, sb);
      appendLine("}");

      appendLine();
      appendLine(null, getGetGxpClosureMethodSignature(false, false) + " {");
      sb = new StringBuilder("return ");
      sb.append(getClassName(template.getName()));
      sb.append("::GetGxpClosure(");
      Join.join(sb, ", ", Iterables.transform(template.getAllParameters(),
          paramToCallName));
      sb.append(");");
      appendLine(pos, sb);
      appendLine("}");

      if (!cParams.isEmpty()) {
        appendLine("private:");
        for (Parameter param : cParams) {
          formatLine(param.getSourcePosition(), "%s %s;",
                     toCppType(param.getType()), param.getPrimaryName());
        }
      }
      appendLine("};");
    }
  }
}
