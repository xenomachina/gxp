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

package com.google.gxp.compiler.functests.annotate;

import com.google.gxp.testing.BaseFunctionalTestCase;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

/**
 * Annotation tests for gxpc
 */
public class JavaCodeTest extends BaseFunctionalTestCase {
  public void testInterface() throws Exception {
    Class<AnnotatedInterfaceGxp> iface = AnnotatedInterfaceGxp.class;

    // check for interface annotations
    if (!iface.isAnnotationPresent(BazAnnotation.class)) {
      fail("Interface is missing BazAnnotation.");
    }

    if (!iface.isAnnotationPresent(BuzAnnotation.class)) {
      fail("Interface is missing BuzAnnotation.");
    }

    if (!iface.isAnnotationPresent(QuxAnnotation.class)) {
      fail("Interface is missing QuxAnnotation.");
    }
  }

  public void testTemplate() throws Exception {
    Class<AnnotatedGxp> cls = AnnotatedGxp.class;
    Class<AnnotatedGxp.Interface> interfaceClass = AnnotatedGxp.Interface.class;
    Class<AnnotatedGxp.Instance> instanceClass = AnnotatedGxp.Instance.class;
    Constructor ctor = instanceClass.getConstructors()[0];

    // check for class annotations
    if (!cls.isAnnotationPresent(BazAnnotation.class)) {
      fail("Class is missing BazAnnotation.");
    }

    if (!cls.isAnnotationPresent(BuzAnnotation.class)) {
      fail("Class is missing BuzAnnotation.");
    }

    if (!cls.isAnnotationPresent(QuxAnnotation.class)) {
      fail("Class is missing QuxAnnotation.");
    }

    // check for interface class annotation
    if (!interfaceClass.isAnnotationPresent(QuuxAnnotation.class)) {
      fail("Interface class is missing QuuxAnnotation.");
    }

    // check for instance class annotation
    if (!instanceClass.isAnnotationPresent(QuuuxAnnotation.class)) {
      fail("Instance class is missing QuuuxAnnotation.");
    }

    // check for constructor annotation
    if (!ctor.isAnnotationPresent(FooAnnotation.class)) {
      fail("Instance constructor is missing FooAnnotation.");
    }

    // check for parameter annotation
    Annotation paramAnnotation = ctor.getParameterAnnotations()[0][0];
    assertEquals(paramAnnotation.annotationType(), BarAnnotation.class);
  }
}
