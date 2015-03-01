title=Simple Hashing
date=2005-10-25
type=post
tags=blog,java
status=published
~~~~~~
A co-worker showed me this recently as a quick means of creating a simple hash. Say you have the following user
information:

```
Name: John Doe
YOB: 1973
```

and you need to generate a six character string identifier for the user. First, you need to convert the name to a
long by parsing it with a radix of 36.

```java
long nameId = Long.parseLong("John Doe",36);
```

Then, lets use an exclusive OR (^) to blend the name and birth year to get a new identifier value (adds a little
obfuscation):

```java
long id = 1973L ^ nameId;
```

To limit the number of characters in the final string, we need to put an upper limit on the number by taking the
modulus of the max value:

```java
long limit = Long.parseLong("zzzzzz",36);
long value = id % limit;
```

Note that we want six characters so there are six Zs. To get the string value simply convert the long to a string
using a radix of 36 (all 26 letters and all ten digits).

```java
String idstr = Long.toString(value,36);
```

Interesting.
