title=Commons Collections - Predicates
date=2005-01-04
type=post
tags=blog,java
status=published
~~~~~~
Nestled in the [Jakarta Commons](http://commons.apache.org/) project is a monstrosity called the
[Jakarta Commons - Collections](http://commons.apache.org/collections) API. It contains a wealth of extensions to the
standard collections as well as new collections and collection-related utilities. To try and document the use of the
whole API would be a good topic for a book (and there are a few
[on Amazon](http://www.amazon.com/s/ref=br_ss_hs/103-3887221-2235807?platform=gurupa&amp;url=index%3Dblended&amp;keywords=jakarta+commons&amp;Go.x=0&amp;Go.y=0&amp;Go=Go)).
I am going to cover one of my favorite interfaces from this API, the Predicate, and its implementations.

From the documentation for the Predicate interface:

> A Predicate defines a functor interface implemented by classes that perform a predicate test on an object. Predicate
instances can be used to implement queries or to do filtering.

That sums it up pretty well; but, how do you use it?

## General Usage

Let's say we have an `ArrayList` containing ten `Integer` objects as follows:

```java
List<Integer>; numbers = new ArrayList<Integer>();
for(int i=0; i<10; i++){
    numbers.add(i);
}
```

Now let's say that for some reason only the even numbers in the list are relevant and that the rest can be ignored
and/or removed. There are three main approaches to doing this with Predicates. First, you can select all even numbers
from the list into a new `Collection`. Second, you can filter the list so that all non-even numbers are removed
from the list. Third, you can create a predicated list that will only store even numbers.

Before we go any farther, we need a Predicate to work with. The Predicate interface is pretty simple, containing only a single method to
implement, so I will just show it below:

```java
public class EvenIntegerPredicate implements Predicate {
    public boolean evaluate(Object obj){
        boolean accept = false;
        if(obj instanceof Integer){
            accept = ((Integer)obj).intValue() % 2 == 0;
        }
        return(accept);
    }
}
```

The `evaluate()` method is called for each element to be tested. In this case, the object must be an Integer
implementation and have an even value to be accepted.

## Select the Even Numbers

This case uses the `select(Collection,Predicate)` method of the `CollectionUtils`
class. This method selects all elements from input collection which match the given predicate into an output collection.

```java
Predicate evenPred = new EvenIntegerPredicate();
Collection nums = CollectionUtils.select(numbers,evenPred);
```

which will yield a new collection containing only the even numbers from the original list while the original list
will remain unchanged.

## Filter the Collection

This next method is good when you are able to reuse the original collection once it is filtered. The `CollectionUtils.filter(Collection,Predicate)`
method filters the collection by testing each element and removing any that the predicate rejects.

```java
CollectionUtils.filter(numbers,new EvenIntegerPredicate());
```

Once again, only the even values are preserved; however, this time, the original collection is maintained.

## Predicated List

In the third approach, we use a method that allows new values to be added to the list and tested at the same time. This
approach is best when you have control over the original collection and could possibly add new elements to the collection.
For this we use the `predicatedList(List,Predicate)` method of the `ListUtils` class which returns a predicated list
backed by the given list. Only values that are accepted by the predicate will be added to the list any other values
will cause an `IllegalArgumentException` to be thrown.

```java
List<Integer> list = new ArrayList<Integer>();
Predicate evenPred = new EvenIntegerPredicate();
List predList = ListUtils.predicatedList(list,evenPred);
predList.add(new Integer(2));
predList.add(new Integer(4));
predList.add(new Integer(6));
predList.add(new Integer(8));
predList.add(new Integer(10));

// this next one will throw an IllegalArgumentException
predList.add(new Integer(11));
```

The resulting list will contain only the even values (you should be sure to use the predicated list (predList) not
the original backing list.

## Combining Predicates

I will take this discussion one step farther and pose the question, "what if you only want even integers greater than 5?"
Your first thought might be to re-write the `EvenIntegerPredicate` to handle this, but a better approach would be to
write a new predicate that only accepts values greater than a specified value.

```java
public class GreaterThanPredicate implements Predicate {
    private int value;

    public GreaterThanPredicate(int value){
        this.value = value;
    }

    public boolean evaluate(Object obj){
        boolean accept = false;
        if(obj instanceof Integer){
            accept = ((Integer)obj).intValue() &gt; value;
        }
        return(accept);
    }
}
```

Now we have a predicate that matches even numbers and a predicate that matches numbers greater than a specified
number... how do we combine them? Two Predicate implementations jump to mind, `AllPredicate` and `AndPredicate`. The
`AllPredicate` is built with an array of Predicates that must all evaluate to true for the containing predicate to be
true. The `AndPredicate`, which we will use here, takes two predicates as arguments and returns true if both evaluate to true.

```java
Predicate evenInt = new EvenIntegerPredicate();
Predicate greater = new GreaterThanPredicate(5);
Predicate andPred = new AndPredicate(evenInt,greater);
```

which could be used in any of the previous examples to accept only even numbers greater than 5.

## Conclusion

Predicates are a powerful tool for object filtering and searching. They are fairly simple to
learn and if written properly, very reusable. At first they may feel a bit like excess code, but once you find yourself
using the same predicate in multiple projects, you will see the benefits.
