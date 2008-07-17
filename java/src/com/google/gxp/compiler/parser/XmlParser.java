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

import com.google.gxp.compiler.fs.FileRef;

import java.io.IOException;

/**
 * An low-level XML parser. Reads from a {@code FileRef} and generates
 * events which are "sent" to an {@code XmlEventHandler}. Note that {@code
 * XmlParser}s should be reusable and reentrant.
 */
public interface XmlParser {
  void parse(FileRef input, XmlEventHandler eventHandler) throws IOException;
}
