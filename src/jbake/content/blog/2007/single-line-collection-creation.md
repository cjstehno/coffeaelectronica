title=Single-line Collection Creation
date=2007-10-09
type=post
tags=blog,java
status=published
~~~~~~
I like having nice compact code. No, I am not one of those "write the whole app on one line" developers, but I do
like code collapsed and out of the way. One of the things that has always annoyed me was that while you can create and
populate an array on one line, you cannot do the same with `Map`s, `List`s, and `Set`s: but, I finally realized that
there is a very simple way to do it using instance initializers.

```java
Map<String,String> map = new HashMap<String, String>(){{ put("akey","avalue"); }}
```

Basically you are anonymously extending the `HashMap` and calling the `put()` method to populate the data. Notice the
double curley braces, which signify instance initialization.

I am not suggesting that all of your collection populating should be done this way; however, it is nice when you simply
want a single value put in a map for some reason.