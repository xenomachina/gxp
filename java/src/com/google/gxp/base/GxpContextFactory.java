/*
 * Copyright (C) 2004 Google Inc.
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

import javax.servlet.ServletRequest;

/**
 * Factory class for making {@link GxpContext}s. This class differs
 * from the previous {@link com.google.gxp.servlet.GxpContextFactory} in that
 * it uses the Java Servlet API exclusively in lieu of the Google
 * Servlet Framework.
 */
public final class GxpContextFactory {
  /**
   * Creates a new {@link GxpContext} for the specified request.
   */
  public static GxpContext newInstance(ServletRequest req) {
    return newInstance(req, false);
  }

  /**
   * Creates a new {@link GxpContext} for the specified request that will
   * use xml syntax if specified.
   */
  public static GxpContext newInstance(ServletRequest req, boolean useXmlSyntax) {
    return new GxpContext(req.getLocale(), useXmlSyntax);
  }

  private GxpContextFactory() {}
}
