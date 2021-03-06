title=IOTemplate API 
date=2009-08-11
type=post
tags=blog,java
status=published
~~~~~~
I had an idea recently that I have coded up into my [CodePerks](http://github.com/cjstehno/codeperks) project. I was writing
a Spring MVC Controller that had to write the response output directly rather than to a view, but the content written
differed based on some other criteria.

That got me thinking about how often we write and rewrite the standard IO handling construct.

```java
OutputStream out = null;
try {
    out = // create your stream
    // use the stream
} catch(Exception e){
    // handle it
} finally {
    if(out != null){
        try { out.close(); } catch(Exception ex){}
    }
}
```

It's not rocket science code, but I have seen it done wrong more often than you would expect. If nothing else, it gets
tedious to write when you have to do it or something like it more than once in a project. Inspired by the
[SpringFramework](http://springsource.org/)'s use of the template pattern and factory methods, I thought that there should
be something along those same lines for handling IO streams such that you still maintain the current flexibility while
also adding an additional layer of abstraction to remove the repetitive, potentially error-prone parts. Most of the code
in the [CodePerks IO API](http://github.com/cjstehno/codeperks/tree/master/src/main/java/com/stehno/codeperks/io) is what
I came up with to answer that need. Basically what it boils down to is that you have a factory and a callback interface.
The Factory is used to create the specific stream or reader/writer you need, while the callback is provided so that operations
can be performed on the stream without the need to manage resource cleanup yourself.

```java
final OutputStreamFactory<ServletOutputStream> osf = new OutuputStreamFactory<ServletOutputStream>(){
    public ServletOutputStream outputStream() throws IOException {
        return response.getOutputStream();
    }
};

final OutputStreamCallback<ServletOutputStream> osc = new OutputStreamCallback<ServletOutputStream>(){
    public void output(ServletOutputStream out) throws IOException {
        out.println("shun the");
        out.println("nonbeliever");
        out.println("charlie");
    }
};

IoTemplate.output(osf,osc);
```

You can even simplify this down for cases such as that shown above, when you already have a stream you are working with.
The `IoTemplate` class provides wrapper methods as a convenience for the standard IO types.

```java
final OutputStreamCallback<ServletOutputStream> osc = new OutputStreamCallback<ServletOutputStream>(){
    public void output(ServletOutputStream out) throws IOException {
        out.println("shun the");
        out.println("nonbeliever");
        out.println("charlie");
    }
};

IoTemplate.output(response.getOutputStream(), osc);
```

Leaving you to focus on writing your functionality, not all that boilerplate code. You could create a reusable factory for
your application, or use one of the file-based or adapter implementations provided. Then each time you need to perform an
IO operation you could use that factory and create a new callback, or even come up with a reusable set. I will admit that
this does not really cut down on the code all that much and I think I see the reason why Spring has not implemented anything
like this. It's hard to do it in an elegant manner without injecting a lot of assumptions and/or restrictions. I went for
the shotgun approach of trying to cover everything.

Also, there are APIs such as the [Jakarta Commons IO API](http://commons.apache.org/io) which do a much more compact job of
reading/writing data all at once; however, that is not really what this API is for. CodePerks IO is meant for those
situations where you need a bit more fine-grained control over how things are handled but still want some re-usability and
abstraction from boilerplate code. Perhaps someone else will find it useful too. I will be maintaining that project and
will eventually produce a release. Drop me an email if you are using it or would like to use it... sometimes that can get
me to move things along faster.
