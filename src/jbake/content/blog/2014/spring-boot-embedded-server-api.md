title=Spring Boot Embedded Server API
date=2014-09-15
type=post
tags=blog,spring,groovy,java,gradle
status=published
~~~~~~
I have been investigating [Spring-Boot](http://projects.spring.io/spring-boot/) for both work and personal projects and while it seems very all-encompassing and useful, I have found that its "opinionated" approach to development was a bit too aggressive for the project conversion I was doing at work; however, I did come to the realization that you don't have to use Spring-Boot as your projects core - you can use it and most of its features in your own project, just like any other java library.

The project I was working on had a customized embedded Jetty solution with a lot of tightly-coupled Jetty-specific configuration code with configuration being pulled from a Spring Application context. I did a little digging around in the Spring-Boot documentation and found that their API provides direct access to the embedded server abstraction used by a Boot project. On top of that, it's actually a very sane and friendly API to use. During my exploration and experimentation I was able to build up a simple demo application, which seemed like good fodder for a blog post - we're not going to solve any problems here, just a little playtime with the Spring-Boot embedded server API.

To start off, we need a project to work with; I called mine "spring-shoe" (not big enough for the whole boot, right?). I used Java 8, Groovy 2.3.2 and Gradle 2.0, but slightly older versions should also work fine - the build file looks like:

```groovy
apply plugin: 'groovy'

compileJava {
    sourceCompatibility = 1.8
    targetCompatibility = 1.8
}

compileGroovy {
    groovyOptions.optimizationOptions.indy = false
}

repositories {
    jcenter()
}

dependencies {
    compile 'org.codehaus.groovy:groovy-all:2.3.2'

    compile 'javax.servlet:javax.servlet-api:3.0.1'
    compile 'org.eclipse.jetty:jetty-webapp:8.1.15.v20140411'

    compile 'org.springframework.boot:spring-boot:1.1.5.RELEASE'
    compile 'org.springframework:spring-web:4.0.6.RELEASE'
    compile 'org.springframework:spring-webmvc:4.0.6.RELEASE'
}
```
Notice, that I am using the spring-boot library, not the Gradle plugin or "starter" dependencies - this also means that you have to bring in other libraries yourself (e.g. the web and webmvc libraries above).

Next, we need an application starter, which just instantiates a specialized Application context, the `AnnotationConfigEmbeddedWebApplicationContext`:

```groovy
package shoe

import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext

class Shoe {
    static void main( args ){
        EmbeddedWebApplicationContext context = new AnnotationConfigEmbeddedWebApplicationContext('shoe.config')
        println "Started context on ${new Date(context.startupDate)}"
    }
}
```
Where the package `shoe.config` is where my configuration class lives - the package will be auto-scanned. When this class' main method is run, it instantiates the context and just prints out the context start date. Internally this context will search for the embedded server configuration beans as well as any servlets and filters to be loaded on the server - but I am jumping ahead; we need a configuration class:

```groovy
package shoe.config

import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Configuration
@EnableWebMvc
class ShoeConfig {

    @Bean EmbeddedServletContainerFactory embeddedServletContainerFactory(){
        new JettyEmbeddedServletContainerFactory( 10101 )
    }
}
```
As you can see, it's just a simple Java-based configuration class. The `EmbeddedServletContainerFactory` class is the crucial part here. The context loader searches for a configured bean of that type and then loads it to create the embedded servlet container - a Jetty container in this case, running on port 10101.

Now, if you run `Shoe.main()` you will see some logging similar to what is shown below:

```
...
INFO: Jetty started on port: 10101
Started context on Thu Sep 04 18:59:24 CDT 2014
```
You have a running server, though its pretty boring since you have nothing useful configured. Let's start make it say hello using a simple servlet named `HelloServlet`:

```groovy
package shoe.servlet

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HelloServlet extends HttpServlet {

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException{
        resp.writer.withPrintWriter { w->
            w.println "Hello, ${req.getParameter('name')}"
        }
    }
}
```
It's just a simple `HttpServlet` extension that says "hello" with the input value from the "name" parameter. Nothing really special here. We could have just as easily used an extension of Spring's `HttpServletBean` here instead. Moving back to the `ShoeConfig` class, the modifications are minimal, you just create the servlet and register it as a bean.

```groovy
@Bean HttpServlet helloServlet(){
    new HelloServlet()
}
```
Now fire the server up again, and browse to [http://localhost:10101/helloServlet?name=Chris](http://localhost:10101/helloServlet?name=Chris) and you will get a response of:

```
Hello, Chris
```
Actually, any path will resolve to that servlet since it's the only one configured. I will come back to configuration of multiple servlets and how to specify the url-mappings in a little bit, but let's take the next step and setup a `Filter` implementation. Let's create a Filter that counts requests as they come in and then passes the current count along with the continuing request.

```groovy
package shoe.servlet

import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import java.util.concurrent.atomic.AtomicInteger

class RequestCountFilter extends GenericFilterBean {

    private final AtomicInteger count = new AtomicInteger(0)

    @Override
    void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException{
        request.setAttribute('request-count', count.incrementAndGet())

        chain.doFilter( request, response )
    }
}
```
In this case, I am using the Spring helper, `GenericFilterBean` simply so I only have one method to implement, rather than three. I could have used a simple `Filter` implementation.

In order to make use of this new count information, we can tweak the `HelloServlet` so that it prints out the current count with the response - just change the `println` statement to:

```groovy
w.println "<${req.getAttribute('request-count')}> Hello, ${req.getParameter('name')}"
```
Lastly for this case, we need to register the filter as a bean in the `ShoeConfig` class:

```groovy
@Bean Filter countingFilter(){
    new RequestCountFilter()
}
```
Now, run the application again and hit the hello servlet a few times and you will see something like:

```
<10> Hello, Chris
```
The default url-mapping for the filter is "/*" (all requests). While, this may be useful for some quick demo cases, it would be much more useful to be able to define the servlet and filter configuration similar to what you would do in the web container configuration - well, that's where the `RegistrationBeans` come into play.

Revisiting the servlet and filter configuration in `ShoeConfig` we can now provide a more detailed configuration with the help of the `ServletRegistrationBean` and the `FilterRegistrationBean` classes, as follows:

```groovy
@Bean ServletRegistrationBean helloServlet(){
    new ServletRegistrationBean(
        urlMappings:[ '/hello' ],
        servlet: new HelloServlet()
    )
}

@Bean FilterRegistrationBean countingFilter(){
    new FilterRegistrationBean(
        urlPatterns:[ '/*' ],
        filter: new RequestCountFilter()
    )
}
```
We still leave the filter mapped to all requests, but you now have access to any of the filter mapping configuration parameters. For instance, we can add a simple init-param to the `RequestCountingFilter`, such as:

```groovy
int startValue = 0

private AtomicInteger count

@Override
protected void initFilterBean() throws ServletException {
    count = new AtomicInteger(startValue)
}
```
This will allow the starting value of the count to be specified as a filter init-parameter, which can be easily configured in the filter configuration:

```groovy
@Bean FilterRegistrationBean countingFilter(){
    new FilterRegistrationBean(
        urlPatterns:[ '/*' ],
        filter: new RequestCountFilter(),
        initParameters:[ 'startValue': '1000' ]
    )
}
```
Nice and simple. Now, when you run the application again and browse to [http://localhost:10101/helloServlet?name=Chris](http://localhost:10101/helloServlet?name=Chris) you get a 404 error. Why? Well, now you have specified a url-mapping for the servlet, try [http://localhost:10101/hello?name=Chris](http://localhost:10101/hello?name=Chris) and you will see the expected result, something like:

```
<1004> Hello, Chris
```
You can also register `ServletContextListeners` in a similar manner. Let's create a simple one:

```groovy
package shoe.servlet

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

class LoggingListener implements ServletContextListener {

    @Override
    void contextInitialized(ServletContextEvent sce) {
        println "Initialized: $sce"
    }

    @Override
    void contextDestroyed(ServletContextEvent sce) {
        println "Destroyed: $sce"
    }
}
```
And then configure it in `ShoeConfig`:

```groovy
@Bean ServletListenerRegistrationBean listener(){
    new ServletListenerRegistrationBean(
        listener: new LoggingListener()
    )
}
```
Then, when you run the application, you will get a message in the server output like:

```
Initialized: javax.servlet.ServletContextEvent[source=ServletContext@o.s.b.c.e.j.JettyEmbeddedWebAppContext{/,null}]
```
Now, let's do something a bit more interesting - let's setup a Spring-MVC configuration inside our embedded server.

The first thing you need for a minimal Spring-MVC configuration is a `DispatcherServlet` which, at its heart, is just an `HttpServlet` so we can just configure it as a bean in `ShoeConfig`:

```groovy
@Bean HttpServlet dispatcherServlet(){
    new DispatcherServlet()
}
```
Then, we need a controller to make sure this configuration works - how about a simple controller that responds with the current time; we will also dump the request count to show that the filter is still in play. The controller looks like:

```groovy
package shoe.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

@RestController
class TimeController {

    @RequestMapping('/time')
    String time( HttpServletRequest request ){
        "<${request.getAttribute('request-count')}> Current-time: ${new Date()}"
    }
}
```
Lastly for this example, we need to load the controller into the configuration; just add a `@ComponentScan` annotation to the `ShoeConfig` as:

```groovy
@ComponentScan(basePackages=['shoe.controller'])
```
Fire up the server and hit the [http://localhost:10101/time](http://localhost:10101/time) controller and you see something similar to:

```
<1002> Current-time: Fri Sep 05 07:02:36 CDT 2014
```
Now you have the ability to do any of your Spring-MVC work with this configuration, while the standard filter and servlet still work as before.

> As a best-practice, I would suggest keeping this server configuration code separate from other configuration code for anything more than a trivial application (i.e. you wouldn't do your security and database config in this same file).

For my last discussion point, I want to point out that the embedded server configuration also allows you to do additional customization to the actual server instance during startup. To handle this additional configuration, Spring provides the `JettyServerCustomizer` interface. You simply implement this interface and add it to your sever configuration factory bean. Let's do a little customization:

```groovy
class ShoeCustomizer implements JettyServerCustomizer {

    @Override
    void customize( Server server ){
        SelectChannelConnector myConn = server.getConnectors().find { Connector conn ->
            conn.port == 10101
        }

        myConn.maxIdleTime = 1000 * 60 * 60
        myConn.soLingerTime = -1

        server.setSendDateHeader(true)
    }
}
```
Basically just a tweak of the main connector and also telling the server to send an additional response header with the date value. This needs to be wired into the factory configuration, so that bean definition becomes:

```groovy
@Bean EmbeddedServletContainerFactory embeddedServletContainerFactory(){
    def factory = new JettyEmbeddedServletContainerFactory( 10101 )
    factory.addServerCustomizers( new ShoeCustomizer() )
    return factory
}
```
Now when you start the server and hit the time controller you will see an additional header in the response:

```
Date:Fri, 05 Sep 2014 12:15:27 GMT
```

As you can see from this long discussion, the Spring-Boot embedded server API is quite useful all on its own. It's nice to see that Spring has exposed this functionality as part of its public API rather than hiding it under the covers somewhere.

> The code I used for this article can be found in the main repository for this project, under the [spring-shoe](https://github.com/cjstehno/coffeaelectronica/tree/master/spring-shoe) directory.