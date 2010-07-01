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

// Functional tests for GXP C++ Code Generation

#include "com/google/gxp/compiler/functests/CondGxp.h"
#include "com/google/gxp/compiler/functests/DoctypeGxp.h"
#include "com/google/gxp/compiler/functests/HelloGxp.h"
#include "com/google/gxp/compiler/functests/IfBasicGxp.h"
#include "com/google/gxp/compiler/functests/IfElseGxp.h"
#include "com/google/gxp/compiler/functests/IfElseIfGxp.h"
#include "com/google/gxp/compiler/functests/SomeTagsGxp.h"
#include "gxp/testing/base_functional_test_case.h"

using namespace com::google::gxp::compiler::functests;

TEST(HelloWorld) {
  HelloGxp::Write(&out, gxp_context);
  AssertOutputEquals("hello, world!");
}

const char kHtmlStrictDoctype[] =
    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\""
    " \"http://www.w3.org/TR/html4/strict.dtd\">";

const char kXhtmlStrictDoctype[] =
    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
    " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";

const char kXhtmlMobileDoctype[] =
    "<!DOCTYPE html PUBLIC \"-//WAPFORUM//DTD XHTML Mobile 1.0//EN\""
    " \"http://www.wapforum.org/DTD/xhtml-mobile10.dtd\">";

TEST(Doctype) {
  DoctypeGxp::Write(&out, gxp_context);
  AssertOutputEquals(string(kHtmlStrictDoctype) + "<html></html>");
}

TEST(SomeTags) {
  SomeTagsGxp::Write(&out, gxp_context);
  AssertOutputEquals("foo <b>bar</b> <img src=\"baz.gif\" alt=\"baz\">");
}

TEST(IfBasic) {
  IfBasicGxp::Write(&out, gxp_context, 1);
  AssertOutputEquals("That number is less than 5.");

  IfBasicGxp::Write(&out, gxp_context, 7);
  AssertOutputEquals("");
}

TEST(IfElse) {
  IfElseGxp::Write(&out, gxp_context, 1);
  AssertOutputEquals("That number is\nless than\n5.");

  IfElseGxp::Write(&out, gxp_context, 7);
  AssertOutputEquals("That number is\ngreater than or equal to\n5.");
}

TEST(IfElseIf) {
  IfElseIfGxp::Write(&out, gxp_context, 1);
  AssertOutputEquals("That number is\nless than\n5.");

  IfElseIfGxp::Write(&out, gxp_context, 5);
  AssertOutputEquals("That number is\nequal to\n5.");

  IfElseIfGxp::Write(&out, gxp_context, 7);
  AssertOutputEquals("That number is\ngreater than or equal to\n5.");
}

TEST(Cond) {
  CondGxp::Write(&out, gxp_context, 1);
  AssertOutputEquals("That number is\nless than\n5.");

  CondGxp::Write(&out, gxp_context, 5);
  AssertOutputEquals("That number is\nequal to\n5.");

  CondGxp::Write(&out, gxp_context, 7);
  AssertOutputEquals("That number is\ngreater than or equal to\n5.");
}
