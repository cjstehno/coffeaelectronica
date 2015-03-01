title=Tour de Mock 3 - Mockito
date=2009-07-22
type=post
tags=blog,java,testing
status=published
~~~~~~
[Mockito](http://mockito.org/) is a fairly new player in the mocking arena, but one that I have found particularly interesting. It's as expressive and flexible as [JMock](http://jmock.org/) (from what I have seen so far) but it's a bit more compact and concise, which is something I really like.

It's also got some nice annotation support that really helps cleans up the code. If you look at the
`EmailListServlet_MockitoTest` class you will see that again (as with JMock) the `@RunWith` annotation is used to provide
a special test runner for Mockito. In this case it is used to evaluate the Mockito annotations. Another thing that you
will notice is that instance variable mocks can be done with annotations in Mockito, using the `@Mock` annotation, very
useful.

```java
@RunWith(MockitoJUnit44Runner.class)
public class EmailListServlet_MockitoTest {
    private EmailListServlet servlet;

    @Mock
    private HttpServletRequest request;
}
```

The `@Before` method is somewhere between the Spring-mock and JMock complexity level:

```java
@Before
public void before() throws ServletException {
    final ServletConfig servletConfig = mock(ServletConfig.class);
    final ServletContext servletContext = mock(ServletContext.class);

    when(servletConfig.getServletContext()).thenReturn(servletContext);
    when(servletContext.getAttribute(EmailListService.KEY)).thenReturn(emailListService);

    this.servlet = new EmailListServlet();
    servlet.init(servletConfig);
}
```

Its as expressive as JMock but with less of the distracting overhead (no instance initialization block expectations) so
it's a bit easier to read, and to maintain. The "without list name" test is almost as simple as it was in the spring-mock
version:

```java
@Test(expected=IOException.class)
public void doGet_without_list() throws Exception {
    when(request.getParameter("listName")).thenReturn(null);
    when(emailListService.getListByName(null)).thenThrow(new IOException());
    servlet.doGet(request, response);
}
```

Notice, too, that it reads fairly clear as to what is going on. "When you get this method call, return this". Again
with the "with list name" test we see that Mockito also has good sequential call support:

```java
@Test
public void doGet_with_list() throws Exception {
    final PrintWriter writer = mock(PrintWriter.class);
    when(request.getParameter("listName")).thenReturn("foolist");

    final List<String> list = Arrays.asList("larry@stooge.com","moe@stooge.com","curley@stooge.com");
    when(emailListService.getListByName("foolist")).thenReturn(list);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    final InOrder order = inOrder(writer);
    order.verify(writer).println("larry@stooge.com");
    order.verify(writer).println("moe@stooge.com");
    order.verify(writer).println("curley@stooge.com");
    order.verify(writer).close();
}
```

Another thing to point out here is that with Mockito, there is nothing special you need to to in order to mock out
concrete classes, it just works. The whole test case using Mockito is shown below:

```java
@RunWith(MockitoJUnit44Runner.class)
public class EmailListServlet_MockitoTest {
    private EmailListServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private EmailListService emailListService;

    @Before
    public void before() throws ServletException {
        final ServletConfig servletConfig = mock(ServletConfig.class);
        final ServletContext servletContext = mock(ServletContext.class);

        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(EmailListService.KEY)).thenReturn(emailListService);

        this.servlet = new EmailListServlet();
        servlet.init(servletConfig);
    }

    @Test(expected=IOException.class)
    public void doGet_without_list() throws Exception {
        when(request.getParameter("listName")).thenReturn(null);
        when(emailListService.getListByName(null)).thenThrow(new IOException());

        servlet.doGet(request, response);
    }

    @Test
    public void doGet_with_list() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);
        when(request.getParameter("listName")).thenReturn("foolist");

        final List<String> list = Arrays.asList("larry@stooge.com","moe@stooge.com","curley@stooge.com");
        when(emailListService.getListByName("foolist")).thenReturn(list);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        final InOrder order = inOrder(writer);
        order.verify(writer).println("larry@stooge.com");
        order.verify(writer).println("moe@stooge.com");
        order.verify(writer).println("curley@stooge.com");
        order.verify(writer).close();
    }
}
```

I plan on spending some more time with Mockito as I could see it becoming my favourite mocking API.

> You can find the source code used in this posting in my [TourDeMock](http://github.com/cjstehno/coffeaelectronica/tree/master/tourdemock) project.
