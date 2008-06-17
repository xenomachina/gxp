/*
 * Copyright (C) 2004 Google Inc.
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
 * An exception class used for "tunneling" checked exceptions across generic
 * interface boundaries. This is currently used for catching and rethrowing
 * exceptions in the automatically generated anonymous inner class implemented
 * by GxpTemplate.getGxpClosure.
 */
public class GxpRuntimeException extends RuntimeException {
  private static final long serialVersionUID = -1;

  /**
   * @return cause wrapped in a GxpRuntimeException, unless it's already a
   * RuntimeException in which case cause itself is returned.
   */
  public static RuntimeException wrap(Exception cause) {
    if (cause instanceof RuntimeException) {
      return (RuntimeException) cause;
    } else {
      return new GxpRuntimeException(cause);
    }
  }

  /**
   * Creates a GxpRuntimeException with the specified cause. Try to avoid
   * wrapping RuntimeExceptions in GxpRuntimeExceptions. It's recommended that
   * subclasses create their own implementation of {@link #wrap(Exception)}.
   */
  protected GxpRuntimeException(Exception cause) {
    super(cause);
  }
}
