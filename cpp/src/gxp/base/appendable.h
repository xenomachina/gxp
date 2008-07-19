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

// A simple wrapper class that wraps anything that can be appended to with
// a common interface.

#ifndef GXP_BASE_APPENDABLE_H__
#define GXP_BASE_APPENDABLE_H__

#include <string>

class Appendable {
 public:
  Appendable() {}
  virtual ~Appendable() {}
  virtual void Append(char c) = 0;
  virtual void Append(const string& s) = 0;
  virtual void Append(const char* s) = 0;
};

//
// NOTE: StringAppendable does not take ownership of the string*.  Callers
//       must ensure that the lifetime of the string* is longer than the
//       lifetime of the StringAppendable.
//
class StringAppendable : public Appendable {
 public:
  StringAppendable(string *outbuf) : outbuf_(outbuf) { }
  virtual void Append(char c) { *outbuf_ += c; }
  virtual void Append(const string& s) { *outbuf_ += s; }
  virtual void Append(const char* s) { *outbuf_ += s; }
 private:
  string* const outbuf_;
};

#endif  // GXP_BASE_APPENDABLE_H__
