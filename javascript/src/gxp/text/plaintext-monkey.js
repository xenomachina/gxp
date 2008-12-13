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

goog.provide('goog.gxp.text');

goog.require('goog.gxp.base');
goog.require('goog.gxp.text.PlaintextClosure');
goog.require('goog.gxp.text.PlaintextClosures');

Number.prototype.gxp$writePlaintext = function(out, gxpContext) {
  out.append(this);
  return out;
};

String.prototype.gxp$writePlaintext = function(out, gxpContext) {
  out.append(this);
  return out;
};
