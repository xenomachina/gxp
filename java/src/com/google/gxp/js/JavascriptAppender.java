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

package com.google.gxp.js;

import com.google.common.base.CharEscapers;
import com.google.common.base.Preconditions;
import com.google.gxp.base.GxpContext;
import com.google.i18n.Localizable;

import java.io.IOException;
import java.io.StringWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Appender class for text/javascript
 */
public class JavascriptAppender {
  protected JavascriptAppender() {}

  public static final JavascriptAppender INSTANCE = new JavascriptAppender();

  //////////////////////////////////////////////////////////////////////////////////////////
  // Primitives
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Converts value to a JavaScript literal and appends to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, byte value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Converts value to a JavaScript literal and appends to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, short value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Converts value to a JavaScript literal and appends to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, int value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Converts value to a JavaScript literal and appends to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, long value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Converts value to a JavaScript literal and appends to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, float value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Converts value to a JavaScript literal and appends to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, double value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  /**
   * Converts value to a JavaScript literal and appends to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, char value)
      throws IOException {
    out.append('"');
    CharEscapers.JAVASCRIPT_ESCAPER.escape(out).append(value);
    out.append('"');
    return out;
  }

  /**
   * Converts value to a JavaScript literal and appends to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, boolean value)
      throws IOException {
    out.append(String.valueOf(value));
    return out;
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // Closures
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Appends a {@code JavascriptClosure} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, JavascriptClosure closure)
      throws IOException {
    Preconditions.checkNotNull(closure);
    closure.write(out, gxpContext);
    return out;
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // Objects
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Outputs a {@code JSONArray} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, JSONArray value)
      throws IOException {
    Preconditions.checkNotNull(value);
    StringWriter sw = new StringWriter();
    try {
      value.write(sw);
    } catch (JSONException e) {
      throw new IOException(e);
    }
    out.append(sw.toString());
    return out;
  }

  /**
   * Outputs a {@code JSONObject} to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, JSONObject value)
      throws IOException {
    Preconditions.checkNotNull(value);
    StringWriter sw = new StringWriter();
    try {
      value.write(sw);
    } catch (JSONException e) {
      throw new IOException(e);
    }
    out.append(sw.toString());
    return out;
  }

  /**
   * Converts value to a JavaScript string literal and appends to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, CharSequence value)
      throws IOException {
    Preconditions.checkNotNull(value);
    out.append('"');
    CharEscapers.JAVASCRIPT_ESCAPER.escape(out).append(value);
    out.append('"');
    return out;
  }

  /**
   * Converts value to a JavaScript String literal and appends to out.
   */
  public <A extends Appendable> A append(A out, GxpContext gxpContext, Localizable value)
      throws IOException {
    Preconditions.checkNotNull(value);
    return append(out, gxpContext, value.toString(gxpContext.getLocale()));
  }
}
