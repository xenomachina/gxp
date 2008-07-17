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

package com.google.gxp.compiler.schema;

/**
 * An output content type. We need to know about these primarily for escaping
 * (both at compile time and run time).
 */
public enum ContentFamily {
  // TODO(laurence): move to base package?
  CSS{
    @Override
    public <K,V> V acceptVisitor(ContentFamilyVisitor<K,V> visitor, K arg) {
      return visitor.visitCss(arg);
    }
  },

  JAVA_SCRIPT{
    @Override
    public <K,V> V acceptVisitor(ContentFamilyVisitor<K,V> visitor, K arg) {
      return visitor.visitJavaScript(arg);
    }
  },

  MARKUP {
    @Override
    public <K,V> V acceptVisitor(ContentFamilyVisitor<K,V> visitor, K arg) {
      return visitor.visitMarkup(arg);
    }
  },

  PLAINTEXT {
    @Override
    public <K,V> V acceptVisitor(ContentFamilyVisitor<K,V> visitor, K arg) {
      return visitor.visitPlaintext(arg);
    }
  };

  public abstract <K,V> V acceptVisitor(ContentFamilyVisitor<K,V> visitor,
                                        K arg);

  public static ContentFamily fromContentTypeName(String contentTypeName) {
    if ("text/javascript".equals(contentTypeName)) {
      return ContentFamily.JAVA_SCRIPT;
    } else if ("text/css".equals(contentTypeName)) {
      return ContentFamily.CSS;
    } else if ("text/plain".equals(contentTypeName)) {
      return ContentFamily.PLAINTEXT;
    } else {
      // TODO(laurence): return null here once Expressions use ContentType
      // rather than ContentFamily.
      return ContentFamily.MARKUP;
    }
  }
}
