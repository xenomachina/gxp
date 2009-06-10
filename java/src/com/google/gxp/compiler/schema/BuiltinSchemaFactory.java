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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.DefaultAlertPolicy;
import com.google.gxp.compiler.alerts.ErroringAlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.fs.FileSystem;
import com.google.gxp.compiler.fs.ResourceFileSystem;

import java.util.*;

/**
 * A {@code SchemaFactory} that can return all of the built-in {@code Schema}s
 */
public class BuiltinSchemaFactory extends FileBackedSchemaFactory {
  public static final SchemaFactory INSTANCE = new BuiltinSchemaFactory();

  Map<String, Schema> nonMarkupSchemas = Maps.newHashMap();

  public BuiltinSchemaFactory() {
    this(new ErroringAlertSink(DefaultAlertPolicy.INSTANCE));
  }

  public BuiltinSchemaFactory(AlertSink alertSink) {
    super(alertSink);

    Schema plaintextSchema = addNonMarkupSchema("plaintext", "text/plain", null,
                                                "PlaintextClosure",
                                                "PlaintextAppender",
                                                "gxp/text/plaintext.h",
                                                "com.google.gxp.text.PlaintextClosure",
                                                "com.google.gxp.text.PlaintextAppender",
                                                "com.google.gxp.text.*",
                                                "goog.gxp.text.PlaintextClosure",
                                                "goog.gxp.text");

    addNonMarkupSchema("javascript", "text/javascript", plaintextSchema,
                       "JavascriptClosure",
                       "JavascriptAppender",
                       "gxp/js/javascript.h",
                       "com.google.gxp.js.JavascriptClosure",
                       "com.google.gxp.js.JavascriptAppender",
                       "com.google.gxp.js.*",
                       "goog.gxp.js.JavascriptClosure",
                       "goog.gxp.js");

    addNonMarkupSchema("css", "text/css", plaintextSchema,
                       "CssClosure",
                       "CssAppender",
                       "gxp/css/css.h",
                       "com.google.gxp.css.CssClosure",
                       "com.google.gxp.css.CssAppender",
                       "com.google.gxp.css.*",
                       "goog.gxp.css.CssClosure",
                       "goog.gxp.css");

    FileSystem fs = new ResourceFileSystem();

    // html
    add(fs, "html.xml", "http://www.w3.org/1999/xhtml", "text/html", "application/xhtml+xml");
  }

  private static final String PATH_PREFIX = "/com/google/gxp/compiler/schema/";

  private void add(FileSystem fs, String fnam, String nsUri, String... contentTypeNames) {
    FileRef ref = fs.parseFilename(PATH_PREFIX).join(fnam);
    addSchemaPromise(ref, nsUri, contentTypeNames);
  }

  private Schema addNonMarkupSchema(String name, String contentType, Schema msgSchema,
                                    String cppType, String cppAppender, String cppImport,
                                    String javaType, String javaAppender, String javaImport,
                                    String javaScriptType, String javaScriptImport) {
    Schema schema = new Schema(new SourcePosition(name), "<schema>",
                               name, "", contentType, false, contentType, null,
                               cppType, cppAppender, ImmutableList.of(cppImport),
                               javaType, javaAppender, ImmutableList.of(javaImport),
                               javaScriptType, ImmutableList.of(javaScriptImport),
                               ImmutableList.<ElementBuilder>of(), ImmutableList.<SchemaRef>of(),
                               msgSchema);
    nonMarkupSchemas.put(contentType, schema);

    return schema;
  }

  public Schema fromContentTypeName(String contentTypeName) {
    Schema schema = nonMarkupSchemas.get(contentTypeName);
    return (schema != null)
        ? schema
        : super.fromContentTypeName(contentTypeName);
  }
}
