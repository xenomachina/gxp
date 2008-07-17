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

package com.google.gxp.compiler.servicedir;

import com.google.common.collect.ImmutableMap;
import com.google.gxp.compiler.GxpcTestCase;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.ClassImport;
import com.google.gxp.compiler.base.Implementable;
import com.google.gxp.compiler.base.Import;
import com.google.gxp.compiler.base.InstanceCallable;
import com.google.gxp.compiler.base.PackageImport;
import com.google.gxp.compiler.base.TemplateName;
import com.google.gxp.testing.StubCallable;

import java.util.List;
import java.util.Map;

/**
 * Tests of {@link ScopedServiceDirectory}.
 */
public class ScopedServiceDirectoryTest extends GxpcTestCase {

  private static Callable FOO_BAR_BAZ =  new StubCallable("FOO_BAR_BAZ");
  private static Callable FOO_BAR_QUUX = new StubCallable("FOO_BAR_QUUX");
  private static Callable BIZ_BUZ_BAZ =  new StubCallable("BIZ_BUZ_BAZ");
  private static Callable BIZ_BUZ_BOOZ = new StubCallable("BIZ_BUZ_BOOZ");
  private static Callable DAZ_CAZ_BAZ = new StubCallable("DAZ_CAZ_BAZ");

  private final ServiceDirectory BASE_SERVICE_DIR = createBaseServiceDirectory();

  private AlertSink ALERT_SINK;

  private static class SimpleServiceDirectory implements ServiceDirectory {
    private final Map<TemplateName, Callable> callableMap;

    SimpleServiceDirectory(Map<TemplateName, Callable> callableMap) {
      this.callableMap = callableMap;
    }

    @Override
    public Callable getCallable(TemplateName templateName) {
      return callableMap.get(templateName);
    }

    @Override
    public InstanceCallable getInstanceCallable(TemplateName templateName) {
      return null;
    }

    @Override
    public Implementable getImplementable(TemplateName templateName) {
      return null;
    }
  }

  private ServiceDirectory createBaseServiceDirectory() {
    ImmutableMap.Builder<TemplateName, Callable> mapBuilder =
        ImmutableMap.builder();
    mapBuilder.put(fqTemplateName("foo.bar.Baz"), FOO_BAR_BAZ);
    mapBuilder.put(fqTemplateName("foo.bar.Quux"), FOO_BAR_QUUX);
    mapBuilder.put(fqTemplateName("biz.buz.Baz"), BIZ_BUZ_BAZ);
    mapBuilder.put(fqTemplateName("biz.buz.Booz"), BIZ_BUZ_BOOZ);
    mapBuilder.put(fqTemplateName("daz.caz.Baz"), DAZ_CAZ_BAZ);
    return new SimpleServiceDirectory(mapBuilder.build());
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    ALERT_SINK = new AlertSetBuilder();
  }

  public void testNoImportsUnqualifiedRef() throws Exception {
    List<Import> imports = list();
    ServiceDirectory dir;

    // Find template in current package.
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo.bar", imports);
    assertSame(FOO_BAR_BAZ, dir.getCallable(templateName("Baz")));

    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "biz.buz", imports);
    assertSame(BIZ_BUZ_BAZ, dir.getCallable(templateName("Baz")));

    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo.bar", imports);
    assertSame(FOO_BAR_QUUX, dir.getCallable(templateName("Quux")));

    // If not in current package, return null.
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "biz.buz", imports);
    assertNull(dir.getCallable(templateName("Quux")));
  }

  public void testClassImportsWithUnQualifiedRefs() throws Exception {
    List<ClassImport> imports;
    ServiceDirectory dir;

    // Imported class has precedence over class in current package.
    imports = list(classImport("biz.buz.Baz"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo.bar", imports);
    assertSame(BIZ_BUZ_BAZ, dir.getCallable(templateName("Baz")));

    // Imported class has different base name, so get from current package.
    imports = list(classImport("biz.buz.Booz"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo.bar", imports);
    assertSame(FOO_BAR_BAZ, dir.getCallable(templateName("Baz")));

    // Imported class has different base name, but basename not in currnet
    // package.
    imports = list(classImport("biz.buz.Booz"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "zip.zap", imports);
    assertNull(dir.getCallable(templateName("Baz")));

    // Imported class has base name we're looking for, but class doesn't exist
    // in base ServiceDirectory.
    imports = list(classImport("zip.zap.Zork"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo.bar", imports);
    assertNull(dir.getCallable(templateName("Zork")));

    // ... but we can still find classes in the current package.
    assertSame(FOO_BAR_BAZ, dir.getCallable(templateName("Baz")));

    // However, a nonexistant class that's been explicitly imported shadows
    // classes in the current package, making it impossible to access them by
    // base name.
    imports = list(classImport("zip.zap.Baz"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo.bar", imports);
    assertNull(dir.getCallable(templateName("Baz")));
  }

  public void testPackageImportsWithUnQualifiedRefs() throws Exception {
    List<PackageImport> imports;
    ServiceDirectory dir;

    // Find class in imported package if it isn't in our package.
    imports = list(packageImport("biz.buz"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "zip.zap", imports);
    assertSame(BIZ_BUZ_BAZ, dir.getCallable(templateName("Baz")));

    // Importing the same package twice is okay.
    imports = list(packageImport("biz.buz"),
                   packageImport("biz.buz"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "zip.zap", imports);
    assertSame(BIZ_BUZ_BAZ, dir.getCallable(templateName("Baz")));

    // If not in imported package but in current package, get the one in
    // current package.
    imports = list(packageImport("zip.zap"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo.bar", imports);
    assertSame(FOO_BAR_BAZ, dir.getCallable(templateName("Baz")));

    // Actually, even if it's in an imported package, the current package takes
    // precedence.
    imports = list(packageImport("foo.bar"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "biz.buz", imports);
    assertSame(BIZ_BUZ_BAZ, dir.getCallable(templateName("Baz")));

    // Ambiguous references are not resolved.
    imports = list(packageImport("foo.bar"),
                   packageImport("biz.buz"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "zip.zap", imports);
    assertNull(dir.getCallable(templateName("Baz")));

    // The same set of imports behave fine with non-ambiguous references,
    // though.
    assertSame(BIZ_BUZ_BOOZ, dir.getCallable(templateName("Booz")));
    assertSame(FOO_BAR_QUUX, dir.getCallable(templateName("Quux")));

    // The current package takes precedence over package imports even in the
    // case of ambiguity.
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "daz.caz", imports);
    assertSame(DAZ_CAZ_BAZ, dir.getCallable(templateName("Baz")));

    // The existence of this ambiguity doesn't stop us from being able to look
    // up other things in those packages.
    assertSame(BIZ_BUZ_BOOZ, dir.getCallable(templateName("Booz")));
    assertSame(FOO_BAR_QUUX, dir.getCallable(templateName("Quux")));

    // Nor does it make us see things that aren't there.
    assertNull(dir.getCallable(templateName("Zork")));
  }

  public void testMixedImportsWithUnQualifiedRefs() throws Exception {
    List<Import> imports;
    ServiceDirectory dir;

    // Class import takes precedence over things in current package.
    imports = list(classImport("biz.buz.Baz"),
                   packageImport("zip.zap"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo.bar", imports);
    assertSame(BIZ_BUZ_BAZ, dir.getCallable(templateName("Baz")));

    // Class import takes precedence over package import.
    imports = list(classImport("foo.bar.Baz"),
                   packageImport("biz.buz"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "zip.zap", imports);
    assertSame(FOO_BAR_BAZ, dir.getCallable(templateName("Baz")));

    // Importing the same package is not considered an ambiguity.
    imports = list(classImport("biz.buz.Baz"),
                   packageImport("biz.buz"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo.bar", imports);
    assertSame(BIZ_BUZ_BAZ, dir.getCallable(templateName("Baz")));

    // Importing classes and packages doesn't interfere with getting stuff from
    // the current package.
    imports = list(classImport("biz.buz.Booz"),
                   packageImport("biz.buz"));
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo.bar", imports);
    assertSame(FOO_BAR_BAZ, dir.getCallable(templateName("Baz")));

  }

  private void assertQualifiedRefsWork(List<? extends Import> imports)
      throws Exception {
    ServiceDirectory dir;

    // In same package.
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo.bar", imports);
    assertSame(FOO_BAR_BAZ, dir.getCallable(templateName("foo.bar.Baz")));

    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "biz.buz", imports);
    assertSame(BIZ_BUZ_BAZ, dir.getCallable(templateName("biz.buz.Baz")));

    // In different package.
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "biz.buz", imports);
    assertSame(FOO_BAR_QUUX, dir.getCallable(templateName("foo.bar.Quux")));

    // In different package, but with basename that also exists in this
    // package.
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo.bar", imports);
    assertSame(BIZ_BUZ_BAZ, dir.getCallable(templateName("biz.buz.Baz")));

    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "biz.buz", imports);
    assertSame(FOO_BAR_BAZ, dir.getCallable(templateName("foo.bar.Baz")));

    // Nonexistant thing in other package.
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "biz.buz", imports);
    assertNull(dir.getCallable(templateName("foo.bar.Quacks")));

    // Some tests with shorter package names.
    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo.bar", imports);
    assertNull(dir.getCallable(templateName("bar.Buz")));

    dir = new ScopedServiceDirectory(ALERT_SINK, BASE_SERVICE_DIR, "foo", imports);
    assertNull(dir.getCallable(templateName("bar.Buz")));

    // Class import has precedence over package import.
  }

  public void testNoImportsWithQualifiedRefs() throws Exception {
    List<Import> imports = list();
    assertQualifiedRefsWork(imports);
  }

  public void testClassImportsWithQualifiedRefs() throws Exception {
    assertQualifiedRefsWork(list(
          classImport("foo.bar.Baz"),
          classImport("biz.buz.Booz")
        ));
  }

  public void testPackageImportsWithQualifiedRefs() throws Exception {
    assertQualifiedRefsWork(list(
          packageImport("foo.bar"),
          packageImport("i.dont.exist")
        ));
  }

  public void testMixedImportsWithQualifiedRefs() throws Exception {
    assertQualifiedRefsWork(list(
          classImport("biz.buz.Baz"),
          packageImport("foo.bar"),
          packageImport("i.dont.exist")
        ));
  }
}
