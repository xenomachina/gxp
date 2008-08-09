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

package com.google.gxp.compiler.ant;

import com.google.gxp.compiler.fs.InMemoryFileSystem;

import org.apache.tools.ant.BuildException;

import junit.framework.TestCase;

/**
 * Tests for {@code GxpcTask}.
 */
public class GxpcTaskTest extends TestCase {
  private final InMemoryFileSystem fs = new InMemoryFileSystem();
  private final GxpcTask task = new GxpcTask(fs, fs.getRoot());

  protected void assertBuildException(String error) {
    try {
      task.configure();
      fail("should throw BuildException: " + error);
    } catch (BuildException e) {
      assertEquals(error, e.toString());
    }
  }

  public void testNoConfig() {
    assertBuildException("Attribute 'srcdir' was not set.");
  }

  // TODO(harryh): write more tests
}
