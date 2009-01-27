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

package com.google.gxp.compiler.parser;

/**
 * The http://google.com/2001/gxp/call/...  namespace.
 */
public class QualifiedCallNamespace extends CallNamespace {

  static final String NEW_CALL_PREFIX    = "http://google.com/2001/gxp/call/";
  private static final int PREFIX_LENGTH = NEW_CALL_PREFIX.length();

  private String nsUri;
  private String packageName;

  public QualifiedCallNamespace(String nsUri) {
    this.nsUri = nsUri;
    this.packageName = nsUri.substring(PREFIX_LENGTH).replace('/', '.') + ".";
  }

  @Override
  public String getUri() {
    return nsUri;
  }

  @Override
  protected String getTagName(String tagName) {
    return packageName + tagName;
  }
}
