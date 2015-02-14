title=Simple Configuration DSL using Groovy
date=2014-07-19
type=post
tags=blog,java,groovy
status=published
~~~~~~
Recently at work we were talking about being able to process large configuration files from legacy applications where the config file had a fairly simple text-based format. One of my co-workers mentioned that you could probably just run the configuration file like a Groovy script and just handle the `missingMethod()` calls and use them to populate a configuration object. This sounded like an interesting little task to play with so I threw together a basic implementation - and it's actually easier than I thought.

To start out with, we need a configuration holder class, which we'll just call `Configuration`:

```groovy
class Configuration {
    String hostName
    String protocol
    int port
    Headers headers
}
```

Say we are collecting configuration information for some sort of HTTP request util or something, it's a contrived example, but shows the concept nicely. The `Headers` class is a simple delegated builder in itself, and looks like:

```groovy
@ToString(includeNames=true)
class Headers {
    Map<String,Object> values = [:]

    static Headers headers( Closure closure ){
        Headers h = new Headers()
        closure.delegate = h
        closure()
        return h
    }
    
    void header( String name, value ){
        values[name] = value
    }
}
```

I won't explain much about the `Headers` class, other than it takes a closure and delegates the method calls of it onto a `Headers` instance to populate it. For our purposes it just makes a nice simple way to show closure usage in the example.

Now, we need a configuration file to load. It's just a simple text file:

```text
hostname 'localhost'
protocol = 'https'
port 2468

headers {
    header 'Content-type','text/html'
    header 'Content-length',10101
}
```

The script-based configuration is similar to the delegated builder, in that the method calls of the "script" (text configuration file) will be delegated to an instance of the `Configuration` class. For that to work, we could override the `missingMethod()` method and handle each desired operation, or if we have a good idea of the configuration (as we do in our case), we could just add the missing methods, as follows:

```groovy
@ToString(includeNames=true)
class Configuration {

    String hostName
    int port
    Headers headers
   
    void hostname( final String name ){
        this.hostName = name
    }

    void port( final int port ){
        this.port = port
    }
    
    void headers( final Closure closure ){
        this.headers = Headers.headers( closure )
    }
}
```

Basically, they are just setters in our case; however, you could do whatever conversion or validation you need, they're just method calls. Also, notice that the `protocol` property in the configuration file is actually setting the property directly with an equals `=` rather than using a method call - this is also valid, though personally I like the way it looks without all the equals signs.

The final part needed to make this work, is the Groovy magic. We need to load the text as a script in a `GroovyShell`, parse it and run it. The whole code for the `Configuration` object is shown below:

```groovy
@ToString(includeNames=true)
class Configuration {

    String hostName
    String protocol
    int port
    Headers headers
   
    void hostname( final String name ){
        this.hostName = name
    }

    void port( final int port ){
        this.port = port
    }
    
    void headers( final Closure closure ){
        this.headers = Headers.headers( closure )
    }

    static Configuration configure( final File file ){
        def script = new GroovyShell(
            new CompilerConfiguration(
                scriptBaseClass:DelegatingScript.class.name 
            )
        ).parse(file)

        def configuration = new Configuration()
        script.setDelegate( configuration )
        script.run()

        return configuration
    }
}
```

The important parts are the use of the `DelegatingScript` as the `scriptBaseClass` and then setting the `Configuration` instance as the delegate for the script. Now if you run the following:

```
def conf = Configuration.configure( new File('conf.txt') )
println conf
```

You get something like the following output: 

```
Configuration(protocol:https, hostName:localhost, port:2468, headers:Headers(values:[Content-type:text/html, Content-length:10101]))
```

> Notice, that in the example we didn't define a method for `protocol`, which means that the only way you can set it in the configuration is as a property; however, we could use the property format to set the value of the other fields, such as `port` since there is a setter method available along with the helper method (options are nice).

Groovy makes simple DSLs, well... simple.