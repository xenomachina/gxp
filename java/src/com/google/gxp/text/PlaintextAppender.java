/*
 * Copyright (C) 2007 Google Inc.
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

package com.google.gxp.text;

import com.google.common.base.Preconditions;
import com.google.gxp.base.GxpContext;
import com.google.i18n.Localizable;

import java.io.IOException;

/**
 * Appender class for text/plain
 */
public class PlaintextAppender {
  private PlaintextAppender() {}

  public static final PlaintextAppender INSTANCE = new PlaintextAppender();

  ////////////////////////////////////////////////////////////////////////////////
  // Primitives
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * Appends a {@code byte} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, byte value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Appends a {@code short} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, short value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Appends an {@code int} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, int value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Appends a {@code long} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, long value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Appends a {@code float} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, float value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Appends a {@code double} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, double value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Appends a {@code char} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, char value)
      throws IOException {
    out.append(value);
    return out;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Closures
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * Appends a {@code PlaintextClosure} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, PlaintextClosure closure)
      throws IOException {
    Preconditions.checkNotNull(closure);
    closure.write(out, gxpContext);
    return out;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Objects
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * Appends a {@code String} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, String value)
      throws IOException {
    Preconditions.checkNotNull(value);
    out.append(value);
    return out;
  }

  /**
   * Appends a {@code Localizable} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, Localizable value)
      throws IOException {
    Preconditions.checkNotNull(value);
    out.append(value.toString(gxpContext.getLocale()));
    return out;
  }
}
