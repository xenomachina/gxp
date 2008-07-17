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

package com.google.gxp.compiler.util;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

/**
 * Maps from classes to objects (not necessarily objects of that class).
 * Attempts to adhere to the Liskov Substitution principle in that requests for
 * a class that does not have any value associated with it will retrieve the
 * value for its nearest superclass.
 *
 * TODO(laurence): Make this implement Map? What should it mean to iterate over
 * one of these? The remove method also seems weird. Actualy, even put is a bit
 * funny. Should it wipe out settings for subclasses?
 *
 * @param <K> base class for keys
 * @param <V> type of values
 */
public class ClassMap<K, V> implements Serializable {
  private final Map<Class<?>, V> map;

  public ClassMap() {
    this.map = Maps.newIdentityHashMap();
  }

  public static <K, V> ClassMap<K, V> create() {
    return new ClassMap<K, V>();
  }

  public V put(Class<? extends K> key, V value) {
    if (key.isInterface()) {
      // Interfaces are disallowed because the semantics get really weird with
      // multiple inheritance.
      // TODO(laurence): Is there a way to statically enforce this?
      throw new IllegalArgumentException(
          "ClassMap cannot use interfaces as keys!");
    }
    V result = map.put(key, value);
    if (result == null) {
      result = getImpl(key.getSuperclass());
    }
    return result;
  }

  public V get(Object key) {
    if (key instanceof Class) {
      return getImpl((Class<?>) key);
    } else {
      return null;
    }
  }

  private V getImpl(Class<?> key) {
    Class<?> superKey = key;
    while (superKey != null) {
      V result = map.get(superKey);
      if (result != null) {
        return result;
      }
      superKey = superKey.getSuperclass();
    }
    return null;
  }

  private static final long serialVersionUID = 1L;
}
