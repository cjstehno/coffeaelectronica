title=Tour de Mock 2 - JMock
date=2009-07-21
type=post
tags=blog,java,testing
status=published
~~~~~~
The next mocking API to consider is one that I have used for years, [JMock](http://jmock.org/), specifically JMock 2 (for a discussion of JMock 1 usage, please see my older posting "[Are You Mocking Me?](Are-You-Mocking-Me%3F)").

JMock uses a more Domain Specific Language approach to mocking that is very flexible and very expressive, though it can be a bit daunting to someone who's never used it before. One of the first things you will notice about the test in the `EmailListServlet_JMockTest` is that it uses the `@RunWith` annotation provided by [JUnit](http://junit.org/). This annotation tells JUnit to use a test runner other than the default. In this case, JMock provides a test runner to simplify use of its Mockery construct (you will see it as one of the instance variables). The Mockery is used to create and manage the mocking system. Using the JMock runner allows JUnit to handle the mockery verification step after each test so that you don't have to do it yourself. You may also notice that I have defined a constructor for this test to set the "imposteriser" (mock creator) used. Since we have both interfaces and classes to mock, the "legacy" imposteriser must be used.

```java
@RunWith(JMock.class)
public class EmailListServlet_JMockTest {
    private Mockery mockery = new JUnit4Mockery();

    public EmailListServlet_JMockTest(){
        mockery.setImposteriser(ClassImposteriser.INSTANCE);
    }
}
```

The `@Before` method in this example is a bit more complex than that of the Spring mocking example, since you have to do a bit more of the binding work yourself:

```java
@Before
public void before() throws ServletException {
    this.emailListService = mockery.mock(EmailListService.class);
    final ServletConfig servletConfig = mockery.mock(ServletConfig.class);
    mockery.checking(new Expectations(){
        {
            final ServletContext servletContext = mockery.mock(ServletContext.class);
            one(servletConfig).getServletContext(); will(returnValue(servletContext));
            one(servletContext).getAttribute(EmailListService.KEY); will(returnValue(emailListService));
        }
    });

    this.servlet = new EmailListServlet();
    servlet.init(servletConfig);

    this.request = mockery.mock(HttpServletRequest.class);
    this.response = mockery.mock(HttpServletResponse.class);
}
```

The `mockery.checking()` method is one of the most used constructs in mocking with JMock. It allows you to provide your test expectations. In this case you can see that we are expecting one call to `servletConfig.getServletContext()`, which will return the mock `ServletContext` we have created. We are also expecting one call to the `getAttribute()` method of the mocked servlet context with the email service key, which will return our mocked `EmailListService` (note, in this test we don't actually implement the service interface, we just mock it like everything else). The "no list name" exception-checking test is also a bit more complicated than the previous example:

```java
@Test(expected=IOException.class)
public void doGet_without_list() throws Exception {
    mockery.checking(new Expectations(){
        {
            one(request).getParameter("listName"); will(returnValue(null));
            one(emailListService).getListByName(null); will(throwException(new IOException()));
        }
    });

    servlet.doGet(request, response);
}
```

We actually need to code the behavior for a null return of the "listName" parameter and cause the exception to be thrown by the service. The other test, the "with list name" test is the more interesting of the two now:

```java
@Test
public void doGet_with_list() throws Exception {
    final PrintWriter writer = mockery.mock(PrintWriter.class);
    final Sequence printSequence = mockery.sequence("printSequence");
    mockery.checking(new Expectations(){
        {
            one(request).getParameter("listName"); will(returnValue("foolist"));
            final List<String> list = Arrays.asList("larry@stooge.com","moe@stooge.com","curley@stooge.com");
            one(emailListService).getListByName("foolist"); will(returnValue( list ));
            one(response).getWriter(); will(returnValue(writer));
            one(writer).println("larry@stooge.com"); inSequence(printSequence);
            one(writer).println("moe@stooge.com"); inSequence(printSequence);
            one(writer).println("curley@stooge.com"); inSequence(printSequence);
            one(writer).close(); inSequence(printSequence);
        }
    });

    servlet.doGet(request, response);
}
```

You will see again, that the basic call behavior is specified as is the service return value, but here, also we actually see some of the benefit of all this extra code. Notice the sequence behavior. You can define a sequence of calls which must be performed in the order specified or the test will fail. This is useful in cases like this where, if nothing else, you want to ensure that the `close()` method is called after all of the `println()` calls on the writer. The whole test case is as follows:

```java
@RunWith(JMock.class)
public class EmailListServlet_JMockTest {
    private Mockery mockery = new JUnit4Mockery();
    private EmailListServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private EmailListService emailListService;

    public EmailListServlet_JMockTest(){
        mockery.setImposteriser(ClassImposteriser.INSTANCE);
    }

    @Before
    public void before() throws ServletException {
        this.emailListService = mockery.mock(EmailListService.class);
        final ServletConfig servletConfig = mockery.mock(ServletConfig.class);
        mockery.checking(new Expectations(){
            {
                final ServletContext servletContext = mockery.mock(ServletContext.class);
                one(servletConfig).getServletContext(); will(returnValue(servletContext));
                one(servletContext).getAttribute(EmailListService.KEY); will(returnValue(emailListService));
            }
        });

        this.servlet = new EmailListServlet();
        servlet.init(servletConfig);

        this.request = mockery.mock(HttpServletRequest.class);
        this.response = mockery.mock(HttpServletResponse.class);
    }

    @Test(expected=IOException.class)
    public void doGet_without_list() throws Exception {
        mockery.checking(new Expectations(){
            {
                one(request).getParameter("listName"); will(returnValue(null));
                one(emailListService).getListByName(null); will(throwException(new IOException()));
            }
        });

        servlet.doGet(request, response);
    }

    @Test
    public void doGet_with_list() throws Exception {
        final PrintWriter writer = mockery.mock(PrintWriter.class);
        final Sequence printSequence = mockery.sequence("printSequence");
        mockery.checking(new Expectations(){
            {
                one(request).getParameter("listName"); will(returnValue("foolist"));
                final List<String> list = Arrays.asList("larry@stooge.com","moe@stooge.com","curley@stooge.com");
                one(emailListService).getListByName("foolist"); will(returnValue( list ));
                one(response).getWriter(); will(returnValue(writer));
                one(writer).println("larry@stooge.com"); inSequence(printSequence);
                one(writer).println("moe@stooge.com"); inSequence(printSequence);
                one(writer).println("curley@stooge.com"); inSequence(printSequence);
                one(writer).close(); inSequence(printSequence);
            }
        });

        servlet.doGet(request, response);
    }
}
```

JMock has a good amount of development time under its belt so it is pretty well documented and tested itself. It's got a great API for writing your own parameter matchers, method matchers and expectations. It can be a bit cumbersome at times when the test cases get large and full of a lot of mock plumbing. I tend to try and treat test code with the same refactoring attention that I do normal code... pulling out shared expectations, and creating useful shared assertions, etc, then it's not so bad. There is definitely more code involved with this approach than with something like spring mock; however, you do have quite a wide range of mock-ability that you don't really get with predefined mock implementations.

> You can find the source code used in this posting in my [TourDeMock](http://github.com/cjstehno/coffeaelectronica/tree/master/tourdemock) project.
