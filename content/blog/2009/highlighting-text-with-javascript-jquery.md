title=Highlighting Text with JavaScript and JQuery
date=2009-09-24
type=post
tags=blog,java
status=published
~~~~~~
In my recent post about [Highlighting Text With JavaScript](Highlighting-Text-with-JavaScript) I suggested that I should try doing the same functionality with [JQuery](http://jquery.com/).

So I did, and I am really starting to like JQuery. Below are the modifications to the HTML shown in the previous posting; basically you just swap out the two script elements in the [Prototype](http://prototypejs.org/) version with those shown below:

```html
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js"></script>
<script type="text/javascript">
    jQuery(function(){
        $('#content').bind('click',highlight);
    });

    function highlight(){
        var htm = $('#content').html();
        var str = '';

        jQuery.each( htm.split(' '), function(){
            if(jQuery.trim(this) != ''){
                str += this.replace('pick','<span>pick</span>');
            }

            str += ' ';
        });

        $('#content').html(str);
    }
</script>
```

JQuery does on-load event handling and event-handling in general, from what I have seen, in a very similar way to
how Prototype does it. The `highlight()` function is a bit more complex in this version as JQuery does not seem
to have the same level of String manipulation support out of the box; however, maybe I missed it in the documentation or
perhaps there is a good plugin that adds better string handling. This post is not really meant to compare the two
libraries overall; it was more to satisfy my curiousity and get some practice with JQuery when solving a problem I have
already solved with Prototype.
