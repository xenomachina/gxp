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

goog.provide('goog.gxp.base');

goog.require('goog.gxp.base.GxpContext');
goog.require('goog.gxp.base.GxpAttrBundle');
goog.require('goog.gxp.base.GxpAttrBundle.Builder');

/**
 * A function that will either iterate over an Array with a standard
 * for loop, or over an Object with a for in loop. Used by generated
 * code to properly handle <gxp:loop>s whether the iterable is an
 * Array or an Object
 */
goog.gxp.base.forEach = function(iterable, f) {
  if (iterable instanceof Array) {
    for (var i = 0; i < iterable.length; i++) {
      f(i, iterable[i], i == 0);
    }
  } else {
    var isFirst = true;
    for (var key in iterable) {
      f(key, iterable[key], isFirst);
      isFirst = false;
    }
  }
}
