**Note: This tutorial is currently very much a work in progress.**

# Getting Started #

To get started you'll need to go to the
[Downloads tab](http://code.google.com/p/gxp/downloads/list) and get the
latest GXP Release JAR. You don't need to unjar/unzip this JAR.

Next, create a directory for going through this tutorial, and move (or
copy) that JAR into that directory. We're going to do all of our work in
this tutorial relative to this new directory.

# Hello, World! #

International law requires that all programming tutorials start out
with a variation of “hello, world!” as their first example.

The GXP compiler requires that the directory any .gxp file resides in
matches the name declared in that file, so let's first create a
subdirectory called `com/google/tutorial`. Throughout this tutorial we
use the Unix style forward-slash. If you're using Windows just use
backslash in file names instead. For example, `com\google\tutorial`.

Next, let's create a file in that new directory called `HelloWorld.gxp`
that looks like this:

```
<!-- HelloWorld.gxp -->
<gxp:template name='com.google.tutorial.HelloWorld' xmlns:gxp='http://google.com/2001/gxp'>
Hello, World!
</gxp:template>
```

You may have noticed that this file is XML. GXP is an XML language so
you can use any text editor or IDE that will let you edit text files or
XML files to edit GXP's.

Let's look at the pieces that make up this template:

  1. A comment. We'll be including comments like this one in our examples to make it clear which file we're talking about. Comments are completely ignored, so you can leave these out if you like.
  1. The `<gxp:template>` start-tag. `<gxp:template>` is the most common top-level element for a GXP template. The start tag for this element has a `name`, which is a dotted fully-qualified name for your template. This is also where you typically put your `xmlns` declarations. Here we've defined the `gxp:` prefix to correspond to the `http://google.com/2001/gxp` namespace. This is the namespace which contains most of GXP's core language features.
  1. The test "`Hello, World!`". This is what our template will output.
  1. The `<gxp:template>` end-tag. Because GXP is XML-based, it is fairly strict: all tags must be closed explicitly, and all elements must be nested properly. This takes a bit of getting used to if you're more used to editing HTML, but the strictness removes ambiguity which makes the code more maintainable in the long run.

To run this template we first need to build it. We'll use the
command-line tool, though you could use any or configure your IDE to
build GXP's. There are [wikis](http://code.google.com/p/gxp/w/list) for
some of these alternate methods of building GXP's.

To build, from the command prompt type:

```
java -cp gxp-0.2.4-beta.jar com.google.gxp.compiler.cli.Gxpc --output_language java com/google/tutorial/*.gxp
```

This will compile all of the `.gxp` files in `com/google/tutorial` (currently just `HelloWorld.gxp`) into `.java` files.

Let's take a look at `com/google/tutorial/HelloWorld.java`. You should
see a few things in this file:

  1. Some scary comments warning you not to edit this file.  If you edit the `.java` files directly your edits will be overwritten by the compiler the next time it is run, so you should always make your edits in the `.gxp` source files.
  1. A class, `HelloWorld` in the `com.google.tutorial` package.
  1. Several public methods in this class including `static void write(Appendable, GxpContext)`.

It looks like a lot of code for just `HelloWorld`, but it gives us a lot
of flexibility in how we use GXP templates. We'll get into that
flexibility later in the  tutorial.

To “run”  our GXP  we'll need a little test harness. Create the file
`com/google/tutorial/HelloWorldMain.java`:

```
package com.google.tutorial;

import java.util.Locale;
import com.google.gxp.base.GxpContext;

public class HelloWorldMain {
  public static void main(String[] args) throws Exception {
    GxpContext gc = new GxpContext(Locale.ENGLISH);
    HelloWorld.write(System.out, gc);
    System.out.println();
  }
}
```

The important bit here is the line where we call `HelloWorld.write`. Every
GXP, when compiled into Java code, has a static `write` method. This
method has at least two parameters: an
[Appendable](http://java.sun.com/javase/6/docs/api/java/lang/Appendable.html) which is where the
output is sent, and a
[GxpContext](http://gxp.googlecode.com/svn/trunk/javadoc/com/google/gxp/base/GxpContext.html) object.

You might've noticed that unlike some other Java templating systems GXP's
have no dependency upon servlets. This makes GXP's easier to test
and also easier to use in circumstances where servlets make no sense
(like the generation of static files or emails).

Now let's compile the  Java code. If you're doing this from the command
line you can use the following command:

```
javac -cp gxp-0.2.4-beta.jar com/google/tutorial/*.java
```

Now that it is compiled we can run it:

```
java -cp gxp-0.2.4-beta.jar:. com.google.tutorial.HelloWorldMain
```

If you'd rather compile and run from your IDE make sure to include the
GXP Release JAR in your class path. This JAR contains not only the
compiler but also the runtime libraries used by the code that it
generates.

The output should look like:
```
Hello, World!
```

# Adding Some HTML #

GXP is designed to work well with markup languages, in particular HTML
and XHTML.  Let's create a new template that emits some HTML tags:

```
<!-- com/google/tutorial/HtmlHelloWorld.gxp -->
<gxp:template
  name='com.google.tutorial.HtmlHelloWorld'
  xmlns:gxp='http://google.com/2001/gxp'
  xmlns='http://www.w3.org/1999/xhtml'>
<b>Hello, <br/> World!</b>
</gxp:template>
```


In our template we've added to HTML elements: a bold element surrounding
the entire body of the template and a line break just before the word
“world”. Remember that GXP is XML so the line break tag needs to be a
self-closing tag. You can pretty much use the same rules that you would
use if you were writing XHTML rather than HTML. Also, exp expects there
to be a namespace for every element. We can use `xmlns` to set a default
namespace for elements. By convention, we typically set the default
namespace to the main namespace that we expect to be outputting. Since
we're mostly going to be generating HTML and XHTML we set our default
namespace to XHTML's standard namespace.


Let's see what happens when we run this template. Here's a test harness:

```
// com/google/tutorial/HtmlHelloWorldMain.java

package com.google.tutorial;

import java.util.Locale;
import com.google.gxp.base.GxpContext;

public class HtmlHelloWorldMain {
  public static void main(String[] args) throws Exception {
    GxpContext gc = new GxpContext(Locale.ENGLISH);
    HtmlHelloWorld.write(System.out, gc);
    System.out.println();
  }
}
```

When we run this the output looks like:

```
<b>Hello, <br> World!</b>
```

Notice that the output is HTML. Even though that line break was a
self-closing tag in our GXP source it's turned into a plain old
HTML line break in the output. This is the default behavior, but if we
would like XHTML instead that easily changed. Just change the line where
we create a new `GxpContext` to look like this:

```
GxpContext gc = new GxpContext(Locale.ENGLISH, true);
```

The second (optional) parameter to `GxpContext`'s constructor is a flag
that says whether it should emit “XML syntax”. This flag is really only
used for switching between HTML and XHTML. Most other markups that GXP
supports are XML-only and so specifying this flag would be redundant in
those cases.

When we run the modified test harness the output looks like this:

```
<b>Hello, <br /> World!</b>
```

(If you're really familiar with XML you may have noticed that there's an
extra space inside the self-closing  line break tag. This space is
unnecessary for XML parsers but recommended in XHTML to increase
compatibility with HTML parsers.)

# Fail Fast Philosophy #

GXP follows a “fail fast” philosophy. In other words, if something is
going to go wrong we'd like it to go wrong as soon as possible so we can
more easily identify the source of the problem.  Just as a Java compiler
(or IDE) identifies problems with your code as you build,  the GXP
compiler will identify common problems in your templates when you build
them. Here's an example template the GXP will have a few issues with:

```
<!-- com/google/tutorial/BadExample.gxp -->
<gxp:template
  name='com.google.tutorial.BadExample'
  xmlns:gxp='http://google.com/2001/gxp'
  xmlns='http://www.w3.org/1999/xhtml'>
  <tabel>
    <tr>
      <td>I'm in a table!</td>
    </tr>
  </tabel>
  <img scr="foo.gif"/>
  <br>
    This is a test.
  </br>
</gxp:template>
```

When we compile this week a bunch of errors from the compiler:

```
com/google/tutorial/BadExample.gxp:6:3:6:3: Unknown element <tabel> (in http://www.w3.org/1999/xhtml namespace)
com/google/tutorial/BadExample.gxp:11:3:11:3: 'scr' attribute is unknown in <img>.
com/google/tutorial/BadExample.gxp:12:7:12:7: text not allowed inside <br>
com/google/tutorial/BadExample.gxp:11:3:11:3: <img> must have an 'alt' attribute
```

In addition to markup checking GXP makes use of static type checking in a
number of ways. GXP templates have formal parameters, just like methods in
Java. These parameters, as well as the templates themselves, are all “typed”.
In addition,  expressions that appear within a template are checked (or not) by
the native-language compiler. This means that if you try to call a nonexistent
method on a data access object you'll know about it at compile time. This also
makes refactoring easier as you can be sure that any values used by your
templates are being passed into them, once they compile.

It's important to not get frustrated by these errors. They're there to
help you. Remember, it's always better to have it break in development
so you can fix it rather than to have it break out in production.

# Formal Parameters and Dynamic Values #

Up to this point all of our templates have been pretty static. The real
reason to use a templating system is to generate markup dynamically. To
do that we need two things: we need to be able to pass data into our
templates, and we need to be able to use that data.


For passing data in many templating systems use what is known as a “data
dictionary” approach: a dictionary (often hierarchical) of data is
passed around to all of the templates. The code that populates the  data
dictionary needs to be kept in sync with the templates that access the
dictionary but there typically isn't any compile time checking that
these  have actually been kept in sync. To make matters worse, it's
often very difficult to visually inspect templates to determine what
values they actually depend on as they not only depend of values that
they mention directly, but they can also have indirect dependencies due
to the templates they call.  This approach suffers from many of the same
problems as using global variables for passing around data. Indeed, a
data dictionary is essentially just a big bag of global variables.

GXP uses a different approach. GXP uses formal parameters just like
methods and functions in modern programming languages that you're used
to. Formal parameters are declared using the `gxp:param` element. Let's
create a variation of hello world that takes a user name as a
parameter:

```
<!-- com/google/tutorial/HtmlHelloUser.gxp -->
<gxp:template
  name='com.google.tutorial.HtmlHelloUser'
  xmlns:gxp='http://google.com/2001/gxp'
  xmlns='http://www.w3.org/1999/xhtml'>
  <gxp:param name='userName' type='String'/>

  <b>Hello, <br/> <gxp:eval expr='userName'/>!</b>
</gxp:template>
```

Here's our test harness:

```
// com/google/tutorial/HtmlHelloUserMain.java

package com.google.tutorial;

import java.util.Locale;
import com.google.gxp.base.GxpContext;

public class HtmlHelloUserMain {
  public static void main(String[] args) throws Exception {
    GxpContext gc = new GxpContext(Locale.ENGLISH, true);
    HtmlHelloUser.write(System.out, gc, args[0]);
    System.out.println();
  }
}
```

A few things to notice here:

  1. The parameter has a name and a type. The name is used to refer to the parameter within the template and the type ( in this case String) is just a Java type name.
  1. The `write` method now takes on additional parameter. Previously it only took an `Appendable` and a `GxpContext`. Now it also takes our `userName` parameter. Note that the order in which we declare formal parameters in a template is important: that's the order we use when passing parameters into the template from Java.
  1. To display the value of a parameter we use the `gxp:eval`  element.  The `expr` attribute of `gxp:eval` is an expression. It can be almost any Java expression, though certain operators are prohibited. In this case we're just using the parameter name.

We run this just like the other test harnesses, except we need to pass in a name parameter:

```
java -cp gxp-0.2.4-beta.jar:. com.google.tutorial.HtmlHelloUserMain "Laurence"
```

This will output:

```
<b>Hello, <br /> Laurence!</b>
```

# Escaping #

An interesting difference between GXP and many other templating systems
is that GXP will automatically escape dynamic values.
We can see this with the
`HtmlHelloUserMain` test harness. Try passing it something that needs to
be escaped in HTML:

```
java -cp gxp-0.2.4-beta.jar:. com.google.tutorial.HtmlHelloUserMain "Bobby<script>doSomethingEvil();</script>"
```

the output should look something like this:

```
<b>Hello, <br /> Bobby&lt;script&gt;doSomethingEvil();&lt;/script&gt;!</b>
```

Notice that the angle brackets have all been escaped so that if you were
to view this output in a browser you'd see  “Bobby” followed by a
less-than sign followed by the word “script”, and so on.

This automatic escaping makes  applications that use GXP much more
resilient to  cross site scripting, or “XSS”. With templating systems
that don't have automatic escaping  developers need to remember to
escape virtually every single instance of dynamic content. Forgetting
even once can make your application vulnerable to XSS exploits.
With GXP, the
default behavior is the safe behavior. It is still possible to display
text without escaping but you need to go out of your way to do it and so
it is **far** less likely to happen by accident.

Furthermore, GXP doesn't just do HTML escaping. GXP is “type aware” and
will escape dynamic content to match the surrounding context.  We can
see this by making a template that uses JavaScript:

```
<!-- com/google/tutorial/JsHelloUser.gxp -->
<gxp:template
  name='com.google.tutorial.JsHelloUser'
  xmlns:gxp='http://google.com/2001/gxp'
  xmlns='http://www.w3.org/1999/xhtml'>
  <gxp:param name='userName' type='String'/>

  <script gxtype='text/javascript'>
    var userName = <gxp:eval expr='userName'/>;
    alert('Hello, ' + userName);
  </script>
</gxp:template>
```

When we run this template passing in “Austin "Danger" Powers” as the
parameter we get the following output:

```
<script type="text/javascript">
    var userName = "Austin \x22Danger\x22 Powers";
    alert('Hello, ' + userName);
  </script>
```

Notice that the quotation marks in the parameter have been escaped in
the resulting JavaScript. Also note that the entire value has
been enclosed in quotation marks (look carefully at the template and
you'll see that we did _not_ have quotes surrounding the `gxp:eval`
element). In a JavaScript context dynamic
values are converted into a JavaScript “equivalent” value. So a string
will be converted into a JavaScript string literal containing the same
value.


# Dynamic Attribute Values #

_to be written_

_This section will explain how to dynamically set an attribute value on a markup element._


# Data Types #

_to be written_

_This section will describe the content type to native data type
mapping that GXP uses._

# Abbreviations #

_to be written_

_This section will explain `gxp:abbr`._

# Conditionals #

_to be written_

_This section will explain `gxp:if` (and friends)._

# Loops #

_to be written_

_This section will explain `gxp:loop`._

# Calling Templates from Templates #

_to be written_

# More about Parameters #

_to be written_

_This section will explain `content="*"` parameters._

# Attribute bundles #

_to be written_


# GxpClosure #

_to be written_


# Instantiable Templates #

_to be written_


# Internationalization (i18n) #

_to be written_

# Space Collapsing #

_to be written_
