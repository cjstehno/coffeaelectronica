title=Tour de Mock 4 - Easymock
date=2009-07-23
type=post
tags=blog,java,testing
status=published
~~~~~~
[EasyMock](http://easymock.org/) is a bit of a different breed. The latest version (2.5.x) seems more expressive than the previous version but it has an odd, and in my opinion cumbersome, need to call a replay method, while most of the other mocking APIs seemingly either negate the need for this or do it under the covers so you don't have to worry about it.

The `@Before` method is nothing very different from the other examples:

```java
@Before
public void before() throws ServletException {
    this.emailListService = createMock(EmailListService.class);

    final ServletConfig servletConfig = createMock(ServletConfig.class);
    final ServletContext servletContext = createMock(ServletContext.class);

    expect(servletConfig.getServletContext()).andReturn(servletContext);
    expect(servletContext.getAttribute(EmailListService.KEY)).andReturn(emailListService);

    replay(servletConfig,servletContext);

    this.servlet = new EmailListServlet();
    servlet.init(servletConfig);
    this.request = createMock(HttpServletRequest.class);
    this.response = createMock(HttpServletResponse.class);
}
```

However, the code is reasonably expressive and gets the functional point across in a reasonable amount of code. The exception
test is expressive; however, again as in the `before()` method we see that clunky `replay()` method that just feels like
an out-of-place artifact of a poor design decision.

```java
@Test(expected=IOException.class)
public void doGet_without_list() throws Exception {
    expect(request.getParameter("listName")).andReturn(null);
    expect(emailListService.getListByName(null)).andThrow(new IOException());

    replay(request,emailListService);

    servlet.doGet(request, response);
}
```

The sequential handling, in the "with list" test is fairly straight-forward, probably the least invasive of all the APIs so
far in handling method invocation sequences.

```java
@Test
public void doGet_with_list() throws Exception {
    expect(request.getParameter("listName")).andReturn("foolist");

    final List<String> list = Arrays.asList("larry@stooge.com","moe@stooge.com","curley@stooge.com");
    expect(emailListService.getListByName("foolist")).andReturn(list);

    final PrintWriter writer = createMock(PrintWriter.class);
    checkOrder(writer,true);
    expect(response.getWriter()).andReturn(writer);

    writer.println("larry@stooge.com");
    writer.println("moe@stooge.com");
    writer.println("curley@stooge.com");
    writer.close();

    replay(request,response,writer,emailListService);

    servlet.doGet(request, response);
    verify(writer);
}
```

The code for the entire test case is as follows:

```java
public class EmailListServlet_EasyMockTest {

    private EmailListServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private EmailListService emailListService;

    @Before
    public void before() throws ServletException {
        this.emailListService = createMock(EmailListService.class);
        final ServletConfig servletConfig = createMock(ServletConfig.class);
        final ServletContext servletContext = createMock(ServletContext.class);

        expect(servletConfig.getServletContext()).andReturn(servletContext);
        expect(servletContext.getAttribute(EmailListService.KEY)).andReturn(emailListService);

        replay(servletConfig,servletContext);

        this.servlet = new EmailListServlet();
        servlet.init(servletConfig);

        this.request = createMock(HttpServletRequest.class);
        this.response = createMock(HttpServletResponse.class);
    }

    @Test(expected=IOException.class)
    public void doGet_without_list() throws Exception {
        expect(request.getParameter("listName")).andReturn(null);
        expect(emailListService.getListByName(null)).andThrow(new IOException());

        replay(request,emailListService);

        servlet.doGet(request, response);
    }

    @Test
    public void doGet_with_list() throws Exception {
        expect(request.getParameter("listName")).andReturn("foolist");

        final List<String> list = Arrays.asList("larry@stooge.com","moe@stooge.com","curley@stooge.com");
        expect(emailListService.getListByName("foolist")).andReturn(list);

        final PrintWriter writer = createMock(PrintWriter.class);
        checkOrder(writer,true);
        expect(response.getWriter()).andReturn(writer);

        writer.println("larry@stooge.com");
        writer.println("moe@stooge.com");
        writer.println("curley@stooge.com");
        writer.close();

        replay(request,response,writer,emailListService);

        servlet.doGet(request, response);

        verify(writer);
    }
}
```

Maybe it's because I am used to a different mocking strategy, or perhaps it's just an older API (I think EasyMock was one
of the first), but I am not really fond of EasyMock. It's another tool in the toolbox and I know there are quite a few
developers out there who swear by it. It's not one that I will use often.

> You can find the source code used in this posting in my [TourDeMock](http://github.com/cjstehno/coffeaelectronica/tree/master/tourdemock) project.
