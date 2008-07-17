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

package com.google.gxp.base;

import com.google.i18n.MessageBundle;

import java.io.IOException;
import java.util.Locale;

/**
 * This is the superclass of all GXP templates. Contains helper classes and
 * functions.
 *
 * Subclasses will have static methods of the form:
 *
 *     public static void write(Appendable gxp_output, GxpContext gxp_context, ...)
 *     public static HtmlClosure getGxpClosure(...)
 *
 * where '...' is the list of parameters defined in the GXP source.
 */
public class GxpTemplate {
  /**
   * @param source message source to use
   * @param locale the Locale to translate this message to
   * @param id numeric ID of message to retrieve
   * @param arguments the arguments necessary for the specified message
   *
   * @return a String representing the specified message with the given arguments in
   * this given Locale.
   */
  protected static String getMessage(String source, Locale locale, long id, String... arguments) {
    return MessageBundle.getInstance(source, locale).getMessage(id).toString(arguments);
  }

  /**
   * Abstract base class for {@code GxpClosure}s created by the GXP
   * compiler that need to use exception tunneling.
   *
   * @see com.google.gxp.compiler.java.JavaCodeGenerator
   */
  protected abstract static class TunnelingGxpClosure implements GxpClosure {
    /**
     * {@inheritdoc}
     *
     * @throws GxpTunnelException when the underlying implementation throws a
     * checked exception other than {@code IOException}.
     */
    public final void write(Appendable out, GxpContext gxpContext)
        throws IOException {
      try {
        writeImpl(out, gxpContext);
      } catch (RuntimeException rtx) {
        throw rtx;
      } catch (IOException iox) {
        throw iox;
      } catch (Exception checkedException) {
        throw GxpRuntimeException.wrap(checkedException);
      }
    }

    /**
     * The underlying implementation. It is recommended that implementations of
     * this method declare specific checked exceptions rather than just "throws
     * Exception".
     */
    protected abstract void writeImpl(Appendable out, GxpContext gxpContext)
        throws Exception;
  }

  /**
   * Abstract base class for anonymous {@code GxpClosure}s created by the GXP
   * compiler. The equals and hashCode methods use the class's identity for
   * equality. That is, all instances of a specific {@code AnonymousGxpClosure}
   * subclass are considered to be equal to eachother. This is required so that
   * the Once template can reliably compare these anonymous GxpClosure
   * implementations.
   *
   * @see com.google.gxp.compiler.java.JavaCodeGenerator
   * @see OnceClosure
   */
  protected abstract static class AnonymousGxpClosure
      extends TunnelingGxpClosure {

    @Override
    public boolean equals(Object obj) {
      return (obj == null) ? false : obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
      return getClass().hashCode();
    }
  }
}
