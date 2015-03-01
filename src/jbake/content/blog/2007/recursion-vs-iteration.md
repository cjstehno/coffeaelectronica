title=Recursion vs Iteration
date=2007-09-28
type=post
tags=blog,java
status=published
~~~~~~
Say you have a directory under which there can be multiple sub-directories of infinite depth each with any number
of files. If you wanted to walk down the directory tree and do some sort of processing of the files the first thing that
always came to my mind was recursion. Write a little recursive method to iterate over the files and call the method
again on directories, performing the processing as you go. Something like this:

```java
void walk(File dir){
    for(File item : dir.listFiles()){
        if(item.isDirectory()){
            walk(item);
        } else {
            // process file
        }
    }
}
```

Now, this works fine and I have never run into problems with the approach; however, the iterative approach can be a
useful and often more efficient alternative. Rather than recursively calling the `walk()` method, perform the
looping and use a `Stack` object to maintain the set of directories to be searched. Here is what I mean:

```java
void walk(File dir){
    Stack<File> stack = new Stack<File>();
    stack.push(dir);

    while(!stack.isEmpty()){
        for(File item : stack.pop().listFiles()){
            if(item.isDirectory()){
                stack.push(item);
            } else {
                // process file
            }
        }
    }
}
```

This method does the same thing but without recursion. I would like to do some profiling of the two approaches to see
how each performs for various directory structures.
