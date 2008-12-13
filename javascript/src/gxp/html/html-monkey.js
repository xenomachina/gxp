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

goog.provide('goog.gxp.html');

goog.require('goog.gxp.base');
goog.require('goog.gxp.css');
goog.require('goog.gxp.html.HtmlClosure');
goog.require('goog.gxp.html.HtmlClosures');
goog.require('goog.gxp.js');
goog.require('goog.gxp.text');
goog.require('goog.string.StringBuffer');

String.prototype.gxp$writeHtml = function(out, gxpContext) {
  out.append(goog.string.htmlEscape(this));
  return out;
};

Number.prototype.gxp$writeHtml = function(out, gxpContext) {
  out.append(this);
  return out;
};

goog.gxp.css.CssClosure.prototype.gxp$writeHtml = function(out, gxpContext) {
  out.append('<style type="text/css">\n');
  this.gxp$writeCss(out, gxpContext);
  out.append('\n</style>');
  return out;
};

goog.gxp.js.JavascriptClosure.prototype.gxp$writeHtml = function(out, gxpContext) {
  out.append('<script type="text/javascript">\n');
  this.gxp$writeJavascript(out, gxpContext);
  out.append('\n</script>');
  return out;
};

goog.gxp.text.PlaintextClosure.prototype.gxp$writeHtml = function(out, gxpContext) {
  var sb = new goog.string.StringBuffer();
  this.gxp$writePlaintext(sb, gxpContext);
  return sb.toString().gxp$writeHtml(out, gxpContext);
};
