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

goog.provide('goog.gxp.base.GxpContext');

/**
 * This is a context used for expanding GXP templates.  The context is
 * effectively a collection of parameters that are implicitely passed to all
 * sub-templates.
 *
 * @constructor
 * @param {boolean} opt_forceXmlSyntax flag indicating if gxp should generate XML
 *                  (instead of SGML).  Defaults to false.
 */
goog.gxp.base.GxpContext = function(opt_forceXmlSyntax) {
  /**
   * @type {boolean}
   * @private
   */
  this.forceXmlSyntax_ = opt_forceXmlSyntax == true;
};

/**
 * @return {boolean} Whether this context renders strict xml compliant output
 */
goog.gxp.base.GxpContext.prototype.isForcingXmlSyntax = function() {
  return this.forceXmlSyntax_;
};

/**
 * Given a method that takes an output buffer and a GxpContext, invoke
 * the method on a new buffer and return the results as a String.
 */
goog.gxp.base.GxpContext.prototype.getString = function(method) {
  var sb = new goog.string.StringBuffer();
  method(sb, this);
  return sb.toString();
};
