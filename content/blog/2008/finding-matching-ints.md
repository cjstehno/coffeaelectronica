title=Spring Deserialized Object Factory
date=2008-02-02
type=post
tags=blog,java,spring
status=published
~~~~~~
Okay, here is a little question I have taken to asking in the interviews we have been giving lately.

The questions is as follows:

> Assume that you have an array of `int`s with exactly two of them being equivalent. Write a method to return the `int` that is duplicated.

Since most candidates have been failing this one for the most part, I have started compiling a catalog of all the possible and sensible
solutions to the problem. It's kind of a fun little project. Below are some of the implementations I have come up with. When I came
across this question, it did not have an answer provided. My first shot at it was the following, but without the array sorting. Oops,
without the sorting it works as long as they are next to each other, hence the need for sorting:

```java
public int findMatching(final int[] array){
    Arrays.sort(array);
    for(int i=0; i<array.length-1; i++){
        if(array[i] == array[i+1]) return array[i];
    }
    throw new IllegalArgumentException("Array contains no matches!");
}
```

The second implementation was something I had as a first thought but could not remember the exact functionality of the `Set` `add(Object)`
method, which is kind of important in this case.

```java
public int findMatching(final int[] array){
    final Set<Integer> set = new HashSet<Integer>(array.length);
    for(int i : array){
        if(!set.add(i)) return i;
    }
    throw new IllegalArgumentException("Array contains no matches!");
}
```

The third implementation is the one most of our candidates seem to jump for, brute force, comparing every element with every other element
(we still give credit for it though):

```java
public int findMatching(final int[] array){
    for(int i=0; i<array.length; i++){
        for(int j=0; j<array.length; j++){
            if(i != j && array[i] == array[j]) return array[i];
        }
    }
    throw new IllegalArgumentException("Array contains no matches!");
}
```

You will notice that I used an `IllegalArgumentException` to denote the lack of matches. You can't really return a -1 or something like
that since your `int`s could be of any value.

I am sure that there are one or two more interesting solutions for this problem, but thought I would share what I have found. These are always fun
little code problems to play with. Yes, we are still using this in our interview process, but I am not afraid that a potential candidate will
find this since they generally don't know my name, and likewise I don't advertise what company I work for. Actually, I would probably give a
"golf clap" to the candidate that walks into our interview with a print out of this entry.

## Recursive Solution to Finding Ints Question

> Originally a separate posting on 5/28/2008

Earlier I showed a few different solutions to the problem. A candidate we had recently suggested solving it via recursion; I decided to whip up a
little recursive solution for my collection:

```java
public static int find(int[] array){
    return scan(new HashSet<Integer>(),array,0);
}

private static int scan(Set<Integer> values, int[] array, int idx){
    if(idx == array.length){
        throw new IllegalArgumentException("No match exists");
    } else if(!values.add(array[idx])){
        return array[idx];
    }
    return scan(values,array,++idx);
}
```

Note that this solution requires an additional method to perform the recursion but there are no loops. An alternate
version removes the `Set` and uses pre-sorting of the array.

```java
public static int find(int[] array){
    Arrays.sort(array);
    return scan(array,0);
}

public static int scan(int[] array, int idx){
    if(idx == array.length){
        throw new IllegalArgumentException("No match exists");
    } else if(array[idx] == array[idx+1]){
        return array[idx];
    }
    return scan(array,++idx);
}
```

This problem has really become an interesting study; as I use it as one of our tests for interview candidates I
really find it an interesting ruler to compare how various developers think.

Two interesting common threads are that most developers find the brute-force approach (double for loop) which is good but very telling, the other is that
when faced with the idea that it is possible an array may be passed in without a match, they struggle on what to do at
that point. The first solution people look at is some signal like a -1 or `null`, neither of which works. After
hinting they will come across the idea of the exception but usually want to create their own unique exception for this
method.

I think it would also be an interesting exercise to implement this problem in other languages such as Groovy, Ruby or Scala.

## Finding Matching Ints Using Regex

> Originally a separate post on 6/42008

If you are following along you may have noticed that I have been compiling a long list of solutions to this problem; I was talking
to one of my co-workers, who is not a developer but used to do some Perl hacking, and he suggested that it could be done with
regular expressions.

Lo and behold, with the help of another of my more regular-expression-ized co-workers we found this to be true:

```java
public int findDuplicate(final int[] array){
    final Pattern p = Pattern.compile(".*?(\\d+. ).*?\\1.*");
    final Matcher m = p.matcher(join(array));
    if(m.find()){
        return Integer.valueOf(m.group(1).trim());
    }

    throw new IllegalArgumentException("No match found!");
}

private String join(final int[] array){
    final StringBuilder str = new StringBuilder();
    for(final int i : array){
        str.append(i).append(" ");
    }
    return str.toString();
}
```

Granted, it's not quite as straight-forward as the other solutions, but it is a very novel approach to solving the
problem... leave it to a Perl guy. :-) I wonder what the runtime of this would be?

## Finding Duplicate Ints: PHP

> Originally a separate post on 8/13/2008

I have have come across another solution from a php developer that I recently interviewed... in php.

```php
function findInts($array){
    $out = Array();
    foreach($array as $num){
        if(array_exist($out,$num){
            return $num;
        }
        array_push($num);
    }
}
```

He also mentioned the pre-sorting approach as well in order to speed things up. I still need to fully validate the
php functions that he mentions, but it seems correct. I also didn't go into the error-case much with him, not really being a php expert myself.

## Find Matching Ints with JavaScript

> Originally separate post on 10/30/2009

I realized that I had yet to come up with a JavaScript solution so I decide a quick implementation would be fun.

```html
<html>
    <head>
        <script type="text/javascript">
            function findDup(items){
                items.sort();
                for( var i=0; i<items.length-1; i++){
                    if( items[i] == items[i+1] ) return items[i];
                }
                throw "No duplicate values!";
            }
        </script>
    </head>
    <body onload="alert( findDup( new Array( 6, 9, 2, 5, 1, 6) ) )">
    </body>
</html>
```

There is not much to it, and nothing really exciting. I also worked up quick versions using [Prototype](http://prototypejs.org) and [JQuery](http://jquery.com); however, neither one really provided any useful deviation from the standard JavaScript approach.

## Finding Duplicate Ints: Clojure

> Originally separate post on 10/23/2009

Here's a solution based on [Clojure](http://clojure.org/). I worked on the solution to this problem in a few steps, which I will share. First, I assumed that the list of numbers has been sorted:

```clojure
(defn find-dupint [intseq]
    (loop [result nil its intseq]
        (if (= result (peek its))
            result
            (recur (peek its) (pop its))
        )
    )
)
```

This function simmply iterates over the list of numbers and returns the first number whose value matches the next
number in the sequence. This code will fail if the numers are not in order and the problem definition says that they can
be in any order, so I worked up the following code to sort the numbers before looping through them:

```clojure
(defn find-dupint [intseq]
    (let [sorint (sort intseq)]
        (loop [result nil its sorint]
            (if (= result (first its))
                result
                (recur (first its) (rest its))
            )
        )
    )
)
```

Notice that I changed `(peek coll)` and `(pop coll)` to `(first coll)` and `(rest coll)`, which work on sequences rather
than stacks. I kept getting a `ClassCastException` the other way. Now all that's left is to have an exception thrown
when no duplicated number is found. The function as written returns a `nil`, which is ok for Clojure I guess but not
really the same behavior as the other versions of this code as it is defined, so I dipped into the Java
interoperability features to add some exception throwing for the finished product:

```clojure
(defn find-dupint [intseq]
    (let [sorint (sort intseq)]
        (loop [result nil its sorint]
            (if (= result (first its))
                (if (nil? result)
                    (throw (new IllegalArgumentException "No duplicate found!"))
                        result
                    )
                (recur (first its) (rest its))
            )
        )
    )
)
```

Note: the `IllegalArgumentException` does not need to be fully qualified because the `java.lang` package is imported by
default, just as it is in Java. The output from using this function is shown below:

```
1:1 user=> (find-dupint [1 2 3])
java.lang.IllegalArgumentException: No duplicate found! (repl-1:1)

1:2 user=> (find-dupint [1 2 3 2])
2
```

An intesting side-effect of this version is that you are not constrained to use ints, or even numbers for that
matter. You could actually use any `Comparable` object. (It would not be that difficult to add the ability to
really generalize the function by passing in a comparator) A few other comparable objects are tested below:

```
1:1 user=> (find-dupint ["a" "c" "b" "a"])
"a"

1:2 user=> (find-dupint [1.1 1.4 1.3 1.22 1.1])
1.1
```

I guess I should rename the function "find-dup" to reflect the broader scope than just integers. At this point,
reviewing the code, it's not all that less verbose than the Java version, but it gets the job done. I will have to come
back to this problem again as I get more experience with Clojure... I am willing to bet there is a more concise way to
achieve the same results.

## Finding Duplicate Ints: Python

> Originally separate posting on 11/19/2009

Here's a version with Python. It's actually a very interesting language and I am surprised that I have not really looked into it sooner. It has a rich set of built-in libraries and tons of extension modules. The code I came up with is pretty straight-forward:

```python
def find_dup_int(items):
    items.sort()
    for i in range(len(items)-1):
        if items[i] == items[i+1]:
            return items[i]
    raise RuntimeError('No duplicate values found!')
```

I need to find a handful of other more interesting problems to try when playing with other languages, since this one seems to look basically the same in each language I have tried. I will have to spend a little more time with Python, it feels very useful, and as you can see, not very verbose (though not to a fault).

## Finding Duplicate Ints: Ruby and Groovy

> Originally separate posting on 2/22010

### Groovy Version

```groovy
def findDups( n ){
    n.sort()
    for( i in (1..n.size())){
        if(n[i] == n[i-1]) return n[i]
    }
}
```

In this case, I am allowing null to be the value returned when no duplicate is found. It does seem to be a more
realistic value for Groovy. It's a pretty straight forward function, along the same lines as the other languages. I
thought maybe I could find some really cool feature of Groovy that would make this radically different from the Java
version... nope. It does collapse nicely down to a single line though:

```groovy
def findDups( n ){ n.sort(); for( i in (1..n.size()) ) if(n[i] == n[i-1]) return n[i] }
```

### Ruby Version

```ruby
def findDups( n )
    n.sort!()
    for i in (1..n.size())
        if(n[i] == n[i-1]) then return n[i] end
    end
    return nil
end
```

Nothing exciting in either case, but worth doing for completeness.


