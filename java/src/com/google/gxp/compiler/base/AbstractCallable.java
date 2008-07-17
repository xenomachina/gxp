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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gxp.compiler.schema.Schema;

import java.util.List;
import java.util.Map;

/**
 * Straightforward abstract implementation of {@link Callable}.
 */
public abstract class AbstractCallable implements Callable {
  private final TemplateName.FullyQualified name;
  private final Schema schema;
  private final ImmutableList<FormalParameter> parameters;

  private final Map<String, FormalParameter> parameterMap;
  private final Map<String, FormalParameter> parameterPrimaryMap;
  private final FormalParameter contentConsumingParameter;

  protected AbstractCallable(TemplateName.FullyQualified name, Schema schema,
                             List<Parameter> parameters) {
    this.name = Objects.nonNull(name);
    this.schema = Objects.nonNull(schema);
    this.parameters = Util.map(Objects.nonNull(parameters), Parameter.GET_FORMAL);

    // Construct maps from parameter names -> FormalParameter, and pull out
    // the content consuming parameter if one exists
    parameterPrimaryMap = Maps.newHashMap();
    parameterMap = Maps.newHashMap();
    FormalParameter contentConsumer = null;
    for (FormalParameter param : this.parameters) {
      parameterPrimaryMap.put(param.getPrimaryName(), param);
      for (String paramName : param.getNames()) {
        parameterMap.put(paramName, param);
      }
      if (param.consumesContent()) {
        contentConsumer = param;
      }
    }
    this.contentConsumingParameter = contentConsumer;
  }

  public TemplateName.FullyQualified getName() {
    return name;
  }

  public Schema getSchema() {
    return schema;
  }

  public List<FormalParameter> getParameters() {
    return parameters;
  }

  public FormalParameter getParameter(String name) {
    return parameterMap.get(name);
  }

  public FormalParameter getParameterByPrimary(String name) {
    return parameterPrimaryMap.get(name);
  }

  public FormalParameter getContentConsumingParameter() {
    return contentConsumingParameter;
  }

  @Override
  public abstract boolean equals(Object that);

  protected final boolean equalsAbstractCallable(AbstractCallable that) {
    return Objects.equal(getName(), that.getName())
        && Objects.equal(getSchema(), that.getSchema())
        && Objects.equal(getParameters(), that.getParameters());
  }

  @Override
  public abstract int hashCode();

  protected int abstractCallableHashCode() {
    return Objects.hashCode(
        getName(),
        getSchema(),
        getParameters());
  }
}
