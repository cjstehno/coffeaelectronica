title=Are You Mocking Me?
date=2006-09-20
type=post
tags=blog,java,testing,mocking
status=published
~~~~~~
Most of us agree that unit testing is an important part of development and that unit tests should be isolated as
much as possible from external configuration and management issues so that you test only the object under examination
and not the rest of the environment surrounding them; this is where mocking comes in handy.

Let's say I want to develop a JSP tag that pulls a name from somewhere in the scope available to the tag and then renders a
hello message to the given name. It's not a very useful tag, but it will work as an example. First, setup the test and a
method to test for the desired output.

```java
public class HelloTagTest extends TestCase {
    public void testHello() throws Exception {
        HelloTag helloTag = new HelloTag();
        assertEquals("Hello, Mr. Anderson!", helloTag.buildOutput());
    }
}
```

I am using test-driven development techniques here, so we want to start simple and build only what we need to
accomplish our goal. When you compile this code (I am intentionally leaving out imports to save space) it will fail
because we have not yet created a HelloTag class. Let's do that now.

```java
public class HelloTag extends TagSupport {
    String buildOutput(){}
}
```

Notice that I also added the `buildOutput()` method. It is a package-scoped method because it is the method
we will be testing for the tag output. This allows the testing of the tag without having to worry about start and end
tag support right away. Both classes should now compile, but the test will fail. The quickest way to get the test to
work is to fake the return value from `buildOutput()`.

```java
public class HelloTag extends TagSupport {
    String buildOutput(){
        return( "Hello, Mr. Anderson!" );
    }
}
```

If you run the test now, everything is green, though not very useful. We want to be able to pull the name of the
person from the page context. The page context is passed to the tag by the tag container, so how do we use it "out of
the container" This is where mocking comes into play. I use [JMock](http://jmock.org/) for mocking. It is
easy to use once you get the hang of it, and very powerful. We need to create a mock `PageContext` so that we
don't need a whole servlet container to test this one little tag. Let's flesh out the mock context and then add it to
the test case.

```java
Mock mockPageCtx = new Mock(PageContext.class);
```

You will need to import the [cglib](http://cglib.sourceforge.net/) version of the mock api (org.jmock.cglib.*) due to the
fact that `PageContext` is not an interface. With JMock you can mock interfaces or classes as long as they have an empty
constructor. To use the mock object as the "real" thing, you just create a proxy.

```java
PageContext pageCtx = (PageContext)mockPageCtx.proxy();
```

Let's add this to our test case.

```java
public class HelloTagTest extends MockObjectTestCase {
    public void testHello() throws Exception {
        Mock mockPageCtx = new Mock(PageContext.class);
        HelloTag helloTag = new HelloTag();
        helloTag.setPageContext((PageContext)mockPageCtx.proxy());
        assertEquals( "Hello, Mr. Anderson!", helloTag.buildOutput());
    }
}
```

Notice that we are using `MockObjectTestCase` instead of `TestCase`. If you run the test, everything is still fine. We
are not using the `PageContext` yet. We need the page context to be able to provide the name for us when we request it.
Let's say its stored somewhere in scope under the id foo. We need to tell the `mockPageCtx` to return the correct value
when the right conditions are met. This is where the real mocking comes in (and the power of JMock).

```java
public class HelloTagTest extends MockObjectTestCase {
    public void testHello() throws Exception {
        Mock mockPageCtx = new Mock(PageContext.class);
        mockPageCtx.stubs().method("findAttribute").with(eq("foo")).will(returnValue( "Mr. Anderson" ));

        HelloTag helloTag = new HelloTag();
        helloTag.setPageContext((PageContext)mockPageCtx.proxy());

        assertEquals("Hello, Mr. Anderson!",helloTag.buildOutput());
    }
}
```

We added one line of code that probably looks a little confusing... it did to me when I first learned [JMock](http://jmock.org/).
You can break it down to:

> I want to stub out the method findAttribute that takes the parameter foo and will return the value "Mr. Anderson".

You are mocking the response of the `PageContext` instance. The test still runs, but now we need to modify the tag itself
to actually use the page context.

```java
public class HelloTag extends TagSupport {
    String buildOutput(){
        String name = (String)pageContext.findAttribute("foo");
        return("Hello, " + name + "!");
    }
}
```

Now run the test and everything is still green. Your tag thinks it has a real `PageContext`. Now, let's say
that we want to be able to specify the name of the attribute using a tag attribute called id. As you probably know, tag
attributes are simply setters in the tag class.

```java
public class HelloTagTest extends MockObjectTestCase {
    public void testHello() throws Exception {
        Mock mockPageCtx = new Mock(PageContext.class);
        mockPageCtx.stubs().method("findAttribute").with(eq("foo")).will(returnValue("Mr. Anderson"));

        HelloTag helloTag = new HelloTag();
        helloTag.setPageContext((PageContext)mockPageCtx.proxy());
        helloTag.setId("matrix");

        assertEquals("Hello, Mr. Anderson!",helloTag.buildOutput());
    }
}
```

Then we need to add the setter to the tag and use the id in the output.

```java
public class HelloTag extends TagSupport {
    private String id;

    public void setId(String id){ this.id = id; }

    String buildOutput(){
        String name = (String)pageContext.findAttribute(id);
        return("Hello, " + name + "!");
    }
}
```

Aww, but now the test fails. The expected `findAttribute()` parameter was not found. We need to modify our
test to expect the right one.

```java
public class HelloTagTest extends MockObjectTestCase {
    public void testHello() throws Exception {
        Mock mockPageCtx = new Mock(PageContext.class);
        mockPageCtx.stubs().method("findAttribute").with(eq("matrix"))
            .will(returnValue("Mr. Anderson"));

        HelloTag helloTag = new HelloTag();
        helloTag.setPageContext((PageContext)mockPageCtx.proxy());
        helloTag.setId("matrix");

        assertEquals("Hello, Mr. Anderson!",helloTag.buildOutput());
    }
}
```

And now everything is green again. Let's take a break for a second and look at what we have. We have a completely
tested JSP tag (though not fully implemented) that does everything we need it to do. Now we just need to add the rest of
the tag support so that it will work in a servlet container. Let's write out our output in the `doEndTag()`
method. Can we test this to make sure we are outputting the right data? Yes, another job for JMock, with a little help
from a concrete mock helper. We need to mock `JspWriter`, which is abstract and has no empty constructor... no
JMock friendly. So, what I do is make a simple mock adapter for it.

```java
public abstract JspWriterMockAdapter extends JspWriter {
    public JspWriterMockAdapter(){
        super(1024,false);
    }
}
```

Which we can then use with JMock to test for the expected output.

```java
public class HelloTagTest extends MockObjectTestCase {
    public void testHello() throws Exception {
        Mock mockJspWriter = new Mock(JspWriterMockAdapter.class);
        mockJspWriter.expects(once()).method("print")
            .with(eq("Hello, Mr. Anderson!"));

        Mock mockPageCtx = new Mock(PageContext.class);
        mockPageCtx.stubs().method("findAttribute").with(eq("matrix"))
            .will(returnValue("Mr. Anderson"));

        mockPageCtx.stubs().method("getOut").withNoParameters()
            .will(returnValue((JspWriter)mockJspWriter.proxy()));

        HelloTag helloTag = new HelloTag();
        helloTag.setPageContext((PageContext)mockPageCtx.proxy());
        helloTag.setId("matrix");
        helloTag.doStartTag();
        helloTag.doEndTag();

        assertEquals("Hello, Mr. Anderson!",helloTag.buildOutput());

        mockJspWriter.verify();
    }
}
```

This expectation checking shows the other way to use JMock. You can test for an expected method call on the mock
object. Here we are saying that the mock `JspWriter` expects the `print()` method to be called only once
with the given string. I also added a method stub to return the mock `JspWriter` when `getOut()` is called
on the `PageContext`. The last statement in the test method is also important. The verify method is called on the
mock object when you want to test for expected method calls. If they are not found, the test fails. If we run the test
now, it will fail because we are not doing anything in the `doEndTag()` method.

```java
public class HelloTag extends TagSupport {
    private String id;

    public void setId(String id){ this.id = id; }

    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().print(buildOutput());
        } catch(Exception ex){throw new JspException(ex);}
            return(EVAL_PAGE);
        }

        String buildOutput(){
            String name = (String)pageContext.findAttribute(id);
            return("Hello, " + name + "!");
        }
    }
}
```

Run the test again and everything is green. Now you also see why I use a separate method to build the output. This
keeps your tag methods very simple and allows for greater test coverage. This is as far as I am going to go with this
example. It is a fully working JSP tag. If you build a tag descriptor for it and use it, you would see the output we are
testing for. Don't get me wrong, this approach does not necessarily negate the need for in-container testing, but it can
lessen that need in most cases. You can use something like Cactus to do your in-container testing once your mock testing
is done. Though my servlet container can fire up pretty fast, it's still slower than the mock approach when you are in a
rapid test-driven cycle.

> *Update:*  [JMock 2](http://jmock.org/) is a radical change from version one. See [Are You Still Mocking Me?](Are-You-Still-Mocking-Me%3F) for an updated version of this posting.