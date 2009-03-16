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

package com.google.gxp.testing.dynamic;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.io.Bytes;
import com.google.gxp.base.GxpContext;
import com.google.gxp.base.dynamic.StubGxpTemplate;
import com.google.gxp.compiler.codegen.DefaultCodeGeneratorFactory;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.fs.JavaFileManagerImpl;
import com.google.gxp.compiler.fs.SystemFileSystem;
import com.google.gxp.testing.BaseBuildingTestCase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * Base {@code TestCase} for gxpc tests that require building and running GXPs
 * as part of the tests. Provide utility methods for running GXPs that were
 * built in memory.
 */
public abstract class BaseRunningTestCase extends BaseBuildingTestCase {
  protected static final JavaCompiler SYSTEM_JAVA_COMPILER = ToolProvider.getSystemJavaCompiler();

  protected static final Class[] DEFAULT_WRITE_PARAMS =
      new Class[] { Appendable.class, GxpContext.class };

  protected Method writeMethod;

  protected Map<String, Method> getDefaultMethods = Maps.newHashMap();
  protected CountingJavaCompiler countingJavaCompiler =
      new CountingJavaCompiler(SYSTEM_JAVA_COMPILER);

  private final DefaultCodeGeneratorFactory codeGeneratorFactory;

  protected BaseRunningTestCase() {
    codeGeneratorFactory = new DefaultCodeGeneratorFactory();
    codeGeneratorFactory.setSourcePaths(Collections.singleton(fs.getRoot()));
    codeGeneratorFactory.setSchemaFiles(Collections.<FileRef>emptySet());
    codeGeneratorFactory.setAlertPolicy(getAlertPolicy());
  }

  @Override
  protected DefaultCodeGeneratorFactory getCodeGeneratorFactory() {
    return codeGeneratorFactory;
  }

  @Override
  protected void setUp() {
    StubGxpTemplate.setSystemFileSystem(fs);
    StubGxpTemplate.setJavaCompiler(countingJavaCompiler);
  }

  @Override
  protected void tearDown() {
    StubGxpTemplate.setSystemFileSystem(SystemFileSystem.INSTANCE);
    StubGxpTemplate.setJavaCompiler(SYSTEM_JAVA_COMPILER);
  }

  protected void compileAndLoad(FileRef gxp, Class... extraWriteParams) throws Exception {

    // compile gxp
    getCodeGeneratorFactory().setSourceFiles(Collections.singletonList(gxp));
    compileFiles(gxp);

    String className = gxp.removeExtension().getName().substring(1).replace('/', '.');

    // compile java
    DiagnosticCollector<JavaFileObject> diagnosticCollector
        = new DiagnosticCollector<JavaFileObject>();

    JavaFileManager javaFileManager
        = new JavaFileManagerImpl(SYSTEM_JAVA_COMPILER.getStandardFileManager(diagnosticCollector,
                                                                              Locale.US,
                                                                              Charsets.US_ASCII),
                                  fs);

    JavaFileObject compilationUnit = javaFileManager.getJavaFileForInput(
        StandardLocation.SOURCE_PATH, className, Kind.SOURCE);

    Iterable<JavaFileObject> compilationUnits = Collections.singleton(compilationUnit);

    SYSTEM_JAVA_COMPILER.getTask(null, javaFileManager, diagnosticCollector,
                                 null, null, compilationUnits).call();

    // get method
    ClassLoader cl = new JavaFileManagerClassLoader(javaFileManager);
    Class<?> cls = cl.loadClass(className);

    writeMethod = cls.getMethod("write", ObjectArrays.concat(DEFAULT_WRITE_PARAMS,
                                                             extraWriteParams,
                                                             Class.class));
    for (Method m : cls.getMethods()) {
      if (m.getName().startsWith("getDefault")) {
        getDefaultMethods.put(m.getName(), m);
      }
    }
  }

  protected void assertOutputEquals(String expected, Object... params) throws Throwable {
    StringBuilder sb = new StringBuilder();
    invoke(sb, new GxpContext(Locale.US), params);
    assertEquals(expected, sb.toString());
  }

  protected void assertDefaultEquals(String param, Object value) throws Throwable {
    param = Character.toUpperCase(param.charAt(0)) + param.substring(1);
    Method m = getDefaultMethods.get("getDefault" + param);
    assertEquals(value, m.invoke(null, new Object[] {}));
  }

  protected void assertGxpCompilationError() throws Throwable {
    // precall isTopLevalCall() so the exception isn't caught internally
    GxpContext gxpContext = new GxpContext(Locale.US);
    gxpContext.isTopLevelCall();

    try {
      invoke(new StringBuilder(), gxpContext);
      fail("should have thrown a Gxp compilation exception.");
    } catch (com.google.gxp.base.dynamic.GxpCompilationException.Gxp e) {
      // good!
    }
  }

  protected void assertJavaCompilationError() throws Throwable {
    // precall isTopLevalCall() so the exception isn't caught internally
    GxpContext gxpContext = new GxpContext(Locale.US);
    gxpContext.isTopLevelCall();

    try {
      invoke(new StringBuilder(), gxpContext);
      fail("should have thrown a Java compilation exception.");
    } catch (com.google.gxp.base.dynamic.GxpCompilationException.Java e) {
      // good!
    }
  }

  protected void assertGxpParamChangeError(Object... params) throws Throwable {
    // precall isTopLevalCall() so the exception isn't caught internally
    GxpContext gxpContext = new GxpContext(Locale.US);
    gxpContext.isTopLevelCall();

    try {
      invoke(new StringBuilder(), gxpContext, params);
      fail("should have thrown a gxp param change exception.");
    } catch (com.google.gxp.base.dynamic.GxpCompilationException.GxpParamChange e) {
      // good!
    }
  }

  protected void assertCompilationCountEquals(int count) {
    assertEquals(count, countingJavaCompiler.getCompilationCount());
  }

  protected void invoke(Appendable appendable, GxpContext gxpContext, Object... rest)
      throws Throwable {
    try {
      Object[] params = new Object[2 + rest.length];
      params[0] = appendable;
      params[1] = gxpContext;
      System.arraycopy(rest, 0, params, 2, rest.length);
      writeMethod.invoke(null, params);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  /**
   * A {@code ClassLoader} that loads .class files from the supplied
   * {@code JavaFileManager}.
   */
  protected static class JavaFileManagerClassLoader extends ClassLoader {
    private final JavaFileManager jfm;

    public JavaFileManagerClassLoader(JavaFileManager jfm) {
      this.jfm = Preconditions.checkNotNull(jfm);
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
      try {
        JavaFileObject file = jfm.getJavaFileForInput(StandardLocation.CLASS_OUTPUT,
                                                      name, Kind.CLASS);
        if (file == null) {
          throw new FileNotFoundException();
        }
        byte[] bytes = Bytes.toByteArray(file.openInputStream());
        return defineClass(name, bytes, 0, bytes.length);
      } catch (IOException e) {
        throw new ClassNotFoundException();
      }
    }
  }

  /**
   * A {@code JavaCompiler} that counts the number of times that
   * {@code getTask} has been called.
   */
  private static class CountingJavaCompiler implements JavaCompiler {
    private final JavaCompiler delegate;
    private int compilationCount = 0;

    public CountingJavaCompiler(JavaCompiler delegate) {
      this.delegate = Preconditions.checkNotNull(delegate);
    }

    public int getCompilationCount() {
      return compilationCount;
    }

    public Set<SourceVersion> getSourceVersions() {
      return delegate.getSourceVersions();
    }

    public StandardJavaFileManager getStandardFileManager(
        DiagnosticListener<? super JavaFileObject> diagnosticListener,
        Locale locale, Charset charset) {
      return delegate.getStandardFileManager(diagnosticListener, locale, charset);
    }

    public CompilationTask getTask(Writer out, JavaFileManager fileManager,
                                   DiagnosticListener<? super JavaFileObject> diagnosticListener,
                                   Iterable<String> options, Iterable<String> classes,
                                   Iterable<? extends JavaFileObject> compilationUnits) {
      compilationCount++;
      return delegate.getTask(out, fileManager, diagnosticListener, options,
                              classes, compilationUnits);
    }

    public int isSupportedOption(String option) {
      return delegate.isSupportedOption(option);
    }

    public int run(InputStream in, OutputStream out, OutputStream err, String... arguments) {
      return delegate.run(in, out, err, arguments);
    }
  }
}
