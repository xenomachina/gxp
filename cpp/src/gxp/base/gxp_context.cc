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

#include "gxp/base/gxp_context.h"

GxpContext::GxpContext(bool is_using_xml_syntax)
    : is_using_xml_syntax_(is_using_xml_syntax) {
}

bool GxpContext::IsUsingXmlSyntax() {
  return is_using_xml_syntax_;
}
