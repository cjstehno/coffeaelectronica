title=Velociscript
date=2007-07-06
type=post
tags=blog,javascript
status=published
~~~~~~
I was toying with the idea of [velocity](http://velocity.apache.org/)-like expression evaluation and replacement on the
client-side and I came up with this handy little JavaScript function:

```javascript
function ev(str,model){
    var buf = "";
    for(var t=0; t<str.length; t++){
        var tok = str.charAt(t);
        if(tok == "$" && str.charAt(t+1) == "{"){
            t += 2;
            tok = str.charAt(t);
            var expr = "";
            while(tok != "}"){
                expr += tok;
                t++;
                tok = str.charAt(t);
            }
            buf += eval(expr);
        } else {
            buf += tok;
        }
    }
    return(buf);
}
```

which will evaluate the given string using the specified model object (or null). The template string may also access
global JavaScript functions or any other valid JavaScript. An example of its usage is shown below. First the helper
function:

```javascript
function onGo(){
    var resultElt = document.getElementById("result");
    resultElt.innerHTML = ev(resultElt.innerHTML,new Date());
}
```

Then the HTML that calls the helper function:

```html
Name: <input type="text" id="name" /> <button onclick="doGo()">Go</button>
<br/>
<span id="result">
Your name is: ${document.getElementById("name").value} and 
today is: ${model.getMonth()+1}/${model.getDate()}/${model.getFullYear()}
</span>
```

When you enter a name and click "Go", the template string will be replaced by the evaluated string which contains the
name value from the form field and the formatted date. The model object passed in was a JavaScript `Date` object,
but it could have been any object. This still needs a little work and could use some regex love from a regex guru, but
it works on both IE and FireFox. It might be interesting to expand this a bit more to create a Velociscript object
engine similar to Velocity itself so that templates can be managed and cached... we'll see what happens when I start
using this code.
