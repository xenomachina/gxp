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

/**
 * This package contains an abstraction for a filesystem. This provides an
 * abstraction layer between the GXP compiler and the API available to the
 * GXP compiler consumer.
 *
 * <p>A good place to start understanding this package is with the
 * {@link FileSystem} interface. A {@code FileSystem} is better thought of as
 * a {@link FileRef} factory.
 *
 * <p>The {@link FileStore} class is an internal representation of {@code
 * FileSystem}, meant to supply {@link InputStream} and {@link OutputStream}s
 * to the {@code FileRef} objects.
 *
 * <p>The {@link FileRef} class represents an abstract reference to a file
 * and also contains a number of methods for interacting with the file
 * abstractions (three examples are {@link FileRef#openInputStream()},
 * {@link FileRef#openOutputStream()}, and
 * {@link FileRef#isAncestorOf(FileRef)})
 *
 * <p>At this point you're probably itching for an example. Take a look at
 * {@link InMemoryFileSystem}: it's fairly small and shows the interaction of
 * all these components.
 *
 * <p>{@link SystemFileSystem} is a loose adapter to the filesystem provided
 * by the {@code java.io} libraries. This can be seen by reading the
 * internal implementation of {@code FileSystem} provided by
 * {@code SystemFileSystem}. Read that implementation and see how light the
 * implementations of {@link FileStore#openInputStream(FileRef)}
 * and {@link FileStore#openOutputStream(FileRef)} are.
 *
 * <p>I don't get {@link SourcePathFileSystem} yet. The word "weird" appears
 * in the first sentence of the class javadoc, so I'm not gonna handle that
 * one tonight.
 *
 * <p>Note that these filesystem abstractions are not meant to be feature-rich;
 * they are only meant to satisfy those features required for the GXP compiler.
 *
 * <h2>What is a system filename?</h2>
 *
 * This package makes a distinction between <i>system filenames</i>, which
 * are filename-dependent strings, and {@link FileRef} objects, that
 * internally use a simple, idealized Unix-like path structure.
 *
 * A <i>system filename</i> is supposed to be the type of filename that users
 * are used to dealing with in whatever system. eg:
 *
 * {@code C:\\windows\\foobar.txt} on Windows
 * {@code /usr/lib/hello} on Unix
 * {@code http://example.com/foo} in a URL-based filesystem
 * {@code /MyProject/java/com/google/my.gxp} in an eclipse-based filesystem.
 * {@code buffer#3: Snarf.java} in some unknown editing environment
 */

package com.google.gxp.compiler.fs;

import java.io.InputStream;
import java.io.OutputStream;
