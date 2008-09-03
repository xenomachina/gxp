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

package com.google.gxp.compiler.base;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.InvalidNameError;
import com.google.gxp.compiler.alerts.common.UnqualifiedNameError;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The name of a template. Essentially a pair of a package name and a base
 * name. The package name may be null, which indicates that this is an
 * "unqualified" template name (otherwise it's "fully-qualified").
 */
@SuppressWarnings("serial") // let java pick the SerialVersionUID
public abstract class TemplateName implements Serializable {
  private final String packageName;
  private final String baseName;
  private final boolean isValid;

  private static final Pattern PACKAGE_NAME_PATTERN =
      Pattern.compile("\\p{Alpha}\\w*(?:\\.\\p{Alpha}\\w*)*");

  private static final Pattern BASE_NAME_PATTERN =
      Pattern.compile("\\p{Alpha}\\w*");

  public static TemplateName create(String packageName, String baseName) {
    if (packageName == null) {
      return new Unqualified(baseName);
    } else {
      return new FullyQualified(packageName, baseName);
    }
  }

  /**
   * @param packageName package name of template, or null if this is not a
   * fully qualified name
   * @param baseName base name of template
   */
  private TemplateName(String packageName, String baseName) {
    this.packageName = packageName;
    this.baseName = Preconditions.checkNotNull(baseName);
    this.isValid = isValidPackageName(packageName)
        && isValidBaseName(baseName);
  }

  /**
   * A fully qualified {@code TemplateName}. eg: "com.google.foo.Bar"
   */
  public static final class FullyQualified extends TemplateName {
    public FullyQualified(String packageName, String baseName) {
      super(Preconditions.checkNotNull(packageName), baseName);
    }

    @Override
    public String toString() {
      return getPackageName() + "." + getBaseName();
    }
  }

  /**
   * An unqualified {@code TemplateName}. eg: "Bar"
   */
  private static final class Unqualified extends TemplateName {
    public Unqualified(String baseName) {
      super(null, baseName);
    }

    @Override
    public String toString() {
      return getBaseName();
    }
  }

  public String getPackageName() {
    return packageName;
  }

  public String getBaseName() {
    return baseName;
  }

  public boolean isValid() {
    return isValid;
  }

  private static boolean isValidPackageName(String packageName) {
    return packageName == null
        || PACKAGE_NAME_PATTERN.matcher(packageName).matches();
  }

  public static boolean isValidBaseName(String baseName) {
    return BASE_NAME_PATTERN.matcher(baseName).matches();
  }

  // Matches any string that has a dot in it, and splits it on the last dot
  private static final Pattern DOTTED_NAME_PATTERN =
      Pattern.compile("(.*)\\.([^\\.]*)", Pattern.DOTALL);

  // Used to remove any whitespace that appears on either side of a period
  private static final Pattern WHITESPACE_REMOVER_PATTERN =
      Pattern.compile("(\\s*)\\.(\\s*)");

  /**
   * Parses the supplied "dotted name". For example, {@code "foo.bar.Baz"} will
   * parse into the equivalent of {@code new TemplateName("foo.bar", "Baz")}.
   * If the name cannot be parsed, the problem will be reported to the
   * supplied {@code AlertSink}.
   */
  public static TemplateName parseDottedName(AlertSink alertSink,
                                             SourcePosition sourcePosition,
                                             String dottedName) {

    dottedName = WHITESPACE_REMOVER_PATTERN.matcher(dottedName).replaceAll(".");

    String packageName;
    String baseName;
    Matcher matcher = DOTTED_NAME_PATTERN.matcher(dottedName);
    if (matcher.matches()) {
      packageName = matcher.group(1);
      baseName = matcher.group(2);
    } else {
      packageName = null;
      baseName = dottedName;
    }
    if (!isValidPackageName(packageName) || !isValidBaseName(baseName)) {
      if (alertSink != null) {
        alertSink.add(new InvalidNameError(sourcePosition, dottedName));
      }
    }
    return create(packageName, baseName);
  }

  /**
   * Parses the supplied "dotted name", which must be fully qualified.
   * If the name cannot be parsed or is not fully-qualified then null will be
   * returned, and the problem will be reported to the supplied {@code
   * AlertSink}.
   */
  public static TemplateName.FullyQualified parseFullyQualifiedDottedName(
      AlertSink alertSink, SourcePosition sourcePosition, String dottedName) {
    TemplateName result = parseDottedName(alertSink, sourcePosition,
                                          dottedName);
    if (result instanceof TemplateName.FullyQualified || result == null) {
      return (TemplateName.FullyQualified) result;
    } else {
      if (alertSink != null) {
        alertSink.add(new UnqualifiedNameError(sourcePosition, dottedName));
      }
      return null;
    }
  }

  /**
   * Parses the supplied "dotted name", as above, but does not generate
   * any alerts in the case of a malformed name.
   */
  public static TemplateName.FullyQualified parseFullyQualifiedDottedName(
      String dottedName) {
    return parseFullyQualifiedDottedName(null, null, dottedName);
  }

  /**
   * @return the (dotted, if fully-qualified) name that this {@code
   * TemplateName} represents.
   */
  @Override
  public abstract String toString();

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof TemplateName && equals((TemplateName) that));
  }

  public boolean equals(TemplateName that) {
    return Objects.equal(this.packageName, that.packageName)
        && Objects.equal(this.baseName, that.baseName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        packageName,
        baseName);
  }
}
