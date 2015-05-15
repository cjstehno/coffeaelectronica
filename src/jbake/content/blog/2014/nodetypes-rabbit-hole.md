title=NodeTypes - Deeper Down the Rabbit Hole
date=2014-08-23
type=post
tags=blog,java,groovy
status=published
~~~~~~
In my last post about [Jackrabbit](http://jackrabbit.apache.org), "[Wabbit Season with Jackrabbit](http://coffeaelectronica.com/blog/2014/wabbit-season-with-jackrabbit.html)", I fleshed out the old Jackrabbit tutorial and expanded it a bit to ingest some image file content. I touched on the subject of node types briefly, but did little with them. In this post, I am going to delve a bit deeper into using node types and creating your own.

In the older versions of Jackrabbit, they a text-based format for configuring your own node types. I is not well documented, and I was not at all sad to see that it is no longer used since Jackrabbit 2.x. There may be another approach to loading node types, but I found the programmatic approach interesting.

For this post, you will want to refer to the code presented in the other post, "[Wabbit Season with Jackrabbit](Wabbit-Season-with-Jackrabbit)" as a starting point (especially the last version of the code, which the code here will be based on).

For this example, we are going to expand the previous example to include image metadata in the stored node properties. I was originally under the impression that Jackrabbit would automatically extract the metadata on ingestion of the data, but it appears that this is only the case for text-based data when doing indexing. This is not a big roadblock, though, since [Apache Tika](http://tika.apache.org) is included with Jackrabbit, although a slightly older version than what I wanted to use. You can add the following to your `build.gradle` file to update the version:

```groovy
compile 'org.apache.tika:tika-parsers:1.5'
```

Tika provides metadata extractors for a wide range of file formats, one of which is JPEG images, which is what we are playing with here.

First, we need to extract the metadata from the image file. I did this just after the main method's file reference statement:

```groovy
def metadata = extractMetadata( file )
```

The code for the `extractMetadata(File)` method is as follows:

```groovy
private static Map<String,String> extractMetadata( File imageFile ){
    def meta = new Metadata()
    def extractor = new ImageMetadataExtractor( meta )

    log.info 'Extracting metadata from {}', imageFile

    extractor.parseJpeg(imageFile)

    def props = [:]
    meta.names().sort().each { name->
        props[name] = meta.get(name)
        log.info " : <image-meta> $name : ${meta.get(name)}"
    }

    return props
}
```

It's just a simple straight-forward use of the Tika `ImageMetadataExtractor`, which pulls out all the data and stores it into a Map for use later.

Then, after we create the main file node, we want to apply the metadata properties to it:

```groovy
applyMetadata( fileNode, metadata )
```

The `applyMetadata(Node,Map)` method applies the metadata from the map as properties on the node. The code is as shown below:

```groovy
private static void applyMetadata( Node node, Map<String,String> metadata ){
    node.addMixin('pp:photo')
    node.setProperty('pp:photo-width', metadata['Image Width'].split(' ')[0] as long )

    log.info 'Applied mixin -> {} :: {}', node.mixinNodeTypes.collect { it.name }.join(', '), node.getProperty('pp:photo-width').string
}
```

For the metadata, I used the concept of "Mixin" node types. Every node has a primary node type, in this case it's an "nt:file" node, but nodes can have multiple mixin node types also applied to them so that they can have additional properties available. This works perfectly in my case, since I want a file that is a photo with extra metadata associated with it.

Also, the `dumpProps(Node)` method changed slightly to avoid errors during extraction, and to hide properties we don't care about seeing:

```groovy
private static void dumpProps( Node node ){
    log.info 'Node ({}) of type ({}) with mixins ({})', node.name, node.getPrimaryNodeType().name, node.getMixinNodeTypes()

    def iter = node.properties
    while( iter.hasNext() ){
        def prop = iter.nextProperty()
        if( prop.type != PropertyType.BINARY ){
            if( prop.name != 'jcr:mixinTypes' ){
                log.info ' - {} : {}', prop.name, prop.value.string
            }
        } else {
            log.info ' - {} : <binary-data>', prop.name
        }
    }
}
```

If you run the code at this point, you will get an error about the node type not being defined, so we need to define the new node type. In the current version of Jackrabbit, they defer node type creation to the standard JCR 2.0 approach, which is pretty clean. The code is shown below:

```groovy
private static void registerNodeTypes(Session session ) throws Exception {
    if( !session.namespacePrefixes.contains('pp') ){
        session.workspace.namespaceRegistry.registerNamespace('pp', 'http://stehno.com/pp')
    }

    NodeTypeManager manager = session.getWorkspace().getNodeTypeManager()

    if( !manager.hasNodeType('pp:photo') ){
        NodeTypeTemplate nodeTypeTemplate = manager.createNodeTypeTemplate()
        nodeTypeTemplate.name = 'pp:photo'
        nodeTypeTemplate.mixin = true

        PropertyDefinitionTemplate propTemplate = manager.createPropertyDefinitionTemplate()
        propTemplate.name = 'pp:photo-width'
        propTemplate.requiredType = PropertyType.LONG
        propTemplate.multiple = false
        propTemplate.mandatory = true

        nodeTypeTemplate.propertyDefinitionTemplates << propTemplate

        manager.registerNodeType( nodeTypeTemplate, false )
    }
}
```

Which is called just after logging in and getting a reference to a repository session. Basically, you use the `NodeTypeManager` to create a `NodeTypeTemplate` which you can use to specify the configuration settings of your new node type. There is a similar construct for node type properties, the `PropertyDefinitionTemplate`. Once you have your configuration done, you register the node type and you are ready to go.

When run, this code generates output similar to:

```
2014-08-23 16:43:02 Rabbits [INFO] User (admin) logged into repository (Jackrabbit)
...
2014-08-23 16:43:02 Rabbits [INFO]  : <image-meta> Image Width : 2448 pixels
...
2014-08-23 16:43:02 Rabbits [INFO] Applied mixin -> pp:photo :: 2448
2014-08-23 16:43:02 Rabbits [INFO] Stored image file data into node (2014-08-19 20.49.40.jpg)...
2014-08-23 16:43:02 Rabbits [INFO] Node (2014-08-19 20.49.40.jpg) of type (nt:file) with mixins ([org.apache.jackrabbit.core.nodetype.NodeTypeImpl@5b3bb1f7])
2014-08-23 16:43:02 Rabbits [INFO]  - jcr:createdBy : admin
2014-08-23 16:43:02 Rabbits [INFO]  - pp:photo-width : 2448
2014-08-23 16:43:02 Rabbits [INFO]  - jcr:primaryType : nt:file
2014-08-23 16:43:02 Rabbits [INFO]  - jcr:created : 2014-08-23T16:43:02.531-05:00
2014-08-23 16:43:02 Rabbits [INFO] Node (jcr:content) of type (nt:resource) with mixins ([])
2014-08-23 16:43:02 Rabbits [INFO]  - jcr:lastModified : 2014-08-19T20:49:44.000-05:00
2014-08-23 16:43:02 Rabbits [INFO]  - jcr:data : <binary-data>
2014-08-23 16:43:02 Rabbits [INFO]  - jcr:lastModifiedBy : admin
2014-08-23 16:43:02 Rabbits [INFO]  - jcr:uuid : a699fbd6-4493-4dc7-9f7a-b87b84cb1ef9
2014-08-23 16:43:02 Rabbits [INFO]  - jcr:primaryType : nt:resource
```
(I omitted a bunch of the metadata output lines to clean up the output)

You can see that the new node type data is populated from the metadata and the mixin is properly applied.

Call me crazy, but this approach seems a lot cleaner than the old text-based approach. There are some rules around node types and ensuring that they are not created if they already exist, though this only seems to be a problem in certain use cases - need to investigate that a bit more, but be aware of it.

Now, you can stop here and create new node types all day long, but let's take this experiment a little farther down the rabbit hole. The programmatic approach to node type configuration seems to lend itself nicely to a Groovy-based DSL approach, something like:

```groovy
private static void registerNodeTypes( Session session ) throws Exception {
    definitions( session.workspace ){
        namespace 'pp', 'http://stehno.com/pp'

        nodeType {
            name 'pp:photo'
            mixin true

            propertyDefinition {
                name 'pp:photo-width'
                requiredType PropertyType.LONG
                multiple false
                mandatory true
            }

            propertyDefinition {
                name 'pp:photo-height'
                requiredType PropertyType.LONG
                multiple false
                mandatory true
            }
        }
    }
}
```

Seems like a nice clean way to create new node types and their properties with little fuss and muss. So, using a little Groovy DSL closure delegation we can do this without too much pain:

```groovy
class NodeTypeDefiner {

    private final NodeTypeManager manager
    private final Workspace workspace

    private NodeTypeDefiner( final Workspace workspace ){
        this.workspace = workspace
        this.manager = workspace.nodeTypeManager
    }

    void namespace( String name, String uri ){
        if( !workspace.namespaceRegistry.prefixes.contains(name) ){
            workspace.namespaceRegistry .registerNamespace(name, uri)
        }
    }

    static void definitions( final Workspace workspace, Closure closure ){
        NodeTypeDefiner definer = new NodeTypeDefiner( workspace )
        closure.delegate = definer
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure()
    }

    void nodeType( Closure closure ){
        def nodeTypeTemplate = new DelegatingNodeTypeTemplate( manager )

        closure.delegate = nodeTypeTemplate
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure()

        manager.registerNodeType( nodeTypeTemplate, true )
    }
}
```

The key pain point I found here was that with the nested closure structures, I needed to change the `resolveStrategy` so that you get the delegate only rather than the owner - took a little debugging to trace that one down.

The other useful point here was the "Delegating" extensions of the two "template" classes:

```groovy
class DelegatingNodeTypeTemplate implements NodeTypeDefinition {

    @Delegate NodeTypeTemplate template
    private final NodeTypeManager manager

    DelegatingNodeTypeTemplate( final NodeTypeManager manager ){
        this.manager = manager
        this.template = manager.createNodeTypeTemplate()
    }

    void name( String name ){
        template.setName( name )
    }

    void mixin( boolean mix ){
        template.mixin = mix
    }

    void propertyDefinition( Closure closure ){
        def propertyTemplate = new DelegatingPropertyDefinitionTemplate( manager )
        closure.delegate = propertyTemplate
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure()
        propertyDefinitionTemplates << propertyTemplate
    }
}

class DelegatingPropertyDefinitionTemplate implements PropertyDefinition {

    @Delegate PropertyDefinitionTemplate template
    private final NodeTypeManager manager

    DelegatingPropertyDefinitionTemplate( final NodeTypeManager manager ){
        this.manager = manager
        this.template = manager.createPropertyDefinitionTemplate()
    }

    void name( String name ){
        template.setName( name )
    }

    void requiredType( int propertyType ){
        template.setRequiredType( propertyType )
    }

    void multiple( boolean value ){
        template.multiple = value
    }

    void mandatory( boolean value ){
        template.mandatory = value
    }
}
```

They provide the helper methods to allow a nice clean DSL. Without them you have only setters, which did not work out cleanly. You just end up with some small delegate classes.

This code takes care of adding in the property definitions, registering namespaces and node types. It does not currently support all the configuration properties; however, that would be simple to add - there are not very many available.

As you can see from the DSL example code, you can now add new node types in a very simple manner. This kind of thing is why I love Groovy so much.

> If there is any interest in this DSL code, I will be using it in one of my own projects, so I could extract it into a library for more public use - let me know if you are interested.