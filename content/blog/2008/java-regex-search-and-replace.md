title=Java Regex Search and Replace
date=2008-08-12
type=post
tags=blog,java
status=published
~~~~~~
For many years, I felt that there was nothing "regular" about Regular Expressions, but lately I have been warming
up to them a bit. The [QuickRex](http://www.bastian-bergerhoff.com/eclipse/features/web/QuickREx/toc.html) Eclipse plug-in
has really helped make them easier to manage, but that's not what this post is about.

I recently needed to do a regex-based search and replace operation to convert all the html entities in a string to their
actual character equivalents, basically unescape all the entities in an html string (don't ask why). With a little regex
and a little searching documentation browsing I found that it is very easy to do. Start out with the pattern, which should
be a static class member (it is thread-safe once created):

```java
private static final Pattern entityPattern = Pattern.compile("(&amp;[a-z]*;)");
```

The pattern will match any html entity, which have the form `&name;`. Next we need the search and replace code:

```java
private String unescapeEntities(final String html){
    final StringBuffer buffer = new StringBuffer();
    final Matcher matcher = entityPattern.matcher(html);
    while (matcher.find()) {
        matcher.appendReplacement(
            buffer,
            StringEscapeUtils.unescapeHtml(matcher.group())
        );
    }
    matcher.appendTail(buffer);
    return buffer.toString();
}
```

Your `StringBuffer` will end up with the replaced content of your string. The `StringEscapeUtils` class is from the
[Jakarta Commons - Lang](http://commons.apache.org/lang) API. Sorry, this isn't much of a tutorial... it's more of a
code snippet for future use.
