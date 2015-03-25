## XML Syntax ##

### Why do I keep getting "not well-formed" errors? ###

GXP is an XML language. XML has a concept of "well-formedness". Your file does
not comply, hence the problem. XML is much more strict than HTML (and HTML is
actually a lot more strict than most people realize -- browsers are very
forgiving and will accept a lot of invalid HTML).

The most common problems people run into are:

  * bare `<`'s and `>`'s aren't allowed anywhere, not even inside quoted attribute values, except as tag delimiters
  * ampersands always start an entity reference (except in a CDATA section). If you need a bare ampersand (`&`) use `&amp;` -- this includes inside of URLs (like in `href` or `src`).
  * attribute values must be quoted (either single or double quotes are acceptable)
  * quotes in attribute values must be escaped if they are the same kind of quote used to quote the attribute value
  * all tags must have a corresponding end tag, or you can used the `<... />` syntactic sugar. So tags like the `<img ...>` tag in HTML must be `<img .../>` or `<img ...></img>`
  * tags must be nested correctly.


### How do I use entities like nbsp? ###

Your document must include a doctype to tell the xml parser about html
entities.  Try inserting this at the top of your document:

```
<?xml version="1.0" ?>
<!DOCTYPE gxp:template SYSTEM "http://gxp.googlecode.com/svn/trunk/resources/xhtml.ent">
```


### What does "unbound prefix" mean? ###

This means that you're using an XML namespace prefix (eg: the `"gxp:"` in `"<gxp:template>"`) that hasn't been declared. To fix the problem you need to add an `xmlns` declaration to your `<gxp:template>` element. For example:

```
<gxp:template name="com.google.foo.Bar"
              xmlns="http://www.w3.org/1999/xhtml"
              xmlns:call="http://google.com/2001/gxp/call"
              xmlns:expr="http://google.com/2001/gxp/expressions"
              xmlns:gxp="http://google.com/2001/gxp">
```

See also [Namespaces in XML 1.0: Declaring Namespaces](http://www.w3.org/TR/REC-xml-names/#ns-decl).


### The 

&lt;input&gt;

 element doesn't support onClick, onFocus, or onChange. What should I do? ###

It does, only they're called `onclick`, `onfocus`, and `onchange`. GXP is based on XHTML, and in XHTML, all HTML attribute and element names are lowercase.


### How to I make a checkbox/radio-button checked? ###

I have a checkbox/radio-button that I want to be always checked. In HTML I'd write <input type="checkbox" name="foo" value="bar" checked>, but this doesn't work in GXP. What should I do?

This is another XHTML versus HTML difference. In GXP and XHTML you make a boolean attribute 'true' by making its value the same as its name. So to make something checked, you need to write `checked="checked"`.

If the attribute's value is dynamic, just use a boolean expression as the value in GXP. eg: `expr:checked='foo.isBar()'`


### I need the onclick attribute to have quotes in it, but GXP requires that I escape them with &quot;. How do I prevent GXP from doing this? ###

It's actually okay for these quotes to be escaped. Standards conforming browsers are supposed to un-escape the entities in these attributes before passing them to the JavaScript interpreter. So the following example is valid HTML:

```
<a href="#" onclick="alert(&quot;Hello World&quot;)">Click here</a>
```

JavaScript also allows single quotes around string literals, so the following is also valid:

```
<a href="#" onclick="alert('Hello World')">Click here</a>
```

### Why isn't the XXXX attribute of the YYYY tag supported? ###

It probably isn't in the HTML 4.01 standard, or perhaps you forgot to make the attribute lowercase.

If you need support for nonstandard attributes (or elements) you can create an alternate schema.


## HTML Generation ##

### How can I include a DOCTYPE in my resulting HTML pages? ###

You can specify an HTML doctype for your template by using the `gxp:doctype` attribute in your root `<html>` node. It can take the following values:

| **Value** 		| **HTML DOCTYPE** | **XHTML DOCTYPE** | **IE Mode** | **Mozilla Mode** |
|:------------|:-----------------|:------------------|:------------|:-----------------|
| `strict` 		| `<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">` | `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">` | Standards | Standards |
| `transitional` 	| `<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">` | `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">` | Standards | Quirks |
| `frameset` 		| `<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">` | `<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">` | Standards | Quirks |
| `mobile` 		| _not allowed_ | `<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd>` |  |  |
| _attribute not present_ | N/A | N/A | Quirks | Quirks |

Note that this does not cause your generated code to magically validate against the specified DTD.


### Why does it keep escaping the ampersands (`&`) in my URLs? ###

This is actually correct behavior. Strictly speaking, HTML with unescaped ampersands in URLs is invalid (and ambiguous). See also [Common HTML Validation Problems: Ampersands in URLs](http://htmlhelp.com/tools/validator/problems.html#amp).


## Escaping Generated Output ##

### Is there any way to prevent 

&lt;gxp:eval&gt;

 from escaping the value of the expression? ###

For most types of expressions
`<gxp:eval>` (and expr: attributes) will do the equivalent of a `String.valueOf()` on the value, and
then escape the resulting string. A notable exception is objects of
type `GxpClosure` where the `writeHtml` method is called and no escaping is
performed.

If you are suffering from excess escaping you can either create an
implementation of the appropriate `GxpClosure` interface, or instantiate one of
the implementations included as part of the runtime library.


### How do get around having to escape characters when I write Javascript inside of a gxp file? ###

Illegal XML characters must be escaped inside a gxp file. This means `<` `>` `"` `'` and `&` all have to be escaped. Escaping these characters makes javascript hard to read; not escaping these characters renders the gxp file not compilable. One solution to this problem is to use CDATA to enclose the javascript. The XML parser will ignore everything enclosed by the CDATA tag. See the [XML Specification](http://www.w3.org/TR/REC-xml#sec-cdata-sect) for more information.

Here's the syntax for using CDATA:

```
<script type="text/javascript">
<![CDATA[

function foo() {

}

]]>
</script>
```

Note that:

  1. Any gxp tag is ignored inside the CDATA block. This includes gxp:msg tags. So if you have strings that need to be translated, don't put them inside CDATA.
  1. You can't use CDATA if your block contains `<![CDATA[` or `]]>`