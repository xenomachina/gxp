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
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.NothingToCompileError;
import com.google.gxp.compiler.base.BooleanType;
import com.google.gxp.compiler.base.BundleType;
import com.google.gxp.compiler.base.ContentType;
import com.google.gxp.compiler.base.CppFileImport;
import com.google.gxp.compiler.base.CppLibraryImport;
import com.google.gxp.compiler.base.DefaultingImportVisitor;
import com.google.gxp.compiler.base.FormalTypeParameter;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.Import;
import com.google.gxp.compiler.base.ImportVisitor;
import com.google.gxp.compiler.base.InstanceType;
import com.google.gxp.compiler.base.Interface;
import com.google.gxp.compiler.base.NativeType;
import com.google.gxp.compiler.base.NullRoot;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.RootVisitor;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.base.TemplateType;
import com.google.gxp.compiler.base.Tree;
import com.google.gxp.compiler.base.Type;
import com.google.gxp.compiler.base.TypeVisitor;
import com.google.gxp.compiler.codegen.BracesCodeGenerator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Base class for C++ {@code CodeGenerator}s.
 */
public abstract class BaseCppCodeGenerator<T extends Tree<Root>> extends BracesCodeGenerator<T> {
  protected BaseCppCodeGenerator(T tree) {
    super(tree);
  }

  public void generateCode(final Appendable appendable, final AlertSink alertSink)
      throws IOException {
    alertSink.addAll(tree.getAlerts());

    root.acceptVisitor(new RootVisitor<Void>() {
      public Void visitInterface(Interface iface) {
        createInterfaceWorker(appendable, alertSink, iface).run();
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

  protected abstract static class Worker extends BracesCodeGenerator.Worker {
    protected Worker(Appendable out, AlertSink alertSink) {
      super(out, alertSink, "public:", "private:");
    }

    protected String getClassName(TemplateName name) {
      return name.getBaseName();
    }

    protected String getQualifiedClassName(TemplateName name) {
      return Join.join("::", name.toString().split("\\."));
    }

    private String getIfdefGuard(TemplateName templateName) {
      return templateName.toString().replace('.', '_').toUpperCase() + "_H__";
    }

    protected void appendIfdefGuardStart(Root root) {
      String ifdefGuard = getIfdefGuard(root.getName());
      formatLine("#ifndef %s", ifdefGuard);
      formatLine("#define %s", ifdefGuard);
    }

    protected void appendIfdefGuardEnd(Root root) {
      formatLine("#endif  // %s", getIfdefGuard(root.getName()));
    }

    private final ImportVisitor<Void> IMPORT_VISITOR =
        new DefaultingImportVisitor<Void>(){
          public Void defaultVisitImport(Import imp) {
            // do nothing
            return null;
          }

          public Void visitCppFileImport(CppFileImport imp) {
            formatLine(imp.getSourcePosition(), "#include %s", imp.getTarget());
            return null;
          }

          public Void visitCppLibraryImport(CppLibraryImport imp) {
            formatLine(imp.getSourcePosition(), "#include %s", imp.getTarget());
            return null;
          }
        };

    protected void appendImports(Root root) {
      for (String imp : root.getSchema().getCppImports()) {
        formatLine(root.getSourcePosition(), "#include \"%s\"", imp);
      }

      for (Import imp : root.getImports()) {
        imp.acceptVisitor(IMPORT_VISITOR);
      }
    }

    protected void appendImports(Root root, Set<TemplateName> extraIncludes) {
      appendImports(root);
      for (TemplateName extraInclude : extraIncludes) {
        formatLine("#include \"%s.h\"", extraInclude.toString().replace('.', '/'));
      }
    }

    protected void appendNamespacesOpen(TemplateName templateName) {
      for (String part : templateName.getPackageName().split("\\.")) {
        formatLine("namespace %s {", part);
      }
    }

    protected void appendNamespacesClose(TemplateName templateName) {
      for (String part : templateName.getPackageName().split("\\.")) {
        appendLine("}");
      }
    }

    protected static final String GXP_OUT_VAR = "gxp_out";
    protected static final String GXP_CONTEXT_VAR = "gxp_context";

    protected static final String GXP_SIG =
        Join.join(", ", "%s " + GXP_OUT_VAR, "const GxpContext& " + GXP_CONTEXT_VAR);

    protected static final String DEFAULT_GXP_OUT_TYPE = "Appendable*";

    /**
     * Defines a type that can be the first parameter to a gxp Write method along
     * with the wrapper class that turns this type into an Appendable.
     */
    protected class ExtraOutType {
      private final String outType;
      private final String outWrapper;

      public ExtraOutType(String outType, String outWrapper) {
        this.outType = Preconditions.checkNotNull(outType);
        this.outWrapper = Preconditions.checkNotNull(outWrapper);
      }

      public String getOutType() {
        return outType;
      }

      public String getOutWrapper() {
        return outWrapper;
      }

      public String getGxpSig() {
        return String.format(GXP_SIG, outType);
      }
    }

    protected List<ExtraOutType> extraOutTypes = ImmutableList.of(
        new ExtraOutType("string*", "StringAppendable"));

    protected String toCppType(Type type) {
      return type.acceptTypeVisitor(new TypeVisitor<String>() {
        public String visitBooleanType(BooleanType type) {
          return "bool";
        }

        public String visitBundleType(BundleType type) {
          // TODO(harryh): make this real, only an int now so it's
          //               a valid C++ type
          return "int";
        }

        public String visitContentType(ContentType type) {
          return type.getSchema().getCppType() + "*";
        }

        public String visitInstanceType(InstanceType type) {
          return getClassName(type.getTemplateName()) + "::Interface";
        }

        public String visitNativeType(NativeType type) {
          return CppUtil.validateType(alertSink, type);
        }

        public String visitTemplateType(TemplateType type) {
          return getClassName(type.getTemplateName());
        }
      });
    }

    protected Function<Parameter, String> parameterToCallName =
      new Function<Parameter, String>() {
        public String apply(Parameter param) {
          StringBuilder sb = new StringBuilder();
          sb.append(toCppType(param.getType()));
          sb.append(" ");
          sb.append(param.getPrimaryName());
          return sb.toString();
        }
      };

    protected List<String> toBoundedTypeDecls(boolean isDeclaration,
                                              Iterable<FormalTypeParameter> formalTypeParameters) {
      List<String> result = Lists.newArrayList();
      for (FormalTypeParameter formalTypeParameter : formalTypeParameters) {
        if (isDeclaration) {
          result.add("typename " + formalTypeParameter.getName());
        } else {
          result.add(formalTypeParameter.getName());
        }
      }
      return result;
    }

    protected void appendCppFormalTypeParameters(StringBuilder sb, boolean isDeclaration,
                                                 List<FormalTypeParameter> formalTypeParameters) {
      if (!formalTypeParameters.isEmpty()) {
        if (isDeclaration) {
          sb.append("template");
        }
        sb.append("<");
        Join.join(sb, ", ", toBoundedTypeDecls(isDeclaration, formalTypeParameters));
        sb.append(">");
      }
    }

    protected void appendCppFormalTypeParameters(boolean isDeclaration,
                                                 List<FormalTypeParameter> formalTypeParameters) {
      StringBuilder sb = new StringBuilder();
      appendCppFormalTypeParameters(sb, isDeclaration, formalTypeParameters);
      appendLine(sb.toString());
    }

    protected Function<Parameter, String> parameterToInitializer =
      new Function<Parameter, String>() {
        public String apply(Parameter param) {
          return param.getPrimaryName() + "(" + param.getPrimaryName() + ")";
        }
      };
  }

  protected abstract InterfaceWorker createInterfaceWorker(Appendable out,
                                                           AlertSink alertSink,
                                                           Interface iface);

  protected abstract static class InterfaceWorker extends Worker {
    protected final Interface iface;

    protected InterfaceWorker(Appendable out, AlertSink alertSink, Interface iface) {
      super(out, alertSink);
      this.iface = Preconditions.checkNotNull(iface);
    }

    protected abstract void appendClass();

    public void run() {
      appendHeader(iface);
      appendLine();
      appendClass();
      appendLine();
      appendFooter();
    }
  }

  protected abstract TemplateWorker createTemplateWorker(Appendable out,
                                                         AlertSink alertSink,
                                                         Template template);

  protected abstract static class TemplateWorker extends Worker {
    protected final Template template;

    protected TemplateWorker(Appendable out, AlertSink alertSink, Template template) {
      super(out, alertSink);
      this.template = Preconditions.checkNotNull(template);
    }

    protected abstract void appendClass();

    public void run() {
      appendHeader(template);
      appendLine();
      appendClass();
      appendLine();
      appendFooter();
    }

    protected String getWriteMethodSignature(boolean outsideClass, boolean isStatic) {
      return getWriteMethodSignature(DEFAULT_GXP_OUT_TYPE, outsideClass, isStatic);
    }

    protected String getWriteMethodSignature(String outType, boolean outsideClass,
                                             boolean isStatic) {
      Iterable<Parameter> params = isStatic
          ? template.getAllParameters()
          : template.getParameters();

      StringBuilder sb = new StringBuilder();
      if (isStatic) {
        if (!template.getFormalTypeParameters().isEmpty()) {
          appendCppFormalTypeParameters(sb, true, template.getFormalTypeParameters());
          sb.append("\n");
        }
        if (!outsideClass) {
          sb.append("static ");
        }
      }
      sb.append("void ");
      if (outsideClass) {
        sb.append(getQualifiedClassName(template.getName()));
        sb.append("::");
      }
      sb.append("Write(");
      Join.join(sb, ", ", Iterables.concat(
          Collections.singleton(String.format(GXP_SIG, outType)),
          Iterables.transform(params, parameterToCallName)));
      sb.append(")");
      return sb.toString();
    }

    // TODO(harryh): combine this with getWriteMethodSignature() in some way?
    protected String getGetGxpClosureMethodSignature(boolean outsideClass, boolean isStatic) {
      Iterable<Parameter> params = isStatic
          ? template.getAllParameters()
          : template.getParameters();

      StringBuilder sb = new StringBuilder();
      if (isStatic) {
        if (!template.getFormalTypeParameters().isEmpty()) {
          appendCppFormalTypeParameters(sb, true, template.getFormalTypeParameters());
          sb.append("\n");
        }
        if (!outsideClass) {
          sb.append("static ");
        }
      }
      sb.append(toCppType(new ContentType(template.getSchema())));
      sb.append(" ");
      if (outsideClass) {
        sb.append(getQualifiedClassName(template.getName()));
        sb.append("::");
      }
      sb.append("GetGxpClosure(");
      Join.join(sb, ", ", Iterables.transform(params, parameterToCallName));
      sb.append(")");
      return sb.toString();
    }
  }
}
