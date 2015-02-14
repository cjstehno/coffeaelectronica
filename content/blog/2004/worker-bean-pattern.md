title=Worker Bean Pattern
date=2004-05-28
type=post
tags=blog,java
status=published
~~~~~~
I stumbled upon something that I thought would make a good design pattern, if no one else has already come up
with it. The only name I could think of for it was the "Worker Bean Pattern".

Basically the pattern consists of two classes, the Manager class and the Worker class. The Manager class is used to
create semi-transient Worker classes as required for use by client objects. The example I will use to help me describe
this pattern is a web-based wizard-style input form.

The Manager class is basically a loose combination of the Factory Pattern and the Singleton Pattern. There should be
only one instance of a particular Manager object in the system. For my example, let's call the Manager class `FormManager`
and say that it is an object stored in the application scope of the server context (so that there will be only one
instance per server context). The Manager class' function is to "manage" the Worker
instances. It creates them, populates their data, and destroys them with no external objects acting directly on the
Worker classes in a fashion other than read-only. We can stub out the methods of our `FormManager` as
the following:

```java
public class FormManager {
    public FormWorker createFormWorker(){}

    public void postFormData(FormWorker fw, String name, String[] values){}

    public void destroyFormWorker(FormWorker fw){}
}
```

where `FormWorker` is the Worker. The `createFormWorker()` method is used to create a
`FormWorker` or use by the client when they first enter the wizard form set. This Worker could be pulled from a
pool of available workers or created as needed. The client puts the `FormWorker` the session scope. As the pages
of the wizard form are submitted, each posts its data to the `postFormData()` method, which processes the data
and modifies the `FormWorker` as necessary. Once the final wizard page has been submitted and the client has no
more use for the `FormWorker`, it is passed to the `destroyFormWorker()` method so that it can be disposed
of, or returned to the pool.

The Worker is basically an encapsulation of data required to perform an operation in the Manager that requires more
than a single step. In our example that `FormWorker` is stored in the user's session so that the current
state of the wizard form is maintained between pages. The Worker's data should not be accessible outside of the
Manager. It exists only as a temporary extension of the Manager. Once the client has finished with a Worker it
should be disposed of by the Manager. In our example, `FormWorker`s could be pooled within the
`FormManager` to minimize new object creation and increase efficiency. Our `FormWorker` would look
something like this:

```java
public class FormWorker {
    FormWorker(){}

    void addData(String name, String[] value){}

    String[] getData(String name){}

    void setUserId(String userId){}

    public String getUserId(){}
}
```

You'll notice that most of the methods have the default access, accessible to only other classes in the same
package. You would want to place the `FormManager` and `FormWorker` in the same package for this to work.
Another interesting means of achieving this association, if you prefer, would be to nest the `FormWorker` inside
the `FormManager` as a static nested class; with a nested class, you can enforce the isolation of the worker
using private methods. Let's do that for our example and flesh out the methods a bit more for the final stub:

```java
// Manager
public class FormManager {
    public FormWorker createFormWorker(String userId){
        FormWorker fw = checkoutWorker();
        if(fw != null){ fw.setUserId(userId); }
        return(fw);
    }

    public void postFormData(FormWorker fw, String name, String[] values){
        fw.addData(name,values);
    }

    public void destroyFormWorker(FormWorker fw){
        fw.clearData();
        fw.setUserId(null);
        checkinWorker(fw);
    }

    // Worker
    public static class FormWorker {
        private String userId;
        private HashMap data;

        private FormWorker(){ this.data = new HashMap(); }

        private void addData(String name, String[] value){
            data.put(name,value);
        }

        private String[] getData(String name){
            return( (String[])data.get(name) );
        }

        private void clearData(){ data.clear(); }

        private void setUserId(String userId){
            this.userId = userId;
        }

        public String getUserId(){ return(userId); }
    }
}
```

Obviously this is still missing some code and does not really do anything interesting; however, it should give you
the basic feel of my idea. I used something similar to this in a project recently (actually, a wizard web form set) and
it worked very nicely. Of course there are other ways to skin this cat. You could even simply put the "working" data
into the session itself, though I tend to like things neat and tidy without having to keep track of a lot of session
variables.

This may or may not already be a pattern on someone's list or in a book somewhere, but I did not
find it. I present it in the hope that it will be useful to someone, and as I said, I am always open for comments or
suggestions.
