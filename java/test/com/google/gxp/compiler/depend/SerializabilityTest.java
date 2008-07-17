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

package com.google.gxp.compiler.depend;

import com.google.gxp.compiler.GxpcTestCase;
import com.google.gxp.compiler.alerts.SourcePosition;
import com.google.gxp.compiler.base.FormalTypeParameter;
import com.google.gxp.compiler.base.Import;
import com.google.gxp.compiler.base.Interface;
import com.google.gxp.compiler.base.Parameter;
import com.google.gxp.compiler.base.ThrowsDeclaration;
import com.google.gxp.compiler.base.Template;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;

/**
 * Tests of serializability.
 */
public class SerializabilityTest extends GxpcTestCase {
  public void testCallables() throws Exception {
    SourcePosition pos = pos();

    Template template1 = template(pos, "com.google.foo.Bar", schema("text/html"),
                                  Collections.<Import>emptyList(),
                                  Collections.<ThrowsDeclaration>emptyList(),
                                  Collections.<Parameter>emptyList(),
                                  Collections.<FormalTypeParameter>emptyList(),
                                  str("foo"));

    Template template2 = template(pos, "com.google.foo.Bar", schema("text/html"),
                                  Collections.<Import>emptyList(),
                                  Collections.<ThrowsDeclaration>emptyList(),
                                  Collections.<Parameter>emptyList(),
                                  Collections.<FormalTypeParameter>emptyList(),
                                  str("foo"));

    Interface iface1 = iface(pos, "com.google.foo.Bar", schema("text/html"),
                             Collections.<Import>emptyList(),
                             Collections.<ThrowsDeclaration>emptyList(),
                             Collections.<Parameter>emptyList(),
                             Collections.<FormalTypeParameter>emptyList());

    Interface iface2 = iface(pos, "com.google.foo.Bar", schema("text/html"),
                             Collections.<Import>emptyList(),
                             Collections.<ThrowsDeclaration>emptyList(),
                             Collections.<Parameter>emptyList(),
                             Collections.<FormalTypeParameter>emptyList());

    assertSerializes(template1.getCallable(), template2.getCallable());
    assertSerializes(template1.getInstanceCallable(), template2.getInstanceCallable());
    assertSerializes(iface1.getCallable(), iface2.getCallable());
    assertSerializes(iface1.getInstanceCallable(), iface2.getInstanceCallable());
    assertSerializes(iface1.getImplementable(), iface2.getImplementable());
  }

  public void testFormalParameter() throws Exception {
    // TODO
  }

  public void testSchemas() throws Exception {
    assertSerializes(schema("text/plain"), schema("text/plain"));
    assertSerializes(schema("text/css"),   schema("text/css"));
    assertSerializes(schema("text/html"),  schema("text/html"));
  }

  public void testTypes() throws Exception {
    SourcePosition pos = pos();

    assertSerializes(booleanType(pos),
                     booleanType(pos));

    assertSerializes(bundleType(pos, schema("text/html"), "div", "class"),
                     bundleType(pos, schema("text/html"), "div", "class"));

    assertSerializes(contentType(pos, schema("text/html")),
                     contentType(pos, schema("text/html")));

    assertSerializes(instanceType(pos, "com.google.foo.Bar"),
                     instanceType(pos, "com.google.foo.Bar"));

    assertSerializes(nativeType(pos, "String"),
                     nativeType(pos, "String"));

    assertSerializes(templateType(pos, "com.google.foo.Bar"),
                     templateType(pos, "com.google.foo.Bar"));
  }

  /**
   * NOTE: o1 and o2 should be created to be the same.  We can't just take
   *       a single object and test it against itself because that would not
   *       fully test that all appropriate fields were being serialized and
   *       used when testing equality.
   */
  protected <T extends Serializable> void assertSerializes(T o1, T o2) throws Exception {
    T o3 = serializeAndDeserialize(o2);

    // we have different objects
    assert(o1 != o2);
    assert(o1 != o3);
    assert(o2 != o3);

    // with the same hash codes
    assertEquals(o1.hashCode(), o2.hashCode());
    assertEquals(o1.hashCode(), o3.hashCode());
    assertEquals(o2.hashCode(), o3.hashCode());

    // that are equal
    assertEquals(o1, o2);
    assertEquals(o1, o3);
    assertEquals(o2, o3);
  }

  @SuppressWarnings("unchecked")
  protected <T extends Serializable> T serializeAndDeserialize(T input) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(out);
    oos.writeObject(input);
    oos.close();
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
    T reconstituted = (T) ois.readObject();
    ois.close();
    return reconstituted;
  }
}
