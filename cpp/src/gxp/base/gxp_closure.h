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

#ifndef GXP_BASE_GXP_CLOSURE_H__
#define GXP_BASE_GXP_CLOSURE_H__

class GxpContext;

//
// Interface for a GXP template with all (explicit) parameters bound.
//
class GxpClosure {
 public:
  GxpClosure() {}
  virtual ~GxpClosure() {}
  virtual void Write(class Appendable* out, const GxpContext& gxp_context) const = 0;

  void Write(string *out, const GxpContext& gxp_context) const {
    StringAppendable a(out);
    Write(&a, gxp_context);
  }
};

#endif  // GXP_BASE_GXP_CLOSURE_H__
