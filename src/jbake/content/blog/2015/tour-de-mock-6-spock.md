title=Tour de Mock 6: Spock
date=2015-04-09
type=post
tags=blog,groovy,testing
status=published
~~~~~~

My last entry in my "Tour de Mock" series was focused on basic [Groovy mocking](http://coffeaelectronica.com/blog/2010/tour-de-mock-5.html). In this post, I am going to take a look at the [Spock Framework](https://code.google.com/p/spock/), which is an alternative testing framework with a lot of features, including its own mocking API.

Since it's been a while, let's refer back to the [original posting](http://coffeaelectronica.com/blog/2009/tour-de-mock-1.html) as a refresher of what is being tested. We have a `Servlet`, the `EmailListServlet` 

```groovy
public class EmailListServlet extends HttpServlet {

    private EmailListService emailListService;

    public void init() throws ServletException {
        final ServletContext servletContext = getServletContext();
        this.emailListService = (EmailListService)servletContext.getAttribute(EmailListService.KEY);

        if(emailListService == null) throw new ServletException("No ListService available!");
    }

    protected void doGet(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        final String listName = req.getParameter("listName");
        final List<String> list = emailListService.getListByName(listName);
        PrintWriter writer = null;
        try {
            writer = res.getWriter();
            for(final String email : list){
                writer.println(email);
            }
        } finally {
            if(writer != null) writer.close();
        }
    }
}
```

which uses an `EmailListService`

```groovy
public interface EmailListService {

    public static final String KEY = "com.stehno.mockery.service.EmailListService";

    /**
     * Retrieves the list of email addresses with the specified name. If no list
     * exists with that name an IOException is thrown.
     */
    List<String> getListByName(String listName) throws IOException;
}
```

to retrieve lists of email addresses, because that's what you do, right? It's just an example. :-)

First, we need to add Spock to our build (recently converted to Gradle, but basically the same) by adding the following line to the `build.gradle` file:

    testCompile "org.spockframework:spock-core:1.0-groovy-2.4"

Next, we need a test class. Spock uses the concept of a test "Specification" so we create a simple test class as:

```groovy
class EmailListServlet_SpockSpec extends Specification {
    // test stuff here...
}
```

Not all that different from a JUnit test; conceptually they are very similar.

Just as in the other examples of testing this system, we need to setup our mock objects for the servlet environment and other collaborators:

```groovy
def setup() {
    def emailListService = Mock(EmailListService) {
        _ * getListByName(null) >> { throw new IOException() }
        _ * getListByName('foolist') >> LIST
    }

    def servletContext = Mock(ServletContext) {
        1 * getAttribute(EmailListService.KEY) >> emailListService
    }

    def servletConfig = Mock(ServletConfig) {
        1 * getServletContext() >> servletContext
    }

    emailListServlet = new EmailListServlet()
    emailListServlet.init servletConfig

    request = Mock(HttpServletRequest)
    response = Mock(HttpServletResponse)
}
```

Spock provides a `setup` method that you can override to perform your test setup operations, such as mocking. In this example, we are mocking the service interface, and the servlet API interfaces so that they behave in the deisred manner.

The mocking provided by Spock took a little getting used to when coming from a primarily mockito-based background, but once you grasp the overall syntax, it's actually pretty expressive. In the code above for the `EmailListService`, I am mocking the `getListByName(String)` method such that it will accept any number of calls with a `null` parameter and throw an exception, as well as any number of calls with a `foolist` parameter which will return a reference to the email address list. Similarly, you can specify that you expect only N calls to a method as was done in the other mocks. You can dig a little deeper into the mocking part of the framework in the [Interaction-based Testing](http://spockframework.github.io/spock/docs/1.0/interaction_based_testing.html) section of the Spock documentation.

Now that we have our basic mocks ready, we can test something. As in the earlier examples, we want to test the condition when no list name is specified and ensure that we get the expected `Exception` thrown:

```groovy
def 'doGet: without list'() {
    setup:
    1 * request.getParameter('listName') >> null

    when:
    emailListServlet.doGet request, response

    then:
    thrown(IOException)
}
```

One thing you should notice right away is that Spock uses label blocks to denote different parts of a test method. Here, the `setup` block is where we do any additional mocking or setup specific to this test method. The `when` block is where the actual operations being tested are performed while the `then` block is where the results are verified and conditions examined.

In our case, we need to mock out the reuest parameter to return `null` and then we need to ensure that an `IOException` is thrown.

Our other test is the case when a valid list name is provided:

```groovy
def 'doGet: with list'() {
    setup:
    1 * request.getParameter('listName') >> 'foolist'

    def writer = Mock(PrintWriter)

    1 * response.getWriter() >> writer

    when:
    emailListServlet.doGet request, response

    then:
    1 * writer.println(LIST[0])
    1 * writer.println(LIST[1])
    1 * writer.println(LIST[2])
}
```

In the `then` block here, we verify that the `println(String)` method of the mocked `PrintWriter` is called with the correct arguments in the correct order.

Overall, Spock is a pretty clean and expressive framework for testing and mocking. It actually has quite a few other interesting features that beg to be explored.

> You can find the source code used in this posting in my [TourDeMock](http://github.com/cjstehno/coffeaelectronica/tree/master/tourdemock) project.
