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

// Main test runner. Runs all test declared with the TEST() macro

#include <vector>

vector<void (*)()> g_testlist;  // the tests to run

int main(int argc, char **argv) {
  for (vector<void (*)()>::const_iterator it = g_testlist.begin();
       it != g_testlist.end(); ++it) {
    (*it)();
  }
  return 0;
}
