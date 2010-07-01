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

package com.google.gxp.compiler.schema;

import junit.framework.TestCase;

/**
 * Unit test for com.google.gxp.compiler.schema.SchemaParser.
 */
public class SchemaParserTest extends TestCase {
  /**
   * The HTML schema is all we really care about for now, so don't bother
   * testing the general case.
   */
  public void testHtmlSchema() throws Exception {
    Schema schema = BuiltinSchemaFactory.INSTANCE.fromContentTypeName("text/html");

    // Check schema properties.
    assertEquals("html", schema.getName());
    assertEquals("http://www.w3.org/1999/xhtml", schema.getNamespaceUri());
    assertEquals("application/xhtml+xml", schema.getXmlContentType());
    assertTrue(schema.defaultsToSgml());
    assertEquals("text/html", schema.getSgmlContentType());
    assertEquals("text/html", schema.getCanonicalContentType());
    assertEquals("com.google.gxp.html.HtmlClosure", schema.getJavaType());

    // Non-existant elements don't exist.
    assertNull(schema.getElementValidator("foo"));
    assertNull(schema.getElementValidator("bar"));
    assertNull(schema.getElementValidator("baz"));

    // Real elements do exist...
    ElementValidator aTag = schema.getElementValidator("a");
    ElementValidator htmlTag = schema.getElementValidator("html");
    ElementValidator styleTag = schema.getElementValidator("style");
    ElementValidator basefontTag = schema.getElementValidator("basefont");
    ElementValidator frameTag = schema.getElementValidator("frame");
    ElementValidator framesetTag = schema.getElementValidator("frameset");
    ElementValidator embedTag = schema.getElementValidator("embed");
    ElementValidator divTag = schema.getElementValidator("div");
    ElementValidator inputTag = schema.getElementValidator("input");
    ElementValidator ulTag = schema.getElementValidator("ul");
    ElementValidator imgTag = schema.getElementValidator("img");
    ElementValidator formTag = schema.getElementValidator("form");

    // and have the right names...
    assertEquals("html", htmlTag.getTagName());
    assertEquals("style", styleTag.getTagName());
    assertEquals("basefont", basefontTag.getTagName());
    assertEquals("frame", frameTag.getTagName());
    assertEquals("frameset", framesetTag.getTagName());
    assertEquals("embed", embedTag.getTagName());
    assertEquals("div", divTag.getTagName());
    assertEquals("input", inputTag.getTagName());
    assertEquals("ul", ulTag.getTagName());
    assertEquals("img", imgTag.getTagName());
    assertEquals("form", formTag.getTagName());

    // and the right flags.
    assertTrue(styleTag.isFlagSet(ElementValidator.Flag.CHILDLESS));
    assertTrue(styleTag.isFlagSet(ElementValidator.Flag.EVILCDATA));
    assertTrue(basefontTag.isFlagSet(ElementValidator.Flag.DEPRECATED));
    assertTrue(frameTag.isFlagSet(ElementValidator.Flag.FRAMESETDTD));
    assertTrue(basefontTag.isFlagSet(ElementValidator.Flag.LOOSEDTD));
    assertTrue(basefontTag.isFlagSet(ElementValidator.Flag.NOENDTAG));
    assertTrue(htmlTag.isFlagSet(ElementValidator.Flag.OPTIONALENDTAG));
    assertTrue(styleTag.isFlagSet(ElementValidator.Flag.INVISIBLEBODY));
    assertTrue(styleTag.isFlagSet(ElementValidator.Flag.PRESERVESPACES));
    assertTrue(embedTag.isFlagSet(ElementValidator.Flag.NONSTANDARD));

    assertFalse(htmlTag.isFlagSet(ElementValidator.Flag.CHILDLESS));
    assertFalse(basefontTag.isFlagSet(ElementValidator.Flag.EVILCDATA));
    assertFalse(htmlTag.isFlagSet(ElementValidator.Flag.DEPRECATED));
    assertFalse(embedTag.isFlagSet(ElementValidator.Flag.FRAMESETDTD));
    assertFalse(divTag.isFlagSet(ElementValidator.Flag.LOOSEDTD));
    assertFalse(htmlTag.isFlagSet(ElementValidator.Flag.NOENDTAG));
    assertFalse(styleTag.isFlagSet(ElementValidator.Flag.OPTIONALENDTAG));
    assertFalse(basefontTag.isFlagSet(ElementValidator.Flag.INVISIBLEBODY));
    assertFalse(divTag.isFlagSet(ElementValidator.Flag.PRESERVESPACES));
    assertFalse(htmlTag.isFlagSet(ElementValidator.Flag.NONSTANDARD));

    // Check that non-existant attributes don't exist.
    assertNull(htmlTag.getAttributeValidator("foo"));
    assertNull(styleTag.getAttributeValidator("bar"));
    assertNull(htmlTag.getAttributeValidator("type"));
    assertNull(styleTag.getAttributeValidator("class"));

    // Check that elements have attributes they should.
    AttributeValidator classAttr = divTag.getAttributeValidator("class");
    AttributeValidator typeAttr = styleTag.getAttributeValidator("type");
    AttributeValidator alignAttr = divTag.getAttributeValidator("align");
    AttributeValidator hrefAttr = aTag.getAttributeValidator("href");
    AttributeValidator autocompleteAttr =
        inputTag.getAttributeValidator("autocomplete");

    assertEquals("class", classAttr.getName());
    assertEquals("type", typeAttr.getName());
    assertEquals("align", alignAttr.getName());
    assertEquals("autocomplete", autocompleteAttr.getName());

    // Test attribute value validation.
    assertTrue(classAttr.isValidValue("foobar"));
    assertTrue(alignAttr.isValidValue("left"));
    assertFalse(alignAttr.isValidValue("steal-their-cookies"));
    assertTrue(autocompleteAttr.isValidValue("on"));
    assertFalse(autocompleteAttr.isValidValue("maybe"));
    assertTrue(autocompleteAttr.isValidValue("ON"));

    // Test attribute flags;
    AttributeValidator compactAttr = ulTag.getAttributeValidator("compact");
    AttributeValidator onpagehideAttr =
        framesetTag.getAttributeValidator("onpagehide");
    AttributeValidator imgAltAttr = imgTag.getAttributeValidator("alt");
    AttributeValidator inputAltAttr = inputTag.getAttributeValidator("alt");
    AttributeValidator oncontextmenuAttr =
        divTag.getAttributeValidator("oncontextmenu");

    assertTrue(compactAttr.isFlagSet(AttributeValidator.Flag.BOOLEAN));
    assertTrue(compactAttr.isFlagSet(AttributeValidator.Flag.DEPRECATED));
    assertTrue(onpagehideAttr.isFlagSet(AttributeValidator.Flag.FRAMESETDTD));
    assertTrue(compactAttr.isFlagSet(AttributeValidator.Flag.LOOSEDTD));
    assertTrue(imgAltAttr.isFlagSet(AttributeValidator.Flag.REQUIRED));
    assertTrue(imgAltAttr.isFlagSet(AttributeValidator.Flag.VISIBLETEXT));
    assertTrue(onpagehideAttr.isFlagSet(AttributeValidator.Flag.NONSTANDARD));
    assertTrue(oncontextmenuAttr.isFlagSet(
        AttributeValidator.Flag.INTERNAL_ONLY));

    assertFalse(onpagehideAttr.isFlagSet(AttributeValidator.Flag.BOOLEAN));
    assertFalse(imgAltAttr.isFlagSet(AttributeValidator.Flag.DEPRECATED));
    assertFalse(compactAttr.isFlagSet(AttributeValidator.Flag.FRAMESETDTD));
    assertFalse(imgAltAttr.isFlagSet(AttributeValidator.Flag.LOOSEDTD));
    assertFalse(inputAltAttr.isFlagSet(AttributeValidator.Flag.REQUIRED));
    assertFalse(onpagehideAttr.isFlagSet(AttributeValidator.Flag.VISIBLETEXT));
    assertFalse(imgAltAttr.isFlagSet(AttributeValidator.Flag.NONSTANDARD));
    assertFalse(imgAltAttr.isFlagSet(AttributeValidator.Flag.INTERNAL_ONLY));

    // Test default Values
    AttributeValidator enctypeAttr = formTag.getAttributeValidator("enctype");
    assertNull(imgAltAttr.getDefaultValue());

    assertEquals("application/x-www-form-urlencoded",
                 enctypeAttr.getDefaultValue());

    // test doctypes
    DocType docType = htmlTag.getDocType("strict");
    assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN",
                 docType.getPublicId());
    assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd",
                 docType.getSystemId());
    assertEquals("-//W3C//DTD HTML 4.01//EN",
                 docType.getSgmlPublicId());
    assertEquals("http://www.w3.org/TR/html4/strict.dtd",
                 docType.getSgmlSystemId());
    assertEquals(null, htmlTag.getDocType("bogus"));
    assertEquals(null, imgTag.getDocType("strict"));

    // test content type
    assertEquals("text/css", styleTag.getInnerContentType());
    assertEquals(null, htmlTag.getInnerContentType());
  }

  // TODO(laurence): create general tests (ie: not HTML schema specific tests)
}
