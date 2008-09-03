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

package com.google.gxp.compiler.msgextract;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gxp.compiler.alerts.AlertSetBuilder;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.alerts.common.BadNodePlacementError;
import com.google.gxp.compiler.alerts.common.InvalidMessageError;
import com.google.gxp.compiler.base.CollapseExpression;
import com.google.gxp.compiler.base.Concatenation;
import com.google.gxp.compiler.base.ConvertibleToContent;
import com.google.gxp.compiler.base.DefaultingExpressionVisitor;
import com.google.gxp.compiler.base.ExampleExpression;
import com.google.gxp.compiler.base.ExhaustiveExpressionVisitor;
import com.google.gxp.compiler.base.Expression;
import com.google.gxp.compiler.base.ExtractedMessage;
import com.google.gxp.compiler.base.Node;
import com.google.gxp.compiler.base.NoMessage;
import com.google.gxp.compiler.base.OutputElement;
import com.google.gxp.compiler.base.PlaceholderEnd;
import com.google.gxp.compiler.base.PlaceholderNode;
import com.google.gxp.compiler.base.PlaceholderStart;
import com.google.gxp.compiler.base.Root;
import com.google.gxp.compiler.base.StringConstant;
import com.google.gxp.compiler.base.UnexpectedNodeException;
import com.google.gxp.compiler.base.UnextractedMessage;
import com.google.gxp.compiler.i18ncheck.I18nCheckedTree;
import com.google.transconsole.common.messages.InvalidMessageException;
import com.google.transconsole.common.messages.Message;
import com.google.transconsole.common.messages.MessageBuilder;

import java.util.*;

/**
 * Replaces message elements and contained Output nodes with validated message
 * objects that hold message data as well as a set of DynamicPlaceholder
 * children. For example:
 *
 * <center><img src="http://go/gxpc.java/MessageExtraction.png"></center>
 */
public class MessageExtractor implements Function<I18nCheckedTree, MessageExtractedTree> {

  public MessageExtractedTree apply(I18nCheckedTree tree) {
    List<ExtractedMessage> messages = Lists.newArrayList();
    AlertSetBuilder alertSetBuilder = new AlertSetBuilder(tree.getAlerts());
    Root root = tree.getRoot().acceptVisitor(new OutsideMessageVisitor(alertSetBuilder, messages));

    return new MessageExtractedTree(tree.getSourcePosition(), alertSetBuilder.buildAndClear(),
                                    root, messages);
  }

  /**
   * Visitor used when we are <em>not</em> inside of a {@code <gxp:msg>} or
   * {@code <gxp:nomsg>} element.
   */
  private static class OutsideMessageVisitor extends ExhaustiveExpressionVisitor {
    private final AlertSink alertSink;
    private final List<ExtractedMessage> messages;

    OutsideMessageVisitor(AlertSink alertSink, List<ExtractedMessage> messages) {
      this.alertSink = Preconditions.checkNotNull(alertSink);
      this.messages = Preconditions.checkNotNull(messages);
    }

    public void addMessages(ExtractedMessage message) {
      messages.add(message);
    }

    @Override
    public Expression visitNoMessage(NoMessage noMsg) {
      InsideNoMessageVisitor insideNoMsgVisitor =
          new InsideNoMessageVisitor(alertSink, noMsg, this);
      return noMsg.getSubexpression().acceptVisitor(insideNoMsgVisitor);
    }

    @Override
    public Expression visitUnextractedMessage(UnextractedMessage msg) {
      InsideMessageVisitor subVisitor = new InsideMessageVisitor(alertSink, this, msg);
      msg.getContent().acceptVisitor(subVisitor);
      return subVisitor.getResult();
    }

    @Override
    public Expression visitPlaceholderNode(PlaceholderNode ph) {
      alertSink.add(new BadNodePlacementError(ph, null));
      return ph.getContent().acceptVisitor(this);
    }

    @Override
    public Expression visitPlaceholderStart(PlaceholderStart value) {
      throw new UnexpectedNodeException(value);
    }

    @Override
    public Expression visitPlaceholderEnd(PlaceholderEnd value) {
      throw new UnexpectedNodeException(value);
    }
  }

  /**
   * Visitor used when we are <em>not</em> inside of a {@code <gxp:msg>} or
   * {@code <gxp:nomsg>} element.
   */
  private static class InsideNoMessageVisitor extends ExhaustiveExpressionVisitor {
    private final AlertSink alertSink;
    private final NoMessage rootNoMsg;
    private final OutsideMessageVisitor outsideMessageVisitor;

    InsideNoMessageVisitor(AlertSink alertSink,
                           NoMessage rootNoMsg,
                           OutsideMessageVisitor outsideMessageVisitor) {
      this.alertSink = Preconditions.checkNotNull(alertSink);
      this.rootNoMsg = Preconditions.checkNotNull(rootNoMsg);
      this.outsideMessageVisitor = Preconditions.checkNotNull(outsideMessageVisitor);
    }

    @Override
    public Expression visitUnextractedMessage(UnextractedMessage msg) {
      alertSink.add(new BadNodePlacementError(msg, rootNoMsg));
      return msg.acceptVisitor(outsideMessageVisitor);
    }

    @Override
    public Expression visitNoMessage(NoMessage noMsg) {
      alertSink.add(new BadNodePlacementError(noMsg, rootNoMsg));
      return noMsg.getSubexpression().acceptVisitor(this);
    }

    @Override
    public Expression visitPlaceholderNode(PlaceholderNode ph) {
      alertSink.add(new BadNodePlacementError(ph, rootNoMsg));
      return ph.getContent().acceptVisitor(this);
    }

    @Override
    public Expression visitPlaceholderStart(PlaceholderStart value) {
      throw new UnexpectedNodeException(value);
    }

    @Override
    public Expression visitPlaceholderEnd(PlaceholderEnd value) {
      throw new UnexpectedNodeException(value);
    }
  }

  /**
   * Visitor used when we <em>are</em> inside of a {@code <gxp:msg>} element.
   * The calling protocol is to create the InsideMessageVisitor, have it visit
   * the content of an {@code UnextractedMessage} (which will cause to to
   * accumulate all of the information it needs) and then call the
   * {@code getResult} method, which will return an {@code ExtractedMessage},
   * or a stand-in if there was an error.
   */
  private static class InsideMessageVisitor extends DefaultingExpressionVisitor<Void> {
    private final AlertSink alertSink;
    private final OutsideMessageVisitor outsideMessageVisitor;
    private final UnextractedMessage msg;

    // Mutable state:
    private final MessageBuilder tcMessageBuilder = new MessageBuilder();
    private final List<Expression> parameters = Lists.newArrayList();
    private boolean invalid = false;

    /**
     * {@link com.google.i18n.Message} only supports the pattern "%[1-9%]" when
     * looking for dynamic placeholders, so only 9 dynamic placeholders are
     * allowed.
     */
    private static final int MAX_DYNAMIC_PLACEHOLDER_COUNT = 9;

    InsideMessageVisitor(AlertSink alertSink,
                         OutsideMessageVisitor outsideMessageVisitor,
                         UnextractedMessage msg) {
      this.alertSink = Preconditions.checkNotNull(alertSink);
      this.outsideMessageVisitor = Preconditions.checkNotNull(outsideMessageVisitor);
      this.msg = Preconditions.checkNotNull(msg);

      // Populate the tcMessageBuilder with a content-type
      tcMessageBuilder.setContentType(msg.getSchema().getCanonicalContentType());

      // Populate tcMessageBuilder with attributes from msg.
      // - The meaning is used to compute the msg id.
      if (null != msg.getMeaning()) {
        tcMessageBuilder.setMeaning(msg.getMeaning());
      }

      // mark this message as hidden if indicated
      if (msg.isHidden()) {
        tcMessageBuilder.setHidden(true);
      }

      // - Comments are notes for translators
      if (null != msg.getComment()) {
        tcMessageBuilder.setDescription(msg.getComment());
      }

      // - Include the source gxp file so that l10n experts for other projects
      //   can avoid clobbering this project's translated strings.
      SourcePosition source = msg.getSourcePosition();
      if (source != null) {
        tcMessageBuilder.addSource(source.getSource().toRelativeFilename()
                                   + ": L" + source.getLine()
                                   // For backwards compatibility with the old
                                   // compiler, report a 0-based column number.
                                   + ", C" + (source.getColumn() - 1));
      }
    }

    Expression getResult() {
      if (!invalid) {
        try {
          Message message = tcMessageBuilder.createMessage();
          ExtractedMessage emsg = new ExtractedMessage(msg, msg.getSchema(), message, parameters);
          outsideMessageVisitor.addMessages(emsg);
          return emsg;
        } catch (InvalidMessageException imx) {
          recordInvalidMessageException(msg, imx);
        }
      }

      if (!invalid) {
        throw new AssertionError("Attempting to create stand-in when message is valid!");
      }
      // TODO(laurence): create a better stand-in
      return new StringConstant(msg, msg.getSchema(), "");
    }

    private void recordInvalidMessageException(Node node, InvalidMessageException imx) {
      alertSink.add(new InvalidMessageError(node.getSourcePosition(), imx));
      invalid = true;
    }

    @Override
    public Void visitStringConstant(StringConstant value) {
      tcMessageBuilder.appendText(value.evaluate());
      return null;
    }

    @Override
    public Void visitConcatenation(Concatenation value) {
      for (Expression subExpression : value.getValues()) {
        subExpression.acceptVisitor(this);
      }
      return null;
    }

    @Override
    public Void visitConvertibleToContent(ConvertibleToContent value) {
      value.getSubexpression().acceptVisitor(this);
      return null;
    }

    @Override
    public Void visitExampleExpression(ExampleExpression value) {
      return value.getSubexpression().acceptVisitor(this);
    }

    @Override
    protected Void defaultVisitExpression(Expression node) {
      alertSink.add(new BadNodePlacementError(node, msg));
      return null;
    }

    @Override
    public Void visitPlaceholderNode(PlaceholderNode ph) {
      StringBuilder sb = new StringBuilder();
      for (Expression subExpression : ph.getContent().separate()) {
        subExpression =
            subExpression.acceptVisitor(outsideMessageVisitor);
        if (subExpression.hasStaticString()) {
          String s = Preconditions.checkNotNull(subExpression.getStaticString(alertSink,
                                                                   null));
          sb.append(s.replace("%", "%%"));
        } else {
          int index;
          for (index = 0; index < parameters.size(); index++) {
            if (parameters.get(index).alwaysEquals(subExpression)) {
              break;
            }
          }

          // Placeholders parameters start at 1.
          int id = index + 1;
          if (id > MAX_DYNAMIC_PLACEHOLDER_COUNT) {
            alertSink.add(new TooManyDynamicPlaceholdersError(subExpression));
          } else {
            if (index == parameters.size()) {
              parameters.add(subExpression);
            }
            sb.append("%");
            sb.append(String.valueOf(id));
          }
        }
      }

      try {
        tcMessageBuilder.appendPlaceholder(sb.toString(),
                                           ph.getName().toUpperCase(),
                                           ph.getExample());
      } catch (InvalidMessageException imx) {
        recordInvalidMessageException(ph, imx);
      }
      return null;
    }

    @Override
    public Void visitOutputElement(OutputElement value) {
      throw new UnexpectedNodeException(value);
    }

    @Override
    public Void visitCollapseExpression(CollapseExpression value) {
      throw new UnexpectedNodeException(value);
    }

    @Override
    public Void visitExtractedMessage(ExtractedMessage value) {
      throw new UnexpectedNodeException(value);
    }

    @Override
    public Void visitPlaceholderStart(PlaceholderStart value) {
      throw new UnexpectedNodeException(value);
    }

    @Override
    public Void visitPlaceholderEnd(PlaceholderEnd value) {
      throw new UnexpectedNodeException(value);
    }
  }
}
