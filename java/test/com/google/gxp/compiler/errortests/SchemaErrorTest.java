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

package com.google.gxp.compiler.errortests;

import com.google.gxp.compiler.fs.FileRef;
import com.google.gxp.compiler.schema.DuplicateContentTypeSchemaError;

/**
 * Tests of proper error reporting by the GXP compiler relating to {@code
 * Schema}s.
 */
public class SchemaErrorTest extends BaseTestCase {
  public void testSchema_duplicateContentTypes() throws Exception {
    FileRef schema1 = createSchemaFile(
        "schema1",
        "<schema name='schema1'",
        "        namespace='http://google.com/gxp/schema1'",
        "        content-type='text/xml'",
        "        java-type='com.google.gxp.base.GxpClosure'>",
        "</schema>");
    FileRef schema2 = createSchemaFile(
        "schema2",
        "<schema name='schema2'",
        "        namespace='http://google.com/gxp/schema2'",
        "        content-type='text/xml'",
        "        java-type='com.google.gxp.base.GxpClosure'>",
        "</schema>");
    compileSchemas(schema1, schema2);
    assertAlert(new DuplicateContentTypeSchemaError(pos(), "text/xml"));
    assertNoUnexpectedAlerts();
  }
}
