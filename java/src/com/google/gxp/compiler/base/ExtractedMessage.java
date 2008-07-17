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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.gxp.compiler.schema.Schema;
import com.google.transconsole.common.messages.Message;

import java.util.*;

/**
 * A {@code <gxp:msg>} that has been "extracted" by the {@code
 * com.google.gxp.compiler.msgextract.MessageExtractor MessageExtractor}.
 */
public class ExtractedMessage extends Expression {
  private final Message tcMessage;
  private final ImmutableList<Expression> parameters;

  public ExtractedMessage(Node fromNode, Schema schema,
                          Message tcMessage, List<Expression> parameters) {
    super(fromNode, schema);
    this.tcMessage = Objects.nonNull(tcMessage);
    this.parameters = ImmutableList.copyOf(parameters);
  }

  public Message getTcMessage() {
    return tcMessage;
  }

  public List<Expression> getParameters() {
    return parameters;
  }

  public ExtractedMessage transformParams(Function<Expression, Expression> function) {
    List<Expression> newParams = Util.map(parameters, function);
    return new ExtractedMessage(this, this.getSchema(), tcMessage, newParams);
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitExtractedMessage(this);
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof ExtractedMessage
            && equals((ExtractedMessage) that));
  }

  public boolean equals(ExtractedMessage that) {
    return equalsExpression(that)
        && Objects.equal(getTcMessage(), that.getTcMessage())
        && Objects.equal(getParameters(), that.getParameters());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        getTcMessage(),
        getParameters());
  }
}
