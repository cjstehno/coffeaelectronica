title=JavaScript Link Decoration
date=2009-06-20
type=post
tags=blog,javascript
status=published
~~~~~~
No, not decorating with pretty colors, decorating with additional functionality... specifically click-tracking and
confirmation. With a little bit of JavaScript and a little help from [Prototype](http://prototypejs.org/) you
can add functionality to link clicks.

If you have the following links

```html
<p><a href="http://dzone.com" class="track">DZone</a></p>
<p><a href="http://cnn.com">CNN</a></p>
<p><a href="http://thinkgeek.com" class="confirm track">Geek Porn</a></p>
```

noting that they are annotated with CSS classes. These classes are the key. You can use the following JavaScript:

```javascript
Event.observe(window,'load',function(evt){
    $$('a').each(function(it){
        it.observe('click',handleLinkClick);
    });
});

function handleLinkClick(evt){
    var elt = evt.element();
    var cont = true;
    if(elt.hasClassName('confirm')){
        cont = confirm("Are you sure?");
        if(!cont){
            Event.stop(evt);
        }
    }

    if(cont && elt.hasClassName('track')){
        var url = elt.readAttribute('href');
        new Ajax.Request('recorder.jsp?url=' + url,{method:'get'});
    }
}
```

The script will catch clicks on the appropriate link and add functionality to it. In the case of a links with the
"track" class it will fire off an Ajax request to a request tracking service, while the "confirm" class adds a
confirmation dialog which will stop the click event if confirmation is canceled. You can put a simple dummy link tracker
at "recorder.jsp":

```html
<% System.out.println( request.getParameter("url") ); %>
```

This decorating works in IE 6+ and in FireFox; however, it will not catch link following events generated by a
right-click and "Open in New Tab" selection from the pop-up menu. This seems to be a browser issue that I was unable to
find a work-around for. Personally, I tend to open external links that way to preserve the original page... meaning that
this procedure will not track clicks from someone like me. Since external links are most likely the ones you want to
track, this procedure is mostly useless. I will have to play around with it and see if there is another way. If you do
want to use it you could refactor this a bit and make it use a more object-oriented decorator pattern approach, but this
is just to get the general idea out there as an alternative to the old link-modification way of click tracking.
