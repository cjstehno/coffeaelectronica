title=Lazy Immutables
date=2015-09-23
type=post
tags=blog,groovy,vanilla
status=published
~~~~~~
A co-worker and I were discussing the Groovy `@Immutable` annotation recently where I was thinking it would be useful if it allowed you to work on the object as a mutable object until you were ready to make it permanent, and then you could "seal" it and make it immutable. This would give you a bit more freedom in how the object is configured - sometimes the standard immutable approach can be overly restrictive.

Consider the case of of an immutable `Person` object:

```groovy
@Immutable
class Person {
    String firstName
    String middleName
    String lastName
    int age
}
```
With `@Immutable` you have to create the object all at once:

```groovy
def person = new Person('Chris','J','Stehno',42)
```
and then you're stuck with it. You can create a copy of it with one or more different properties using the `copyWith` method, but you need to specify the `copyWith=true` in the annotation itself, then you can do something like:

```groovy
Person otherPerson = person.copyWith(firstName:'Bob', age:50)
```
I'm not sure who "Bob J Stehno" is though. With more complicated immutables, this all at once requirement can be annoying. This is where the `@LazyImmutable` annotation comes in (part of my [Vanilla - Core library](http://stehno.com/vanilla/)). With a similar `Person` class:

```groovy
@LazyImmutable @Canonical
class Person {
    String firstName
    String middleName
    String lastName
    int age
}
```
using the new annotation, you can create and populate the instance over time:

```groovy
def person = new Person('Chris')
person.middleName = 'J'
person.lastName = 'Stehno'
person.age = 42
```
Notice that the `@LazyImmutable` annotation does not apply any other transforms (as the standard `@Immutable` does). It's a standard Groovy object, but with an added method: the `asImmutable()` method is injected via AST Transformation. This method will take the current state of the object and create an immutable version of the object - this does imply that the properties of lazy immutable objects should follow the same rules as those of the standard immutable so that the conversion is determinate. For our example case:

```groovy
Person immutablePerson = person.asImmutable()
```
The created object is the same immutable object as would have been created by using the `@Immutable` annotation and it is generated as an extension of the class you created so that it's type is still valid. The immutable version of the object also has a useful added method, the `asMutable()` method is used to create a copy of the original mutable object.

```groovy
Person otherMutable = immutablePerson.asMutable()
```
It's a fairly simple helper annotation, but it just fills one of those little functional gaps that you run into every now and then. Maybe someone else will find it useful.