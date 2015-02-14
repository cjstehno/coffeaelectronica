title=If at First You Don't Succeed...
date=2009-12-20
type=post
tags=blog,java
status=published
~~~~~~
Sometimes you run into an operation that is a little on the flaky side and might sometimes fail on one execution
but then work fine on the next; apart from fixing the underlying issue which may not be within your control, you can
implement an operation retry strategy. With an operation retry strategy you attempt to execute an operation and then
retry it if it fails. After a specified number of attempts (or under certain exceptions) you can allow the operation to
fail gracefully. This gives you greater isolation of the questionable service while also allowing you more control what
happens on a failure.

I have run into this issue a few years back with a twitchy SMTP server, and now again with a slightly-flaky database
connection pool. Last time I wrote a very code-specific retry strategy (and like a doof, never blogged about it), but
this time something more reusable is in order since this component would be used extensively throughout the code.

Basically the requirement is the ability to run a repeatable operation some number of times until it either succeeds or
exceeds a specified number of attempts, at which point it will stop trying and fail. Since there are no closures in
Java yet, I came up with a `Retriable` interface with a single method which will execute the operation and return it's
return value if there is one.

```java
public interface Retriable<T> {
    public T execute() throws Exception;
}
```

I had originally considered using `java.lang.Runnable`; however, a return value simplifies cases where you
are trying to extract some value from the operation. Similarly I considered the `org.apache.commons.lang.Closure`
class and discarded if for the same reason.

The retry logic itself is pretty straight-forward. The failure condition is based on exceptions thrown by the operation
execution. If an execption is thrown that is not contained in the "catchAndThrow" list, the counter will be incremented and the operation will be tried again if the max number of
tries has not been exceeded. The "catchAndThrow" list is an array of Exception classes which if caught are to be thrown
out of the handler rather than initiating a retry. This allows some desired exceptions to be handled by the calling
method.

```java
public <T> T execute( final Retriable<T> op ) throws Exception {
    boolean retry = true;
    int count = 0;
    do {
        try {
            return op.execute();
        } catch(final Exception e) {
            if( ArrayUtils.contains(catchAndThrow, e.getClass()) ) throw e;

            retry = ++count < maxRetries;

            if(log.isWarnEnabled() && !retry) {
                log.warn("RetriesFailed[" + op.getClass().getName() + "]: " + e.getMessage(), e);
            }
        }
    } while( retry );

    throw new MaxRetriesExceededException(maxRetries,op.getClass().getName());
}
```

You will notice that if all the retries fail, a `MaxRetriesExceededException` is thrown. This allows calling
methods to catch and handle the case when total failure occurs in a method-specific manner.

A more advanced retry strategy could also be derived from this where the failure condition is based on an injected
object (ala strategy pattern) so that other criteria could be used to determine success and failure. The exception catch
and throw filter could also be enhanced in this manner.<br/><br/>The retry object itself, which I have called
the `Retrier` is a reusable thread-safe POJO so it can be configured via dependency injection (Spring) and used for any
number of `Retriable` operations.

```xml
<bean id="retrier" class="retry.Retrier">
    <property name="maxRetries" value="3" />
    <property name="catchAndThrow">
        <list>
            <value>java.lang.NullPointerException</value>
        </list>
    </property>
</bean>
```

What you end up with is a very clean way to perform retiable logic:

```java
final long id = someBeanId;
final String criteria = searchCriteria;

SomeBean resultBean = retrier.execute( new Retriable<SomeBean>(){
    public SomeBean execute() throws Exception {
        return searchDao.findBean(id,criteria);
    }
} );
```

In this sample code a searchDao is being called in a retriable manner using parameters from the calling method. As
long as parameters are final they can be passed into the anonymous inner class craeted by the inline creation of the
`Retriable`. You will also see that with the use of generics you get a seamless integration of the retiable
operation into your code.

## Slightly Different Approach

After a bit more thought, I came up with a slightly different approach to the retry execution method:

```java
public <T> T execute( final Retriable<T> op ) throws Exception {
    for(int r=0; r<maxRetries; r++){
        try {
            return op.execute();
        } catch(final Exception e) {
            if( ArrayUtils.contains(catchAndThrow, e.getClass()) ) throw e;

            if(log.isWarnEnabled()){
                log.warn("RetryCaughtException[" + r + "]: " + e.getMessage());
            }
        }
    }

    if(log.isWarnEnabled()) {
        log.warn("RetriesFailed: " + op.getClass().getName());
    }

    throw new MaxRetriesExceededException(maxRetries,op.getClass().getName());
}
```

This approach does not rely on the boolean to end the loop. If an exception is thrown it will try again until a
return value is returned or the retry count has been exceeded. With this approach you only fall out of the loop on a
retry exceeded condition, hence the logging and the thrown exception. This feels a little cleaner than the other
approach. I have not run it through the unit testing so, you might do that first if you intend to use this code.


> I have added this code to my [CodePerks](https://github.com/cjstehno/codeperks) project.
