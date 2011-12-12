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
import com.google.common.collect.Sets;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.NothingToCompileError;
import com.google.gxp.compiler.base.BooleanType;
import com.google.gxp.compiler.base.BoundImplementsDeclaration;
import com.google.gxp.compiler.base.BundleType;
import com.google.gxp.compiler.base.ClassImport;
import com.google.gxp.compiler.base.Constructor;
import com.google.gxp.compiler.base.ContentType;
import com.google.gxp.compiler.base.DefaultingImportVisitor;
import com.google.gxp.compiler.base.FormalTypeParameter;
import com.google.gxp.compiler.base.Implementable;
import com.google.gxp.compiler.base.ImplementsDeclaration;
import com.google.gxp.compiler.base.ImplementsVisitor;
import com.google.gxp.compiler.base.Import;
import com.google.gxp.compiler.base.ImportVisitor;
import com.google.gxp.compiler.base.InstanceType;
import com.google.gxp.compiler.base.Interface;
import com.google.gxp.compiler.base.JavaAnnotation;
import com.google.gxp.compiler.base.NativeImplementsDeclaration;
import com.google.gxp.compiler.base.NativeType;
import com.google.gxp.compiler.base.NullRoot;
import com.google.gxp.compiler.base.PackageImport;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.RootVisitor;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.base.TemplateType;
import com.google.gxp.compiler.base.ThrowsDeclaration;
import com.google.gxp.compiler.base.Tree;
import com.google.gxp.compiler.base.Type;
import com.google.gxp.compiler.base.TypeVisitor;
import com.google.gxp.compiler.base.UnboundImplementsDeclaration;
import com.google.gxp.compiler.base.UnexpectedNodeException;
import com.google.gxp.compiler.codegen.BaseCodeGenerator;
import com.google.gxp.compiler.codegen.BracesCodeGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Base class for the two scala code generators.  Contains functionallity
 * common to both.
 */
public abstract class BaseScalaCodeGenerator<T extends Tree<Root>> extends BracesCodeGenerator<T> {
  protected final String runtimeMessageSource;

  /**
   * @param tree the MessageExtractedTree to compile.
   * @param runtimeMessageSource the message source to use at runtime, or null
   * if none was provided. This is a (typically dotted) prefix used when
   * loading message resources at runtime.
   */
  protected BaseScalaCodeGenerator(T tree, String runtimeMessageSource) {
    super(tree);
    this.runtimeMessageSource = runtimeMessageSource;
  }

  public void generateCode(final Appendable appendable,
                           final AlertSink alertSink)
      throws IOException {
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
        createTemplateWorker(appendable, alertSink, template,
                             runtimeMessageSource).run();
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

  protected abstract static class Worker extends BracesCodeGenerator.Worker {

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

    protected void appendAnnotations(Iterable<JavaAnnotation> annotations) {
      for (JavaAnnotation annotation : annotations) {
        appendLine(annotation.getSourcePosition(),
                   ScalaUtil.validateAnnotation(alertSink, annotation));
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

  protected static class InterfaceWorker extends Worker {
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
      appendAnnotations(iface.getJavaAnnotations(JavaAnnotation.Element.INTERFACE));
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
          formatLine(param.getSourcePosition(), "public %s %s(String %s);",
                     toScalaType(param.getType()),
                     getConstructorMethodName(param),
                     param.getPrimaryName());
        }
      }
    }
  }

  protected abstract TemplateWorker createTemplateWorker(
      Appendable appendable, AlertSink alertSink,
      Template template, String runtimeMessageSource);

  protected abstract static class TemplateWorker extends Worker {
    protected final Template template;

    protected TemplateWorker(Appendable appendable, AlertSink alertSink,
                             Template template) {
      super(appendable, alertSink);
      this.template = Preconditions.checkNotNull(template);
    }

    public void run() {
      for (Parameter param : template.getAllParameters()) {
        SCALA.validateName(alertSink, param, param.getPrimaryName());
      }
      appendHeader(template);
      appendImports(template);
      appendLine();
      appendClass();
      appendLine();
      appendFooter();
    }

    protected String getFullClassName(TemplateName templateName) {
      return (templateName == null) ? null : templateName.toString();
    }

    protected abstract String getBaseClassName();

    protected abstract void appendClass();

    protected Function<Parameter, String> parameterToAnnotatedCallParameter =
      new Function<Parameter, String>() {
        public String apply(Parameter param) {
          StringBuilder sb = new StringBuilder();
          sb.append("final ");
          for (JavaAnnotation annotation : param.getJavaAnnotations()) {
            sb.append(ScalaUtil.validateAnnotation(alertSink, annotation));
            sb.append(" ");
          }
          sb.append(toScalaType(param.getType()));
          sb.append(" ");
          sb.append(param.getPrimaryName());
          return sb.toString();
        }
      };

    private final ImplementsVisitor<String> javaNameImplementsVisitor =
        new ImplementsVisitor<String>() {
      public String visitUnboundImplementsDeclaration(UnboundImplementsDeclaration uid) {
        throw new UnexpectedNodeException(uid);
      }

      public String visitBoundImplementsDeclaration(BoundImplementsDeclaration bid) {
        return bid.getImplementable().getName().toString();
      }

      public String visitNativeImplementsDeclaration(NativeImplementsDeclaration nid) {
        return toScalaType(nid.getNativeType());
      }
    };

    private final Function<ImplementsDeclaration, String> getInterfaceFromImplementsDeclaration =
      new Function<ImplementsDeclaration, String>() {
        public String apply(ImplementsDeclaration implementsDeclaration) {
          return implementsDeclaration.acceptImplementsVisitor(javaNameImplementsVisitor);
        }
      };

    private void appendImplementsDeclaration(StringBuilder sb,
        List<ImplementsDeclaration> implementsDeclarations) {
      if (!implementsDeclarations.isEmpty()) {
        sb.append(" extends ");
        COMMA_JOINER.appendTo(sb, Iterables.transform(implementsDeclarations,
                                                      getInterfaceFromImplementsDeclaration));
      }
    }

    protected abstract void appendWriteMethodBody();

    protected void appendWriteMethod() {
      appendLine(getWriteMethodSignature(true) + " {");
      appendWriteMethodBody();
      appendLine("}");
    }

    /**
     * Get the base name of a fully qualified type name.
     */
    protected String getBaseName(String fullName) {
      String[] parts = fullName.split("\\.");
      return parts[parts.length - 1];
    }

    protected void appendGetGxpClosureMethod(boolean isStatic) {
      String scalaType = template.getSchema().getScalaType();
      String tunnelingScalaType = "Tunneling" + getBaseName(scalaType);
      if (isStatic) {
        appendLine();
        formatLine("private abstract static class %s", tunnelingScalaType);
        appendLine("    extends GxpTemplate.TunnelingGxpClosure");
        formatLine("    implements %s {", scalaType);
        appendLine("}");
      }
      appendLine();
      appendLine(getGetGxpClosureMethodSignature(isStatic) + " {");
      formatLine("return new %s() {", tunnelingScalaType);
      StringBuilder sb = new StringBuilder("public void writeImpl(" + GXP_SIG + ")");
      sb.append(" {");
      appendLine(sb);
      sb = new StringBuilder();
      sb.append(isStatic ? getFullClassName(template.getName()) : "Instance.this");
      sb.append(".write(" + GXP_OUT_VAR + ", " + GXP_CONTEXT_VAR);
      for (Parameter param : (isStatic ? template.getAllParameters() : template.getParameters())) {
        sb.append(", ");
        sb.append(param.getPrimaryName());
      }
      sb.append(");");
      appendLine(sb);
      appendLine("}");
      appendLine("};");
      appendLine("}");
    }

    private static final String GET_ARG_LIST_FORMAT = loadFormat("getArgList");

    protected void appendGetArgListMethod() {
      // create the constant
      StringBuilder sb = new StringBuilder(
          "private static final java.util.List<String> GXP$ARGLIST = ");

      List<String> parameterNames = Lists.newArrayList();
      for (Parameter param : template.getAllParameters()) {
        parameterNames.add(SCALA.toStringLiteral(param.getPrimaryName()));
      }

      if (parameterNames.isEmpty()) {
        sb.append("java.util.Collections.emptyList();");
      } else {
        sb.append("java.util.Collections.unmodifiableList(java.util.Arrays.asList(");
        COMMA_JOINER.appendTo(sb, parameterNames);
        sb.append("));");
      }
      appendLine();
      appendLine(sb);

      // create the accessor
      appendLine();
      appendLine(GET_ARG_LIST_FORMAT);
    }

    protected enum Access {
      _public("public"),
      _private("private"),
      _protected("protected");

      private final String s;

      Access(String s) {
        this.s = s;
      }

      public String toString() {
        return s;
      }
    }

    private String getWriteMethodSignature(boolean isStatic) {
      return getWriteMethodSignature(Access._public, isStatic, "write");
    }

    protected String getWriteMethodSignature(Access access, boolean isStatic,
                                             String methodName) {
      Iterable<Parameter> params = isStatic
          ? template.getAllParameters()
          : template.getParameters();

      StringBuilder sb = new StringBuilder(access.toString());
      if (isStatic) {
        sb.append(" static");
        if (!template.getFormalTypeParameters().isEmpty()) {
          sb.append(" ");
        }
        appendScalaFormalTypeParameters(sb, true,
                                        template.getFormalTypeParameters());
      }
      sb.append(" void ");
      sb.append(methodName);
      sb.append("(" + GXP_SIG);
      for (Parameter param : params) {
        sb.append(", ");
        sb.append(parameterToCallName.apply(param));
      }
      sb.append(")");
      return sb.toString();
    }

    private String getGetGxpClosureMethodSignature(boolean isStatic) {
      Iterable<Parameter> params = isStatic
          ? template.getAllParameters()
          : template.getParameters();

      StringBuilder sb = new StringBuilder("public");
      if (isStatic) {
        sb.append(" static");
        if (!template.getFormalTypeParameters().isEmpty()) {
          sb.append(" ");
        }
        appendScalaFormalTypeParameters(sb, true,
                                        template.getFormalTypeParameters());
      }
      sb.append(" ");
      sb.append(template.getSchema().getScalaType());
      sb.append(" getGxpClosure(");
      COMMA_JOINER.appendTo(sb, Iterables.transform(params, parameterToCallName));
      sb.append(")");
      return sb.toString();
    }

    protected void appendInterface() {
      appendLine();
      appendLine("/**\n"
                 + " * Interface that defines a strategy for writing this GXP\n"
                 + " */");
      appendAnnotations(template.getJavaAnnotations(JavaAnnotation.Element.INTERFACE));
      StringBuilder sb = new StringBuilder("public interface Interface");
      appendScalaFormalTypeParameters(sb, true, template.getFormalTypeParameters());
      appendImplementsDeclaration(sb, template.getImplementsDeclarations());
      sb.append(" {");
      appendLine(sb);
      appendLine(getWriteMethodSignature(false) + ";");
      appendLine();
      appendLine(getGetGxpClosureMethodSignature(false) + ";");
      for (Parameter param : template.getParameters()) {
        if (param.getDefaultValue() != null) {
          appendLine();
          formatLine("%s %s();", toScalaType(param.getType()), getDefaultMethodName(param));
        }
      }
      for (Parameter param : template.getParameters()) {
        if (param.getConstructor() != null) {
          appendLine();
          formatLine("%s %s(String %s);",
                     toScalaType(param.getType()), getConstructorMethodName(param),
                     param.getPrimaryName());
        }
      }
      appendLine("}");
    }

    protected void appendInstance() {
      StringBuilder sb;
      Constructor constructor = template.getConstructor();
      List<Parameter> cParams = constructor.getParameters();

      appendLine();
      appendLine("/**\n"
                 + " * Instantiable instance of this GXP\n"
                 + " */");
      appendAnnotations(template.getJavaAnnotations(JavaAnnotation.Element.INSTANCE));
      sb = new StringBuilder("public static class Instance");
      appendScalaFormalTypeParameters(sb, true, template.getFormalTypeParameters());
      sb.append(" implements Interface");
      appendScalaFormalTypeParameters(sb, false, template.getFormalTypeParameters());
      sb.append(" {");
      appendLine(sb);
      for (Parameter param : cParams) {
        formatLine("private final %s %s;", toScalaType(param.getType()), param.getPrimaryName());
      }

      appendLine();
      appendAnnotations(constructor.getJavaAnnotations());
      sb = new StringBuilder("public Instance(");
      COMMA_JOINER.appendTo(sb, Iterables.transform(cParams, parameterToAnnotatedCallParameter));
      sb.append(") {");
      appendLine(sb);
      for (Parameter param : cParams) {
        formatLine("this.%s = %s;", param.getPrimaryName(), param.getPrimaryName());
      }
      appendLine("}");
      appendLine();
      appendLine(getWriteMethodSignature(false) + " {");
      sb = new StringBuilder(getFullClassName(template.getName()));
      sb.append(".write(" + GXP_OUT_VAR + ", " + GXP_CONTEXT_VAR);
      for (Parameter param : template.getAllParameters()) {
        sb.append(", ");
        sb.append(paramToCallName.apply(param));
      }
      sb.append(");");
      appendLine(sb);
      appendLine("}");
      appendGetGxpClosureMethod(false);
      for (Parameter param : template.getParameters()) {
        if (param.getDefaultValue() != null) {
          appendLine();
          formatLine("public %s %s() {",
                     toScalaType(param.getType()), getDefaultMethodName(param));
          formatLine("return %s.%s();",
                     getFullClassName(template.getName()), getDefaultMethodName(param));
          appendLine("}");
        }
      }

      for (Parameter param : template.getParameters()) {
        if (param.getConstructor() != null) {
          appendLine();
          formatLine("public %s %s(String %s) {",
                     toScalaType(param.getType()),
                     getConstructorMethodName(param),
                     param.getPrimaryName());
          formatLine("return %s.%s(%s);",
                     getFullClassName(template.getName()),
                     getConstructorMethodName(param),
                     param.getPrimaryName());
          appendLine("}");
        }
      }

      appendLine("}");
    }
  }
}
