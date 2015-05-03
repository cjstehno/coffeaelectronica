title=Secure REST in Spring
date=2015-05-04
type=post
tags=blog,groovy
status=published
~~~~~~
Getting HTTPS to play nice with REST and non-browser web clients in development (with a self-signed certificate) can be a frustrating effort. I struggled for a while down the path of using the Spring `RestTemplate` thinking that since I was using Spring MVC as my REST provider, it would make things easier; in this case, Spring did not come to the rescue, but Groovy did or rather the Groovy [HTTPBuilder](https://github.com/jgritman/httpbuilder) did.

To keep this discussion simple, we need a simple REST project using HTTPS. I found the [Spring REST Service Guide](https://github.com/spring-guides/gs-rest-service) project useful for this (with a few modifications to follow).

Go ahead and clone the project:

    git clone git@github.com:spring-guides/gs-rest-service.git

Since this is a tutorial project, it has a few versions of the code in it. We are going to work with the "complete" version, which is a Gradle project. Let's go ahead and do a build and run just to ensure everything works out of the box:

    cd gs-rest-service/complete
    ./gradlew bootRun
    
After a bunch of downloading and startup logging you should see that the application has started. You can give it a test by opening `http://localhost:8080/greeting?name=Chris` in your browser, which should respond with:

```json
{
    "id": 2,
    "content": "Hello, Chris!"
}
```

Now that we have that running, we want a RESTful client to call it rather that hitting it using the browser. Let's get it working with the simple HTTP case first to ensure that we have everything working before we go into the HTTPS configuration. Create a groovy script, `rest-client.groovy` with the following content:

```groovy
@Grapes(
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
)

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET

def http = new HTTPBuilder( 'http://localhost:8080/' )

http.get( path: 'greeting', query:[name:'Chris'] ) { resp, json ->
    println "Status: ${resp.status}"
    println "Content: $json"
}
```

Since this is not a discussion of HTTPBuilder itself, I will leave most of the details to your own research; however, it's pretty straight forward. We are making the same request we made in the browser, and after another initial batch of dependency downloads (grapes) it should yield:

```
Status: 200
Content: [content:Hello, Chris!, id:6]
```
 
Ok, our control group is working. Now, let's add in the HTTPS. For the Spring Boot project, it's pretty trivial. We need to add an `application.properties` file in `src/main/resources` with the following content:

```
server.port = 8443
server.ssl.key-store = /home/cjstehno/.keystore
server.ssl.key-store-password = tomcat
server.ssl.key-password = tomcat
```

Of course, update the key-store path to your home directory. For the server, we also need to install a certificate for our use. 

> I am not a security certificate expert, so from here on out I will state that this stuff works in development but I make no claims that this is suitable for production use. Proceed at your own risk!

From the [Tomcat 8 SSL How To](http://tomcat.apache.org/tomcat-8.0-doc/ssl-howto.html), run the `keytool -genkey -alias tomcat -keyalg RSA` and run through the questions answering everything with 'localhost' (there seems to be a reason for this).

At this point you should be able to restart the server and hit it via HTTPS (https://localhost:8443/greeting?name=Chris) to retrieve a successful response as before, though you will need to accept the self-signed certificate.

Now try the client. Update the URL to the new HTTPS version:

    def http = new HTTPBuilder( 'https://localhost:8443/' )

and give it a run. You should see something like:

```
Caught: javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
```

I will start with the simplest method of resolving this problem. HTTPBuilder provides a configuration method that will just ignore these types of SSL errors. If you add:

    http.ignoreSSLIssues()
    
before you make a request, it will succeed as normal. This should be used only as a development configuration, but there are times when you just want to get something workign for testing. If that's all you want here, you're done. From here on out I will show how to get the SSL configuration working for a more formal use case.

Still with me? Alright, let's have fun with certificates! The [HTTPBuilder wiki page for SSL](https://github.com/jgritman/httpbuilder/wiki/SSL) gives us most of what we need. To summarize, we need to export our server certificate and then import it into a keyfile that our client can use. To export the server certificate, run:

    keytool -exportcert -alias "tomcat" -file mytomcat.crt -keystore ~/.keystore -storepass tomcat

which will export the "tomcat" certificate from the keystore at "~/.keystore" (the one we created earlier) and save it into "mytomcat.crt". Next, we need to import this certificate into the keystore that will be used by our client as follows:

    keytool -importcert -alias "tomcat" -file mytomcat.crt -keystore clientstore.jks -storepass clientpass
    
You will be asked to trust this certificate, which you should answer "yes" to continue.

Now that we have our certificate ready, we can update the client script to use it. The client script becomes:

```groovy
@Grapes(
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
)

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import java.security.KeyStore
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory

def http = new HTTPBuilder( 'https://localhost:8443/' )

def keyStore = KeyStore.getInstance( KeyStore.defaultType )

new File( args[0] ).withInputStream {
   keyStore.load( it, args[1].toCharArray() )
}

http.client.connectionManager.schemeRegistry.register(new Scheme("https", new SSLSocketFactory(keyStore), 443) )

http.get( path: 'greeting', query:[name:'Chris'] ) { resp, json ->
    println "Status: ${resp.status}"
    println "Content: $json"
}
```

The main changes from the previous version are the loading and use of the keystore by the connection manager. When you run this version of the script, with:

```
groovy rest-client.groovy clientstore.jks clientpass
```

you get:

```
Status: 200
Content: [content:Hello, Chris!, id:1]
```

We are now using HTTPS on both the server and client for our rest service. It's not all that bad to setup once you figure out the steps, but in general the information seems to be tough to find.
