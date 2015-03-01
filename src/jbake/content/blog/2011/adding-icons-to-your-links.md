title=Adding Icons to your Links
date=2011-02-09
type=post
tags=blog,javascript
status=published
~~~~~~
I have started working on a new browser start page for myself and thought it would be nice to have the favicon images for the links I add to the page. Obviously, you could just download them and render them next to the link by brute force, but then you'd have to do that for every link you added... a big waste of time.

I thought about it for a few minutes and came up with an automated solution that works pretty well. You know the path to the favicon for a given url; it's just thesite.com/favicon.ico, though there are ways to change this that I am not
accounting for. With a little JQuery JavaScripting you can pull the icon from the link itself. First, we need to style
the external anchor tags in preparation for the icon:

```css
a[href^="http://"]{
    background-repeat:no-repeat;
    background-position:left;
    padding-left:17px;
}
```

This will cause all anchor tags with an href starting with "http://" (external) to be left-padded and have some
additional background configuration. This basically adds a blank spot for the icon. Now we need some JavaScript to pull
the icon for the link:

```javascript
jQuery(function(){
    $('a[href^="http"]').each(function(it){
        var url = $(this).attr('href');
        var slashIndex = url.indexOf('/',7);
        if( slashIndex != -1 ){
            url = url.substring(0, slashIndex);
        }

        $(this).css('background-image',"url('" + url + "/favicon.ico')");
    });
});
```

Using JQuery, I find each of the external anchor tags, and ignoring the path part of the url, I set the
"background-image" property of the anchor to be the favicon url. And that's it. Below is the full example:

```html
<html>
    <head>
        <title>Test</title>
        <style type="text/css">
            a[href^="http://"]{
                background-repeat:no-repeat;
                background-position:left;
                padding-left:17px;
            }
        </style>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.5.0/jquery.min.js"></script>
        <script type="text/javascript">
            jQuery(function(){
                $('a[href^="http"]').each(function(it){
                    var url = $(this).attr('href');
                    var slashIndex = url.indexOf('/',7);
                    if( slashIndex != -1 ){
                        url = url.substring(0, slashIndex);
                    }

                    $(this).css('background-image',"url('" + url + "/favicon.ico')");
                });
            });
        </script>
    </head>
    <body>
        This is some text surrounding <a href="http://jqueryui.com">JQuery UI</a> the link I am playing with.
        Also I will add another link <a href="http://google.com">Google</a>.
        More <a href="http://jqueryui.com/demos/">Demos</a>.<br/>
        Local links like <a href="local.html">this</a> will be unchanged.
    </body>
</html>
```

When you render this example, you will get something like the following image. href-icons.png I am sure there are
tweaks that could be done and other scenarios to consider, but this is an interesting starting point.
