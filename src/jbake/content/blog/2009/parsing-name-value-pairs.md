title=Parsing Name Value Pairs
date=2009-08-15
type=post
tags=blog,java
status=published
~~~~~~
A coding problem I have run into numerous times has been the relatively simple parsing of a sequence of name/value
pair data.

Say you have something like the following:

```java
String data = "alpha=100,bravo=200,charlie=300";
```

which you need to parse into a `Map<string,Integer>` for whatever reason. Generally I have taken the brute force (i.e. lazy solution)
approach of splitting the string by the grouping token (comma in this case) and then looping through those results and
splitting each by the separation token (equals in this case), something like the code shown below:

```java
final String[] nvps = StringUtils.split( data, ',' );
for( final String nvp : nvps ){
    final String[] nv = StringUtils.split( nvp, '=' );
    map.put(nv[0], Integer.valueOf(nv[1]) );
}
```

It's not a bad solution to the problem, not very efficient, but not bad. Most of the time when I have run into the
need for this, efficiency has not really been much of an issue. Sidenote: I used `org.apache.commons.lang.StringUtils.split(String,char)`
due to how it handles nulls and blanks safely. I recently thought about a more efficent approach to this problem. With a
little help from regular expressions, you can refactor the above code to:

```java
final String[] parts = data.split("=|,");
for(int i=0; i<parts.length; i+=2){
    map.put( parts[i], Integer.valueOf(parts[i+1]) );
}
```

which does the same work in almost negligible time when compared to the original. I ran some simple benchmark tests
with larger lists of name/value pairs and found that even as the first method took about 32ms, the second method still
registered as taking 0ms. I would say that it's an improvement. Also, with the second approach you would not even really
need to have different tokens for the group and the separator since it's really just taking two values at a time and
then skipping to the next pair. Now, generally I am of the opinion that premature optimization is evil; however, I don't
really count things like this as optimization, but rather as "better coding".

As a followup, you can also write a version of the second method using an alternate form of the `StringUtils.split(...)`
method, with no noticeable loss of efficiency:

```java
final String[] parts = StringUtils.split(data,",=");
for(int i=0; i<parts.length; i+=2){
    map.put( parts[i], Integer.valueOf(parts[i+1]) );
}
```

which kind of combines the best of both worlds. You get more efficient and simple code without the need for regular
expressions and with the extra safety features provided by `StringUtils`.
