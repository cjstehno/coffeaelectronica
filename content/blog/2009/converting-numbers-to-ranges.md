title=Converting Numbers to Ranges
date=2009-11-21
type=post
tags=blog,java
status=published
~~~~~~
## Python

I have decided to start doing little programming puzzle problems in various languages, since my duplicate int finding problem is getting old and repetitive. My first puzzle comes from a site called [Code Golf](http://codegolf.com/) and it is titled [Home on the Range](http://codegolf.com/home-on-the-range). I decided to start with [Python](http://python.org/) since it is something I have been playing with.

Basically the puzzle boils down to taking a series of numbers (in sequence) as input and producing a result where the numbers that are reduced to ranges where applicable, as below:

```
[1 2 3 6 9 10 11 15] becomes "1-3, 6, 9-11, 15."
```

After a couple iterations and a handful of language/api reference searches I came up with:

```python
import sys

def convert_to_ranges(nums):
    ranges, holder = '',''
    num_count = len(nums)
    cap = endcap(num_count)

    for i in range(num_count):
        if i+1 < num_count and int(nums[i])+1 == int(nums[i+1]):
            if not holder: holder = nums[i]
            else:
                if holder:
                    ranges += holder + '-' + nums[i] + cap(i)
                    holder = ''
                else:
                    ranges += nums[i] + cap(i)
    return ranges

def endcap(ln):
    return lambda idx: '.' if idx == ln-1 else ', '

print(convert_to_ranges(sys.argv[1:]))
```

I had originally had the `holder` variable as a list to hold each grouped number, but I realized that you
really only need to store the first one so that you can use it to create the start of the grouping once you find the end
of it. The lambda function `endcap()` was originally just a normal function, but I wanted to play with some
interesting built-in features, and it actually worked out nicely. The python ternary operator is also interesting
( `VAL if CONDITION else OTHERVAL` ); it reads better than other ternary operators. The `int()` calls I make in the
function are simply there because the input comes from the standard in, where they are strings.

## Java

> Originally separate posting on 11/222009

I decided to do a quick Java implementation.

```java
public class HomeOnTheRange {
    public static void main( final String[] args ) {
        System.out.println( convertToRanges( args ) );
    }

    public static String convertToRanges(final String[] args){
        final int[] nums = new int[args.length];
        for( int n=0; n<args.length; n++){
            nums[n] = Integer.valueOf( args[n] );
        }

        String hold = null;
        final StringBuilder str = new StringBuilder();

        for(int i=0; i<args.length; i++){
            if( i+1 < args.length && nums[i]+1 == nums[i+1] ){
                if(hold == null) hold = args[i];
            } else {
                if(hold != null){
                    str.append(hold).append('-').append(args[i]).append(", ");
                    hold = null;
                } else {
                    str.append( args[i] ).append(", ");
                }
            }
        }
        str.delete( str.length()-2, str.length() ).append( '.' );

        return str.toString();
    }
}
```

There is not much difference from the Python version; I guess with simple problems like these you are going to end
up with pretty much the same form of solution unless you can find some tricky little piece of the language that works
better as a solution. One thing I did different in the Java solution is that I pre-converted the argument String array
into an integer array. I noticed that you really end up doing the string to int conversion twice for most of the input
elements, which in Java can really add up for a large data set. I wonder if the same problem is inherent in the python
solution? I also did the ending period a little different here, simple deleting the end of the string buffer and adding
a period; an approach similar to the python version would probably be a little better, but either way is fine. I guess
you could write a separate method to do the string to int conversion and then cache the results so that you are still
doing the conversion once and "inline" with the main loop.

With a little thought about the problem space you can see that each string is converted and used at most twice, so you
can setup the caching to convert and cache on the first use and then just return the converted int on the second call.

```java
public class HomeOnTheRange2 {
    private static final ThreadLocal<Converter> converter = new ThreadLocal<Converter>(){
        @Override
        protected Converter initialValue() {
            return new Converter();
        };
    };

    public static void main( final String[] args ) {
        System.out.println( convertToRanges( args ) );
    }

    public static String convertToRanges(final String[] args){
        String hold = null;
        final StringBuilder str = new StringBuilder();

        final Converter conv = converter.get();
        for(int i=0; i<args.length; i++){
            if( i+1 < args.length && conv.toInt( args[i] )+1 == conv.toInt( args[i+1] )){
                if(hold == null) hold = args[i];
            } else {
                if(hold != null){
                    str.append(hold).append('-').append(args[i]).append(", ");
                    hold = null;
                } else {
                    str.append( args[i] ).append(", ");
                }
            }
        }

        str.delete( str.length()-2, str.length() ).append( '.' );

        return str.toString();
    }

    private static final class Converter {
        private String key;
        private int value;

        public int toInt(final String str){
            if( !str.equals( key ) ){
                this.key = str;
                this.value = Integer.valueOf( str );
            }
            return value;
        }
    }
}
```

You will see that this code no longer does the initial conversion loop. The string to int conversion is now done
using the `Converter` class, which simply converts and caches the value on the first request for a value, and
will simply return the cached int for a second call of the same number. This makes the assumption that the numbers are
in ascending order, which is valid for this problem. I also added the converter as a `ThreadLocal` variable since
this conversion is now very tied to the order in which the values are converted. Making it thread-safe ensures that two
calls to this method on different threads will not mess with each others values.

I considered just using a Map of some sort, but without some sort of bounds you end up caching every number, when as you can see you really only need
one. Technically delving that deep into the problem to come up with a custom caching solution is really premature
optimization, which is generally a bad thing. You should just to the original conversion as needed and then do the
followup refactoring if performance bottlenecks lead you to do so.

## Clojure

> Originally separate posting on 10/10/2010

Ok, in a wild mood, I decided to go back and try some [Clojure](http://clojure.org/) again to work out another implementation
of the number range problem. I think Clojure needed some time to sink into my brain, as it seemed to make more sense this
time around.

Working out the solution for this was not all that bad:

```clojure
(defn rangify [items]
    (loop [its items holder nil outp ""]
        (if (empty? its)
            outp
            (if (= (+ (first its) 1) (first (rest its)) )
                (if (nil? holder)
                    (recur (rest its) (first its) outp )
                    (recur (rest its) holder outp )
                )
                (if (nil? holder)
                    (recur (rest its) nil (print-str outp (first its) "," ) )
                    (recur (rest its) nil (print-str outp holder "-" (first its) "," ) )
                )
            )
        )
    )
)
```

I am sure that there is still a lot that can be done to make this cleaner and more efficient. Even now it does not fully
satisfy the criteria since the output is not quite right, as shown below:

```
ranges=> (rangify [1 2 3 4 5 6 10])
" 1 - 6 , 10 ,"
```

After a little more thought, I was able to move some of the function calls around and merge them into the recur
calls to simplify and tighten the code down a bit so that we end up with:

```
(defn rangify [items]
    (loop [its items holder nil outp ""]
        (if (empty? its)
            outp
            (if (= (inc (first its)) (first (rest its)) )
                (recur (rest its) (if holder holder (first its) ) outp )
                (recur (rest its) nil
                    (if holder
                        (print-str outp holder "-" (first its) "," )
                        (print-str outp (first its) "," )
                    )
                )
            )
        )
    )
)
```

This, actually is a bit cleaner and easier to read, but it sure seems like an odd language. I decided to put out
what I have and come back to finish it later. It was a fun exercise.

## Groovy

> Originally separate posting on 10/9/2010

Having been working with Groovy a lot more lately, I decided to give it a whirl and here is what I came up with:

```groovy
def rangeize( items ){
    def hold, str = ''

    items.eachWithIndex { it,i->
        if( (it as int)+1 == (items[i+1] as int) ){
            if( !hold ) hold = it
        } else {
            str += hold ? "$hold-${it}, " : "${it}, "
            hold = null
        }
    }

    str[ 0..(str.length()-3) ] + '.'
}
```

The problem states that the input will be in order and have no duplicate entries, so I don't need to account for
those. Other than some slightly simplified syntax, it's not all that much different from the Java version, thought it's
a bit more condensed. I kept the string to int conversions inline on this version as I am still up in the air about how
much overhead that would really add. You could just as easily convert the string array before doing the grouping. Always
fun to practice.

## Obsessed With Ranges

> Originally separate posting on 10/16/2010

Okay, I had drafts for three more postings of different solutions to the Home on the Range problem mentioned in earlier
postings. I decided that I need to merge them into one post to save you all from being overly spammed by odd little
programming snippets.

First off, we have an alternate Groovy solution based on the recursive approach which was necessary for the Clojure
implementation:

```groovy
def iter( items, holder, str ){
    if( items.isEmpty() ) return "${str[0..(str.length()-3)]}."
        def it = items.remove(0) as int
        if( it+1 == (items[0] as int) ){
            iter( items, holder ?: it, str )
        } else {
            iter( items, null, holder ? "$str$holder-$it, " : "$str$it, " )
        }
    }

    def rangize( items ){
        iter( items, null, '' )
    }
```

I had fun with this one as I got to play with some Groovy syntax sugar; however, I was unable to do it in a single
function definition, so you get two. Next, we have a solution done using Ruby, which I was surprised to see that I had
not previously done.

```ruby
def rangize( items )
    holder, str = nil, ''
    for i in (0...items.size())
        if items[i].to_i + 1 == items[i+1].to_i then
            if !holder then holder = items[i] end
        else
            str << (holder ? "#{holder}-" : '') << "#{items[i]}, "
            holder = nil
        end
    end

    str.chomp(', ') << '.'
end
```

Well, after working on it I realized why I didn't bother before... other than a few Ruby syntax items, it's not all
that interesting. The final one is the kicker. I got a little crazy and decide to do an implementation of the recursive
approach using browser-based JavaScript:

```javascript
function rangize(nums){
    function iter(items, holder, str){
        if(items.length == 0) return str;
        var itm = items[0];
        if( (parseInt(itm)+1) == parseInt(items[1]) ){
            return iter( items.slice(1), holder ? holder : itm, str);
        } else {
            return iter( items.slice(1), null, str + (holder ? holder+ '-' + itm : itm) + ', ' );
        }
    }
    var str = iter(nums,null,'');
    return str.slice(0, str.length-2) + '.';
}
```

Yes, you can nest functions inside of functions in JavaScript... I never realized that. The implementaion is not
quite as clean as some of the others since JavaScript lacks some of the fancy string manipulation syntax; however, it
really helps to show that JavaScript is a fairly robust and powerful language in itself. For better or worse, you could
add some additional helper functions (also nested) to clean up the iteration a bit, though it does make it a bit more
"busy" when you first look at it:

```javascript
function rangize(nums){
    function eqNxt( items ){ return (parseInt(items[0])+1) == parseInt(items[1]); }

    function endcap( str ){ return str.slice(0, str.length-2) + '.'; }

    function iter(items, holder, str){
        if(items.length == 0) return str;
        return iter(
            items.slice(1),
            eqNxt(items) ? (holder ? holder : items[0]) : null,
            eqNxt(items) ? str : (str + (holder ? holder+ '-' + items[0] : items[0]) + ', ')
        );
    }
    return endcap( iter(nums,null,'') );
}
```

Here is the whole code for the HTML test page:

```html
<html>
    <head>
        <title>Home on The Range</title>
        <script type="text/javascript">
            function rangize(nums){
                function iter(items, holder, str){
                    if(items.length == 0) return str;

                    var itm = items[0];
                    if( (parseInt(itm)+1) == parseInt(items[1]) ){
                        return iter( items.slice(1), holder ? holder : itm, str);
                    } else {
                        return iter( items.slice(1), null, str + (holder ? holder+ '-' + itm : itm) + ', ' );
                    }
                }

                var str = iter(nums,null,'');
                return str.slice(0, str.length-2) + '.';
            }
        </script>
    </head>
    <body onload="alert(rangize(['1','2','3','5','7','8','9','10','13']))">
    </body>
</html>
```

I think I have burned this coding puzzle to the end, so hopefully I can move on to something else.
