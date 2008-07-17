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

import java.io.Serializable;

/**
 * A set of space operators.
 */
@SuppressWarnings("serial") // let java pick the SerialVersionUID
public class SpaceOperatorSet implements Serializable {
  private final SpaceOperator interiorSpaceOperator;
  private final SpaceOperator exteriorSpaceOperator;

  public static final SpaceOperatorSet NULL =
      new SpaceOperatorSet(null, null);

  /**
   * Creates a {@code SpaceOperatorSet} with the given space operators. Either
   * may be null if no space operator was specified in which case {@link
   * #inheritFrom(SpaceOperatorSet)} can be used to inherit the corresponding
   * space operator from a parent {@code SpaceOperatorSet}.
   *
   * @param interiorSpaceOperator the interior space operator, or null if no
   * interior space operator was specified.
   * @param exteriorSpaceOperator the interior space operator, or null if no
   * exterior space operator was specified.
   */
  public SpaceOperatorSet(SpaceOperator interiorSpaceOperator,
                          SpaceOperator exteriorSpaceOperator) {
    this.interiorSpaceOperator = interiorSpaceOperator;
    this.exteriorSpaceOperator = exteriorSpaceOperator;
  }

  /**
   * @return the interior space operator, or null if no interior space operator
   * was specified.
   */
  public SpaceOperator getInteriorSpaceOperator() {
    return interiorSpaceOperator;
  }

  /**
   * @return the exterior space operator, or null if no exterior space operator
   * was specified.
   */
  public SpaceOperator getExteriorSpaceOperator() {
    return exteriorSpaceOperator;
  }

  /**
   * @return a {@code SpaceOperatorSet} with the interior and exterior space
   * operators set as specified. As an optimization, if the result would be
   * equivalent to this {@code SpaceOperatorSet} then this {@code
   * SpaceOperatorSet} is returned.
   */
  public SpaceOperatorSet with(SpaceOperator newInteriorSpaceOperator,
                               SpaceOperator newExteriorSpaceOperator) {
    return ((newInteriorSpaceOperator == getInteriorSpaceOperator())
            && (newExteriorSpaceOperator == getExteriorSpaceOperator()))
        ? this
        : new SpaceOperatorSet(newInteriorSpaceOperator, newExteriorSpaceOperator);
  }

  /**
   * @return a {@code SpaceOperatorSet} that is the same as this one except
   * that any operator that is non-null will be inherited from the supplied
   * "parent" in the result {@code SpaceOperatorSet}.
   */
  public SpaceOperatorSet inheritFrom(SpaceOperatorSet parent) {
    return this.with(ifNull(getInteriorSpaceOperator(),
                            parent.getInteriorSpaceOperator()),
                     ifNull(getExteriorSpaceOperator(),
                            parent.getExteriorSpaceOperator()));
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof SpaceOperatorSet
            && equals((SpaceOperatorSet) that));
  }

  public boolean equals(SpaceOperatorSet that) {
    return Objects.equal(getInteriorSpaceOperator(),
                         that.getInteriorSpaceOperator())
        && Objects.equal(getExteriorSpaceOperator(),
                         that.getExteriorSpaceOperator());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        getInteriorSpaceOperator(),
        getExteriorSpaceOperator());
  }

  /**
   * Returns the supplied value, or a fallback if the value is null.
   *
   * @param value the result, unless it is null
   * @param fallback the result if {@code value} is null
   * @return {@code value} unless it is null, in which case {@code fallback}
   * will be returned
   */
  public static <T> T ifNull(T value, T fallback) {
    // TODO(laurence): move this to Objects class?
    return (value == null) ? fallback : value;
  }
}
