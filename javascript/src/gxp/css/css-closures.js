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

goog.provide('goog.gxp.css.CssClosures');

goog.require('goog.gxp.css.CssClosure');

/**
 * @return {goog.gxp.css.CssClosure} that renders {@code css} as literal CSS
 */
goog.gxp.css.CssClosures.fromCss = function(css) {
  return new goog.gxp.css.CssClosure(function(out, gxpContext) {
    out.append(css);
    return out;
  });
};
