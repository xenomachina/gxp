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

goog.provide('goog.gxp.text.PlaintextClosure');

/**
 * Closure for content-type: text/plaintext
 *
 * @constructor
 * @param {Function} writePlaintext function that will write closure
 *                   contents to a given output.
 */
goog.gxp.text.PlaintextClosure = function(writePlaintext) {
  this.writePlaintext = writePlaintext;
  this.gxp$writePlaintext = writePlaintext;
};
