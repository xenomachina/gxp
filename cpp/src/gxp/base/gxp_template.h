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

#ifndef GXP_BASE_GXP_TEMPLATE_H__
#define GXP_BASE_GXP_TEMPLATE_H__

//
// This is the base class of all GXP templates. Contains helper classes and
// functions.
//
// Subclasses will have static methods of the form:
//
//   static void Write(??? gxp_out, GxpContext gxp_context, ...)
//   static GxpClosure GetGxpClosure(...)
//
// where '...' is the list of parameters defined in the GXP source.
//
class GxpTemplate {
};

#endif  // GXP_BASE_GXP_TEMPLATE_H__
