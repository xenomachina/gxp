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

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.MapConstraints;
import com.google.common.collect.Maps;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.fs.FileRef;

import java.util.*;

/**
 * A {@code SchemaFactory} that can returns {@code Schema}s based on their
 * locations within a supplied {@code Filesystem}.
 *
 * TODO(harryh): add error handling
 * TODO(harryh): detect schema collisions.
 */
public class FileBackedSchemaFactory implements SchemaFactory {
  private final AlertSink alertSink;
  private final Map<String, Supplier<Schema>> byNamespaceUri;
  private final Map<String, Supplier<Schema>> byContentTypeName;

  /**
   * @param alertSink an {@code AlertSink} to accept errors in processing
   *        schema files.
   * @param schemaRefs an {@code Iterable} of {@code FileRef}s to immediately
   *        parse.
   */
  public FileBackedSchemaFactory(AlertSink alertSink,
                                 Iterable<FileRef> schemaRefs) {
    this.alertSink = Preconditions.checkNotNull(alertSink);
    this.byNamespaceUri = createMap();
    this.byContentTypeName = createMap();
    for (FileRef ref : schemaRefs) {
      addSchema(ref);
    }
  }

  public FileBackedSchemaFactory(AlertSink alertSink) {
    this(alertSink, Collections.<FileRef>emptySet());
  }

  private static <K, V> Map<K, V> createMap() {
    Map<K, V> map = Maps.newHashMap();
    return MapConstraints.constrainedMap(map, MapConstraints.NOT_NULL);
  }

  /**
   * Add a memoized {@code Schema} {@code Supplier}.  As the namespace uri is
   * provided, the file is only read if this schema is actually requested.
   */
  public void addSchemaPromise(final FileRef ref, final String nsUri,
                               final String... contentTypeNames) {
    Supplier<Schema> supplier = Suppliers.memoize(new Supplier<Schema>() {
      public Schema get() {
        return SchemaParser.getSchema(ref, alertSink);
      }
    });
    byNamespaceUri.put(nsUri, supplier);
    for (String another : contentTypeNames) {
      byContentTypeName.put(another, supplier);
    }
  }

  /**
   * Add a {@code Schema} directly from a {@code FileRef}.  The file is read
   * immediately to determine the namespace uri.
   */
  public void addSchema(final FileRef ref) {
    Schema schema = SchemaParser.getSchema(ref, alertSink);
    Supplier<Schema> supplier = Suppliers.ofInstance(schema);

    byNamespaceUri.put(schema.getNamespaceUri(), supplier);
    addSupplierForContentType(schema.getXmlContentType(), supplier);
    addSupplierForContentType(schema.getSgmlContentType(), supplier);
  }

  private void addSupplierForContentType(String contentTypeName,
                                         Supplier<Schema> supplier) {
    if (contentTypeName != null) {
      if (byContentTypeName.containsKey(contentTypeName)) {
        alertSink.add(new DuplicateContentTypeSchemaError(supplier.get(), contentTypeName));
      } else {
        byContentTypeName.put(contentTypeName, supplier);
      }
    }
  }

  public Schema fromNamespaceUri(String nsUri) {
    Supplier<Schema> supplier = byNamespaceUri.get(Preconditions.checkNotNull(nsUri));
    return (supplier == null) ? null : supplier.get();
  }

  public Schema fromContentTypeName(String contentTypeName) {
    Supplier<Schema> supplier =
        byContentTypeName.get(Preconditions.checkNotNull(contentTypeName));
    return (supplier == null) ? null : supplier.get();
  }
}
