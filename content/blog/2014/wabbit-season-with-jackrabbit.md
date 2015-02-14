title=Wabbit Season with Jackrabbit
date=2014-08-23
type=post
tags=blog,java,groovy
status=published
~~~~~~
I have been playing with [Apache Jackrabbit](http://jackrabbit.apache.org) today, while doing some research for one of my personal projects, and while it seems to have matured a bit since the last time I looked into it, the documentation has stagnated. Granted, it still works as a jump-start better than nothing at all, but it really does not reflect the current state of the API. I present here a more modern take on the "[First Hops](http://jackrabbit.apache.org/first-hops.html)" document based on what I did for my research - I am using Gradle, Groovy, and generally more modern versions of the libraries involved. Maybe this can help others, or myself at a later date.

## Getting Started

The quickest and easiest way to get started is using an embedded `TransientRepository`. Create a project directory and create a `build.groovy` Gradle build file similar to the following:

```groovy
apply plugin: 'groovy'

repositories {
    jcenter()
}

dependencies {
    compile 'org.codehaus.groovy:groovy-all:2.3.6'

    compile 'javax.jcr:jcr:2.0'
    compile 'org.apache.jackrabbit:jackrabbit-core:2.8.0'
    compile 'org.slf4j:slf4j-log4j12:1.7.7'
}
```

This will give you the required dependencies and a nice playground project to work with.

## Logging in to Jackrabbit

In the `src/main/groovy` directory of the project, create a file called `Rabbits.groovy` with the following code:

```groovy
import groovy.util.logging.Slf4j
import org.apache.jackrabbit.core.TransientRepository

import javax.jcr.Repository
import javax.jcr.Session

@Slf4j
class Rabbits {

    static void main(args) throws Exception {
        Repository repository = new TransientRepository(
            new File('./build/repository')
        )

        Session session = repository.login()
        try {
            String user = session.getUserID()
            String name = repository.getDescriptor(Repository.REP_NAME_DESC)

            log.info 'Logged in as {} to a {} repository.', user, name

        } finally {
            session.logout()
        }
    }
}
```

The important part here is the `TransientRepository` code, which allows you to use/reuse a repository for testing. I found that specifying a repository directory in my build directory was useful since by default it will put a bunch of files and directories in the root of your project when you run the project - it's just a little cleaner when you can run `gradle clean` to wipe out your development repository when needed. The downside of specifying the directory seems to be that your repository is not completely transient. I was not clear whether or not this was always the case or just when I set the directory, hence the need to wipe it out sometimes.

The rest of the code is pretty clear, it just does a login to the repository and writes out some information. When run, you should get something like the following:

```2014-08-23 15:23:09 Rabbits [INFO] Logged in as anonymous to a Jackrabbit repository.```

The `finally` block is used to always logout of the repository, though this seems a bit dubious because it seemed quite easy to lock the repository in a bad state when errors caused application failure - this will require some additional investigation.

Lastly, to round out the first version of the project, create a `log4j.properties` file in `src/main/resources` so that your logger has some configuration. I used:

```
log4j.rootCategory=INFO, Cons

# log4j.logger.com.something=ERROR
log4j.logger.org.apache.jackrabbit=WARN

log4j.appender.Cons = org.apache.log4j.ConsoleAppender
log4j.appender.Cons.layout = org.apache.log4j.PatternLayout
log4j.appender.Cons.layout.ConversionPattern = %d{yyyy-MM-dd HH:mm:ss} %c{1} [%p] %m%n
```

> If you want to see more about what Jackrabbit is doing, set the logging level for `log4j.logger.org.apache.jackrabbit` to `INFO` - it gets a little verbose, so I turned it down to WARN.

## Working with Content

When using a content repository, you probably want to do something with actual content, so let's start off with a simple case of some nodes with simple text content. The `main` method of the `Rabbits` class now becomes:

```groovy
Repository repository = new TransientRepository(
    new File('./build/repository')
)

Session session = repository.login(
    new SimpleCredentials('admin','admin'.toCharArray())
)

try {
    String username = session.userID
    String name = repository.getDescriptor(Repository.REP_NAME_DESC)
    log.info 'User ({}) logged into repository ({})', username, name

    Node root = session.rootNode

    // Store content
    Node hello = root.addNode('hello')
    Node world = hello.addNode('world')
    world.setProperty('message', 'Hello, World!')
    session.save()

    // Retrieve content
    Node node = root.getNode('hello/world')
    log.info 'Found node ({}) with property: {}', node.path, node.getProperty('message').string

    // Remove content
    root.getNode('hello').remove()
    log.info 'Removed node.'

    session.save()

} finally {
    session.logout()
}
```

Notice, that the login code now contains credentials so that we can login with a writable session rather than the read-only default session (previous example).

First, we need to store some content in the repository. Since Jackrabbit is a hierarchical data store, you need to get a reference to the root node, and then add a child node to it with some content:

```groovy
Node root = session.rootNode

// Store content
Node hello = root.addNode('hello')
Node world = hello.addNode('world')
world.setProperty('message', 'Hello, World!')
session.save()
```

We create a node named "hello", the add a child named "world" to that node, and give the child node a "message" property. Notice that we save the session to persist the changes to the underlying data store.

Next, we want to read the data back out:

```groovy
Node node = root.getNode('hello/world')
log.info 'Found node ({}) with property: {}', node.path, node.getProperty('message').string
```

You just get the node by it's relative path, in this case from the root, and then retrieve its data.

Lastly, for this example, we want to remove the nodes we just added:

```groovy
root.getNode('hello').remove()
session.save()
log.info 'Removed node.'
```

Removing the "hello" node removes it and it's children (i.e. the "world" node). We then save the session to commit the node removal.

When you run this version of the code, you should see something like this:

```
2014-08-23 15:45:18 Rabbits [INFO] User (admin) logged into repository (Jackrabbit)
2014-08-23 15:45:18 Rabbits [INFO] Found node (/hello/world) with property: Hello, World!
2014-08-23 15:45:18 Rabbits [INFO] Removed node.
```

## Working with Binary Content

This is where my tour diverts from the original wiki document, which goes on to cover XML data imports. I was more interested in loading binary content, especially image files. To accomplish this, we need to consider how the data is stored in JCR. I found a very helpful article "[Storing Files and Folders](https://docs.jboss.org/author/display/MODE/Storing+files+and+folders?_sscc=t)" from the ModeShape documentation (another JCR implementation) - since it's standard JCR, it is still relevant with Jackrabbit.

Basically you need a node for the file and it's metadata, which has a child node for the actual file content. The article has some nice explanations and diagrams, so if you want more than code and quick discussion I recommend you head over there and take a look at it. For my purpose, I am just going to ingest a single image file and then read out the data to ensure that it was actually stored. The code for the `try/finally` block of our example becomes:

```groovy
String username = session.userID
String name = repository.getDescriptor(Repository.REP_NAME_DESC)
log.info 'User ({}) logged into repository ({})', username, name

Node root = session.rootNode

// Assume that we have a file that exists and can be read ...
File file = IMAGE_FILE

// Determine the last-modified by value of the file (if important) ...
Calendar lastModified = Calendar.instance
lastModified.setTimeInMillis(file.lastModified())

// Create an 'nt:file' node at the supplied path ...
Node fileNode = root.addNode(file.name, 'nt:file')

// Upload the file to that node ...
Node contentNode = fileNode.addNode('jcr:content', 'nt:resource')
Binary binary = session.valueFactory.createBinary(file.newInputStream())
contentNode.setProperty('jcr:data', binary)
contentNode.setProperty('jcr:lastModified',lastModified)

// Save the session (and auto-created the properties) ...
session.save()

log.info 'Stored image file data into node ({})...', file.name

// now get the image node data back out

def node = root.getNode(file.name)
dumpProps node

dumpProps node.getNode('jcr:content')
```

Where `IMAGE_FILE` is a `File` object pointing to a JPEG image file.

The first thing we do is create the file node:

```groovy
Node fileNode = root.addNode(file.name, 'nt:file')
```

Notice, it's of type `nt:file` to designate that it's a file node - you will want to brush up on NodeTypes in the Jackrabbit or JCR documentation if you don't already have a basic understanding; I won't do much more than use them in these examples. For the name of the node, we just use the file name.

Second, we create the file content node as a child of the file node:

```groovy
Node contentNode = fileNode.addNode('jcr:content', 'nt:resource')
Binary binary = session.valueFactory.createBinary(file.newInputStream())
contentNode.setProperty('jcr:data', binary)
contentNode.setProperty('jcr:lastModified',lastModified)

// Save the session (and auto-created the properties) ...
session.save()
```

Notice that the child node is named "jcr:content" and is of type "nt:resource" and that it has a property named "jcr:data" containing the binary data content for the file. Of course, the session is saved to persist the changes.

Once we have the file data stored, we want to pull it back out to see that we stored everything as intended:

```groovy
def node = root.getNode(file.name)
dumpProps node

dumpProps node.getNode('jcr:content')
```

The `dumpProps` method just iterates the properties of a given node and writes them to the log file:

```groovy
private static void dumpProps( Node node ){
    log.info 'Node: ({})', node.name

    def iter = node.properties
    while( iter.hasNext() ){
        def prop = iter.nextProperty()
        if( prop.type != PropertyType.BINARY ){
            log.info ' - {} : {}', prop.name, prop.value.string
        } else {
            log.info ' - {} : <binary-data>', prop.name
        }
    }
}
```

When you run this version of the code, you will have output similar to:

```
2014-08-23 16:09:18 Rabbits [INFO] User (admin) logged into repository (Jackrabbit)
2014-08-23 16:09:18 Rabbits [INFO] Stored image file data into node (2014-08-19 20.49.40.jpg)...
2014-08-23 16:09:18 Rabbits [INFO] Node: (2014-08-19 20.49.40.jpg)
2014-08-23 16:09:18 Rabbits [INFO]  - jcr:createdBy : admin
2014-08-23 16:09:18 Rabbits [INFO]  - jcr:created : 2014-08-23T15:59:26.155-05:00
2014-08-23 16:09:18 Rabbits [INFO]  - jcr:primaryType : nt:file
2014-08-23 16:09:18 Rabbits [INFO] Node: (jcr:content)
2014-08-23 16:09:18 Rabbits [INFO]  - jcr:lastModified : 2014-08-19T20:49:44.000-05:00
2014-08-23 16:09:18 Rabbits [INFO]  - jcr:data : <binary-data>
2014-08-23 16:09:18 Rabbits [INFO]  - jcr:lastModifiedBy : admin
2014-08-23 16:09:18 Rabbits [INFO]  - jcr:uuid : cbdefd4a-ec2f-42d2-b58a-a39942766723
2014-08-23 16:09:18 Rabbits [INFO]  - jcr:primaryType : nt:resource
```

## Conclusion

Jackrabbit seems to still have some development effort behind it, and it's still a lot easier to setup and use when compared with something like ModeShape, which seems to be the only other viable JCR implementation which is not specifically geared to a target use case.

The documentation is lacking, but with some previous experience and a little experimentation, it was not too painful getting things to work.