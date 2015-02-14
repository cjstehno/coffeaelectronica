title=Circular Arrays
date=2008-01-25
type=post
tags=blog,java
status=published
~~~~~~
Another one of those, "well, duh" moments... a very easy way to do wrap-around or circular array indexing.

```java
i = (i + 1) % N
```

Where i is your current index and N is the length of the array. Say you have an array of five elements. When you
are currently on element of index 2, your next index will be:

```java
i = (2 + 1) % 5 = 3
```

However, once you get to the last element, index 4:

```java
i = (4 + 1) % 5 = 0
```

Viola, you are back at 0 again. I don't know why but I really neglect the mod operator (%). It has some interesting
uses.

As an example, below is a simple Groovy `CircularArray` implementation:

```groovy
class CircularArray {

    def items = []
    int index = -1

    def next(){
        items[( (index++) + 1) % items.size()]
    }
}

def circ = new CircularArray( items:['a','b','c','d'] )

10.times {
    def k = circ.next()
    println "$it: $k"
}
```

which, when run, will yield:

```
0: a
1: b
2: c
3: d
4: a
5: b
6: c
7: d
8: a
9: b
```

A good one to keep handy.
