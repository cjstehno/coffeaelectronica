title=Mocking those Stubborn Loggers
date=2007-11-24
type=post
tags=blog,java
status=published
~~~~~~
I have run into a couple instances where I would benefit from being able to mock logging, such as when the result
of an operation is only some output to a log file. Reflection comes in very handy for this.

I created a utility method called `injectFieldValue` which allows a specified value to be injected into a non-final
field of an object.

```java
public static void injectFieldValue(Object target, String fieldName, Object fieldValue) throws Exception {
    final Field logField = target.getClass().getDeclaredField(fieldName);
    logField.setAccessible(true);
    logField.set(target, fieldValue);
}
```

It allows you to inject a value into any non-final field of the target class. With this method you can now create a
mock Log object and inject it into your target object under test which then allows you to put expectations on the
logging so that you are sure that it is called correctly. Your mock test code could look something like this:

```java
public void testLog() throws Exception {
    final Log log = mock(Log.class);
    checking(new Expectations(){
        {
            one(log).info("something");
        }
    });
    MockUtils.injectFieldValue(target,"log",log);

    target.doSomething();
}
```

I don't recommend this approach for normal coding, but for testing the gloves can come off. Also, I don't recommend
this for everywhere you have logging, just for those cases when logging is an expected result that needs to be
validated... in other words, not very often. This technique will work for `static` loggers but not `final`.

> _Note:_ If the field you are mocking is in a parent of your target object, you will need to rewrite this method to
walk up the hierarchy to find the target field. Not hard to do, just not done here.
