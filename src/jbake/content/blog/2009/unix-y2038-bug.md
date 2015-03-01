title=Unix Y2038 Bug
date=2009-09-19
type=post
tags=blog,java
status=published
~~~~~~
Apparently there are still some short-sighted developer date-based issues out there, one of which is the
[Year 2038 Problem](http://en.wikipedia.org/wiki/Year_2038_problem), which is a unix-based problem with how the milliseconds since 1970 value is stored... it's a 32-bit value which will wrap around into negative numbers in 2038.

I ran a quick sanity check in Linux:

```java
Date expiration = new Date(Long.MAX_VALUE);
```

and found that a Mac and Linux running Sun's JVM seems to be fine:

```
Sun Aug 17 01:12:55 CST 292278994
```

while GCJ on Linux produced:

```
Exception in thread "main" java.lang.IllegalArgumentException: month out of range:-19461555
at gnu.java.util.ZoneInfo.getOffset(int, int, int, int, int, int) (/usr/lib/libgcj.so.5.0.0)
at java.util.GregorianCalendar.computeFields() (/usr/lib/libgcj.so.5.0.0)
at java.util.Calendar.setTimeInMillis(long) (/usr/lib/libgcj.so.5.0.0)
at java.util.Date.toString() (/usr/lib/libgcj.so.5.0.0)
at java.io.PrintStream.println(java.lang.Object) (/usr/lib/libgcj.so.5.0.0)
at Main.main(java.lang.String[]) (Unknown Source)
```

Ouch! This is kind of a subtle issue since your JVM may be giving you the correct value; however, your database which may
be running on Linux might give you the wrong value from a time-based operation (or an error). This is one to keep an eye one.
