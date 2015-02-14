title=Embedding Jetty in Spring
date=2006-02-01
type=post
tags=blog,java
status=published
~~~~~~
> The discussion here is based on Jetty 5, while Jetty 6 makes things a lot easier to do. I have an updated version of this post for Jetty 6, [Embedding Jetty 6 in Spring](Embedding-Jetty-6-in-Spring).

I came across [Jetty](http://jetty.mortbay.org/) a while back and finally got around to really playing with
it recently. I was amazed at how flexible it was and how easy it was to embed it inside a [Spring](http://springframework.org/)
Application Context. I did have to write a couple of small helper extensions to ease things along but other than that it was pretty
much just a configuration exercise. What follows is a brief discussion on what I did and how I did it.

Basically what you need, per the Jetty documentation, is an `HttpServer` instance listening on a port, and an
`HttpContext` with a couple `Handler`s. If all you want is a simple web server, with no servlet support it's very easy
and requires no special extensions. Add the following beans to a spring bean config file:

```xml
<bean id='httpServer' class='org.mortbay.http.HttpServer' init-method='start'>
    <property name='listeners'>
        <list>
            <bean class='org.mortbay.http.SocketListener'>
                <property name='port' value='80' />
            </bean>
        </list>
    </property>
    <property name='contexts'>
        <list>
            <bean class='org.mortbay.http.HttpContext'>
                <property name='contextPath' value='/' />
                <property name='resourceBase' value='c:/' />
                <property name='handlers'>
                    <list>
                        <bean class='org.mortbay.http.handler.ResourceHandler'/>
                    </list>
                </property>
            </bean>
        </list>
    </property>
</bean>
```

Now, when you start up the Spring Application Context you will also start up an HTTP Server listening on port 80 that
will serve pages from your `C` drive. How much simpler could that be? You could get rid of the `init-method` and set
`lazy-init` to true if you don't need/want it to fire up right away.

Okay, so web server shmeb server you say? Let's get down to something more interesting. Now, let's turn this plain old
HTTP server into a servlet container... amazingly enough, there is not much more required to make this happen; however,
we do need some extensions in order to work with things in Spring.

First, we will need an instance of `ServletHttpContext` instead of the HTTP Context that we have in there now (or you
could set it up to use both). Unfortunately, the standard `ServletHttpContext` class only has "add" methods for adding
servlets... there is no bulk setter; so we have to add one, which leads up to our first helper class, the
`ServlerHttpContextBean`. All this class does is extend `ServletHttpContext` and add the following method:

```java
public void setServletMappings(Map servletMappings) throws Exception {
    if(MapUtils.isNotEmpty(servletMappings)){
        Iterator paths = servletMappings.keySet().iterator();
        while(paths.hasNext()){
            String path = (String)paths.next();
            ServletDefinitionBean servletConfig = (ServletDefinitionBean)servletMappings.get(path);

            // add the servlet to the context
            ServletHolder holder = addServlet(
                servletConfig.getBeanName(),
                path,
                servletConfig.getServletClassName()
            );

            // configure the holder
            if(holder != null){
                if(servletConfig.getInitOrder() != -1){
                    holder.setInitOrder(servletConfig.getInitOrder());
                }

                Enumeration e = servletConfig.getInitParameterNames();
                while(e.hasMoreElements()){
                    String name = (String)e.nextElement();
                    holder.setInitParameter( name, servletConfig.getInitParameter(name) );
                }
            }
        }
    }
}
```

> _Note:_ Some of my collection helper classes are not shown, but their method signatures should be a good enough explanation of what they do.

The servlet mappings (Path key to `ServletDefinitionBean` value) are processed to add each servlet to the context and
then configure its holder to set any initialization parameters.

You will notice the other helper class being used to configure the servlets. The `ServletDefinitionBean` is used to allow
Spring configuration of the servlets to be added. This is a fairly simple class:

```java
public class ServletDefinitionBean implements Serializable, BeanNameAware {

    private static final long serialVersionUID = 8232043638313653802L;
    private String beanName,servletClassName;
    private Map initParameters;
    private int initOrder = -1;

    public ServletDefinitionBean(){ super(); }

    public void setInitOrder(int initOrder) { this.initOrder = initOrder; }

    public int getInitOrder() { return initOrder; }

    public String getServletClassName() { return servletClassName; }

    public void setServletClassName(String servletClass) {
        this.servletClassName = servletClass;
    }

    public String getBeanName() { return beanName; }

    public void setBeanName(String beanName) { this.beanName = beanName; }

    public void setInitParameters(Map initParameters){
        this.initParameters = initParameters;
    }

    public Enumeration getInitParameterNames(){
        if(MapUtils.isNotEmpty(initParameters)){
            return(IteratorUtils.asEnumeration( initParameters.keySet().iterator()) );
        } else {
            return(CollectionUtils.EMPTY_ENUMERATION);
        }
    }

    public String getInitParameter(String name){
        return(MapUtils.getString(initParameters,name));
    }
}
```

This class is used to store the initialization parameters and any other data required to configure a servlet. And that's
it. Now all you need to do is update the spring configuration to use the new beans.

```xml
<bean id='httpServer' class='org.mortbay.http.HttpServer' init-method='start'>
    <property name='listeners'>
        <list>
            <bean class='org.mortbay.http.SocketListener'>
                <property name='port' value='80' />
            </bean>
        </list>
    </property>
    <property name='contexts'>
        <list>
            <bean class='com.stehno.spring.jetty.ServletHttpContextBean'>
                <property name='contextPath' value='/' />
                <property name='resourceBase' value='c:/' />
                <property name='handlers'>
                    <list>
                        <bean class='org.mortbay.http.handler.ResourceHandler' />
                        <bean class='org.mortbay.jetty.servlet.ServletHandler' />
                    </list>
                </property>
                <property name='servletMappings'>
                    <map>
                        <entry key='/hi/*' value-ref='helloServlet'/>
                    </map>
                </property>
            </bean>
        </list>
    </property>
</bean>
```

You will notice that in order to serve normal resources, you still need to include a `ResourceHandler`. I have mapped
the 'helloServlet' to the path '/hi/*'. The servlet definition bean is configured as follows:

```xml
<bean id='helloServlet' class='com.stehno.spring.jetty.ServletDefinitionBean'>
    <property name='servletClassName' value='test.HelloServlet' />
    <property name='initParameters'>
        <map>
            <entry key='text' value='Hello Jetty-embedded Spring!' />
        </map>
    </property>
</bean>
```

It's just a simple Hello World type servlet that I will leave you to write yourself. But, if you fire up the Application
Context and point your browser to 'http://localhost/hi/blah' you will run this servlet.

I have not fully put this through its paces, but I have installed and run a Spring Dispatcher servlet that had its own application
context with controllers and was able to hit the controllers and get the expected response. Jetty seems to be well-developed
and very flexible and combining it with spring makes it even more so. I think there are many uses for this setup. It could
be used in a desktop application to provide web services, for unit testing of servlets or controllers, or for web proxy-ing.