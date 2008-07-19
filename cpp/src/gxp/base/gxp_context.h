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

#ifndef GXP_BASE_GXP_CONTEXT_H__
#define GXP_BASE_GXP_CONTEXT_H__

//
// This is a context used for expanding GXP templates.  The context is
// effectively a collection of parameters that are implicitely passed to all
// sub-templates.
//
class GxpContext {
  public:
    GxpContext(bool is_using_xml_syntax);
    bool IsUsingXmlSyntax();
  private:
    bool is_using_xml_syntax_;
};

#endif  // GXP_BASE_GXP_CONTEXT_H__
