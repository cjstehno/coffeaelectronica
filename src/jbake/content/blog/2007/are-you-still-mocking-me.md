title=Are Still You Mocking Me?
date=2007-09-20
type=post
tags=blog,java,testing
status=published
~~~~~~
[JMock 2](http://jmock.org/) came out not too long ago and after some initial worry about backwards compatibility I decided to
give it a try. It plays very nicely with version one, but you will want to run out and convert all of your tests once you
see how truly beautiful version two is. A few years ago, I wrote a brief article on unit testing with JMock called
"[Are You Mocking Me?](Are-You-Mocking-Me%3F)". I would like to showcase some of the new goodies in version two by
revisiting that article and converting the examples to the newer version.

You should at least skim the other article to get a feel for the example at hand as I will be covering mocking
differences, not the original issue of mock testing. In the example, our test was written as an extension of
`MockObjectTestCase`, this is still the case (pardon the pun), except that the package is different, we are
now using the `org.jmock.integration.junit3.MockObjectTestCase` class from version two. It serves basically
the same purpose, to provide some helper methods. Our our original test case was:

```java
public class HelloTagTest extends MockObjectTestCase {
    public void testHello() throws Exception {
        Mock mockPageCtx = new Mock(PageContext.class);
        HelloTag helloTag = new HelloTag();
        helloTag.setPageContext((PageContext)mockPageCtx.proxy());
        assertEquals("Hello, Mr. Anderson!",helloTag.buildOutput());
    }
}
```

which, when updated will become:

```java
public class HelloTagTest extends MockObjectTestCase {
    public void testHello() throws Exception {
        final PageContext pageCtx = mock(PageContext.class);
        final HelloTag helloTag = new HelloTag();
        helloTag.setPageContext(pageCtx);
        assertEquals("Hello, Mr. Anderson!"),helloTag.buildOutput());
    }
}
```

The difference is subtle but very interesting. Now instead of a `Mock` object, we have an instance of the
object that was mocked and we no longer need to call the `proxy()` method and do the casting. The second example
adds a stubbed method, which after using JMock for a while I realized was not such a great thing, since it does not
really verify that it was called, just that it can be called.

```java
public class HelloTagTest extends MockObjectTestCase {
    public void testHello() throws Exception {
        Mock mockPageCtx = new Mock(PageContext.class);
        mockPageCtx.stubs().method("findAttribute").with(eq("foo")).will(returnValue("Mr. Anderson"));
        HelloTag helloTag = new HelloTag();
        helloTag.setPageContext((PageContext)mockPageCtx.proxy());
        assertEquals("Hello, Mr. Anderson!",helloTag.buildOutput());
    }
}
```

The new version both takes care of that omission and updates the code to use the new expectation definition method:

```java
public class HelloTagTest extends MockObjectTestCase {
    public void testHello() throws Exception {
        final PageContext pageCtx = mock(PageContext.class);
        checking(new Expectations(){
            {
                one(pageCtx).findAttribute("foo"); will(returnValue("Mr. Anderson"));
            }
        });

        final HelloTag helloTag = new HelloTag();
        helloTag.setPageContext(pageCtx);
        assertEquals("Hello, Mr. Anderson!"),helloTag.buildOutput());
    }
}
```

Now, the first time I saw the code in the checking method, I had to double check that I was looking at a Java
example. Basically you are creating an anonymous extension of the `Expectations` class and then adding an
instance initializer to it (the inner curly braces) to provide the expectations themselves. Notice that the method is
actually being called on the mocked object; it's not a string, which means that refactoring will not break your mock
tests. If you have never run into that problem with version one, count yourself lucky. Okay, I am going to jump down to
the final code to finish up:

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

With version two you can mock objects that do not have an empty constructor, though you have to set a non-default
"Imposterizer". This negates the need for the adapter class defined in the old article.

```java
public class HelloTagTest extends MockObjectTestCase {
    public HelloTagTest(){
        setImposterizer(ClassImposterizer.INSTANCE);
    }

    public void testHello() throws Exception {
        final JspWriter jspWriter = mock(JspWriter.class);
        checking(new Expectations(){
            {
                one(jspWriter).print("Hello, Mr. Anderson!");
            }
        });

        final PageContext pageCtx = mock(PageContext.class);
        checking(new Expectations(){
            {
                one(pageCtx).findAttribute("martix"); will(returnValue("Mr. Anderson"));
                one(pageCtx).getOut(); will(returnValue(jspWriter));
            }
        });

        final HelloTag helloTag = new HelloTag();
        helloTag.setPageContext(pageCtx);
        helloTag.setId("matrix");
        helloTag.doStartTag();
        helloTag.doEndTag();

        assertEquals("Hello, Mr. Anderson!"),helloTag.buildOutput());
    }
}
```

You can't tell me that is not cleaner and more straight-forward. You could actually collapse the
`checking()` methods into one if you wanted to. I tend to group them like the example above since it can often
reveal common blocks of code that can be extracted into separate shared methods. The is a lot more to discuss with JMock
but this gives you a good overview based on old version one code.
