title=Spring Inner-Class Instantiation
date=2005-07-13
type=post
tags=blog,java,spring
status=published
~~~~~~
The other day I ran into something I had never tried to do with [Spring](http://springframework.org/) before; define a
bean as an instance of an inner class. I did a little searching through the Spring docs, but could not find anything
about it, negative or positive. So, I just gave it a try. Consider the class:

```java
public abstract class IService {
    public static class ServiceImpl extends IService {
        // something useful...
    }
}
```

which would have a bean definition of:

```xml
<bean id="myService" class="com.some.pkg.IService$ServiceImpl" />
```

where the $ is the separator between the main class and the inner class. This is how its represented in the actual
class file so it makes sense. Nice.
