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

package com.google.gxp.compiler.collapse;

import com.google.gxp.compiler.base.SpaceOperator;

import junit.framework.TestCase;

/**
 * Tests for com.google.gxp.compiler.collapse.SpaceOperators
 */
public class SpaceOperatorTest extends TestCase {
  /**
   * An array of test cases. Each row is a testcase, each column is the
   * expected output for a particular SpaceOperator. The first column
   * (PRESERVE) is also the input for each testcase. (PRESERVE leaves the input
   * alone, by definition)
   */
  private static final String[][] DATA = new String[][] {
    //           PRESERVE       REMOVE  NORMALIZE  COLLAPSE
    new String[]{"",            "",     "",        ""  },
    new String[]{" ",           "",     " ",       " " },
    new String[]{"\t",          "",     " ",       " " },
    new String[]{"\n",          "",     " ",       "\n"},
    new String[]{"          ",  "",     " ",       " " },
    new String[]{"\n        ",  "",     " ",       "\n"},
    new String[]{"        \n",  "",     " ",       "\n"},
    new String[]{"  \n  \n  ",  "",     " ",       "\n"},
    new String[]{"  \t      ",  "",     " ",       " " },
    new String[]{"  \t  \n  ",  "",     " ",       "\n"},
  };

  public void testPreserve() throws Exception {
    testOperator(SpaceOperator.PRESERVE, 0);
  }

  public void testRemove() throws Exception {
    testOperator(SpaceOperator.REMOVE, 1);
  }

  public void testNormalize() throws Exception {
    testOperator(SpaceOperator.NORMALIZE, 2);
  }

  public void testCollapse() throws Exception {
    testOperator(SpaceOperator.COLLAPSE, 3);
  }

  /**
   * @param operator SpaceOperator to test.
   * @param columnNumber index of column in DATA that contains expected output
   * for operator.
   */
  private void testOperator(SpaceOperator operator, int columnNumber)
      throws Exception {
    for (int rowNumber = 0; rowNumber < DATA.length; rowNumber++) {
      assertEquals(DATA[rowNumber][columnNumber],
                   operator.apply(DATA[rowNumber][0]));
    }
    assertThrowsIllegalArgumentException(operator, " x ");
    assertThrowsIllegalArgumentException(operator, "\nx ");
  }

  private void assertThrowsIllegalArgumentException(SpaceOperator operator,
                                                    String badInput)
      throws Exception {
    try {
      operator.apply(badInput);
      fail("SpaceOperators should throw IllegalArgumentException"
           + " on non-spaces.");
    } catch (IllegalArgumentException iax) {
      // yay!
    }
  }
}
