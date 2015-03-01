title=Map Iteration Tip
date=2007-09-27
type=post
tags=blog,java
status=published
~~~~~~
Say you have a `Map` and that you need to iterate over its contents and do something with both the key and
the value for each mapping. I have often seen the following code used:

```java
Iterator i = map.keySet().iterator();
    while(i.hasNext()){
    Object key = i.next();
    Object val = map.get(key);
    // do something with them
}
```

While this is correct and generally there is nothing wrong with it, you are doing an extra `get()` call into the
map; however, if you iterate over the entry Set you can remove that extra call:

```java
Iterator i = map.entrySet().iterator();
while(i.hasNext()){
    Entry entry = (Entry)i.next();
    Object key = entry.getKey();
    Object val = entry.getValue();
    // do something with them
}
```

Also, as a side note, with Java 5 and above you can use the new foreach loop to simplify things even more:

```java
for(Entry&lt;object,object&gt; entry : map.entrySet()){
    Object key = entry.getKey();
    Object val = entry.getValue();
    // do something
}
```

It's not going to double your processing speed or anything, but it is a little more efficient, especially when you are
iterating over a large map of items.
