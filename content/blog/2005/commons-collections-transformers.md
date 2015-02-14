title=Commons Collections - Transformers
date=2005-01-06
type=post
tags=blog,java
status=published
~~~~~~
Continuing the example from my discussion of [Predicates](/2005/01/jakarta-commons-collections-predicates.html),
I would not like to take a quick look at Transformers. According to the JavaDocs:

> A Transformer defines a functor interface implemented by classes that transform one object into another.

They are very useful when you need to preform an action of some sort on every element, or a group of elements, in a
collection. From the example in the previous article, we have a list of Integers, from which we needed to extract the
even Integers with a value greater than 5. We developed two Predicates and used an `AndPredicate` to join them
and achieve our goal. Now, I would like to take the same list, repeated below and perform some transformations on it.

```java
List<Integer> numbers = new ArrayList<Integer>();
for(int i=0; i<10; i++){
    numbers.add(i);
}
```

We want to take the values that are accepted by the predicates (even values greater than 5) and then multiply those
by an arbitrary value, say 10. You could use an Iterator to loop through the list of filtered numbers and multiply each
value by 10 or you could use a Transformer and have a reusable means of doing the same action.

> _Note:_ For this example, I am going to use the test-driven development approach. The unit tests will use the [JUnit](http://junit.org/) framework.

Let's write a test case for a `MultiplyTransformer` that will do this. Start with the basic test shell...

```java
public class MultiplyTransformerTest extends TestCase {
    private MultiplyTransformer multiplyTransformer;

    protected void setUp(){
        this.multiplyTransformer = new MultiplyTransformer();
    }

    protected void tearDown(){
        this.multiplyTransformer = null;
    }
}
```

We will write the empty class for our Transformer as well.

```java
public class MultiplyTransformer implements Transformer {
    public Object transform(Object input){}
}
```

We will want to test the `transform(Object)` method for the case when the input is null, not an Integer, and
the expected value. For this we will write three test methods. First we will test the case when the input is
`null`. We will want to handle this gracefully and return an Integer value of zero in this case.

```java
public void testTransform_NullInput(){
    assertEquals(new Integer(0), multiplyTransformer.transform(null));
}
```

Next we will test for the case when the input is not an Integer. This case should throw an `IllegalArgumentException`
as all elements should be Integers in our system.

```java
public void testTransform_InvalidInputClass(){
    try {
        multiplyTransformer.transform(new Long(120030));
        fail("Expected Exception was not thrown!");
    } catch(IllegalArgumentException iae){
        assertNotNull(iae);
    }
}
```

Then, finally we test the case when an Integer is passed as the input. This case should return an Integer that is a
specified multiple of the original integer.

```java
public void testTransform_Integer(){
    Integer input = new Integer(5);
    Integer result = new Integer(50);
    int mult = 10;
    multiplyTransform.setMultiplier(mult);
    assertEquals(result,multiplyTransform.transform(input));
}
```

Now all of our test cases have been written and we can start coding the transformer itself. We already have the
shell and we also know from the last test that we will need a setter for the multiplier.

```java
public class MultiplyTransformer implements Transformer {
    private int multiplier;

    public MultiplyTransformer(){this.multiplier = 1;}

    public MultiplyTransformer(int multiplier){this.multiplier = multiplier;}

    public void setMultiplier(int multiplier){this.multiplier = multiplier;}

    public Object transform(Object input){
        Integer result = null;
        if(input != null &amp;&amp; input instanceof Integer){
            int inVal = ((Integer)input).intValue();
            result = new Integer(multiplier * intVal);
        } else if(input == null){
            result = new Integer(0);
        } else {
            throw new IllegalArgumentException("Not an Integer!");
        }
        return(result);
    }
}
```

Not too bad and when we run our test cases, everything comes up green. We now have our transformer and its unit
test. If we need to modify the transformer in any way, will want to run the tests to ensure that everything remains in
working order. To use our transformer to solve our task (using the 'numbers' list) we need to run the predicates and
then apply the transformer to the results.

```java
Predicate evenInt = new EvenIntegerPredicate();
Predicate greater = new GreaterThanPredicate(5);
Predicate andPred = new AndPredicate(evenInt,greater);

// get the list of even numbers greater than 5
CollectionUtils.filter(numbers,andPred);

Transformer mult = new MultiplyTransformer(10);

// multiply all values by 10
CollectionUtils.transform(numbers,mult);
```

And there you have it. But that is not our stopping point. We modified those values so that they could be mapped
and converted to other objects. Assume that we have a Map containing objects of class `SomethingUseful` mapped to
keys that correspond to our multiplied even numbers. We need to replace the even Integers in the list with their
appropriate instances of `SomethingUseful`. The Collections API comes to the rescue again with the `MapTransformer`.
This Transformer is created with a Map and when it is applied, the input objects are replaced by the object keyed to the
input value. We can also combine the two transformers using a `ChainedTransformer`.

```java
Predicate evenInt = new EvenIntegerPredicate();
Predicate greater = new GreaterThanPredicate(5);
Predicate andPred = new AndPredicate(evenInt,greater);

// get the list of even numbers greater than 5
CollectionUtils.filter(numbers,andPred);

Transformer mult = new MultiplyTransformer(10);

Map items = new HashMap();
items.put(new Integer(60),new SomethingUseful(60));
items.put(new Integer(80),new SomethingUseful(80));
items.put(new Integer(100),new SomethingUseful(100));
Transformer map = MapTransformer.getInstance(items);

Transformer chain = ChainedTransformer.getInstance(mult,map);

// apply the transformers
CollectionUtils.transform(numbers,chain);
```

which gives us our complete system. Our list of Integers is filtered so that only even values greater than 5
remain. Those values are then multiplied by 10 and converted to their appropriate `SomethingUseful`
implementations. Transformers are a powerful tool when working with collections and there are many predefined
implementations available. Also, as you can see, it's pretty easy to write your own. As with Predicates, if you write
them carefully, they can be reused in multiple projects, which is always a good thing.
