# Introduction #

A typical web application might have its business
logic implemented in Servlets and then use GXPs for the presentation
layer. Google XML Pages (GXP) gives you a few options for invoking templates from servlets.

Many of the techniques outlined on this guide can also be adapted to other web frameworks, like [WebWork](http://www.opensymphony.com/webwork/) or [Struts](http://struts.apache.org/).

# Overall Structure #

In general, to invoke a GXP template from a servlet you'll want to do have your request handling method (eg: `doGet()`) perform operations in the following order:
  * parse and validate the request parameters, if any.
  * perform the "business logic" for the request.
  * render the results of the business logic (and possibly other values) using a GXP template.

The rest of this guide will focus on the last step: passing values from a Servlet to a GXP template to be rendered.


# The Four Essentials #

There are four things you need to render a GXP template:

  * **The template itself.** Many Servlets will be tied to a specific template. In some cases you may decide that you want s Servlet to choose from a set of templates.
  * **The `Appendable`.** This is where the template's output will be sent. For Servlets this is typically just the result of a call to `response.getWriter()`.
  * **The `GxpContext`.** In most cases you can simply create a new `GxpContext` (they are lightweight objects) for each request, passing in the `Locale` returned by `request.getLocale()`
  * **The template's parameters.** These are the parameters declared in your template with `<gxp:param>`. For example, a template that displays an invoice make take a list of invoice line items as a parameter. Your servlet could fetch these from a database, massage them if necessary, and then pass the list into the template as a parameter.

# The Techniques #

There are a few different techniques for invoking a GXP template. Each has its own pros and cons, though it's common for a given application to pick one technique and use it consistently.

## Using the static write Method ##

Probably the simplest technique for calling a GXP is to just call its static write method. The downside to this approach is that it isn't very amenable to unit testing, but given its simplicity it's a good place to start learning how things work.

Every GXP template has a static method called `write` that takes the Appendable, GxpContext, and declared parameters as method parameters. Here's what calling it from a servlet might look like:

```
  /**
   * A servlet for viewing the "things".
   */
  public class ViewThings extends HttpServlet {
    ...
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {

      // parameter handling
      User user = getUser(request);
      Query query = parseQuery(request);

      // business logic
      List<Thing> things = query.fetchThings(user);

      // "ViewThingsPage.gxp" has the presentation logic
      ViewThingsPage.write(

          // our Appendable:
          response.getWriter(),

          // our GxpContext:
          new GxpContext(request.getLocale()),

          // The parameters declared in the template:
          user,
          things);
    }
  }
```


## Instantiating the Template ##

Another approach is to instantiate the template. This approach allows testing by mocking out the template.

_to be written_

## Using getGxpClosure ##

Another approach is to use `getGxpClosure`. A `GxpClosure` has a write method that that takes the `Appendable` and the `GxpContext`. All of the parameters that are declared in the template are already bound by the time you have a `GxpClosure`.

This approach allows testing by simply skipping the call to the `write` method in the `GxpClosure`.

_to be written_