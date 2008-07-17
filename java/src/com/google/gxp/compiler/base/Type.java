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
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;

import java.util.regex.Pattern;

/**
 * A Node representing a type.  Some types are understood by gxp and should
 * be converted to native types by code generators.  Other types are native
 * and are opaque to gxp.
 */
public abstract class Type extends SerializableAbstractNode {
  protected Type(Node fromNode) {
    super(fromNode);
  }

  protected Type(SourcePosition sourcePosition, String displayName) {
    super(sourcePosition, displayName);
  }

  /**
   * Indicates that this {@code Type} is only allowed for {@code <gxp:param>}
   * and not in {@code <gxp:loop>} or {@code <gxp:abbr>}
   */
  public abstract boolean onlyAllowedInParam();

  /**
   * Indicates if this {@code Type} is a content type. Subtypes that are will
   * override this method.
   */
  public boolean isContent() {
    return false;
  }

  /**
   * Indicates if this type can take a default parameter.
   */
  public abstract boolean takesDefaultParam();

  /**
   * Indicates if this type can take a regex parameter. Subtypes will
   * override this method if they can.
   */
  public boolean takesRegexParam() {
    return false;
  }

  /**
   * Indicates if this type can take a constructor parameter. Subtypes will
   * override this method if they can.
   */
  public boolean takesConstructorParam() {
    return false;
  }

  /**
   * Returns the default value for parameters of this type (if no default is
   * manually specified), or null if there is no default default value.
   */
  public Expression getDefaultValue() {
    return null;
  }

  /**
   * Returns a {@code Pattern} to check static attribute values of this type
   * against, or {@code null} if this type does not have an intrinsic pattern.
   *
   * @param attrName the (local) name of the attribute to be checked.
   */
  public Pattern getPattern(String attrName) {
    return null;
  }

  /**
   * Either parses the specified (untyped) {@code ObjectConstant}, or returns
   * an {@code ObjectConstant} of this type if the {@code
   * CodeGenerator}/runtime is responsible for parsing it.
   */
  public abstract Expression parseObjectConstant(String paramName,
                                                 ObjectConstant objectConstant,
                                                 AlertSink alertSink);

  public abstract <T> T acceptTypeVisitor(TypeVisitor<T> visitor);

  @Override
  public String toString() {
    return getClass().getSimpleName() + "@" + getSourcePosition();
  }

  /**
   * @return true if this matches that.  Used to verify that a template
   * implementing an interface has matching types for all parameters.
   */
  public abstract boolean matches(Type that);

  @Override
  public abstract boolean equals(Object that);

  /**
   * Helper method for implementing {@code equals(Object)} in subclasses.
   */
  protected final boolean equalsType(Type that) {
    return equalsAbstractNode(that);
  }

  @Override
  public abstract int hashCode();

  /**
   * Helper method for implementing {@code hashCode()} in subclasses.
   */
  protected final int typeHashCode() {
    return Objects.hashCode(
        abstractNodeHashCode());
  }
}
