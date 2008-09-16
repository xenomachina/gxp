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

import com.google.common.base.Functions;
import com.google.common.base.Join;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.PrimitiveArrays;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.JavaAnnotation;
import com.google.gxp.compiler.base.Template;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.compiler.base.ThrowsDeclaration;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.msgextract.MessageExtractedTree;
import com.google.gxp.compiler.io.RuntimeIOException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A {@code CodeGenerator} which generates java code that is a stub for
 * dynamic gxp compilation.
 */
public class DynamicStubJavaCodeGenerator extends BaseJavaCodeGenerator<MessageExtractedTree> {
  private final Collection<FileRef> sourceFiles;
  private final Collection<FileRef> schemaFiles;
  private final Collection<FileRef> sourcePaths;
  private final AlertPolicy alertPolicy;

  /**
   * @param tree the MessageExtractedTree to compile.
   */
  public DynamicStubJavaCodeGenerator(MessageExtractedTree tree,
                                      Collection<FileRef> sourceFiles,
                                      Collection<FileRef> schemaFiles,
                                      Collection<FileRef> sourcePaths,
                                      AlertPolicy alertPolicy) {
    super(tree, null);
    this.sourceFiles = Preconditions.checkNotNull(sourceFiles);
    this.schemaFiles = Preconditions.checkNotNull(schemaFiles);
    this.sourcePaths = Preconditions.checkNotNull(sourcePaths);
    this.alertPolicy = Preconditions.checkNotNull(alertPolicy);
  }

  @Override
  protected TemplateWorker createTemplateWorker(Appendable appendable,
                                                AlertSink alertSink,
                                                Template template,
                                                String runtimeMessageSource) {
    return new TemplateWorker(appendable, alertSink, template,
                              sourceFiles, schemaFiles, sourcePaths, alertPolicy);
  }

  /**
   * Helper class which exists mainly so we don't have to pass the CIndenter
   * and AlertSink everywhere manually.
   */
  private static class TemplateWorker extends BaseJavaCodeGenerator.TemplateWorker {
    private final Collection<FileRef> sourceFiles;
    private final Collection<FileRef> schemaFiles;
    private final Collection<FileRef> sourcePaths;
    private final InnerClassTemplateWorker innerWorker;
    private final AlertPolicy alertPolicy;

    private static final String GXP_COMPILATION_EXCEPTION
      = "com.google.gxp.base.dynamic.GxpCompilationException";

    TemplateWorker(Appendable appendable,
                   AlertSink alertSink,
                   Template template,
                   Collection<FileRef> sourceFiles,
                   Collection<FileRef> schemaFiles,
                   Collection<FileRef> sourcePaths,
                   AlertPolicy alertPolicy) {
      super(appendable, alertSink, template);
      this.sourceFiles = sourceFiles;
      this.schemaFiles = schemaFiles;
      this.sourcePaths = sourcePaths;
      this.innerWorker = new InnerClassTemplateWorker(appendable, alertSink, template);
      this.alertPolicy = alertPolicy;
    }

    // Pretty much everything in the stub is based off of the template
    // declaration itself, so let's make things a bit easier on ourselves...

    @Override
    protected SourcePosition getDefaultSourcePosition() {
      return template.getSourcePosition();
    }

    @Override
    protected String getBaseClassName() {
      return "com.google.gxp.base.dynamic.StubGxpTemplate";
    }

    // TODO(harryh): factor common code to base class
    @Override
    protected void appendClass() {
      TemplateName templateName = template.getName();
      appendAnnotations(template.getJavaAnnotations(JavaAnnotation.Element.CLASS));
      formatLine(template.getSourcePosition(), "public class %s extends %s {",
                 getClassName(templateName), getBaseClassName());
      appendStaticContent();
      appendWriteMethod();
      appendLine();
      appendWriteImplMethod();
      appendDefaultAccessors();
      appendParamConstructors();
      appendGetArgListMethod();
      appendGetGxpClosureMethod(true);
      appendInterface();
      appendInstance();
      appendLine();

      // We write directly to the buffer passed into us instead of going through
      // the append methods because for large gxps it becomes expensive to
      // re-indent the string.
      innerWorker.appendClass();

      appendLine("}");
    }

    private String serializeAlertPolicy() {
      try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(alertPolicy);
        oos.close();
        baos.close();
        return Join.join(",", Iterables.transform(PrimitiveArrays.asList(baos.toByteArray()),
                                                  Functions.toStringFunction()));
      } catch (IOException e) {
        throw new RuntimeIOException(e);
      }
    }

    /**
     * Appends static content necessary for runtime compilation of the
     * template.
     */
    private void appendStaticContent() {
      formatLine("private static long COMPILATION$TIME = %dL;",
                 template.getSourcePosition().getSource().getLastModified());
      formatLine("private static long COMPILATION$VERSION = 1;");

      appendLine("private static final com.google.gxp.compiler.alerts.AlertPolicy ALERT$POLICY =");
      formatLine("  createAlertPolicy(new byte[] {%s});", serializeAlertPolicy());

      appendLine("private static final com.google.gxp.compiler.fs.FileRef SRC$GXP = ");
      formatLine("  parseFilename(%s);",
                 JAVA.toStringLiteral(template.getSourcePosition().getSourceName()));

      final String classBase = JAVA.toStringLiteral(template.getName().toString() + "$Impl");

      final String javaBase = JAVA.toStringLiteral(
          "/" + template.getName().toString().replace('.', '/') + "$Impl");

      appendLine("private static final String JAVA$BASE =");
      formatLine("  %s;", javaBase);

      appendLine("private static final String CLASS$BASE =");
      formatLine("  %s;", classBase);

      appendStaticFileRefList("SRC$GXPS",    sourceFiles);
      appendStaticFileRefList("SRC$SCHEMAS", schemaFiles);
      appendStaticFileRefList("SRC$PATHS",   sourcePaths);

      appendLine();
      appendLine("private static com.google.gxp.compiler.fs.FileRef JAVA$FILE = null;");

      appendLine();
      appendLine("private static java.util.Map<String, java.lang.reflect.Method> METHODS$ =");
      formatLine("  getMethodMap(%s.class);",
                 innerWorker.getClassName(template.getName()));
      appendLine();
    }

    private void appendStaticFileRefList(String varName,
                                         Iterable<FileRef> files) {
      appendLine();
      formatLine(
          (SourcePosition) null,
          "private static final java.util.List<com.google.gxp.compiler.fs.FileRef> %s =",
          varName);
      appendLine(null, "  parseFilenameList(");
      List<String> parseFiles = Lists.newArrayList();
      for (FileRef file : files) {
        parseFiles.add("    " + JAVA.toStringLiteral(file.toFilename()));
      }
      appendLine(null, Join.join(",\n", parseFiles));
      appendLine(null, "  );");
    }

    private void appendReload() {
      appendLine("long LAST$MODIFIED = SRC$GXP.getLastModified();");
      appendLine("if ((LAST$MODIFIED != 0 && LAST$MODIFIED != COMPILATION$TIME)");
      appendLine("    || METHODS$ == null) {");
      appendLine("com.google.gxp.compiler.fs.InMemoryFileSystem MEM$FS =");
      appendLine("    new com.google.gxp.compiler.fs.InMemoryFileSystem();");
      appendLine("JAVA$FILE = compileGxp(MEM$FS, SRC$GXPS, SRC$SCHEMAS, SRC$PATHS,");
      appendLine("                       JAVA$BASE, COMPILATION$VERSION, ALERT$POLICY);");
      appendLine("METHODS$ = compileJava(MEM$FS, CLASS$BASE, COMPILATION$VERSION);");
      appendLine("COMPILATION$TIME = LAST$MODIFIED;");
      appendLine("COMPILATION$VERSION++;");
      appendLine("}");
    }

    /**
     * Generates accessor methods for retrieving default parameters.
     */
    private void appendDefaultAccessors() {
      for (Parameter param : template.getParameters()) {
        Expression defaultValue = param.getDefaultValue();
        if (defaultValue != null) {
          String methodName = getDefaultMethodName(param);
          String paramType = toJavaType(param.getType());
          appendLine();
          formatLine("public static %s %s() {",
                     paramType, methodName);
          appendReload();
          formatLine("return (%s)execNoExceptions(METHODS$, \"%s\", "
                     + "new Object[] {});",
                     toReferenceType(paramType), methodName);
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
          String methodName = getConstructorMethodName(param);
          String paramType = toJavaType(param.getType());
          String paramName = param.getPrimaryName();
          appendLine();
          formatLine("public static %s %s(String %s) {",
                     paramType, methodName, paramName);
          appendReload();
          formatLine("return (%s)execNoExceptions(METHODS$, \"%s\", "
                     + "new Object[] { %s });",
                     toReferenceType(paramType), methodName, paramName);
          appendLine("}");
        }
      }
    }

    private static final String TEMP_OUT_VAR = "temp$out";

    /**
     * In the write method of stub classes we cache the output of the gxps at the top level
     * and only send it to the real output if there are no errors durring dynamic compilation.
     *
     * If there is an error, we throw away whatever output we've seen so far, and write the error
     * to the real output
     */
    @Override
    protected void appendWriteMethodBody() {
      StringBuilder sb = new StringBuilder();
      sb.append("writeImpl(%s, gxp_context");
      for (Parameter param : template.getAllParameters()) {
        sb.append(", ");
        sb.append(param.getPrimaryName());
      }
      sb.append(");");
      String writeLine = sb.toString();

      appendLine("if (gxp_context.isTopLevelCall()) {");
      appendLine("try {");
      formatLine("java.io.StringWriter %s = new java.io.StringWriter();", TEMP_OUT_VAR);
      formatLine(writeLine, TEMP_OUT_VAR);
      formatLine("%s.append(%s.toString());", GXP_OUT_VAR, TEMP_OUT_VAR);
      formatLine("} catch (%s gxp$e) {", GXP_COMPILATION_EXCEPTION);
      formatLine("gxp$e.write(%s, gxp_context);", GXP_OUT_VAR);
      appendLine("}");
      appendLine("} else {");
      formatLine(writeLine, GXP_OUT_VAR);
      appendLine("}");
    }

    protected void appendWriteImplMethod() {
      // built a set of throws types
      List<String> throwsTypes = Lists.newArrayList();
      for (ThrowsDeclaration throwsDeclaration : template.getThrowsDeclarations()) {
        throwsTypes.add(throwsDeclaration.getExceptionType());
      }
      throwsTypes.add("java.io.IOException");
      throwsTypes.add("java.lang.RuntimeException");

      appendLine(getWriteMethodSignature(Access._private, true, "writeImpl") + " {");
      appendReload();
      appendLine("try {");
      StringBuilder sb = new StringBuilder("exec(METHODS$, \"write\", ");
      sb.append("new Object[] {");
      sb.append(GXP_OUT_VAR);
      sb.append(", gxp_context");
      for (Parameter param : template.getAllParameters()) {
        sb.append(", ");
        sb.append(param.getPrimaryName());
      }
      sb.append("});");
      appendLine(sb);
      appendLine("} catch (Throwable gxp$t) {");
      appendLine("rewriteStackTraceElements(gxp$t, JAVA$FILE);");
      for (String throwType : throwsTypes) {
        formatLine("if (gxp$t instanceof %s) {", throwType);
        formatLine("throw (%s)gxp$t;", throwType);
        appendLine("}");
      }
      formatLine("throw new %s.Throw(gxp$t);", GXP_COMPILATION_EXCEPTION);
      appendLine("}");
      appendLine("}");
    }
  }

  protected static class InnerClassTemplateWorker
      extends DynamicImplJavaCodeGenerator.TemplateWorker {
    protected InnerClassTemplateWorker(Appendable appendable, AlertSink alertSink,
                                       Template template) {
      super(appendable, alertSink, template, 0);
      out.addIndent();
    }

    @Override
    protected void appendClassDecl() {
      formatLine(template.getSourcePosition(), "public static class %s extends %s {",
                 getClassName(template.getName()), getBaseClassName());
    }
  }

  // TODO(harryh): everything below this line should probably be in JavaUtil
  //               it's a tiny bit different because the Map goes to
  //               specific types instead of things like Number

  private static final Map<String, String> PRIMITIVE_TO_BOXED_MAP =
    ImmutableMap.<String, String>builder()
      .put("boolean", "Boolean")
      .put("byte", "Byte")
      .put("char", "Character")
      .put("double", "Double")
      .put("float", "Float")
      .put("int", "Integer")
      .put("long", "Long")
      .put("short", "Short")
      .build();

  /**
   * @return the most specific reference type that corresponds to the specified
   * a Java type, or the specified Java type if it is already a reference type
   * (ie: a class/interface).
   */
  private static String toReferenceType(String type) {
    String result = PRIMITIVE_TO_BOXED_MAP.get(type);
    return (result == null) ? type : result;
  }
}
