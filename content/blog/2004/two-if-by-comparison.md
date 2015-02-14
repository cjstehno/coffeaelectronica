title=Two, If By Comparison
date=2004-10-14
type=post
tags=blog,java
status=published
~~~~~~
There are two ways to compare objects, directly if they implement the `java.lang.Comparable` interface, and
indirectly using an implementation of the `java.util.Comparator` interface.

## java.lang.Comparable

The `Comparable` interface defines only a single method signature, the `compareTo()`
method that takes as a parameter the object that the implementing object is being compared to. It returns a negative
integer, zero, or a positive integer when the implementing object is less than, equal to, or greater than the specified
object respectively. The Comparable interface is best used when your object has a logical point of comparison, such as
an order number, date stamp, or unique name and that point of comparison is fixed, meaning that the comparison is always
performed on the same comparison point (e.g. always compared by the date stamp). This also assumes that you are the
developer of the object or that you are able to extend it to allow for a `Comparable` implementation (if
    neither of these is the case, you should use the `Comparator` interface instead - see below). First you
    need a class for your object.

```java
public class Person {
    private String id, firstName, lastName;
    private int age;

    public String getId(){ return(id); }

    public String getFirstName(){ return(firstName); }

    public String getLastName(){ return(lastName); }

    public int getAge(){ return(age); }

    public void setId(String id){ this.id = id; }

    public void setFirstName(String firstName){ this.firstName = firstName; }

    public void setLastName(String lastName){ this.lastName = lastName; }

    public void setAge(int age){ this.age = age; }
}
```

Now, say you have a list of people (Person objects) and that you want to display them ordered by their last names.
You can make the Person class implement Comparable and perform the comparison based on the `lastName` property.
This comparison is very easy due to the fact that Strings implement Comparable themselves, as do many standard data
object classes (Numbers, Dates, etc.). The comparable Person class is shown below:

```java
public class Person implements Comparable {
    private String id, firstName, lastName;
    private int age;

    // ... getters/setters not shown...

    public int compareTo(Object obj){
        // cast the obj as a Person -- we are only comparing people
        Person pObj = (Person)obj;

    // compare the last names using their compareTo methods
        return( lastName.compareTo(pObj.getLastName()) );
    }
}
```

Then you can run your list of Person objects through a `Collections.sort()` method and you will have a list
of people ordered by their last names. But what if, when you display your list of people, the user wants the ability to
sort the list by the first name or the age? You could add a new property to your object called `compareBy` that
takes a parameter used to identify which property of the object will be used in the comparison. Then in your
`compareTo()` method you will need to base your comparison on the property that `compareBy` is
pointing to. This is not a very clean approach. It would be better to use a `java.util.Comparator`
implementation.

## java.util.Comparator

The Comparator interface defines two method signatures for implementation,
`compare()` that takes as parameters the two objects to be compared and `equals()` which takes an object
to be compared to the Comparator. The return value of the `compare()` method is basically the same as that for
the `compareTo()` method of the Comparable interface. The main difference between the Comparator and a Comparable
object is that Comparators perform the comparison external to the objects being compared and therefore can be reusable
over many different object types. Let's start out with our clean Person class again:

```java
public class Person {
    private String id, firstName, lastName;
    private int age;

    public String getId(){ return(id); }

    public String getFirstName(){ return(firstName); }

    public String getLastName(){ return(lastName); }

    public int getAge(){ return(age); }

    public void setId(String id){ this.id = id; }

    public void setFirstName(String firstName){ this.firstName = firstName; }

    public void setLastName(String lastName){ this.lastName = lastName; }

    public void setAge(int age){ this.age = age; }
}
```

and then define a comparator to do the work of the comparable Person we created (so we don't lose any
functionality).

```java
public class LastNameComparator implements Comparator {
    public boolean equals(Object obj){
        // we're just going to say that any LastNameComparators are equal
        return(obj instanceof LastNameComparator);
    }

    public int compare(Object obj1, Object obj2){
        // cast both objects are Person
        Person p1 = (Person)obj1;
        Person p2 = (Person)obj2;

        // compare their lastNames
        return( p1.getLastName().compareTo(p2.getLastName()) );
    }
}
```

That's all it takes. You can run your List of Person objects through the version of `Collections.sort()`
that accepts a List and a Comparator to sort your list by last name. You have gained something by doing this; you can
now change the sort criteria on the fly. Let's create a Comparator to compare by age.

```java
public class AgeComparator implements Comparator {
    public boolean equals(Object obj){
        // we're just going to say that any AgeComparators are equal
        return(obj instanceof AgeComparator);
    }

    public int compare(Object obj1, Object obj2){
        // cast both objects are Person
        Person p1 = (Person)obj1;
        Person p2 = (Person)obj2;

        // compare their ages
        int result = 0; // defaults to equal
        if(p1.getAge() > p2.getAge()){
            result = 1;
        } else if(p1.getAge() < p2.getAge()){
            result = -1;
        }

        return(result);
    }
}
```

Now you can use that instead of the `LastNameComparator` to order the Person objects by their ages. The
Comparator at first seems like more coding, but if you design your Comparators well, you will be able to reuse them in
the future, especially if you throw in some reflection. The [Jakarta Commons Collections API](http://commons.apache.org/collections)
has a set of useful Comparators, though I think one of the most useful Comparators is found in the
[Jakarta Commons Bean Utils API](http://commons.apache.org/beanutils), called the `BeanComparator`. The `BeanComparator`
uses reflection compare two objects based on the value of a specified property. Using the `BeanComparator` to perform
our comparisons would be much simpler:

```java
Collections.sort(people,new BeanComparator("lastName"));
// - or -
Collections.sort(people,new BeanComparator("age"));
```

Now how is that for simple and straight forward? So that is a basic introduction to using the Comparable and
Comparator interfaces. In general, it is better to use Comparators so that your comparison is not so tightly tied to
your implementations.
