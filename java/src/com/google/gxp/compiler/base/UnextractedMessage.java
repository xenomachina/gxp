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
import com.google.gxp.compiler.schema.Schema;

/**
 * A {@code <gxp:msg>} that has not yet been "extracted" by the {@code
 * com.google.gxp.compiler.msgextract.MessageExtractor MessageExtractor}.
 */
public class UnextractedMessage extends Expression {
  private final String meaning;
  private final String comment;
  private final boolean hidden;
  private final Expression content;

  public UnextractedMessage(Node fromNode, Schema schema,
                            String meaning, String comment, boolean hidden,
                            Expression content) {
    super(fromNode, schema);
    this.meaning = meaning;
    this.comment = comment;
    this.hidden = hidden;
    this.content = Preconditions.checkNotNull(content);
  }

  @Override
  public <T> T acceptVisitor(ExpressionVisitor<T> visitor) {
    return visitor.visitUnextractedMessage(this);
  }

  public String getMeaning() {
    return meaning;
  }

  public String getComment() {
    return comment;
  }

  public boolean isHidden() {
    return hidden;
  }

  public Expression getContent() {
    return content;
  }

  public UnextractedMessage withContent(Expression newContent) {
    return newContent.equals(content)
        ? this
        : new UnextractedMessage(this, getSchema(), meaning, comment, hidden, newContent);
  }

  public UnextractedMessage withContentAndSchema(Expression newContent,
                                                 Schema newSchema) {
    return (newContent.equals(content) && Objects.equal(newSchema, getSchema()))
        ? this
        : new UnextractedMessage(this, newSchema, meaning, comment, hidden, newContent);
  }

  @Override
  public boolean equals(Object that) {
    return this == that
        || (that instanceof UnextractedMessage
            && equals((UnextractedMessage) that));
  }

  public boolean equals(UnextractedMessage that) {
    return equalsExpression(that)
        && Objects.equal(getMeaning(), that.getMeaning())
        && Objects.equal(getComment(), that.getComment())
        && Objects.equal(isHidden(),   that.isHidden())
        && Objects.equal(getContent(), that.getContent());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expressionHashCode(),
        getMeaning(),
        getComment(),
        isHidden(),
        getContent());
  }
}
