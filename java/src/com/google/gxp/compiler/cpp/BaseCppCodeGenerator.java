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
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.ImmutableList;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.NothingToCompileError;
import com.google.gxp.compiler.base.BooleanType;
import com.google.gxp.compiler.base.BundleType;
import com.google.gxp.compiler.base.ContentType;
import com.google.gxp.compiler.base.Parameter;
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

    protected void appendImports(Root root) {
      for (String imp : root.getSchema().getCppImports()) {
        formatLine(root.getSourcePosition(), "#include \"%s\"", imp);
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
        this.outType = Objects.nonNull(outType);
        this.outWrapper = Objects.nonNull(outWrapper);
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
      this.iface = Objects.nonNull(iface);
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
      this.template = Objects.nonNull(template);
    }

    protected abstract void appendClass();

    public void run() {
      appendHeader(template);
      appendLine();
      appendClass();
      appendLine();
      appendFooter();
    }

    protected String getWriteMethodSignature(boolean includeClassName, boolean isStatic) {
      return getWriteMethodSignature(DEFAULT_GXP_OUT_TYPE, includeClassName, isStatic);
    }

    protected String getWriteMethodSignature(String outType, boolean includeClassName,
                                             boolean isStatic) {
      Iterable<Parameter> params = isStatic
          ? template.getAllParameters()
          : template.getParameters();

      StringBuilder sb = new StringBuilder();
      if (!includeClassName && isStatic) {
        sb.append("static ");
      }
      sb.append("void ");
      if (includeClassName) {
        sb.append(getClassName(template.getName()));
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
    protected String getGetGxpClosureMethodSignature(boolean includeClassName, boolean isStatic) {
      Iterable<Parameter> params = isStatic
          ? template.getAllParameters()
          : template.getParameters();

      StringBuilder sb = new StringBuilder();
      if (!includeClassName && isStatic) {
        sb.append("static ");
      }
      sb.append(toCppType(new ContentType(template.getSchema())));
      sb.append(" ");
      if (includeClassName) {
        sb.append(getClassName(template.getName()));
        sb.append("::");
      }
      sb.append("GetGxpClosure(");
      Join.join(sb, ", ", Iterables.transform(params, parameterToCallName));
      sb.append(")");
      return sb.toString();
    }
  }
}
