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

package com.google.gxp.rss;

import com.google.gxp.testing.BaseFunctionalTestCase;

/**
 * Tests for {@code RssClosures}
 */
public class RssClosuresTest extends BaseFunctionalTestCase {
  public void testEmpty() throws Exception {
    RssClosures.EMPTY.write(out, gxpContext);
    assertOutputEquals("");
  }

  public void testFromRss() throws Exception {
    RssClosures.fromRss("foo < bar > baz & quux").write(out, gxpContext);
    assertOutputEquals("foo < bar > baz & quux");

    try {
      RssClosures.fromRss(null);
      fail("should have thrown NullPointerException");
    } catch (NullPointerException e) {
      // good
    }
  }
}
