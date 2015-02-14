title=Javassist - Mind Blown
date=2013-05-25
type=post
tags=blog,javascript
status=published
~~~~~~
I have been doing a lot with Java reflection recently in one of my personal projects and while doing some research I came across the [Javassist](http://www.javassist.org) bytecode manipulation API.

Javassist allows you to create new classes and/or manipulate existing classes at runtime... at the bytecode level, and it does it without you having to understand all the deep down details of classfiles.

Let's take an example and say that I have an interface:

```java
package jsist;

public interface Greeter {

	String sayHello( String name );
	
	String sayGoodbye( String name );
}
```

It's very easy to dynamically implement that interface at runtime, but first we need a little demo application:

```java
package jsist;

public class Demo {

    private static final ClassPool CLASS_POOL = ClassPool.getDefault();
    private static CtClass STRING_CLASS;

    static {
        try{
            STRING_CLASS = CLASS_POOL.get( "java.lang.String" );
        } catch( NotFoundException e ){
            e.printStackTrace();
        }
    }

    public static void main( final String[] args ) throws Exception {
		useIt( implementIt() );
	}
	
	private static Class implementIt() throws Exception {
		// will contain our javassist code
	}
	
	private static void useIt( Class clazz ) throws  Exception {
        System.out.println( clazz );

        Greeter greeter = (Greeter)clazz.newInstance();

        System.out.println("Hi : " + greeter.sayHello("Bytecode"));
        System.out.println("Bye: " + greeter.sayGoodbye( "Java" ));
    }
}
```

This will give us a simple test bed for the various dynamic implementations of the Greeter interface. Basically, it builds an implementation of the interface, prints out the class and the result of executing the two methods. Now for the fun part.

Our first example will be a simple implementation of the interface:

```java
private static Class implementIt() throws Exception {
	CtClass greeterClass = CLASS_POOL.makeClass("jsist.gen.GreeterImpl");
	greeterClass.addInterface( CLASS_POOL.get("jsist.Greeter") );

	CtMethod sayHelloMethod = new CtMethod( STRING_CLASS, "sayHello", new CtClass[]{STRING_CLASS}, greeterClass );
	greeterClass.addMethod( sayHelloMethod );
	sayHelloMethod.setBody( "{return \\"Hello, \\" + $1;}" );

	CtMethod sayGoodbyeMethod = new CtMethod( STRING_CLASS, "sayGoodbye", new CtClass[]{STRING_CLASS}, greeterClass );
	greeterClass.addMethod( sayGoodbyeMethod );
	sayGoodbyeMethod.setBody( "return \\"Goodbye, \\" + $1;" );

	greeterClass.setModifiers(greeterClass.getModifiers() & ~Modifier.ABSTRACT);

	return greeterClass.toClass();
}
```

We start off by creating a new class called `jsist.gen.Greeter` where the package name does not need to exist; it will be created. We then need to add the interface we want to implement, the `jsist.Greeter` interface. Next we have to provide method implementations.

It feels a bit odd to create a `CtMethod` object with the `greeterClass` instance and then add the method to the instance, but this is the pattern that is used. I am sure there must be some internal reason for doing so.

The `setBody(String)` method is the key worker here. It allows you to provide source code as a template using the Javassist source template language. With what I have done above it it equivalent to:

```java
return "Hello, " + arg0;
```

for the `sayHello(String)` method, and similar for the other. The important thing to note here is that your provided source is compiled down to Java bytecode, this is not some embedded scripting language.

Next we need to change the modifiers of the class to remove "abstract", and then with a call to the `toClass()` method we have a standard Java `Class` object representing our newly created implementation.

If you run the demo with this, you will get:

```
class jsist.gen.GreeterImpl
Hi : Hello, Bytecode
Bye: Goodbye, Java
```

Ok, that was fun, but how about an abstract class? Let's say we have an abstract implemenation of the `Greeter` interface:

```java
public abstract class AbstractGreeter implements Greeter {

    @Override
    public String sayGoodbye( String name ){
        return "(Abstract) Goodbye, " + name;
    }
}
```

Note, I have implemented the `sayGoodbye(String)` method but not the `sayHello(String)` to make things more interesting. Our implementation of the `implementIt()` method now becomes:

```java
private static Class implementIt() throws Exception {
	CtClass greeterClass = CLASS_POOL.makeClass( "jsist.gen.GreeterImpl" );
	greeterClass.setSuperclass( CLASS_POOL.get("jsist.AbstractGreeter") );

	CtMethod sayHelloMethod = new CtMethod( STRING_CLASS, "sayHello", new CtClass[]{STRING_CLASS}, greeterClass );
	greeterClass.addMethod( sayHelloMethod );
	sayHelloMethod.setBody( "{return \\"Hello, \\" + $1;}" );

	greeterClass.setModifiers(greeterClass.getModifiers() & ~Modifier.ABSTRACT);

	return greeterClass.toClass();
}
```

The first difference to note is that now we are setting the superclass rather than the interface, since our superclass already implements the interface. Also, notice that since we already have an implementation of the `sayGoodbye(String)` method, we only need to implement `sayHello(String)`. Other than that, there is little difference. When you run with this implementation you get:

```
class jsist.gen.GreeterImpl
Hi : Hello, Bytecode
Bye: (Abstract) Goodbye, Java
```

As expected, our dynamic implementation plays nicely with the concrete implementation.

Now, what if you already have objects that implement the functionality of the two interface methods, but that do not implement the `Greeter` interface? Say, we have:

```java
public class Hello {

    public String say( String name ){
        return "(Delegate) Hello, " + name;
    }
}

public class Goodbye {

    public String say( String name ){
        return "(Delegate) Goodbye, " + name;
    }
}
```

You can easily implement the interface by copying the methods from these classes:

```java
private static Class implementIt() throws Exception {
	CtClass greeterClass = CLASS_POOL.makeClass("jsist.gen.GreeterImpl");
	greeterClass.addInterface( CLASS_POOL.get("jsist.Greeter") );

	CtClass helloClass = CLASS_POOL.get( "jsist.Hello" );
	CtMethod helloSay = helloClass.getMethod( "say", "(Ljava/lang/String;)Ljava/lang/String;" );

	CtMethod sayHelloMethod = new CtMethod( STRING_CLASS, "sayHello", new CtClass[]{STRING_CLASS}, greeterClass );
	greeterClass.addMethod( sayHelloMethod );
	sayHelloMethod.setBody( helloSay, null );


	CtClass gbClass = CLASS_POOL.get( "jsist.Goodbye" );
	CtMethod gbSay = gbClass.getMethod( "say", "(Ljava/lang/String;)Ljava/lang/String;" );

	CtMethod sayGoodbyeMethod = new CtMethod( STRING_CLASS, "sayGoodbye", new CtClass[]{STRING_CLASS}, greeterClass );
	greeterClass.addMethod( sayGoodbyeMethod );
	sayGoodbyeMethod.setBody( gbSay, null );

	greeterClass.setModifiers(greeterClass.getModifiers() & ~Modifier.ABSTRACT);

	return greeterClass.toClass();
}
```

This version is similar to the original interface implementation, except that now rather than providing source code for the method bodies, we provide a method object. You first find the `Hello` class in the `ClassPool` and then find it's `say(String)` method - the description string is the formal JVM parameter format, but I found it simple to dump out the methods and just copy it as a shortcut.

If you run this version, you get:

```
class jsist.gen.GreeterImpl
Hi : (Delegate) Hello, Bytecode
Bye: (Delegate) Goodbye, Java
```

Showing that both methods were from the delegate classes.

For our final example, to round things out, let's go back to the abstract class and provide a delegate for the abstract method rather than source:

```java
private static Class implementIt() throws Exception {
	CtClass greeterClass = CLASS_POOL.makeClass( "jsist.gen.GreeterImpl" );
	greeterClass.setSuperclass( CLASS_POOL.get("jsist.AbstractGreeter") );

	CtClass helloClass = CLASS_POOL.get( "jsist.Hello" );
	CtMethod helloSay = helloClass.getMethod( "say", "(Ljava/lang/String;)Ljava/lang/String;" );

	CtMethod sayHelloMethod = new CtMethod( STRING_CLASS, "sayHello", new CtClass[]{STRING_CLASS}, greeterClass );
	greeterClass.addMethod( sayHelloMethod );
	sayHelloMethod.setBody( helloSay, null );

	greeterClass.setModifiers(greeterClass.getModifiers() & ~Modifier.ABSTRACT);

	return greeterClass.toClass();
}
```

There is not really anything here, you have not already seen, but when you run it you see:

```
class jsist.gen.GreeterImpl
Hi : (Delegate) Hello, Bytecode
Bye: (Abstract) Goodbye, Java
```

As expected, one method provided by the delegate and one by the abstract class' implementation.

There are other bytecode manipulation libraries, but most of the ones I looked at seemed to be very abstract or probably closer to the actual class file format, whereas Javassist is a lot more familar when coming from a Java reflection background.

It seems very powerful and full of interesting potential. I am by no means an expert with it, but I wanted to share what I had found since the documentation is reasonably good, but not very rich with examples.
