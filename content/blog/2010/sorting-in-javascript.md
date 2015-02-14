title=Sorting in JavaScript
date=2010-04-12
type=post
tags=blog,java
status=published
~~~~~~
Every now and then you need to sort something in JavaScript and though, it's not all that hard to do, it is not the best-documented feature. Here is a quick little summary of how to do it.

You call the sort function of an array with your comparator. A JavaScript comparator is just a function that returns -1, 0, or 1 depending on whether a is less than b, a is equal to b, or a is greater than b:

```javascript
myarray.sort(function(a,b){
    if(a < b){
        return -1;
    } else if(a == b){
        return 0;
    } else { // a > b
        return 1;
    }
});
```

This is just an example, your function can base the comparison on whatever you want, but it needs to return -1,0,1. Say you had a set of custom JavaScript objects that you want sorted by age:

```javascript
var people = [{name:'Bob',age:21}, {name:'Fred',age:34}, {name:'Dan',age:19}];
```

You could easily sort them using

```javascript
people.sort(function(a,b){
    if(a.age < b.age){
        return -1;
    } else if(a.age == b.age){
        return 0;
    } else { // a > b
        return 1;
    }
});
```

Not too hard to do. It's actually very similar to the Java `Comparator` interface.
