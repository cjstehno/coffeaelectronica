title=Floating and Following Div
date=2008-10-28
type=post
tags=blog,java
status=published
~~~~~~
I needed one of those `DIV`s that appears on call and then stays in view even when you scroll, until you
close it. For lack of a better name, I call it the floating following div, and it's pretty easy to make. With a little
help from [Prototype](http://prototypejs.org/) we can even make it work across the major browsers.

First you need to put the div to be floated somewhere on your page. The page itself can be anything you want.

```html
<div id="movable">This is my floating area</div>
```

and then you need to give it some initial style:

```css
#movable {
    position: absolute;
    left: 100px;
    width: 200px;
    height: 200px;
    background-color: red;
}
```

Once all that is on the page, you will need some JavaScript to do the fancy stuff:

```html
<script type="text/javascript" src="prototype.js"></script><script type="text/javascript">
    Event.observe(window,'load',function(evt){
        $('movable').hide();
        Event.observe('showme','click',showDiv);
        Event.observe(window,'scroll', function(evt){
            $('movable').setStyle({ top: 8 + document.viewport.getScrollOffsets().top + 'px' });
        });
    });

    function showDiv(evt){
        $('movable').show();
    }
</script>
```

This causes the "movable" element to be hidden. Once the button with an id of "showme" is clicked, the element will
be shown and will then follow along with vertical scrolling, staying up near the top of the view port. The key to this
following motion is the function mapped to the <tt>window</tt> scrolling event:

```javascript
$('movable').setStyle({ top: 8 + document.viewport.getScrollOffsets().top + 'px' });
```

The [document.viewport.getScrollOffsets()](http://prototypejs.org/api/document/viewport/getscrolloffsets) function is provided by
Prototype. It's nothing exciting, but it works... just another thing posted here for future reference.
