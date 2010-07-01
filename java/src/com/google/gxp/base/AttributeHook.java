/*
 * Copyright (C) 2009 Google Inc.
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

package com.google.gxp.base;

/**
 * The different attribute hooks that can examined at runtime in the GXP.
 */
public enum AttributeHook {
  // A hook type which is called when an element references an
  // external resource which will be embedded in the page.
  // For example: "src" in <img src="http://www.google.com/img.jpg" />
  EMBEDS_EXTERNAL_RESOURCE,
}
