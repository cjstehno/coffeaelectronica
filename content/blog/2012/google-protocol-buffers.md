title=Google Protocol Buffers
date=2012-05-13
type=post
tags=blog,javascript
status=published
~~~~~~
[Google Protocol Buffers](https://developers.google.com/protocol-buffers/) have been around for a while; however, I have
only started working with them recently. We use them at work for well-defined communication protocols between Java and
native systems. I was always under the impression that they were a lot more complicated than what they really are. I
decided to collect my working knowledge into a quick little tutorial.

The best way to describe Google Protocol Buffers is right from the blurb on the project web site:

> Protocol buffers are Google's language-neutral, platform-neutral, extensible mechanism for serializing structured data -
think XML, but smaller, faster, and simpler. You define how you want your data to be structured once, then you can use
special generated source code to easily write and read your structured data to and from a variety of data streams and
using a variety of languages - Java, C++, or Python.

As an example to play with let's consider a simple set of messages for searching a remote database for user information.
We will need a `Person` data structure to hold the information being transferred:

```
Person
    id - the id of the user
    name - the full name of the user
    age - the age of the user
```

We will also need a container for the query itself, let's call it a `PersonQuery`, which will contain a prototype Person
with the search criteria.

```
PersonQuery
    prototype - a partially complete Person used as search criteria
```

And, finally we will need a response structure, `PersonQueryResults` which will contain all the matching Person(s) in the
external data source.

```
PersonQueryResults
    prototype - the search criteria
    results - the resulting Person(s) found
```

That should be enough to work with. Next we need to create the actual protocol definition GPB protocols are defined in a
simple well-documented text format so we create a text file called `PersonMessages.proto` in your favorite text editor.

All three of our structures are considered "message" structures in GPB. The Person would be defined as:

```
message Person {
    optional int32 id = 1;
    optional string name = 2;
    optional int32 age = 3;
}
```

First, we see all three fields are listed as "optional" since these fields do not have to be present. The type
of each field is defined next (int32 and string in our case) and then the field name. After each field is an index
number. These index numbers should not be changed once your protocol is in use as they are used in generating the
compiled objects used by the various supported platforms to determine serialization and deserialization
information.

We can similarly work up the other two structures:

```
message PersonQuery {
    required Person prototype = 1;
}

message PersonQueryResults {
    required Person prototype = 1;
    repeated Person result = 2;
}
```

Note, that the fields in these two are "required", which as it sounds, means that these fields must be filled
in. There are a few other modifiers and a bunch of field types you can use; you should definitely check out the
documentation if you are interested in going deeper.

Since we will be working with Java, we want to specify an "option" at the top of the file:

```
option java_package = "com.stehno.proto";
```

This tells the GPB compiler to put the generated classes in the specified package. Also good to note is that
the wrapper class for the protocol structures will have the same name as the proto file (though I think it will also
convert underscore-separated names as well).

You can either use the command line compiler or, if you are using Maven, you can create a project for all of your
protocols and build them with the "maven-protoc-plugin" plugin. The source for this post will have the complete proto
file and pom.xml file.

Once you have the message classes, you can use the provided Builders to create your message objects for a query:

```java
PersonMessages.Person.Builder personBuilder = PersonMessages.Person.newBuilder();
personBuilder.setName( "Chris" );

PersonMessages.PersonQuery.Builder personQueryBuilder = PersonMessages.PersonQuery.newBuilder();
personQueryBuilder.setPrototype( personBuilder );

PersonMessages.PersonQuery personQuery = personQueryBuilder.build();
byte[] bytes = personQuery.toByteArray();
```

This code creates a Person with `name="Chris"`, the prototype of the person we are searching for, and then
creates a PersonQuery with that Person. The resulting Builder is built with the build() method which creates the actual
message object. Then all you need to do is call `toByteArray()` to render the message as bytes suitable for transfer or
storage.

And, on the other end of the pipeline you can deserialize the message simply by parsing the bytes:

```java
PersonMessages.PersonQuery rebuiltQuery = PersonMessages.PersonQuery.parseFrom( bytes );
String name = rebuiltQuery.getPrototype().getName();
```

This simple serialization and deserialization method could also be useful for long-term object storage, though GPB
works best when the message structures do not change much or often (though there are ways of minimizing the effect of
protocol changes).

To finish up, there are some design concerns you need to be aware of when working with GPB. The serialized message bytes
have no concept of what type of message created them, meaning you need to know what the message is in order to parse it
with the correct message object class. You can work-around this by wrapping all messages in an "envelope message" that
defines a type and the content bytes of that type. Another approach, if available, is to provide message type information
in any header information provided by your transport mechanism.

GPB messages are best for static or slowly-changing protocols, if your needs are not well-defined or may change drastically,
you may not want to use GPB.

So, that's all there is. The GPB documentation is pretty straight-forward and provides good suggestions for message style and design.

> The source code for this post can be found at: [https://github.com/cjstehno/coffeaelectronica](https://github.com/cjstehno/coffeaelectronica)
