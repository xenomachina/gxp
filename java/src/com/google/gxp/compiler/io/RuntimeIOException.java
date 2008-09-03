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

package com.google.gxp.compiler.io;

import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * Wrapper for tunneling IOExceptions. Java generics and checked exceptions
 * don't really get along as well as I wish they would. Since we do a lot of IO
 * inside of visitors it's easiest to "tunnel" the (checked) IOExceptions out.
 */
public class RuntimeIOException extends RuntimeException {
  private static final long serialVersionUID = -1;

  public RuntimeIOException(IOException cause) {
    super(Preconditions.checkNotNull(cause));
  }

  @Override
  public IOException getCause() {
    return (IOException) super.getCause();
  }
}
