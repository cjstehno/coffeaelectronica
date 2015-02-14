title=Simple Time Unit Conversion
date=2012-04-11
type=post
tags=blog,javascript
status=published
~~~~~~
Here's a quick tip I found recently. I am sure we have all done this or run into this while coding time duration values, most of which end up being milliseconds.

```groovy
long waitTime = 2 * 60 * 1000 // 2 minutes
```

or worse yet, you run into the less descriptive version:

```groovy
long waitTime = 120000
```

Neither of these is wrong or even all that horrible considering the fact that generally you set these values and forget them; however, there is a clean way to do this that is also much more explicit using TimeUnit:

```groovy
import java.util.concurrent.TimeUnit
long waitTime1 = TimeUnit.MILLISECONDS.convert( 2, TimeUnit.MINUTES )
```

which says, "Give me 2 minutes, in milliseconds".

This allows you to have the expression of the units involved right in the code while making it trivial to change the value as needed without potential math errors (yes, I have seen missing zeros).
