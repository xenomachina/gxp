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

package com.google.gxp.compiler.dot;

import com.google.common.base.CharEscapers;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gxp.compiler.base.Node;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GraphRenderer which uses reflection to traverse an object graph. Because
 * this uses reflection it should probably only be used for debugging purposes.
 */
public class ReflectiveGraphRenderer implements GraphRenderer<Object> {
  // TODO(laurence): add some tests for this.

  private final String graphName;

  public ReflectiveGraphRenderer(String graphName) {
    this.graphName = Preconditions.checkNotNull(graphName);
  }

  public void renderGraph(GraphSink out, Iterable<?> objects) {
    out.digraphStart(graphName);
    Worker worker = new Worker(out);
    for (Object object : objects) {
      worker.renderSubgraph(object);
    }
    out.digraphEnd();
  }

  private static String javaEscape(Object o) {
    return (o instanceof String)
        ?  "\"" + CharEscapers.javaStringEscaper().escape((String) o) + "\""
        : CharEscapers.javaStringEscaper().escape(String.valueOf(o));
  }

  private static final Pattern GETTER_PATTERN =
      Pattern.compile("^(?:get|is)([A-Z])([A-Za-z0-9_]*)$");

  private static class Worker {
    private final GraphSink out;

    private Map<Object, String> visited = new IdentityHashMap<Object, String>();

    Worker(GraphSink out) {
      this.out = Preconditions.checkNotNull(out);
    }

    String renderSubgraph(Object object) {
      if (visited.containsKey(object)) {
        return visited.get(object);
      } else {
        String nodeId = "n" + visited.size();
        visited.put(object, nodeId);

        // TODO(laurence): use declared return types instead of objects class?
        @SuppressWarnings("unchecked")
        Handler<Object> handler =
          (Handler<Object>) getHandler(object.getClass());
        handler.handle(nodeId, object);

        return nodeId;
      }
    }

    private interface Handler<T> {
      /**
       * Returns true if and only if the values this handler handles can be
       * inlined in record nodes.
       */
      boolean isInlinable();

      /**
       * Render the subgraph rooted at the specified object, with the specified
       * nodeId.
       */
      void handle(String nodeId, T object);
    }

    // TODO(laurence): make this more configurable? In particular, the Node
    // class probably shouldn't be hardcoded here.
    private Handler<?> getHandler(Class<?> cls) {
      if (Map.class.isAssignableFrom(cls)) {
        return mapHandler;
      } else if (Map.Entry.class.isAssignableFrom(cls)) {
        return mapEntryHandler;
      } else if (Iterable.class.isAssignableFrom(cls)) {
        return iterableHandler;
      } else if (Node.class.isAssignableFrom(cls)) {
        return valueObjectHandler;
      } else {
        return toStringHandler;
      }
    }

    private final ToStringHandler toStringHandler = new ToStringHandler();

    /**
     * Converts object into a node using toString().
     */
    private class ToStringHandler implements Handler<Object> {
      public boolean isInlinable() {
        return true;
      }

      public void handle(String nodeId, Object obj) {
        out.simpleNode(nodeId, NodeShape.PLAIN_TEXT, javaEscape(obj));
      }
    }

   private final IterableHandler iterableHandler = new IterableHandler();

    private class IterableHandler implements Handler<Iterable<?>> {
      public boolean isInlinable() {
        return false;
      }

      public void handle(String nodeId, Iterable<?> iterable) {
        out.simpleNode(nodeId, NodeShape.PARALLELOGRAM,
                       iterable.getClass().getSimpleName());
        int i = 0;
        for (Object item : iterable) {
          out.edge(nodeId, "[" + (i++) + "]", renderSubgraph(item));
        }
      }
    }

    private final MapHandler mapHandler = new MapHandler();

    private class MapHandler implements Handler<Map<?, ?>> {
      public boolean isInlinable() {
        return false;
      }

      public void handle(String nodeId, Map<?, ?> map) {
        out.simpleNode(nodeId, NodeShape.TRIANGLE,
                       map.getClass().getSimpleName());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
          out.edge(nodeId, null, renderSubgraph(entry));
        }
      }
    }

    private final ValueObjectHandler valueObjectHandler =
        new ValueObjectHandler();

    private class ValueObjectHandler implements Handler<Object> {
      public boolean isInlinable() {
        return false;
      }

      public void handle(String nodeId, Object object) {
        Map<String, String> map = Maps.newLinkedHashMap();
        Class<?> cls = object.getClass();
        for (Method method : cls.getMethods()) {
          Matcher m = GETTER_PATTERN.matcher(method.getName());
          if (m.matches() && method.getParameterTypes().length == 0) {
            String propName = m.group(1).toLowerCase() + m.group(2);
            try {
              Object propValue = method.invoke(object);
              handleProperty(nodeId, map, propName, propValue);
            } catch (IllegalAccessException iax) {
              map.put(propName, "->" + iax.getClass().getSimpleName());
            } catch (InvocationTargetException itx) {
              map.put(propName, "->" + itx.getClass().getSimpleName());
            }
          }
        }
        out.recordNode(nodeId, map);
      }

      private void handleProperty(String nodeId,
                                  Map<String, String> map,
                                  String propName,
                                  Object propValue) {
        Handler handler = (propValue == null)
            ? null
            : getHandler(propValue.getClass());
        if (handler == null) {
          map.put(propName, "= " + javaEscape(propValue));
        } else {
          out.edge(nodeId, propName, renderSubgraph(propValue));
        }
      }
    }

    private final MapEntryHandler mapEntryHandler = new MapEntryHandler();

    private class MapEntryHandler implements Handler<Map.Entry<?, ?>> {
      public boolean isInlinable() {
        return false;
      }

      public void handle(String nodeId, Map.Entry<?, ?> entry) {
        out.simpleNode(nodeId, NodeShape.POINT, "");
        out.edge(nodeId, "key", renderSubgraph(entry.getKey()));
        out.edge(nodeId, "value", renderSubgraph(entry.getValue()));
      }
    }
  }
}
