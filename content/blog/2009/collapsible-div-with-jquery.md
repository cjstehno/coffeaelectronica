title=Collapsible Divs with JQuery
date=2009-12-23
type=post
tags=blog,java
status=published
~~~~~~
I coded up a nice little collapsible-group side bar thingy using [JQuery](http://jquery.com/) and it was surprisingly
easy. Say you have a sidebar with collapsible group content such as:

```html
<div class="container">
    <div class="group">
        <div class="group-title">Group A</div>
        <div class="group-content">This is where you would put the content for Group A.</div>
    </div>
    <div class="group">
        <div class="group-title">Group B</div>
        <div class="group-content">This is where you would put the content for Group B.</div>
    </div>
    <div class="group">
        <div class="group-title">Group C</div>
        <div class="group-content">This is where you would put the content for Group C.</div>
    </div>
</div>
```

where you have group block titles and group content that you want to be able to toggle the visibility of. With a
couple lines of JavaScript and JQuery it's a sinch:

```javascript
$('div.group-title').bind('click',function(evt){
    $(evt.target).parent().find('.group-content').slideToggle(500);
});
```

which will be put inside an onload handler (also using JQuery). When the group title is clicked, the group-content
block will toggle by sliding up or down in about half a second. With this model you can also place any number of these
"components" on a page without the concern about event collision since the event handling is based on the click
location. Add in a little CSS and you end up with:

![Screen shot of div.](https://raw.github.com/cjstehno/coffeaelectronica/master/content/files/collapsable-div.png)
