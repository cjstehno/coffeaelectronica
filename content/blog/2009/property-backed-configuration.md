title=Property Backed Configuration
date=2009-05-23
type=post
tags=blog,java
status=published
~~~~~~
A useful trick I started doing for property-backed configuration files is to provide something a little more rich
than a simple `Properties` object as a provider.

Use a `Properties` instance as a delegate to pull out the properties, but provide a more useful interface for the values themselves. Say you have a
properties file such as:

```
path.storage = /some/path
```

You could simply access these via a `Properties` instance, and use it directly as in:

```java
String path = props.getProperty("path.storage");
File file = new File(path,"archive.zip");
```

but consider a potentially more useful approach

```java
File file = config.getStorageFile("archive.zip");
```

with

```java
public File getStorageFile(String name){
    return new File( props.getProperty("path.storage"), name );
}
```

You could even pull the file extension into the method if it made sense to do so... say, if all files in that path
were .zip files you could then enforce more control on how it was used such as:

```java
public File getStorageFile(String name){
    return new File(props.getProperty("path.storage"),name + ".zip");
}

File file = config.getStorageFile("archive");
```

These helper methods would be part of a class called Config or something similar that delegates to an internal `Properties` object:

```java
public class Config {
    private final Properties props;

    public Config(Properties p){
        this.props = p;
    }

    public File getStorageFile(String name){
        return new File(props.getProperty("path.storage"),name);
    }
}
```

You could also do the loading of the properties file inside this class. Some property validation could also be helpful.
This strategy really helped to clean up a project that originally had a lot of properties that were used all over the
place with very little order. Converting to this approach actually made the code more understandable and less error prone as well.
