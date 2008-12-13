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

goog.provide('goog.gxp.base.GxpAttrBundle');
goog.provide('goog.gxp.base.GxpAttrBundle.Builder');

goog.require('goog.gxp.base.GxpContext');

/**
 * An attribute bundle.  Used by the GXP compiler for bundling up a bunch
 * of attributes into a single item.
 *
 * @constructor
 * @param attrs
 * @param booleanAttrs
 * @this {goog.gxp.base.GxpAttrBundle} instance
 */
goog.gxp.base.GxpAttrBundle = function(method, attrs, booleanAttrs) {
  this.attrs = attrs;
  this.booleanAttrs = booleanAttrs;
  this[method] = function(out, gxpContext) {
    for (var attr in attrs) {
      out.append(' ');
      out.append(attr);
      out.append('="');
      attrs[attr][method](out, gxpContext);
      out.append('"');
    }
    for (var attr in booleanAttrs) {
      out.append(' ');
      out.append(booleanAttrs[attr]);
      if (gxpContext.isUsingXmlSyntax()) {
        out.append('="');
        out.append(booleanAttrs[attr]);
        out.append('"');
      }
    }
  };
};

/**
 * An attribute bundle builder.
 *
 * @constructor
 * @param method
 * @param opt_includeAttr
 * @param var_args
 * @this {goog.gxp.base.GxpAttrBundle.Builder} instance
 */
goog.gxp.base.GxpAttrBundle.Builder = function(method, opt_includeAttr, var_args) {
  this.method = method;
  this.attrs = new Object();
  this.booleanAttrs = new Array();
  if (opt_includeAttr != null) {
    this.includeAttrs = new Object(); 
    for (var i = 1; i < arguments.length; i++) {
      this.includeAttrs[arguments[i]] = true;
    }
  }
};

/**
 * @param name
 */
goog.gxp.base.GxpAttrBundle.Builder.prototype.includeAttr = function(name) {
  return (!this.includeAttrs || this.includeAttrs[name]);
};

/**
 * @param name
 * @param value
 * @param opt_cond
 */
goog.gxp.base.GxpAttrBundle.Builder.prototype.attr = function(name, value, opt_cond) {
  if (opt_cond === undefined || opt_cond) {
    if (typeof value == 'boolean') {
      if (value) {
        this.booleanAttrs.push(name);
      }
    } else {
      this.attrs[name] = value;
    }
  }
  return this;
};

/**
 * @param bundle
 */
goog.gxp.base.GxpAttrBundle.Builder.prototype.addBundle = function(bundle) {
  for (var attr in bundle.attrs) {
    if (this.includeAttr(attr)) {
      this.attrs[attr] = bundle.attrs[attr];
    }
  }
  for (var attr in bundle.booleanAttrs) {
    if (this.includeAttr(bundle.booleanAttrs[attr])) {
      this.booleanAttrs.push(bundle.booleanAttrs[attr]);
    }
  }
  return this;
};

goog.gxp.base.GxpAttrBundle.Builder.prototype.build = function() {
  return new goog.gxp.base.GxpAttrBundle(this.method, this.attrs, this.booleanAttrs);
};
