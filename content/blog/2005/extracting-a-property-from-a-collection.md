title=Extracting a Property form a Collection
date=2005-10-17
type=post
tags=blog,java
status=published
~~~~~~
I wrote a little tutorial about [Transformers](/2005/01/jakarta-commons-collections.html) a while back and now I found a
nice little use for them today. I needed (and do every now and then) need to extract the value of one property from
every element of a collection. Yes, I could write an iterator loop and pull it out myself, but that takes more lines of
code and is not reusable like this approach... and I love re-usable code. I needed a list containing the ids (long) of
the elements in a collection.

```java
private static final Transformer tx = new InvokerTransformer("getId",null,null);

public static Long[] getIds(List list){
    Collection coll = CollectionUtils.collect(list,tx);
    return(coll != null ? (Long[])coll.toArray(new Long[0]) : null);
}
```

The `InvokerTransformer` invokes the specified method and returns the result as the result of transformation. In this
case, I want the result of the `getId()` method. The `CollectionUtils.collect()` method runs the transformer on each
element in the incoming collection and creates a new collection containing the transformed results. Short and sweet.
