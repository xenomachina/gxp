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

import com.google.gxp.compiler.base.Conditional;
import com.google.gxp.compiler.base.Constructor;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.FormalTypeParameter;
import com.google.gxp.compiler.base.ImplementsDeclaration;
import com.google.gxp.compiler.base.Import;
import com.google.gxp.compiler.base.JavaAnnotation;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.ThrowsDeclaration;

import java.util.*;

/**
 * A categorization of the different "parts" that can go into a ReparentedNode.
 * In the reparenting phase we collect the ParsedAttributes and children
 * (ParsedNodes) of each ParsedNode, convert them into "higher level" node
 * types, and then categorize the resulting nodes into a nuber of buckets.
 *
 * <p>This interface has one getter method for each category's bucket, in
 * addition to the reportUnused method which should be called once the client
 * has finished retrieving the parts it cares about.
 *
 * <p>A Parts object is intended to be used in several phases:
 * <ul>
 * <li>The client calls the getters to get the parts it cares about.
 * <li>{@code reportUnused} is called to report anything that was supplied but
 * not used.
 * </ul>
 */
interface Parts {
  /**
   * @return the Roots for the new node.
   */
  List<Root> getRoots();

  /**
   * @return the Constructors for the new node.
   */
  List<Constructor> getConstructors();

  /**
   * @return the content for the new node.
   */
  Expression getContent();

  /**
   * @return the ImplementsDeclarations for the new node.
   */
  List<ImplementsDeclaration> getImplementsDeclarations();

  /**
   * @return the Imports for the new node.
   */
  List<Import> getImports();

  /**
   * @return the ThrowsDeclarartions for the new node.
   */
  List<ThrowsDeclaration> getThrowsDeclarations();

  /**
   * @return the Parameters for the new node.
   */
  List<Parameter> getParameters();

  /**
   * @return the FormalTypeParameters for the new node.
   */
  List<FormalTypeParameter> getFormalTypeParameters();

  /**
   * @return the clauses for the new node
   */
  List<Conditional.Clause> getClauses();

  /**
   * @return the java annotations for the new node
   */
  List<JavaAnnotation> getJavaAnnotations();

  /**
   * @return the attributes for the new node as an AttributeMap. Note that the
   * client does not need to call reportUnusedAttributes on the resulting
   * AttributeMap as reportUnused will invoke it automatically.
   */
  AttributeMap getAttributes();

  /**
   * Reports any parts that we didn't actually use. This method should be
   * called after all of the desired parts have been retrieved.
   */
  void reportUnused();
}
