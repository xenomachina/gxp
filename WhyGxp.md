# Why GXP? #

Why go off and create a whole new templating language? Aren't there already more than enough of them?  And especially in the Java world, isn't JSP a standard tool that is used and understood everywhere?  These are all good questions, and in many cases JSP may be perfectly sufficient for your needs, in which case you should definitely stick with what you know.  We feel, however, that GXP offers many advantages over other tools currently available.


## GXP is Secure by Default ##

When you use insert dynamic content in a GXP (eg: via 

&lt;gxp:eval&gt;

) it
is automatically escaped and/or quoted in a manner appropriate for the
current context. If you are in a 

&lt;script&gt;

 block, GXP knows that you
are dealing with JavaScript. If you are in a 

&lt;style&gt;

 block, CSS
escaping is used. If you are inside of an "onclick" attribute both
JavaScript and HTML escaping are brought into play, and it's all done
without you having to think about it.

Most other templating systems have no escaping by default, so when
escaping is necessary (almost always) the onus is on the developer of
the page to both remember to escape content, and to escape it in the
proper way.

Doing proper escaping and quoting by hand can be difficult and error-prone meaning that **with other tools it only takes a single oversight to make your site
vulnerable to cross-site scripting (XSS) attacks**. Developers will
inevitably make mistakes and this will lead to security problems.
Using GXP this problem becomes a thing of the past.

## GXP Performs Markup Validation ##

GXP verifies many things at compile time:

  * checks for well-formedness (eg: tags are closed properly and do not overlap inappropriately)
  * only allows for known tags and attributes
  * verifies that tags have required attributes
  * verifies that statically defined attribute values are appropriate

Most other templating systems do none of this. When composing complex
pages it is terribly easy to leave off a close tag, forget to include
a required attribute, or even have a typo so you get 

&lt;imput&gt;

 when you
meant 

&lt;input&gt;

. Compile time checks for this sort of correctness
guarantee that we generate much higher-quality and standards compliant
markup.

## GXP Removes Unnecessary Whitespace ##

GXP will completely remove virtually all unnecessary whitespace, and
gives the GXP developer many options for whitespace handling when
necessary.

Most templating systems blindly preserves whatever whitespace is in
the source file. This forces developers to make a choice between less
legible source code and bloated HTML.

## GXP has a Sensible Modularization System ##

In GXP, factoring our part of a template is easy. You simply create a
new template, and move the factored out content into it. The new
template has a signature (declared parameters, etc.) which aids in
compile-time checks as well as acting as documentation. The new
template's signature and implementation also go in the same place, and
everything is still in GXP, so there's no need to make a mental shift
either for the person doing the refactoring or for the person who has
to maintain the code months later.

JSP has a particularly clumsy modularization system. JSP custom tag
libraries require that you define both a tag handler in Java code and
a tag library descriptor in XML. In other words, to factor out a chunk
of a JSP you must create two separate things in two languages other
than JSP.

In many ways, **GXP's modularization system works like a modern
programming language.** Imagine what Java programming would be
like if it was like JSP and every time you wanted to factor out some
code into a separate method you had to update an XML descriptor and
also write a "handler" in C.

## GXP Simplifies Internationalization ##

JSP's answer to internationalization used to be to send your JSP files
to be translated. This leads to problems where updating the translated
templates becomes a maintenance nightmare, but for a long time this
was one of the recommended practices for translating JSPs.

Eventually JSP's support for internationalization improved
somewhat: custom tag libraries were developed that could look up
messages by ID. To use these libraries one must move all of the
untranslated content out of the JSPs and painstakingly convert them
into MessageFormat-style strings. This is a tedious and error-prone
process, and the end result is JSPs that are much harder to maintain
and debug because every trace of natural language has been extracted
from them.

GXP uses a completely different approach. **In GXP the natural language
(eg: English) content remains in your templates.** One simply marks
the content as a message, gives names to the placeholders, and
examples for the dynamic values. This is a far easier operation to
perform than the message extraction JSP requires as there is no change
to MessageFormat style required, so switching to another file, and no
struggling to come up with meaningful message IDs.  Because the
untranslated messages remain in the templates it also results in
templates that are much easier to read and debug than their JSP
counterparts.

GXP will also (unless otherwise instructed) warn against
un-internationalized text at compile time (JSP has no such
capability), making it easy to keep your templates internationalized.

## GXP Templates Are Real Java Objects ##

_to be written_

## Multiple Language Support ##

_to be written_