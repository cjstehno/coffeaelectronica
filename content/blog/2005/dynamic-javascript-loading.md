title=Dynamic JavaScript Loading
date=2005-06-15
type=post
tags=blog,java
status=published
~~~~~~
I figured out a way to dynamically load JavaScript files at runtime. There are times when you may not always need
to import all of your external JavaScripts, or maybe you are using Ajax to load content into a div and you also need to
import some script that the content needs. Here is the solution and it works in IE and [FireFox](http://mozilla.org/firefox):

```javascript
function loadLibrary(path){
    var headElt = document.getElementsByTagName("head").item(0);
    var scriptElt = headElt.appendChild(document.createElement("script"));
    scriptElt.setAttribute("type","text/javascript");
    scriptElt.setAttribute("src",path);
}
```

Pretty simple, and all you have to do to use it is:

```javascript
loadLibrary("scripts/myscript.js");
```

This works for dynamically loading stylesheet too if you add a link element instead of a script element:

```javascript
function loadStylesheet(path){
    var headElt = document.getElementsByTagName("head").item(0);
    var scriptElt = headElt.appendChild(document.createElement("link"));
    scriptElt.setAttribute("type","text/css");
    scriptElt.setAttribute("rel","stylesheet");
    scriptElt.setAttribute("href",path);
}
```
