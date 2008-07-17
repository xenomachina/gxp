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

import com.google.common.collect.Maps;
import com.google.gxp.compiler.alerts.AlertSink;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.schema.Schema;
import com.google.gxp.compiler.schema.SchemaFactory;

import java.util.*;

/**
 * A mapping from XML namespace URIs to {@link Namespace} objects.
 */
final class NamespaceSet {
  private Map<String, Namespace> exactMappings = Maps.newHashMap();
  private final SchemaFactory schemaFactory;

  public NamespaceSet(SchemaFactory schemaFactory) {
    for (Namespace ns : new Namespace[] {
      GxpNamespace.INSTANCE,
      UnqualifiedCallNamespace.INSTANCE,
      ExprNamespace.INSTANCE,
      CppNamespace.INSTANCE,
      JavaNamespace.INSTANCE,
      MsgNamespace.INSTANCE,
      NoMsgNamespace.INSTANCE,
    }) {
      exactMappings.put(ns.getUri(), ns);
    }

    this.schemaFactory = schemaFactory;
  }

  /**
   * Returns the {@code Namespace} for the given namespace URI, {@code nsUri}.
   * If no matching {@code Namespace} can be found then {@code null} is
   * returned and {@link com.google.gxp.compiler.alerts.Alert}s will be
   * reported to the specified {@link AlertSink}.
   *
   * @param alertSink {@code AlertSink} for reporting
   * {@code com.google.gxp.compiler.alerts.Alert}s to.
   * @param sourcePosition {@link SourcePosition} to use for reported
   * {@code com.google.gxp.compiler.alerts.Alert}s.
   * @param nsUri namespace URI to look up.
   */
  public Namespace get(AlertSink alertSink,
                       SourcePosition sourcePosition,
                       String nsUri) {
    if (nsUri == null || nsUri.equals("")) {
      alertSink.add(new NoNamespaceError(sourcePosition));
      return null;
    }
    Namespace result = exactMappings.get(nsUri);

    // check for a qualified call namespace
    if (result == null && nsUri.startsWith(QualifiedCallNamespace.NEW_CALL_PREFIX)) {
      result = new QualifiedCallNamespace(nsUri);
      exactMappings.put(result.getUri(), result);
    }

    // check for a schema namespace
    if (result == null) {
      Schema schema = schemaFactory.fromNamespaceUri(nsUri);
      if (schema != null) {
        result = new OutputNamespace(schema);
        exactMappings.put(result.getUri(), result);
      }
    }

    if (result == null) {
      alertSink.add(new UnknownNamespaceError(sourcePosition, nsUri));
    }

    return result;
  }
}
