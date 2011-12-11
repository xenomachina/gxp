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

package com.google.gxp.compiler.scala;

import static com.google.gxp.compiler.base.OutputLanguage.SCALA;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.NothingToCompileError;
import com.google.gxp.compiler.base.BooleanType;
import com.google.gxp.compiler.base.BundleType;
import com.google.gxp.compiler.base.ClassImport;
import com.google.gxp.compiler.base.ContentType;
import com.google.gxp.compiler.base.DefaultingImportVisitor;
import com.google.gxp.compiler.base.FormalParameter;
import com.google.gxp.compiler.base.FormalTypeParameter;
import com.google.gxp.compiler.base.Implementable;
import com.google.gxp.compiler.base.Import;
import com.google.gxp.compiler.base.ImportVisitor;
import com.google.gxp.compiler.base.InstanceType;
import com.google.gxp.compiler.base.Interface;
import com.google.gxp.compiler.base.NativeType;
import com.google.gxp.compiler.base.NullRoot;
import com.google.gxp.compiler.base.PackageImport;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.RootVisitor;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.base.TemplateType;
import com.google.gxp.compiler.base.Type;
import com.google.gxp.compiler.base.TypeVisitor;
import com.google.gxp.compiler.codegen.BaseCodeGenerator;
import com.google.gxp.compiler.codegen.BracesCodeGenerator;
import com.google.gxp.compiler.msgextract.MessageExtractedTree;

import java.util.*;

/**
 * {@code CodeGenerator} which generates Scala code.
 */
public class ScalaCodeGenerator extends BracesCodeGenerator<MessageExtractedTree> {
  public ScalaCodeGenerator(MessageExtractedTree tree) {
    super(tree);
  }
  
  public void generateCode(final Appendable appendable, final AlertSink alertSink) {
    alertSink.addAll(tree.getAlerts());
    
    root.acceptVisitor(new RootVisitor<Void>() {
      public Void visitInterface(Interface iface) {
        validateFormalTypeParameters(alertSink, iface.getFormalTypeParameters());
        new InterfaceWorker(appendable, alertSink, iface).run();
        return null;
      }

      public Void visitNullRoot(NullRoot nullRoot) {
        alertSink.add(new NothingToCompileError(nullRoot.getSourcePosition()));
        return null;
      }

      public Void visitTemplate(Template template) {
        validateFormalTypeParameters(alertSink, template.getFormalTypeParameters());
        createTemplateWorker(appendable, alertSink, template).run();
        return null;
      }
    });
    
  }
  
  private void validateFormalTypeParameters(AlertSink alertSink,
                                            List<FormalTypeParameter> formalTypeParameters) {
    for (FormalTypeParameter formalTypeParameter : formalTypeParameters) {
      SCALA.validateName(alertSink, formalTypeParameter, formalTypeParameter.getName());
    }
  }
  
  private TemplateWorker createTemplateWorker(Appendable appendable,
                                              AlertSink alertSink,
                                              Template template) {
    return new TemplateWorker(appendable, alertSink, template);
  }

  private abstract static class Worker extends BracesCodeGenerator.Worker {
    protected Worker(Appendable appendable, AlertSink alertSink) {
      super(appendable, alertSink);
    }
    
    private static final String HEADER_FORMAT = loadFormat("header");

    protected void appendHeader(Root root) {
      super.appendHeader(root);
      TemplateName name = root.getName();
      String sourceName = root.getSourcePosition().getSourceName();
      String packageName = (name == null) ? "" : name.getPackageName();

      // passing a null SourcePosition insures that we never get tail comments
      formatLine((SourcePosition)null, HEADER_FORMAT, sourceName, packageName);
    }
    
    private final ImportVisitor<Void> IMPORT_VISITOR =
        new DefaultingImportVisitor<Void>(){
          public Void defaultVisitImport(Import imp) {
            // do nothing
            return null;
          }

          public Void visitClassImport(ClassImport imp) {
            formatLine(imp.getSourcePosition(), "import %s", imp.getClassName().toString());
            return null;
          }

          public Void visitPackageImport(PackageImport imp) {
            formatLine(imp.getSourcePosition(), "import %s._", imp.getPackageName());
            return null;
          }
        };

    protected void appendImports(Root root) {
      for (String imp : root.getSchema().getScalaImports()) {
        appendLine(root.getSourcePosition(), "import " + imp);
      }
      for (Import imp : root.getImports()) {
        imp.acceptVisitor(IMPORT_VISITOR);
      }
    } 
    
    protected static final String GXP_OUT_VAR = "gxp$out";
    protected static final String GXP_CONTEXT_VAR = "gxp_context";

    protected static final String GXP_SIG =
      COMMA_JOINER.join(GXP_OUT_VAR + ": java.lang.Appendable",
                        GXP_CONTEXT_VAR + ": com.google.gxp.base.GxpContext");
    
    protected String getClassName(TemplateName name) {
      return (name == null) ? null : name.getBaseName();
    }

    protected String toScalaType(Type type) {
      return type.acceptTypeVisitor(new TypeVisitor<String>() {
        public String visitBooleanType(BooleanType type) {
          return "boolean";
        }

        public String visitBundleType(BundleType type) {
          StringBuilder sb = new StringBuilder("com.google.gxp.base.GxpAttrBundle[");
          sb.append(type.getSchema().getScalaType());
          sb.append("]");
          return sb.toString();
        }

        public String visitContentType(ContentType type) {
          return type.getSchema().getScalaType();
        }

        public String visitInstanceType(InstanceType type) {
          return type.getTemplateName().toString() + ".Interface";
        }

        public String visitNativeType(NativeType type) {
          return ScalaUtil.validateType(alertSink, type);
        }

        public String visitTemplateType(TemplateType type) {
          return type.getTemplateName().toString();
        }
      });
    }

    protected Function<Parameter, String> parameterToCallName =
      new Function<Parameter, String>() {
        public String apply(Parameter param) {
          StringBuilder sb = new StringBuilder();
          sb.append(param.getPrimaryName());
          sb.append(": ");
          sb.append(toScalaType(param.getType()));
          return sb.toString();
        }
      };
    
    protected List<String> toBoundedTypeDecls(boolean includeExtends,
                                              Iterable<FormalTypeParameter> formalTypeParameters) {
      List<String> result = Lists.newArrayList();
      for (FormalTypeParameter formalTypeParameter : formalTypeParameters) {
        if (!includeExtends || formalTypeParameter.getExtendsType() == null) {
          result.add(formalTypeParameter.getName());
        } else {
          String type = ScalaUtil.validateConjunctiveType(
              alertSink, formalTypeParameter.getExtendsType());
          result.add(formalTypeParameter.getName() + " <: " + type);
        }
      }
      return result;
    }    
    
    protected void appendScalaFormalTypeParameters(
      StringBuilder sb, boolean includeExtends,
      List<FormalTypeParameter> formalTypeParameters) {
      if (!formalTypeParameters.isEmpty()) {
        sb.append("[");
        COMMA_JOINER.appendTo(sb, toBoundedTypeDecls(includeExtends,
                                                     formalTypeParameters));
        sb.append("]");
      }
    }
    
    protected static String loadFormat(String name) {
      return BaseCodeGenerator.loadFormat("scala/" + name);
    }
  }

  private static class TemplateWorker extends Worker {
    private final Template template;

    public TemplateWorker(Appendable appendable, AlertSink alertSink, Template template) {
      super(appendable, alertSink);
      this.template = Preconditions.checkNotNull(template);
    }

    public TemplateWorker createSubWorker(Appendable newAppendable) {
      return new TemplateWorker(newAppendable, alertSink, template);
    }
    
    public void run() {
      for (Parameter param : template.getAllParameters()) {
        SCALA.validateName(alertSink, param, param.getPrimaryName());
      }
      appendHeader(template);
      appendLine();
      appendFooter();
    }
  }
  
  private static class InterfaceWorker extends Worker {
    protected final Interface iface;

    protected InterfaceWorker(Appendable appendable, AlertSink alertSink,
                              Interface iface) {
      super(appendable, alertSink);
      this.iface = Preconditions.checkNotNull(iface);
    }

    public void run() {
      for (Parameter param :
               Iterables.filter(iface.getParameters(), Implementable.NOT_INSTANCE_PARAM)) {
        SCALA.validateName(alertSink, param, param.getPrimaryName());
      }
      appendHeader(iface);
      appendImports(iface);
      appendLine();
      appendInterface();
      appendLine();
      appendFooter();
    }
    
    @Override
    protected SourcePosition getDefaultSourcePosition() {
      return iface.getSourcePosition();
    }

    protected void appendInterface() {
      StringBuilder sb = new StringBuilder("trait ");
      sb.append(getClassName(iface.getName()));
      appendScalaFormalTypeParameters(sb, true, iface.getFormalTypeParameters());
      sb.append(" {");
      appendLine(iface.getSourcePosition(), sb.toString());
      appendLine(getWriteMethodSignature());
      appendLine(getGetGxpClosureMethodSignature());
      appendGetDefaultMethods();
      appendConstructorMethods();
      appendLine("}");
    }

    private String getWriteMethodSignature() {
      StringBuilder sb = new StringBuilder("def write(");
      sb.append(GXP_SIG);
      for (Parameter param : Iterables.filter(iface.getParameters(),
                                              Implementable.NOT_INSTANCE_PARAM)) {
        sb.append(", ");
        sb.append(parameterToCallName.apply(param));
      }
      sb.append("): Unit");
      return sb.toString();
    }

    private String getGetGxpClosureMethodSignature() {
      StringBuilder sb = new StringBuilder();
      sb.append("def getGxpClosure(");
      COMMA_JOINER.appendTo(sb, Iterables.transform(
                                Iterables.filter(iface.getParameters(),
                                                 Implementable.NOT_INSTANCE_PARAM),
                                parameterToCallName));
      sb.append("): ");
      sb.append(iface.getSchema().getScalaType());
      return sb.toString();
    }

    private void appendGetDefaultMethods() {
      for (Parameter param : iface.getParameters()) {
        if (param.hasDefaultFlag()) {
          formatLine(param.getSourcePosition(), "def %s(): %s",
                     getDefaultMethodName(param), toScalaType(param.getType()));
        }
      }
    }

    private void appendConstructorMethods() {
      for (Parameter param : iface.getParameters()) {
        if (param.hasConstructorFlag()) {
          formatLine(param.getSourcePosition(), "def %s(%s: String): %s",
                     getConstructorMethodName(param),
                     param.getPrimaryName(),
                     toScalaType(param.getType()));
        }
      }
    }
  }
}