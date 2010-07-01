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

#include <iostream>
#include <stdlib.h>
#include "gxp/base/gxp_context.h"
#include "gxp/testing/base_functional_test_case.h"

string BaseFunctionalTestCase::out;

const GxpContext BaseFunctionalTestCase::gxp_context(false);
const GxpContext BaseFunctionalTestCase::xml_gxp_context(true);

void BaseFunctionalTestCase::AssertOutputEquals(const string& expected) {
  if (expected.compare(out) != 0) {
    cout << "Check failed. Expected: " << endl;
    cout << expected << endl;
    cout << "Found: " << endl;
    cout << out << endl;
    exit(1);
  }
  out.clear();
}
