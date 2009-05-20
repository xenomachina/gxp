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

package com.google.gxp.base.dynamic;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.common.io.ByteStreams;
import com.google.gxp.base.GxpTemplate;
import com.google.gxp.compiler.Compiler;
import com.google.gxp.compiler.Configuration;
import com.google.gxp.compiler.InvalidConfigException;
import com.google.gxp.compiler.alerts.AlertPolicy;
import com.google.gxp.compiler.alerts.AlertSet;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.fs.FileSystem;
import com.google.gxp.compiler.fs.InMemoryFileSystem;
import com.google.gxp.compiler.fs.JavaFileManagerImpl;
import com.google.gxp.compiler.fs.JavaFileRef;
import com.google.gxp.compiler.fs.SystemFileSystem;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for all GxpTemplates that compile their source .gxp at runtime.
 */
public class StubGxpTemplate extends GxpTemplate {

  // system filesystem
  // can be changed for testing
  protected static FileSystem systemFS = SystemFileSystem.INSTANCE;

  public static void setSystemFileSystem(FileSystem systemFS) {
    StubGxpTemplate.systemFS = systemFS;
  }

  public static void setJavaCompiler(JavaCompiler javaCompiler) {
    StubGxpTemplate.javaCompiler = javaCompiler;
  }

  protected static FileRef parseFilename(String filename) {
    return systemFS.parseFilename(filename);
  }

  protected static Set<FileRef> parseFilenames(String... filenames) {
    Set<FileRef> fileRefs = Sets.newHashSet();
    for (String filename : filenames) {
      fileRefs.add(parseFilename(filename));
    }
    return fileRefs;
  }

  // java compiler
  // can be changed for testing
  private static JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

  /**
   * Reconstruct an {@code AlertPolicy} that has been serialized to a byte array.
   */
  protected static AlertPolicy createAlertPolicy(byte[] bytes) {
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      ObjectInputStream ois = new ObjectInputStream(bais);
      return (AlertPolicy) ois.readObject();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected static FileRef compileGxp(InMemoryFileSystem outFs,
                                      Set<FileRef> srcGxps,
                                      Set<FileRef> srcSchemas,
                                      Set<FileRef> srcPaths,
                                      String javaBase,
                                      long compilationVersion,
                                      AlertPolicy alertPolicy)
      throws GxpCompilationException {

    String javaFile = javaBase + compilationVersion + ".java";

    Configuration configuration = new RuntimeConfiguration(systemFS, outFs, srcGxps, srcSchemas,
                                                           srcPaths, javaFile, compilationVersion,
                                                           alertPolicy);
    try {
      // Perform GXP Compilation
      AlertSet alertSet = new Compiler(configuration).call();

      // check for gxp compilation errors
      if (alertSet.hasErrors(alertPolicy)) {
        throw new GxpCompilationException.Gxp(alertPolicy, alertSet);
      }
    } catch (InvalidConfigException e) {
      throw new GxpCompilationException.Throw(e);
    }

    return outFs.parseFilename(javaFile);
  }

  protected static Map<String, Method> compileJava(InMemoryFileSystem outFs,
                                                   String classBase,
                                                   final long compilationVersion) {
    // compile java
    DiagnosticCollector<JavaFileObject> diagnosticCollector
        = new DiagnosticCollector<JavaFileObject>();

    JavaFileManager javaFileManager
        = new JavaFileManagerImpl(javaCompiler.getStandardFileManager(diagnosticCollector,
                                                                      Locale.US,
                                                                      Charsets.US_ASCII),
                                  outFs);
    String className = classBase + compilationVersion;

    try {
      JavaFileObject compilationUnit = javaFileManager.getJavaFileForInput(
          StandardLocation.SOURCE_PATH, className, JavaFileObject.Kind.SOURCE);

      Iterable<JavaFileObject> compilationUnits = ImmutableList.of(compilationUnit);

      javaCompiler.getTask(null, javaFileManager, diagnosticCollector,
                           null, null, compilationUnits).call();

      List<Diagnostic<? extends JavaFileObject>> diagnostics =
          filterErrors(diagnosticCollector.getDiagnostics());

      if (!diagnostics.isEmpty()) {
        throw new GxpCompilationException.Java(diagnostics);
      }

      List<byte[]> classFiles = Lists.newArrayList();
      for (FileRef fileRef : outFs.getManifest()) {
        if (fileRef.getKind().equals(JavaFileObject.Kind.CLASS)) {
          String outputClassName = javaFileManager.inferBinaryName(StandardLocation.CLASS_OUTPUT,
                                                                   new JavaFileRef(fileRef));
          if (outputClassName.equals(className) || outputClassName.startsWith(className + "$")) {
            classFiles.add(ByteStreams.toByteArray(fileRef.openInputStream()));
          }
        }
      }

      // A single java compile can generate many .class files due to inner classes, and it
      // is difficult to know what order to load them in to avoid NoClassDefFoundErrors,
      // so what we do is go through the whole list attempting to load them all, keeping
      // track of which ones file with NoClassDefFoundError.  Then we loop and try again.
      // This should eventually work no matter what order the files come in.
      //
      // We have an additional check to make sure that at least one file is loaded each
      // time through the loop to prevent infinite looping.
      //
      // I'm not entirely happy with this scheme, but it's the best I can come up with
      // for now.
      int oldCount, newCount;
      do {
        oldCount = classFiles.size();
        classFiles = defineClasses(classFiles);
        newCount = classFiles.size();
      } while (newCount != 0 && newCount != oldCount);

      // get the main class generated durring this compile
      Class c = Class.forName(className);

      // get methods
      return getMethodMap(c);
    } catch (GxpCompilationException e) {
      throw e;
    } catch (Throwable e) {
      throw new GxpCompilationException.Throw(e);
    }
  }

  protected static Map<String, Method> getMethodMap(Class c) {
    Map<String, Method> map = Maps.newHashMap();
    for (Method method : c.getMethods()) {
      map.put(method.getName(), method);
    }
    return map;
  }

  private static List<byte[]> defineClasses(List<byte[]> classFiles)
      throws Throwable {
    List<byte[]> failures = Lists.newArrayList();
    for (byte[] classFile : classFiles) {
      try {
        defineClass(classFile);
      } catch (NoClassDefFoundError e) {
        failures.add(classFile);
      }
    }
    return failures;
  }

  // TODO(harryh): Consider strategies for detecting and generating an error
  //               when, at runtime, two parameters of the same type but different
  //               names are swapped. Name mangling of the method name with a
  //               parameters name list might be a good way to do this.

  protected static Object exec(Map<String, Method> methods, String function,
                               Object[] args) throws Throwable {
    try {
      return methods.get(function).invoke(null, args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    } catch (IllegalArgumentException e) {
      throw new GxpCompilationException.GxpParamChange(e);
    } catch (Exception e) {
      throw new GxpCompilationException.Throw(e);
    }
  }

  protected static <T> T execNoExceptions(Map<String, Method> methods,
                                          String function,
                                          Object[] args) {
    try {
      @SuppressWarnings("unchecked")
      T result = (T)exec(methods, function, args);
      return result;
    } catch (Error e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable t) {
      throw new GxpCompilationException.Throw(t);
    }
  }

  /**
   * @return a filtered list of {@code Diagnostic}s that only contains
   * errors.
   */
  private static <T> List<Diagnostic<? extends T>> filterErrors(
      List<Diagnostic<? extends T>> diagnostics) {
    List<Diagnostic<? extends T>> newList = Lists.newArrayList();
    for (Diagnostic<? extends T> diagnostic : diagnostics) {
      if (diagnostic.getKind().equals(Diagnostic.Kind.ERROR)) {
        newList.add(diagnostic);
      }
    }
    return Collections.unmodifiableList(newList);
  }

  private static final Method DEFINE_CLASS =
      AccessController.doPrivileged(new PrivilegedAction<Method>() {
        public Method run() {
          try {
            Class<ClassLoader> loader = ClassLoader.class;
            Method m = loader.getDeclaredMethod("defineClass",
                                                new
                                                Class[]{ String.class,
                                                         byte[].class,
                                                         Integer.TYPE,
                                                         Integer.TYPE,
                                                         ProtectionDomain.class });
            m.setAccessible(true);
            return m;
          } catch (NoSuchMethodException e) {
            throw new RuntimeException();
          }
        }
      });

  private static final ProtectionDomain PROTECTION_DOMAIN =
      StubGxpTemplate.class.getProtectionDomain();


  /**
   * Define a class using the SystemClassLoader so that the class has access to
   * package private items in its java package.
   */
  private static Class defineClass(byte[] classFile)
      throws Throwable {
    Object[] args = new Object[]{ null, classFile,
                                  new Integer(0), new Integer(classFile.length),
                                  PROTECTION_DOMAIN };
    try {
      return (Class) DEFINE_CLASS.invoke(ClassLoader.getSystemClassLoader(), args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  /**
   * The pattern for a line directive; 1->file 2->line 3->col.
   */
  private final static Pattern LINE_DIRECTIVE = Pattern.compile("^.* // (.*): L(\\d*), C(\\d*)$");

  /**
   * Examine each element of the stack trace that belongs to this throwable
   * looking for a filename that matches the source file for this template.
   * If we find a match, rewrite the filename and line number. The line number
   * is based on line # comments in the source file.
   */
  protected static void rewriteStackTraceElements(Throwable throwable, FileRef sourceFile) {
    try {
      if (sourceFile != null) {
        String sourceFileName = sourceFile.getName().substring(1).replace('/', '.');
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
          if (sourceFileName.equals(stackTrace[i].getFileName())) {
            // get source name
            String[] parts = sourceFileName.split("[\\.\\$]");
            String sourceName = parts[parts.length-3] + ".gxp";

            // get source line
            String line = CharStreams
                .readLines(sourceFile.openReader(Charsets.UTF_8))
                .get(stackTrace[i].getLineNumber() - 1);
            Matcher m = LINE_DIRECTIVE.matcher(line);
            int sourceLine = m.find() ? Integer.valueOf(m.group(2)) : -1;

            // fix class name
            String className = stackTrace[i].getClassName().split("\\$")[0];

            stackTrace[i] = new StackTraceElement(className,
                                                  stackTrace[i].getMethodName(),
                                                  sourceName,
                                                  sourceLine);
            throwable.setStackTrace(stackTrace);
            return;
          }
        }
      }
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }
}
