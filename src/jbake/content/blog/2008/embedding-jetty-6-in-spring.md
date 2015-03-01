title=Embedding Jetty 6 in Spring
date=2008-08-05
type=post
tags=blog,java,spring
status=published
~~~~~~
A few years ago, I wrote a blog entry about [Embedding Jetty in Spring](Embedding-Jetty-in-Spring).
It became quite popular, at least in relation to other pages on my site. Unfortunately, as I noted in the header of that posting, it has
become a bit out-dated as newer versions of Jetty have been released. Well, with a little prodding via emails and a handful of free time,
I have come up with an updated version for [Jetty 6.1.11](http://jetty.mortbay.com/) and [Spring 2.5.5](http://springframework.org/) that
requires no additional helper-classes.

For simplicity I came up with a spring context for embedding Jetty based on the example included with Jetty which replicates the default
full configuration, [LikeJettyXml.java](http://jetty.mortbay.org/jetty-6/xref/org/mortbay/jetty/example/LikeJettyXml.html). This seemed a
good place to start since you will either need all of that, or slightly less... and removing stuff is simple.

For the most part the spring context configuration mirrors the java source from the example; the only real deviation comes form the life-cycle
addition method calls `addLifeCycle()`, which spring does not directly support. To perform that one missing dependency injection, you
can use the spring `MethodInvokingFactoryBean` to create a bean and then call a method to inject it into the target bean.

```xml
<bean class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
    <property name="targetObject" ref="server.Server" />
    <property name="targetMethod" value="addLifeCycle" />
    <property name="arguments">
        <list><ref local="server.ContextDeployer" /></list>
    </property>
</bean>
```

which simply calls the `addLifeCycle()` method on the server instance to add the two deployer instances. The whole context file reads as follows:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="server.Server" class="org.mortbay.jetty.Server" destroy-method="stop">
        <property name="threadPool">
            <bean class="org.mortbay.thread.QueuedThreadPool">
                <property name="maxThreads" value="100" />
            </bean>
        </property>
        <property name="connectors">
            <list>
                <bean class="org.mortbay.jetty.nio.SelectChannelConnector">
                    <property name="port" value="8080" />
                    <property name="maxIdleTime" value="30000" />
                </bean>
            </list>
        </property>
        <property name="handler">
            <bean class="org.mortbay.jetty.handler.HandlerCollection">
                <property name="handlers">
                    <list>
                        <ref local="server.ContextHandlerCollection" />
                        <bean class="org.mortbay.jetty.handler.DefaultHandler" />
                        <bean class="org.mortbay.jetty.handler.RequestLogHandler">
                            <property name="requestLog">
                                <bean class="org.mortbay.jetty.NCSARequestLog">
                                    <constructor-arg value="cfg/logs/jetty-yyyy_mm_dd.log" />
                                    <property name="extended" value="false"/>
                                </bean>
                            </property>
                        </bean>
                    </list>
                </property>
            </bean>
        </property>
        <property name="userRealms">
            <list>
                <bean class="org.mortbay.jetty.security.HashUserRealm">
                    <property name="name" value="Test Realm" />
                    <property name="config" value="cfg/etc/realm.properties" />
                </bean>
            </list>
        </property>
        <property name="stopAtShutdown" value="true" />
        <property name="sendServerVersion" value="true"/>
    </bean>

    <bean class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
        <property name="targetObject" ref="server.Server" />
        <property name="targetMethod" value="addLifeCycle" />
        <property name="arguments">
            <list><ref local="server.ContextDeployer" /></list>
        </property>
    </bean>

    <bean class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
        <property name="targetObject" ref="server.Server" />
        <property name="targetMethod" value="addLifeCycle" />
        <property name="arguments">
            <list><ref local="server.WebAppDeployer" /></list>
        </property>
    </bean>

    <bean id="server.ContextHandlerCollection" class="org.mortbay.jetty.handler.ContextHandlerCollection" />

    <bean id="server.ContextDeployer" class="org.mortbay.jetty.deployer.ContextDeployer">
        <property name="contexts" ref="server.ContextHandlerCollection" />
        <property name="configurationDir">
            <bean class="org.mortbay.resource.FileResource">
                <constructor-arg value="file://./cfg/contexts" />
            </bean>
        </property>
        <property name="scanInterval" value="1" />
    </bean>

    <bean id="server.WebAppDeployer" class="org.mortbay.jetty.deployer.WebAppDeployer">
        <property name="contexts" ref="server.ContextHandlerCollection" />
        <property name="webAppDir" value="cfg/webapps" />
        <property name="parentLoaderPriority" value="false" />
        <property name="extract" value="true" />
        <property name="allowDuplicates" value="false" />
        <property name="defaultsDescriptor" value="cfg/etc/webdefault.xml" />
    </bean>
</beans>
```

In order to start the server you need to be sure that the `MethodInvokingFactoryBeans` have been fired before the server has been started;
the easiest way is to start the server with an external class once the context has loaded.

```java
public class Main {
    public static void main(String[] args) throws Exception {<
        ApplicationContext context = new FileSystemXmlApplicationContext("cfg/server-context.xml");

        Server server = (Server)context.getBean("server.Server");
        server.start();
        server.join();
    }
}
```

This should be a good starting point and general template for anything you need... just inject or modify whatever configuration setup you
need for your application. I have attached the zipped up Eclipse project I used to create and run this test. You will need the following
jars somewhere on your classpath: ant-1.6.5.jar, commons-el-1.0.jar, commons-logging.jar, jasper-compiler-5.5.15.jar,
jasper-runtime-5.5.1.5.jar, jetty-6.1.11.jar, jetty-util-6.1.11.jar, jsp-api-2.0.jar, servlet-api-2.5-6.1.11.jar, and spring.jar...
all of which can be found in the lib directories of the Spring and Jetty distributions uses.
