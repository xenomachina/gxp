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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.base.Callable;
import com.google.gxp.compiler.base.ClassImport;
import com.google.gxp.compiler.base.DefaultingImportVisitor;
import com.google.gxp.compiler.base.Implementable;
import com.google.gxp.compiler.base.Import;
import com.google.gxp.compiler.base.InstanceCallable;
import com.google.gxp.compiler.base.PackageImport;
import com.google.gxp.compiler.base.TemplateName;

import java.util.*;

/**
 * A {@code ServiceDirectory} that looks up resources with a specific "scope".
 * This scope includes a current package, and a set of class and/or package
 * imports. A {@code ScopedServiceDirectory} acts as a decorator for another
 * {@code ServiceDirectory}, intercepting requests for unqualified resources
 * and qualifying them with respect to its scope. Requests for fully qualified
 * resources are passed through to the underlying {@code ServiceDirectory}
 * without modification.
 *
 * <p>When attempting to resolve an unqualified reference the rules are as
 * close to the rules of the Java proramming language as possible:
 * <ol>
 * <li>class imports have highest precedence. Class imports are not allowed to
 * be ambiguous.
 * <li>the current package has the next highest precedence.
 * <li>package imports have the lowest precedence. Ambigous references via
 * package imports are ignored. That is, if something cannot be found via a
 * class import or in the current package, but can be found via two or more
 * distinct package imports, then it is considered to not have been found at
 * all.
 * </ol>
 */
public class ScopedServiceDirectory implements ServiceDirectory {
  private final String packageName;
  private final Set<String> packageImports;
  private final Map<String, TemplateName> classImports;
  private final ServiceDirectory baseServiceDirectory;
  private final Function<TemplateName, Callable> callableGetter =
      new Function<TemplateName, Callable>() {
        public Callable apply(TemplateName from) {
          return baseServiceDirectory.getCallable(from);
        }
      };
  private final Function<TemplateName, InstanceCallable> instanceCallableGetter =
      new Function<TemplateName, InstanceCallable>() {
        public InstanceCallable apply(TemplateName from) {
          return baseServiceDirectory.getInstanceCallable(from);
        }
      };
  private final Function<TemplateName, Implementable> implementableGetter =
      new Function<TemplateName, Implementable>() {
        public Implementable apply(TemplateName from) {
          return baseServiceDirectory.getImplementable(from);
        }
      };

  public ScopedServiceDirectory(AlertSink alertSink,
                                final ServiceDirectory baseServiceDirectory,
                                String packageName,
                                List<? extends Import> imports) {
    this.baseServiceDirectory = Preconditions.checkNotNull(baseServiceDirectory);
    this.packageName = Preconditions.checkNotNull(packageName);

    ImportProcessor visitor = new ImportProcessor(alertSink, packageName);
    for (Import imp : imports) {
      imp.acceptVisitor(visitor);
    }

    this.packageImports = ImmutableSet.copyOf(visitor.getPackageImports());
    this.classImports = ImmutableMap.copyOf(visitor.getClassImports());
  }

  /**
   * @return an ImportVisitor that, upon visiting an import, validates it and
   * populates either classImports or packageImports as appropriate.
   */
  private static class ImportProcessor extends DefaultingImportVisitor<Void> {
    private final AlertSink alertSink;
    private final Set<String> packageImports = Sets.newHashSet();
    private final Map<String, TemplateName> classImports = Maps.newHashMap();

    public ImportProcessor(AlertSink alertSink, String packageName) {
      this.alertSink = Preconditions.checkNotNull(alertSink);
      packageImports.add(packageName);
    }

    public Set<String> getPackageImports() {
      return packageImports;
    }

    public Map<String, TemplateName> getClassImports() {
      return classImports;
    }

    @Override
    public Void defaultVisitImport(Import imp) {
      // do nothing
      return null;
    }

    @Override
    public Void visitClassImport(ClassImport imp) {
      TemplateName className = imp.getClassName();
      String baseName = className.getBaseName();
      TemplateName redundantImport = classImports.get(baseName);
      if ((redundantImport != null) && !className.equals(redundantImport)) {
        alertSink.add(new AmbiguousImportError(imp, baseName, redundantImport, className));
      } else {
        classImports.put(baseName, className);
      }
      return null;
    }

    @Override
    public Void visitPackageImport(PackageImport imp) {
      packageImports.add(imp.getPackageName());
      return null;
    }
  }

  private <T> T baseGet(TemplateName templateName, Function<TemplateName, T> underlyingGetter) {
    if (templateName.getPackageName() == null) {
      String baseName = templateName.getBaseName();
      if (classImports.containsKey(baseName)) {
        return underlyingGetter.apply(classImports.get(baseName));
      } else {
        templateName = baseFindUniquePackageMatch(baseName, underlyingGetter);
        if (templateName == null) {
          templateName = TemplateName.create(packageName, baseName);
        }
      }
    }
    return underlyingGetter.apply(templateName);
  }

  /**
   * @return the name of the unique callable that can be found by combining the
   * specified baseName with our package imports. If there is not a unique
   * callable, returns null.
   */
  private <T> TemplateName baseFindUniquePackageMatch(String baseName,
                                                      Function<TemplateName, T> underlyingGetter) {
    TemplateName result = null;
    for (String packageImport : packageImports) {
      TemplateName fqName = new TemplateName.FullyQualified(packageImport, baseName);
      if (underlyingGetter.apply(fqName) != null) {
        if (result != null) {
          // Ambiguous match.
          return null;
        } else {
          result = fqName;
        }
      }
    }
    return result;
  }

  public Callable getCallable(TemplateName templateName) {
    return baseGet(templateName, callableGetter);
  }

  public InstanceCallable getInstanceCallable(TemplateName templateName) {
    return baseGet(templateName, instanceCallableGetter);
  }

  public Implementable getImplementable(TemplateName templateName) {
    return baseGet(templateName, implementableGetter);
  }
}
