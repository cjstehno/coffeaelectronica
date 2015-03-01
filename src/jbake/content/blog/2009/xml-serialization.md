title=XML Serialization
date=2009-11-27
type=post
tags=blog,java
status=published
~~~~~~
Another pair of those seemingly forgotten classes in the core Java API are the `java.beans.XMLEncoder` and `java.beans.XMLDecoder`. Added in 1.4, they don't really seem to be used all that much, that I have come across anyway. It seems that whenever someone needs to convert objects into xml, they instantly reach for 3rd-party libraries rather than looking in the core API. Not to downplay other APIs, but I do think XMLEncoder and XMLDecoder deserve consideration, especially when you simply need to export data objects in a simple and repeatable manner.

First off, we need something to work with, how about a nice Person class:

```java
public class Person {
    private String firstName, lastName;
    private short age;

    public Person(){ super(); }

    public Person(final String firstName, final String lastName, final short age){
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public String getFirstName() { return firstName; }

    public void setFirstName(final String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }

    public void setLastName(final String lastName) { this.lastName = lastName; }

    public short getAge() { return age; }

    public void setAge(final short age) { this.age = age; }

    @Override
    public String toString() { return firstName+" "+lastName+" ("+age+")"; }
}
```

It's nothing special, just first name, last name and age with getters and setters. I also overrode the `toString()` method so we can get something useful from it. Now say you are in a situation where you need to quickly convert objects of the Person class to a format usable by another client (maybe another programming language); as much trouble as XML can be a times, it is still a good interoperability choice. Converting a collection of Person objects to XML is trivial:

```java
final Collection<Person> people = new LinkedList<Person>();
people.add( new Person("Joe","Smith",(short) 32) );
people.add( new Person("Abe","Ableman",(short) 54) );
people.add( new Person("Cindy","Lindy",(short) 42) );

final XMLEncoder encoder = new XMLEncoder( outputStream );
encoder.writeObject(people);
encoder.close();
```

Which will generate the following XML in the given output stream:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<java version="1.6.0_13" class="java.beans.XMLDecoder">
    <object class="java.util.LinkedList">
        <void method="add">
            <object class="foo.Person"><void property="age"><short>32</short></void>
                <void property="firstName"><string>Joe</string></void>
                <void property="lastName"><string>Smith</string></void>
            </object>
        </void>
        <void method="add">
            <object class="foo.Person">
                <void property="age"><short>54</short></void>
                <void property="firstName"><string>Abe</string></void>
                <void property="lastName"><string>Ableman</string></void>
            </object>
        </void>
        <void method="add">
            <object class="foo.Person">
                <void property="age"><short>42</short></void>
                <void property="firstName"><string>Cindy</string></void>
                <void property="lastName"><string>Lindy</string></void>
            </object>
        </void>
    </object>
</java>
```

Yeah, it's not pretty and it's a bit on the verbose side, but it was easy to produce and easy to read. Now, if you want to read that back into Java objects, it it equally as simple:

```java
final XMLDecoder decoder = new XMLDecoder( inputStream );
final Collection<Person> persons = (Collection<Person>)decoder.readObject();
decoder.close();
```

which will give you back copies of your original objects. If you were to call `toString()` on the resulting
collection you would get:

```
[Joe Smith (32), Abe Ableman (54), Cindy Lindy (42)]
```

Exactly what you put in. Obviously, this is not the best approach for serializing and deserializing data, but it's another tool in the tool box and I am always surprised by how few developers actually know that they are available.
