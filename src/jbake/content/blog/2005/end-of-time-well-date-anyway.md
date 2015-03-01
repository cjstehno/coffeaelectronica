title=End of Time - Well, Date Anyway
date=2005-02-18
type=post
tags=blog,java
status=published
~~~~~~
I have run across this issue before you need to specify a "far-off" expiration date for something using the `java.util.Date`
class. Generally I have specified something 100 years in the future like 1/1/2100 or something like that using the old
deprecated Date constructors... but that is not really a good practice or habit to get into. I came up with two better solutions.

First, in the case where you want to specify a static date, you could run a quick test to determine the long time value
for the date you create using the deprecated constructors. You then modify your code to use the long value in the
constructor, which is the only remaining non-empty constructor.

The second options is useful if you just want a date sometime in the unreachable future (avoiding any reasonable y2k-like
issues) you can use the following:

```java
Date expiration = new Date(Long.MAX_VALUE);
```

which will yield a date so far in the future that you would be proud to have your code exist that long and happy to fix
it (at least your descendants should be happy to fix it). The date it represents is:

```Sun Aug 17 00:12:55 MST 292278994```

I think that should suffice.
