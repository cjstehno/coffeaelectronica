title=When GStrings Attack
date=2012-03-28
type=post
tags=blog,javascript
status=published
~~~~~~
I have run into this behavior a few times now, so I decided to dig into it a bit. Basically, if you have a map using "string" keys in [Groovy](http://groovy.codehaus.org), they can provide unexpected (or at least non-intuitive results).

Groovy has two object types of "string"... the standard Java String, which is denoted by single-quotes, and the dynamic GString, which is denoted by double-quotes _(Note: the current version of the documentation has a nice outline of [Strings vs GStrings](http://groovy.codehaus.org/Strings+and+GString)). From a developer perspective these generally seem interchangable other than the dynamic nature of GStrings; however, there is a more fundamental difference when you are working with hashes (Sets and Maps) since these are not the same class.

Let's consider the following two strings:

```groovy
def str = 'foo2'<br/>def gstr = "foo${1+1}"
```

Yes, the 1+1 is a bit contrived, but it's where the fun comes in. If you check them for equality:

```groovy
println( str == gstr )<br/>println( str.equals( gstr ) )
```

The first line (==), will print "true", while the second line (equals) will print "false". But, wait, there's more...

```groovy
println( str.hashCode() == gstr.hashCode() )
```

will print "false". They are two different instances of two different classes, lightly hidden by a layer of similarity. As a developer, you can get used to not thinking about the differences, especially when they are not always visible. Consider the case when both strings have the same non-dynamic content:

```groovy
def str = 'foo2'
def gstr = "foo2"

println( str == gstr )
println( str.equals( gstr ))
println( str.hashCode() == gstr.hashCode() )
```

In this scenario, all three lines print "true". So really what it comes down to is the cases when there is actually dynamic content in the GString. These will be considered differently than a seemingly equivalent static string. This is can be a dangerous difference when working with maps:

```groovy
def map = [:]
map.put( 'foo2', 1 )
map.put( "foo${1+1}" as String, 2 )
map.put( "foo${1+1}", 3 )

println "map.size() -&gt; ${map.size()}"
map.each {k,v-> println "$k = $v" }

println map['foo2'] // 2
println map["foo2"] // 2
println map["foo${1+1}"] // 2
```

If you add a similar string in three different ways (static string, GString cast as String, and GString), you will see that it results in:

```groovy
map.size() -> 2
foo2 = 2
foo2 = 3
2
2
2
```

You end up with two unique entries in the map, though only one of them is accessible directly.

I don't really feel that this is a bug in Groovy; however, more of an unexpected behavior that can play against what a developer might feel is intuitive (e.g. a string is a string). This is just something to beware of while developing in Groovy.
