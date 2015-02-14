title=Mocking Objects with Non-empty Constructors
date=2007-09-20
type=post
tags=blog,java,testing
status=published
~~~~~~
While writing mock objects with [JMock](http://jmock.org/), I have run into a reoccurring issue, you
cannot mock concrete classes that do not have an empty constructor (this has been addressed by the JMock development
team; however, the specific code line has not yet been released). Sometimes, for one reason or another, you have neither
an interface nor empty constructor to mock an object with.

The way JMock creates its proxied mock objects is "on creation", meaning that when you create the mock, the proxy
object is created and stored, thereby not allowing you any way to specify constructor arguments. With a little extension
to the mocking API you can still mock those classes. The solution is a simple extension of the
[CGLIB](http://cglib.sourceforge.net/)-based functionality that is already there such that the proxy is
created only when the `proxy()` method is called. The code of the two classes needed is shown below:

```java
public class CGLIBCoreLazyMock extends AbstractDynamicMock implements MethodInterceptor {
    private Enhancer enhancer;
    private Class[] argTypes;
    private Object[] args;
    private Object proxy;

    public CGLIBCoreLazyMock(Class mockedType,Class[] argTypes,Object[] args){
        super(
            mockedType,
            mockNameFromClass(mockedType),
            new LIFOInvocationDispatcher()
        );
        this.argTypes = argTypes;
        this.args = args;
        this.enhancer = new Enhancer();
        enhancer.setSuperclass(mockedType);
        enhancer.setCallback(this);
    }

    public Object proxy() {
        if(proxy == null){
            this.proxy = enhancer.create(argTypes,args);
        }
        return(proxy);
    }

    public Object intercept(Object thisProxy, Method method, Object[] args, MethodProxy superProxy ) throws Throwable {
        return mockInvocation(new Invocation(proxy,method,args));
    }
}
```

and then a Mock extension.

```java
public class LazyMock extends org.jmock.Mock {
    public LazyMock(Class mockedType,Class[] argTypes,Object[] args){
        super(new CGLIBCoreLazyMock(mockedType,argTypes,args));
    }
}
```

By adding this functionality to JMock, you can mock these classes and still use all of the stub and expectation
features that JMock provides. My first resolution to this problem was a quick custom hack using CGLIB. As it threatened
to get more complex, I took a peek at the JMock source and found that it would not be hard to implement. I guess I could
have gotten the source and added it directly to their code base, and I even looked at doing that right off, but their
project was Ant-based and seemed to be missing some of the pieces required for the build. I just made a little extension
jar and it seems to work well enough.

> _Update:_ [JMock 2](http://jmock.org/) fixes this issue internally and has a lot of other improvements. See
[Are You Still Mocking Me?](Are-You-Still-Mocking-Me%3F) for updated information.
