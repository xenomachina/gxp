// Copyright (C) 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#ifndef GXP_TESTING_BASE_FUNCTIONAL_TEST_CASE_H__
#define GXP_TESTING_BASE_FUNCTIONAL_TEST_CASE_H__

#include <string>
#include <vector>

//
// Base class for gxp functional testing. Typical protocol for a test
// is to call the Write() method on a gxp class specifying &out for the
// out param and gxp_context for the GxpContext, and then call
// AssertOutputEquals() with the expected output.
//
class BaseFunctionalTestCase {
 protected:
  static string out;
  static const GxpContext gxp_context;
  static const GxpContext xml_gxp_context;
  static void AssertOutputEquals(const string& expected);
};

extern vector<void (*)()> g_testlist;  // the tests to run

#define TEST(a)                                    \
  class Test_##a : BaseFunctionalTestCase {        \
   public:                                         \
    Test_##a() { g_testlist.push_back(&Run); }     \
    static void Run();                             \
  };                                               \
  static Test_##a g_test_##a;                      \
  void Test_##a::Run()

#endif  // GXP_TESTING_BASE_FUNCTIONAL_TEST_CASE_H__
