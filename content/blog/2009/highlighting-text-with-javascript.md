title=Highlighting Text with JavaScript
date=2009-09-15
type=post
tags=blog,javascript
status=published
~~~~~~
A question arose recently about how to highlight a word or words in the text of a `div` element. It turns out that it's actually pretty easy using [Prototype](http://prototypejs.org/).

The example below is the code needed to highlight each occurrence (up to ten of them) of the word 'pick' in the div. The operation will be performed when the content div is clicked.

```html
<html>
    <head>
        <style type="text/css">
            #content span { background-color: yellow; }
        </style>
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/prototype/1.6.0.2/prototype.js"></script>
        <script type="text/javascript">
            Event.observe(window,'load',function(){
                $('content').observe('click',highlight);
            });

            function highlight(){
                $('content').innerHTML = $('content').innerHTML.sub('pick','<span>pick</span>',10);
            }
        </script>
    </head>
    <body>
        <div id="content">
            Peter Piper picked a peck of pickeled peppers. How many peppers did Peter Piper pick?
        </div>
    </body>
</html>
```

> _Note:_ I used to Google-hosted version of the prototype library which is handy. This could easily be refactored to do
any sort of style operation to the selected text, or replace it altogether. I will have to give this a try with JQuery as a comparison.
