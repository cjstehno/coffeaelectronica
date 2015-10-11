title=Copying Data with ObjectMappers
date=2015-10-10
type=post
tags=blog,groovy,vanilla
status=published
~~~~~~
When working with legacy codebases, I tend to run into a lot of scenarios where I am copying data objects from one format to another while an API is in transition or due to some data model mismatch. 

Suppose we have an object in one system - I am using Groovy because it keeps things simple, but it could be Java as well:

```groovy
class Person {
    long id
    String firstName
    String lastName
    LocalDate birthDate
}
```

and then you are working with a legacy (or external) API which provides similar data in the form of:

```groovy
class Individual {
    long id
    String givenName
    String familyName
    String birthDate
}
```

and now you have to integrate the conversion from the old/external format (`Individual`) to your internal format (`Person`).

You can write the code in Java using the `Transformer` interface from [Apache Commons Collections](http://commons.apache.org/proper/commons-collections/), which ends up with something like this:

```java
public class IndividualToPerson implements Transformer<Individual,Person>{
    
    public Person transform(Individual indiv){
        Person person = new Person();
        person.setId( indiv.getId() );
        person.setFirstName( indiv.getGivenName() );
        person.setLastName( indiv.getFamilyName() );
        person.setBirthDate( LocalDate.parse(indiv.getBirthDate()) );
        return person;
    }
}
```

I wrote a blog post about this many years ago ([Commons Collections - Transformers](http://coffeaelectronica.com/blog/2005/commons-collections-transformers.html)); however, if you have more than a handful of these conversions, you can end up handwriting a lot of the same code over and over, which can be error prone and time consuming. Even switching the code above to full-on Groovy does not really save you much, though it is better:

```java
class IndividualToPerson implements Transformer<Individual,Person>{
    
    Person transform(Individual indiv){
        new Person(
            id: indiv.id,
            firstName: indiv.givenName,
            lastName: indiv.familyName,
            birthDate: LocalDate.parse(indiv.birthDate)
        )
    }
}
```

What I came up with was a simple mapping DSL which allows for straight-forward definitions of the property mappings in the simplest code possible:

```groovy
ObjectMapper inividualToPerson = mapper {
    map 'id'
    map 'givenName' into 'firstName'
    map 'familyName' into 'lastName'
    map 'birthDate' using { d-> LocaleDate.parse(d) }
}
```

which builds an instance of `RuntimeObjectMapper` which is stateless and thread-safe. The `ObjectMapper` interface has a method `copy(Object source, Object dest)` which will copy the properties from the source object to the destination object. Your transformation code ends up something like this:

```groovy
def people = individuals.collect { indiv->
    Person person = new Person()
    individualToPerson.copy(indiv, person)
    person
}
```

or we can use the `create(Object, Class)` method as:

```groovy
def people = individuals.collect { indiv->
    individualToPerson.create(indiv, Person)
}
```

which is just a shortcut method for the same code, as long as you are able to create your destination object with a default constructor, which we are able to do.

There is also a third, slightly more useful option in this specific collector case:

```groovy
def people = individuals.collect( individualToPerson.collector(Person) )
```

The `collector(Class)` method returns a `Closure` that is also a shortcut to the conversion code shown previously. It's mostly syntactic sugar, but it's nice and clean to work with.

Notice the 'using' method - this allows for conversion of the source data before it is set into the destination object. This is one of the more powerful features of the DSL. Consider the case where your `Person` class has an embedded `Name` class:

```groovy
class Person {
    long id
    Name name
    LocalDate birthDate
}

@Canonical
class Name {
    String first
    String last
}
```

Now we want to map the name properties into this new embedded object rather than into the main object. The mapper DSL can do this too:

```groovy
ObjectMapper individualToPerson = mapper {
    map 'id'
    map 'givenName' into 'name' using { p,src-> 
        new Name(src.givenName, src.familyName)
    }
    map 'birthDate' using { d-> LocaleDate.parse(d) }
}
```

It's a bit odd since you are mapping two properties into one property, but it gets the job done. The conversion closure will accept up to three parameters (or none) - the first being the source property being converted, the second is the source object instance and the third is the destination object instance. The one thing to keep in mind when using the two and three parameter versions is that the order of your property mapping definitions may begin to matter, especially if you are working with the destination object.

So far, we have been talking about runtime-based mappers that take your configuration and resolve your property mappings at runtime. It's reasonably efficient since it doesn't do all that much, but consider the case where you have a million objects to transform; those extra property mapping operations start to add up - that's when you go back to hand-coding it unless there is a way to build the mappers at compile time rather than run time...

There is. This was something I have really wanted to get a hold of for this project and others; the ability to use a DSL to control the AST transformations used in code generation... or, using the mapper DSL in an annotation to create the mapper class at compile time so that it is closer to what you would have hand-coded yourself (and also becomes a bit more performant since there are fewer operations being executed at runtime).

Using the static approach is simple, you just write the DSL code in the `@Mapper` annotation on a method, property or field:

```groovy
class Mappers {

    @Mapper({
        map 'id'
        map 'givenName' into 'firstName'
        map 'familyName' into 'lastName'
        map 'birthDate' using { d-> LocaleDate.parse(d) }    
    })
    static final ObjectMapper personMapper(){}
}
```

When the code compiles, a new implementation of `ObjectMapper` will be created and installed as the return value for the `personMapper()` method. The static version of the DSL has all of the same functionality of the dynamic version except that it does not support using `ObjectMappers` direction in the `using` command; however, a workaround for this is to use a closure.

Object property mapping/copying is one of those things you don't run into all that often, but it is useful to have a simple alternative to hand-writing the code for it. Both the dynamic and static version of the object mappers discussed here are available in my [Vanilla](http://stehno.com/vanilla) library.