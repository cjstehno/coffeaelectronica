title=Spring & RMI & JNDI -> Cool
date=2006-09-10
type=post
tags=blog,java,testing,mocking
status=published
~~~~~~
I started looking into RMI again; it's been a while since I have used it directly and as often happens, I got sidetracked.
I found out how easy it is very to setup an RMI registry with [Spring](http://springframework.org/) and then access the
bound objects via JNDI.

First, we need an object to bind. Let's keep it simple and just have two methods: one for getting the current date and
another that will return the string that we pass into it. If you remember your RMI you know that we need an interface
that extends `java.rmi.Remote` and an object that implements that interface. For the interface:

```java
public interface MyRemote extends Remote {
    public Date getTimestamp() throws RemoteException;

    public String echoString(String str) throws RemoteException;
}
```

and for the implementation:

```java
public class MyRemoteImpl implements MyRemote {
    private static final long serialVersionUID = 9222184580750208673L;

    public MyRemoteImpl() throws RemoteException {
        super();
    }

    public Date getTimestamp() throws RemoteException {
        return(new Date());
    }

    public String echoString(String str) throws RemoteException {
        return(str);
    }
}
```

Next, you will need a spring context to work with. I am not going into the details of Spring configuration here,
but I will show the bean definitions for the necessary beans; the object to be bound, the service exporter and the
registry factory. The object to be bound is our `MyRemoteImpl` and it's just a simple bean:

```xml
<bean id='service.MyRemote' class='rmi.MyRemoteImpl' />
```

The RMI registry configuration is pretty simple using the `RmiRegistryFactoryBean`.

```xml
<bean id='rmi.Registry' class='org.springframework.remoting.rmi.RmiRegistryFactoryBean'>
    <property name='port' value='1099' />
</bean>
```

This will retrieve or create a registry on the localhost port 1099.

> *NOTE:* be sure that you do not specify the host property if you want the registry to be created. The bean will only create a registry on the localhost
and only if the host property is not set.

Then we need to export the service to the registry (bind the object). To do that we use the `RmiServiceExporter`.

```xml
<bean id='rmi.service.MyRemote' class='org.springframework.remoting.rmi.RmiServiceExporter'>
    <property name='serviceName' value='myRemote' />
    <property name='serviceInterface' value='rmi.MyRemote' />
    <property name='service' ref='service.MyRemote' />
    <property name='registry' ref='rmi.Registry' />
</bean>
```

As you can see, we specify the name that the object is bound to, the registry it is to be bound to and the interface and
object being bound. Once those three beans are in place, a RMI registry will be started and have the `MyRemoteImpl` object
bound to it. It's as simple as that. You can test the binding with a couple lines of code:

```java
Registry registry = LocateRegistry.getRegistry(1099);
MyRemote myRemote = (MyRemote)registry.lookup("myRemote");
Date date = myRemote.getTimestamp();
```

But, hey, I mentioned something about accessing the registry via JNDI. The details of the whole RMI/JNDI connection
can be found in the Sun J2SE JavaDocs [JNDI Guide](http://java.sun.com/j2se/1.4.2/docs/guide/jndi/jndi-rmi.html), but I
will show a little example code... it's pretty simple. Everything we have done so far stays the same except for the
client code used to access the registry and lookup the object. Now we want to use JNDI. First we need to setup the JNDI
properties either on the command line or some other means. I used the code below:

```java
System.setProperty( Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.rmi.registry.RegistryContextFactory" );

System.setProperty(Context.PROVIDER_URL,"rmi://localhost:1099");
```

Then to access the bound object:

```java
final Context ictx = new InitialContext();
MyRemote myRemote = (MyRemote)ictx.lookup("myRemote");

System.out.println("Date: " + myRemote.getTimestamp());
System.out.println("Echo: " + myRemote.echoString("Hello RMI"));
```

You can now access the bound objects via direct RMI or JNDI lookup and you can embed the remote references in a
different spring context using the JNDI factory beans (I'll leave that for another time). You can't get much simpler
than that.
