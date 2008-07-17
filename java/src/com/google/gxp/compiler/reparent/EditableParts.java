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
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.common.BadNodePlacementError;
import com.google.gxp.compiler.base.Concatenation;
import com.google.gxp.compiler.base.Conditional;
import com.google.gxp.compiler.base.Constructor;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.FormalTypeParameter;
import com.google.gxp.compiler.base.ImplementsDeclaration;
import com.google.gxp.compiler.base.Import;
import com.google.gxp.compiler.base.JavaAnnotation;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.ThrowsDeclaration;

import java.util.*;

/**
 * An editable implementation of Parts.  This is the only implementation of
 * {@code Parts}, but the interface makes it a bit easier to understand when
 * an EditableParts is actually supposed to be editable.
 *
 * <p>An EditableParts is intended to be used in several phases:
 * <ul>
 * <li>It's created empty.
 * <li>The various accumulate methods are used to add various types of "parts"
 * to it.
 * <li>It's used as a Parts object (which, itself, has multiple phases).
 * </ul>
 */
class EditableParts implements Parts {
  private final List<Bucket<?>> buckets = Lists.newArrayList();
  private final Bucket<Root> roots = newBucket();
  private final Bucket<Constructor> constructors = newBucket();
  private final Bucket<Expression> values = newBucket();
  private final Bucket<Import> imports = newBucket();
  private final Bucket<ImplementsDeclaration> implementsDeclarations =
      newBucket();
  private final Bucket<ThrowsDeclaration> throwsDeclarations = newBucket();
  private final Bucket<Parameter> parameters = newBucket();
  private final Bucket<FormalTypeParameter> formalTypeParameters = newBucket();
  private final Bucket<Conditional.Clause> clauses = newBucket();
  private final Bucket<JavaAnnotation> javaAnnotations = newBucket();
  private final AttributeMap attrMap;
  private final AlertSink alertSink;
  private final Node forNode;

  /**
   * @param alertSink where {@code Alert}s should be reported.
   * @param forNode the node that contains these parts.
   */
  EditableParts(AlertSink alertSink, Node forNode) {
    this.alertSink = Objects.nonNull(alertSink);
    this.forNode = Objects.nonNull(forNode);
    attrMap = new AttributeMap(alertSink, forNode);
  }

  public List<Root> getRoots() {
    return roots.get();
  }

  public List<Constructor> getConstructors() {
    return constructors.get();
  }

  public List<Import> getImports() {
    return imports.get();
  }

  public List<ImplementsDeclaration> getImplementsDeclarations() {
    return implementsDeclarations.get();
  }

  public List<ThrowsDeclaration> getThrowsDeclarations() {
    return throwsDeclarations.get();
  }

  public List<Parameter> getParameters() {
    return parameters.get();
  }

  public List<FormalTypeParameter> getFormalTypeParameters() {
    return formalTypeParameters.get();
  }

  public Expression getContent() {
    return Concatenation.create(forNode.getSourcePosition(), null,
                                values.get());
  }

  public List<Conditional.Clause> getClauses() {
    return clauses.get();
  }

  public List<JavaAnnotation> getJavaAnnotations() {
    return javaAnnotations.get();
  }

  public AttributeMap getAttributes() {
    return attrMap;
  }

  public void reportUnused() {
    for (Bucket bucket : buckets) {
      bucket.reportUnused();
    }
    attrMap.reportUnusedAttributes();
  }

  /**
   * Adds a {@code Expression} to its appropriate part bucket.
   */
  void accumulate(Expression value) {
    values.add(value);
  }

  /**
   * Adds a {@code Import} to its appropriate part bucket.
   */
  void accumulate(Import imp) {
    if (imports.contains(imp)) {
      alertSink.add(new DuplicateImportError(imp));
    } else {
      imports.add(imp);
    }
  }

  /**
   * Adds a {@code Implements} to its appropriate part bucket.
   */
  void accumulate(ImplementsDeclaration implementsDeclaration) {
    implementsDeclarations.add(implementsDeclaration);
  }

  /**
   * Adds a {@code ThrowsDeclaration} to its appropriate part bucket.
   */
  void accumulate(ThrowsDeclaration throwsDeclaration) {
    throwsDeclarations.add(throwsDeclaration);
  }

  /**
   * Adds a {@code Parameter} to its appropriate part bucket.
   */
  void accumulate(Parameter parameter) {
    parameters.add(parameter);
  }

  /**
   * Adds a {@code FormalTypeParameter} to its appropriate part bucket.
   */
  void accumulate(FormalTypeParameter formalTypeParameter) {
    formalTypeParameters.add(formalTypeParameter);
  }

  /**
   * Adds a {@code Root} to its appropriate part bucket.
   */
  void accumulate(Root root) {
    roots.add(root);
  }

  /**
   * Adds a {@code Constructor} to its appropriate part bucket.
   */
  void accumulate(Constructor constructor) {
    constructors.add(constructor);
  }

  /**
   * Adds a {@code Conditional.Clause} to its appropriate part bucket.
   */
  void accumulate(Conditional.Clause clause) {
    clauses.add(clause);
  }

  /**
   * Adds an {@code Attribute} to its appropriate part bucket.
   */
  void accumulate(Attribute attribute) {
    attrMap.add(Objects.nonNull(attribute));
  }

  /**
   * Adds an {@link JavaAnnotation} to its appropriate part bucket.
   */
  void accumulate(JavaAnnotation javaAnnotation) {
    javaAnnotations.add(javaAnnotation);
  }

  private class Bucket<T extends Node> {
    private final List<T> nodes = Lists.newArrayList();
    private boolean used = true;

    private Bucket() {
      buckets.add(this);
    }

    public void add(T node) {
      nodes.add(Objects.nonNull(node));
      used = false;
    }

    public List<T> get() {
      used = true;
      return Collections.unmodifiableList(nodes);
    }

    public boolean contains(T node) {
      return nodes.contains(node);
    }

    public void reportUnused() {
      if (!used) {
        for (Node node : nodes) {
          // TODO(laurence): It might make more sense to do this check in the
          // Validator. Then it would be isEmpty instead of isWhitespaceOnly,
          // as we'd be past the SpaceCollapser by that point.
          if (!((node instanceof Expression)
                && ((Expression) node).alwaysOnlyWhitespace())) {
            // We ignore unused nodes that are just whitespace.
            alertSink.add(new BadNodePlacementError(node, forNode));
          }
        }
      }
    }
  }

  private <T extends Node> Bucket<T> newBucket() {
    return new Bucket<T>();
  }
}
