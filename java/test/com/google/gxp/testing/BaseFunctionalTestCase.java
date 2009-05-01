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

package com.google.gxp.testing;

import com.google.gxp.base.GxpContext;

import java.io.*;
import java.util.*;

import junit.framework.TestCase;

/**
 * Base TestCase for gxp functional testing. Typical protocol for a test is to
 * call the write() method on a gxp class specifying {@code out} for the
 * Writer and {@code gxpContext} for the GxpContext, and then call
 * assertOutputEquals() with the expected output.
 */
public abstract class BaseFunctionalTestCase extends TestCase {
  protected StringWriter out = new StringWriter();

  // most tests can use this GxpContext, but some may create their own.
  protected GxpContext gxpContext    = createGxpContext(Locale.US, false);
  protected GxpContext xmlGxpContext = createGxpContext(Locale.US, true);

  public BaseFunctionalTestCase() {
  }

  public BaseFunctionalTestCase(String name) {
    super(name);
  }

  protected GxpContext createGxpContext(Locale locale, boolean forceXmlSyntax) {
    GxpContext.Builder builder = GxpContext.builder(locale);
    if (forceXmlSyntax) {
      builder.forceXmlSyntax();
    }
    return builder.build();
  }

  protected void assertOutputEquals(String expected) {
    assertEquals(expected, out.toString());
    out = new StringWriter();
  }
}
