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

package com.google.gxp.compiler.reparent;

import com.google.common.base.Objects;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.AbstractNode;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.NativeExpression;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.parser.Namespace;
import com.google.gxp.compiler.parser.NullNamespace;
import com.google.gxp.compiler.schema.Schema;

/**
 * An attribute. This is not exactly the same as an XML attribute. An {@code
 * Attribute} can also be created by a {@code gxp:attr} element, and some
 * attributes get renamed. For example, an XML {@code expr:src} attribute will
 * turn into a plain old {@code src} attribute, but with a dynamic {@code
 * Expression}.
 */
public class Attribute extends AbstractNode {
  private final Namespace namespace;
  private final String name;
  private final Expression value;
  private final Expression condition;
  private final Schema innerSchema;

  /**
   * @param sourcePosition the SourcePosition of this attribute
   * @param displayName the name to use for this attribute in Alerts
   * @param namespace the Namespace of this attribute
   * @param name the (local) name of this attribute
   * @param value the value of this attribute
   * @param innerSchema the innerSchema of this attribute
   */
  public Attribute(SourcePosition sourcePosition, String displayName,
                   Namespace namespace, String name, Expression value,
                   Expression condition, Schema innerSchema) {
    super(sourcePosition, displayName);

    this.namespace = Objects.nonNull(namespace);

    if (name.length() < 1) {
      throw new IllegalArgumentException();
    }
    this.name = name;

    this.value = Objects.nonNull(value);
    this.condition = condition;
    this.innerSchema = innerSchema;
  }

  /**
   * Helper for constructing an Attribute from a Node.
   *
   * @param fromNode the Node this Attribute was constructed from
   * @param namespace the Namespace of this attribute
   * @param name the (local) name of this attribute
   * @param value the value of this attribute
   * @param innerSchema the innerSchema of this attribute
   */
  public Attribute(Node fromNode, Namespace namespace,
                   String name, Expression value, Expression condition,
                   Schema innerSchema) {
    this(fromNode.getSourcePosition(), fromNode.getDisplayName(),
         namespace, name, value, condition, innerSchema);
  }

  /**
   * Used to create an Attribute from a {@code gxp:attr}
   *
   * @param fromNode the Node this Attribute was constructed from
   * @param name the (local) name of this attribute
   * @param value the value of this attribute
   * @param condition a condition on expressing this attribute
   */
  public Attribute(Node fromNode, String name, Expression value, Expression condition) {
    this(fromNode.getSourcePosition(),
         "'" + name + "' attribute",
         NullNamespace.INSTANCE, name, value, condition, null);
  }

  /**
   * @return the (local) name of this attribute.
   */
  public String getName() {
    return name;
  }

  /**
   * @return the (new) Namespace of this attribute.
   */
  public Namespace getNamespace() {
    return namespace;
  }

  /**
   * @return the value of this attribute.
   */
  public Expression getValue() {
    return value;
  }

  public Expression getCondition() {
    return condition;
  }

  public Schema getInnerSchema() {
    return innerSchema;
  }

  /**
   * Helper for transforming an Attribute's value.
   */
  public Attribute withValue(Expression newValue) {
    return newValue.equals(value)
        ? this
        : new Attribute(this, getNamespace(), getName(), newValue, condition, getInnerSchema());
  }

  /**
   * Helper for transforming an Attribute's condition
   */
  public Attribute withCondition(Expression newCondition) {
    return Objects.equal(newCondition, condition)
        ? this
        : new Attribute(this, getNamespace(), getName(), value, newCondition, getInnerSchema());
  }

  /**
   * Helper for transforming an {@code Attribute}'s innerSchema.
   */
  public Attribute withInnerSchema(Schema innerSchema) {
    return new Attribute(this, getNamespace(), getName(), getValue(),
                         getCondition(), Objects.nonNull(innerSchema));
  }

  /**
   * @return the "expr" value of this attribute.
   */
  public Expression getExprValue() {
    // TODO(laurence): Make this work with gxp:attr.  ie: a gxp:attr should
    // turn into a GxpClosure.  This should probably be hoisted into
    // AttributeMap, and likewise Reparenter.convertAttribute should probably
    // be moved into there as well.  Then AttributeMap can lazily do the
    // conversion, to avoid data loss.
    return (namespace.equals(NullNamespace.INSTANCE) && value instanceof StringConstant)
        ? new NativeExpression(value, ((StringConstant) value).evaluate(), null, null)
        : value;
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof Attribute && equals((Attribute) that));
  }

  public boolean equals(Attribute that) {
    return equalsAbstractNode(that)
        && Objects.equal(getNamespace(), that.getNamespace())
        && Objects.equal(getName(), that.getName())
        && Objects.equal(getValue(), that.getValue())
        && Objects.equal(getCondition(), that.getCondition());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        abstractNodeHashCode(),
        getNamespace(),
        getName(),
        getValue(),
        getCondition());
  }
}
