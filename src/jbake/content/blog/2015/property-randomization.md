title=Property Randomization for Testing
date=2015-05-06
type=post
tags=blog,groovy,vanilla
status=published-date
~~~~~~
Unit tests are great, but sometimes you end up creating a lot of test objects requiring data, such as DTOs and domain objects. Generally, I have always come up with movie quotes or other interesting content for test data. Recently, while working on a Groovy project, I thought it would be interesting to have a way to randomly generate and populate the data for these objects. The randomization would provide a simpler approach to test data as well as providing the potential for stumbling on test data that would break your code in interesting ways.

My [Vanilla](http://github.com/cjstehno/vanilla) project now has a `PropertyRandomizer` class, which provides this property randomization functionality in two ways. You can use it as a builder or as a DSL.

Say you have a `Person` domain class, defined as:

```groovy
@ToString
class Person {
    String name
    Date birthDate
}
```

You could generate a random instance of it using:

```groovy
def rando = randomize(Person).typeRandomizers( (Date):{ new Date() } )
def instance = rando.one()
```

Note, that there is no default randomizer for `Date` so we had to provide one. The other fields, `name` in this case would be randomized by the default randomizer.

The DSL usage style for the use case above would be:

```groovy
def rando = randomize(Person){
    typeRandomizers( 
        (Date):{ new Date() } 
    )
}
def instance = rando.one()
```

Not really much difference, but sometimes a DSL style construct is cleaner to work with.

What if you need three random instances for the same class, all different? You just ask for them:

```groovy
def instances = rando.times(3)

// or 

instances = rando * 3
```

The multiplication operator is overridden to provide a nice shortcut for requesting multiple random instances.

You can customize the randomizers at either the type or property level or you can configure certain properties to be ignored by the randomization. This allows for nested randomized objects. Say your `Person` has a new `pet` property.

```groovy
@ToString
class Person {
    String name
    Date birthDate
    Pet pet
}

@ToString
class Pet {
    String name
}
```

You can easily provide randomized pets for your randomized people:

```groovy
def rando = randomize(Person){
    typeRandomizers( 
        (Date):{ new Date() },
        (Pet): { randomize(Pet).one() }
    )
}
def instance = rando.one()
```

I have started using this in some of my testing, at it comes in pretty handy. My Vanilla library is not yet available via any public repositories; however, it will be soon, and if there is expressed interest, I can speed this up.