title=Herding Jackrabbit
date=2009-10-28
type=post
tags=blog,java
status=published
~~~~~~
I decided to play around a little with [Jackrabbit](http://jackrabbit.apache.org/) the other day... here are some of my notes... I am running on Ubuntu with [Tomcat 6](http://tomcat.apache.org/) and all I had to do to get started was download the Jackrabbit war distribution and install it in the webapps directory, then add the jcr-1.0.jar (downloaded from Sun) to the TOMCAT/lib directory. Once you start up the server and go into the context you get a nice welcome screen:

![Welcome Screen](https://raw.github.com/cjstehno/coffeaelectronica/master/content/files/jack-screen-1.png)

I created a content repo in my home directory:

```
/home/cjstehno/.jackrabbit
```

Once the repo is configured you get a list of common clients, with links on how to use them:

![Configured Repo](https://raw.github.com/cjstehno/coffeaelectronica/master/content/files/jack-screen-2.png)

I was interested in Standard WebDAV to do some simple content sharing between a few computers. It seemed simple enough and there is even a WebDAV connection setup in Ubuntu, but I had a heck of a time getting it to work. Finally I just tried pasting the connection URL into Nautilus directly:

```
http://localhost:8080/jackrabbit/repository/default
```

but that didn't work, so I tried a slight change,

```
dav://localhost:8080/jackrabbit/repository/default
```

which worked and asked me for a username and password, which the documentation says will take anything until you configure it. It looked like I had everything up and running:

![File browser](https://raw.github.com/cjstehno/coffeaelectronica/master/content/files/jack-screen-3.png)

Looks can be deceiving... I could add files but not edit them or add directories; however, after a refresh of the WebDAV view my folder was there. It did allow me to copy files from it into other directories and then work with them as normal. It does not seem like a very useful means of accessing the files. I can view everything in the web interface, but that is really not much better. I decided to try connecting using one of the more programmatic means, the RMI client provided with Jackrabbit. It was actually very easy to connect and use. After digging around for a while with the files I had in the repository, It seems that this content repository stuff is quite flexible and could be very powerful, but the learning curve to make good use of it may be a little high... and there seems to be little documentation. I wrote a
little dumper app, which I called Jackalope so I could see how things were laid out in the repo:

```java
public class Jackalope {
	public static void main( final String[] args ) {
		final ClientRepositoryFactory factory = new ClientRepositoryFactory();
		Session session = null;
		try {
			final Repository repo = factory.getRepository( "//localhost/jackrabbit.repository" );
			session = repo.login( new SimpleCredentials("cjstehno", "foo".toCharArray() ) );
			
			final String user = session.getUserID();
			final String name = repo.getDescriptor(Repository.REP_NAME_DESC);
			System.out.println("Logged in as " + user + " to a " + name + " repository.");
			
			final Workspace ws = session.getWorkspace();
			System.out.println("Workspace: " + ws.getName());
			
			final Node node = session.getRootNode();
			System.out.println("Node: " + node.getName());
			
			final NodeIterator children = node.getNodes();
			System.out.println("Children: " + children.getSize());
			while(children.hasNext()){
				final Node child = (Node)children.next();
				System.out.println("--> " + child.getName() + " [" + child.getPrimaryNodeType().getName() + "]");
				
				final NodeDefinition nodeDef = child.getDefinition();
				System.out.println("-->\tn: " + nodeDef.getDeclaringNodeType().getName());
				
				final PropertyIterator props = child.getProperties();
				while(props.hasNext()){
					final Property prop = (Property)props.next();
					System.out.println("-->\tp: " + prop.getName() + " = " + prop.getValue().getString());
				}
				
				if(! child.getPrimaryNodeType().isNodeType( "rep:system" ) ){
					final VersionHistory history = child.getVersionHistory();
					System.out.println("--> labels: " + Arrays.toString( history.getVersionLabels() ));
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			session.logout();
		}
	}
}
```

It's nothing fancy, just a little code to poke around. There was apparently an Eclipse plug-in for navigating through JCR repos, but it has since died away. (Also, if you get errors running the code above, remove the last if block related to versioning... the versioning functionality is not well documented and this was bombing a lot for me). Interesting stuff, but not really what I was looking for. I will keep it in mind for future use.

## Further down the rabbit hole

So far, the JCR is a very interesting and seemingly under-utilized API, but I for the purpose of this discussion I am going to assume that you already have a bare minimum of understanding of Jackrabbit and the JCR. The documentation is a little thin in some parts so I decided to share some of my experimentation with `NodeType` registry and SQL searching of Nodes.

The registration of custom NodeTypes is apparently not part of the 1.0 spec, since the only implementation for registering custom node types seems to be in a Jackrabbit-specific class, though it does have a note about changes in JCR 2.0 moving the functionality into the core interface, which is nice. The basic code needed to load and register node types is as follows:

```java
final NodeTypeManagerImpl manager = (NodeTypeManagerImpl)session.getWorkspace().getNodeTypeManager();
manager.registerNodeTypes(new FileInputStream(cndFile), NodeTypeManagerImpl.TEXT_X_JCR_CND, true);
```

The `registerNodeTypes(...)` boolean parameter being true forces a reload of the node types even if they
have already been installed. You will notice that the node types are contained in a separate "cnd" file (see
[Node Type Definition](http://jackrabbit.apache.org/node-type-notation.html)). The file I used for my
test was as follows:

```
<baggage = 'http://baggage.sourceforge.net/baggage'>[baggage:Note] > nt:base - baggage:text (string) = ''
primary
mandatory autocreated
version
```

I am not sure at this point whether or not this is a grammar that they created themselves or if it is some external
standard. It seems a bit clunky, but it gets the job done. Basically I am creating a namespace called "baggage" and
adding a new node type to it called "baggage:Note" which will have one auto-created required property called
"baggage:text". It seems from some other tests I have done that you really need your own node types if you are going to
do any querying of your content. You can use the default types but it could get really cluttered and cause your searches
to slow down (Note: it uses Lucene internally for indexing and searching). Once the new node types have been registered
we can use them simply by creating nodes with them:

```java
final Node root = session.getRootNode();
final Node notesRoot = root.addNode("notes-root");

final Node node1 = notesRoot.addNode("Something Interesting","baggage:Note");
node1.setProperty("baggage:text","This is some note that I would write.");

final Node node2 = notesRoot.addNode("Another Note","baggage:Note");
node2.setProperty("baggage:text","More really cool text content.");
```

which creates a "notes-root" node off of the main root node and then adds some "baggage:Note" nodes to it. Once you
have a few nodes with content it might be nice to search through them. The JCR gives you a fairly straight-forward query
functionality using either XPath or SQL. I chose SQL for my test. Let's find all the "baggage:Note" nodes that contain
the text 'cool'. The following code shows how this is done:

```java
final QueryManager queryManager = session.getWorkspace().getQueryManager();

final String sql = "select * from baggage:Note where contains(baggage:text,'cool')";
final Query xq = queryManager.createQuery(sql, Query.SQL);
final QueryResult result = xq.execute();

final RowIterator rows = result.getRows();
log.info("Found: " + rows.getSize());
while(rows.hasNext()){
    final Row row = rows.nextRow();
    for(final String col : result.getColumnNames()){
        log.info(col + " = " + row.getValue(col).getString());
    }
    log.info("---");
}
```

Using my sample nodes, it will find one matching node and send the following output to the log appender:

```
556  [main] INFO  net.sourceforge.baggage.jcrex.JcrExp2  - Found: 1
560  [main] INFO  net.sourceforge.baggage.jcrex.JcrExp2  - baggage:text = More really cool text content.
560  [main] INFO  net.sourceforge.baggage.jcrex.JcrExp2  - jcr:primaryType = baggage:Note
563  [main] INFO  net.sourceforge.baggage.jcrex.JcrExp2  - jcr:path = /notes-root/Another Note
564  [main] INFO  net.sourceforge.baggage.jcrex.JcrExp2  - jcr:score = 5035
564  [main] INFO  net.sourceforge.baggage.jcrex.JcrExp2  - ---
```

The code for the whole test class is shown below:

```java
public class JcrExp2 {
    private static final Logger log = LoggerFactory.getLogger(JcrExp2.class);

    public static void main(final String[] args) throws Exception {
        final Repository repository = new TransientRepository();
        final Session session = repository.login(new SimpleCredentials("username", "password".toCharArray()));

        final String cndFile = "baggage.cnd";

        final NodeTypeManagerImpl manager = (NodeTypeManagerImpl)session.getWorkspace().getNodeTypeManager();
        manager.registerNodeTypes(new FileInputStream(cndFile), NodeTypeManagerImpl.TEXT_X_JCR_CND, true);
        log.info("Registered Node types");

        try {
            final Node root = session.getRootNode();
            final Node notesRoot = root.addNode("notes-root");
            log.info("Added NotesRoot: " + notesRoot);

            final Node node1 = notesRoot.addNode("Something Interesting","baggage:Note");
            node1.setProperty("baggage:text","This is some note that I would write.");

            final Node node2 = notesRoot.addNode("Another Note","baggage:Note");
            node2.setProperty("baggage:text","More really cool text content.");

            session.save();

            // query
            final QueryManager queryManager = session.getWorkspace().getQueryManager();

            //          final String sql = "select * from baggage:Note";
            final String sql = "select * from baggage:Note where contains(baggage:text,'cool')";
            final Query xq = queryManager.createQuery(sql, Query.SQL);
            final QueryResult result = xq.execute();
            final RowIterator rows = result.getRows();
            log.info("Found: " + rows.getSize());
            while(rows.hasNext()){
                final Row row = rows.nextRow();
                for(final String col : result.getColumnNames()){
                    log.info(col + " = " + row.getValue(col).getString());
                }

                log.info("---");
            }

            // cleanup

            notesRoot.remove();
            session.save();
        } catch(final Exception e){
            log.error("Something bad has happened! " + e.getMessage(),e);
        } finally {
            session.logout();
        }
    }
}
```

You will need the JCR 1.0 jar as well as the jackrabbit 1.6 jar, both of which can be downloaded from the Jackrabbit downloads
page. The JCR seems pretty useful and flexible. I am surprised that it is not used more than it seems to be.

## Versioning

I figured out the basics of how to use its versioning functionality and decided I should do a little post about it so that I don't lose the code somewhere in the shuffle that is my project-space.

I'm going to start out by dumping out the code for my little test runner:

```java
public class JcrVersioning {
    private static final Logger log = LoggerFactory.getLogger(JcrVersioning.class);
    public static void main(final String[] args) throws Exception {
        final Repository repository = new TransientRepository();
        final Session session = repository.login(new SimpleCredentials("username", "password".toCharArray()));

        try {
            final Node root = session.getRootNode();
            // Store content
            final Node textNode = root.addNode("mynode");

            final Node noteNode = textNode.addNode("alpha");
            noteNode.addMixin("mix:versionable");
            noteNode.setProperty("content", "I like jackrabbit");

            session.save();
            noteNode.checkin();

            // Retrieve content
            final Node node = root.getNode("mynode/alpha");
            log.info("Path: " + node.getPath() + " --> " + node.getProperty("content").getString());

            noteNode.checkout();
            noteNode.setProperty("content","Jackrabbit is cool");

            session.save();
            noteNode.checkin();

            log.info("After Modification: " + noteNode.getPath() + " --> " + noteNode.getProperty("content").getString());

            ////
            final VersionHistory vh = noteNode.getVersionHistory();
            final VersionIterator vi = vh.getAllVersions();

            vi.skip(1);
            while (vi.hasNext()) {
                final Version v = vi.nextVersion();
                final NodeIterator ni = v.getNodes();

                while (ni.hasNext()) {
                    final Node nv = ni.nextNode();
                    log.info(" - Found version: " + v.getCreated().getTime() + " --> " + nv.getProperty("content").getString());
                }
            }

            noteNode.checkout();

            final VersionHistory versionHistory = noteNode.getVersionHistory();
            final VersionIterator versionIterator = versionHistory.getAllVersions();

            versionIterator.skip(versionIterator.getSize()-2);

            noteNode.restore(versionIterator.nextVersion(), true);
            noteNode.checkin();

            log.info("After Restore: " + noteNode.getPath() + " --> " + noteNode.getProperty("content").getString());

            // Remove content
            root.getNode("mynode").remove();
            session.save();
        } finally {
            session.logout();
        }
    }
}
```

which creates a couple nodes, one of which has content. The content is set, modified and then restored to it's
initial revision using the JCR versioning functionality. The enabler for versioning the the `mix:versionable`
mixin set on the versionable node. Once that is set you need to be cautious of how your `save()` and `checkout()`
and `checkin()` calls interact. When you run this example you will get something like:

```
INFO  JcrVersioning  - Path: /mynode/alpha --> I like jackrabbit
INFO  JcrVersioning  - After Modification: /mynode/alpha --> Jackrabbit is cool
INFO  JcrVersioning  -  - Found version: Wed Oct 28 19:26:59 CDT 2009 --> I like jackrabbit
INFO  JcrVersioning  -  - Found version: Wed Oct 28 19:26:59 CDT 2009 --> Jackrabbit is cool
INFO  JcrVersioning  - After Restore: /mynode/alpha --> I like jackrabbit
```

Like I said this is really just a storage are for some sample code I came up with... it is what it is. Hopefully it can help if you are stuck with JCR versioning.
