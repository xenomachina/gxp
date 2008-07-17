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

import java.util.*;

/**
 * A "sink" for information about a (directed) graph.
 */
public interface GraphSink {
  /**
   * Called at the start of the graph.
   *
   * @param name the name of the graph
   */
  public void digraphStart(String name);

  /**
   * Called at the end of the graph.
   *
   * @param name the name of the graph
   */
  public void digraphEnd();

  /**
   * Called once for each record node.
   *
   * @param nodeId unique ID for the node
   * @param nodeId unique ID for the node
   */
  public void recordNode(String nodeId, Map<String, String> properties);

  /**
   * Called once for each simple node.
   *
   * @param nodeId unique ID for the node
   * @param shape the shape of the node
   * @param label the node's label
   */
  public void simpleNode(String nodeId, NodeShape shape, String label);

  /**
   * Called once for each directed edge
   *
   * @param fromNodeId ID of source node
   * @param label the edge's label
   * @param fromNodeId ID of destination node
   */
  public void edge(String fromNodeId, String label, String toNodeId);
}
