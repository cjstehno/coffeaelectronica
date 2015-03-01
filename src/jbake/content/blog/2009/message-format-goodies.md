title=MessageFormat Goodies
date=2009-11-24
type=post
tags=blog,java
status=published
~~~~~~
The `java.text.MessageFormat` class is, in my opinion, underutilized. It is what `org.springframework.context.MessageSource` implementations use under the covers to provide message formatting, so they are probably used quite a bit, but do you really let them do what they are made to do... formatting? I am guilty of it to; you convert a Date object to a formatted string before handing it off to a `MessageSource` or `MessageFormat` instance... but no more. I wrote a quick
little format dumper to show what each type of formatting prints out:

```java
Object[] params = new Object[]{ new Date() };
out.println("Dates:");
out.println("  default:\t" + format("{0}",                 params) );
out.println("  short:\t"   + format("{0,date,short}",      params) );
out.println("  medium:\t"  + format("{0,date,medium}",     params) );
out.println("  long:\t\t"  + format("{0,date,long}",       params) );
out.println("  custom:\t"  + format("{0,date,d MMMM yyyy}",params) );

out.println("\nTime:");
out.println("  default:\t" + format("{0,time}",         params) );
out.println("  short:\t"   + format("{0,time,short}",   params) );
out.println("  medium:\t"  + format("{0,time,medium}",  params) );
out.println("  long:\t\t"  + format("{0,time,long}",    params) );
out.println("  full:\t\t"  + format("{0,time,full}",    params) );
out.println("  custom:\t"  + format("{0,time,HH:mm:ss}",params) );

params = new Object[]{ new Float(31415.967F) };
out.println("\nNumbers:");
out.println("  default:\t"  + format("{0}",                  params) );
out.println("  integer:\t"  + format("{0,number,integer}",   params) );
out.println("  currency:\t" + format("{0,number,currency}",  params) );
out.println("  percent:\t"  + format("{0,number,percent}",   params) );
out.println("  custom:\t"   + format("{0,number,#,###.0000}",params) );
```

Note: I used static imports for `System.out` and `MessageFormat.format` so that I could minimize the code
noise. For the same reason, I pulled the format label text outside of the formatting call; they could have easily been done as:

```java
out.println( format("  short:\t{0,time,short}", params) );
```

It just seemed easier to see the formatting the other way. When I ran my format dumper I ended up with:

```
Dates:
  default: 11/23/09 7:59 PM
  short: 11/23/09
  medium: Nov 23, 2009
  long:  November 23, 2009
  custom: 23 November 2009

Time:
  default: 7:59:37 PM
  short: 7:59 PM
  medium: 7:59:37 PM
  long:  7:59:37 PM CST
  full:  7:59:37 PM CST
  custom: 19:59:37

Numbers:
  default: 31,415.967
  integer: 31,416
  currency: $31,415.97
  percent: 3,141,597%
  custom: 31,415.9668
```

I am going to make a point to use these from now on, rather than converting manually beforehand.
