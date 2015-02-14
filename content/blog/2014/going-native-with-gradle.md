title=Going Native with Gradle
date=2014-03-16
type=post
tags=blog,java,groovy,gradle
status=published
~~~~~~
With my recent foray into Java game programming, I found the support for managing the native sub-dependencies of jar files to be a bit lacking in Gradle. I did find a few blog posts about the general ways of adding it to your build; however, I did not find any specific plugin or built-in support. Since I am planning on doing a handful of simple games as a tutorial for game programming it made sense for me to pull out my native library handling functionality into a Gradle plugin... and thus the [Gradle Natives Plugin](https://github.com/cjstehno/gradle-natives) was born.

First, we need a project to play with. I found a simple [LWJGL Hello World](http://philphilphil.wordpress.com/2009/05/28/helloworld-using-lwjgl/) application that works nicely for our starting point. So, create the standard Gradle project structure with the following files:

```java
// hello/src/main/java/hello/HelloWorld.java
package hello;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
 
public class HelloWorld {
    public static void main (String args[]){
        try {
            Display.setTitle("Hello World");
            Display.create();
			
			while(!Display.isCloseRequested()){
				Thread.sleep(100);      
			}
		
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
			Display.destroy();
		}
    }
}
```

with a standard Gradle build file as a starting point: 

```groovy
// hello/build.gradle

apply plugin:'java'

repositories {
	jcenter()
}

dependencies {
	compile 'org.lwjgl.lwjgl:lwjgl:2.9.1'
}
```

At this point, the project will build, but will not run without jumping through some extra hoops. Let's do some of that hoop-jumping in Gradle with the `application` plugin. Add the following to the `build.gradle` file:

```groovy
apply plugin:'application'

mainClassName = 'hello.HelloWorld'
```

This adds the `run` task to the build which will run the `HelloWorld` main class; however, this still won't work since it does not know how to deal with the LWJGL native libraries. That's where the `natives` plugin comes in. At this time there is no official release of the plugin on Bintray (coming soon), so you will need to clone the repo and build the plugin, then install it into your local maven repo:

```
git clone git@github.com:cjstehno/gradle-natives.git

cd gradle-natives

gradle build install
```

Once that is done, you will need to add the natives plugin to your build:

```groovy
buildscript {
    repositories {
        mavenLocal()
    }

    dependencies {
        classpath 'gradle-natives:gradle-natives:0.1'
    }
}

apply plugin:'natives'
```

And then you will need to apply the custom configuration for your specific native libraries. You will need to add an entry in the jars list for each dependency jar containing native libraries. These are the jars that will be searched on the classpath for native libraries by platform.

```groovy
natives {
	jars = [
		'lwjgl-platform-2.9.1-natives-windows', 
		'lwjgl-platform-2.9.1-natives-osx', 
		'lwjgl-platform-2.9.1-natives-linux'
	]
}
```

This will allow the associated native libraries to be unpacked into the build directory with:

`gradle unpackNatives`

Which will copy the libraries into a directory for each platform under `build/natives/PLATFORM`. Then we need one more step to allow it to be run. The `java.library.path` needs to be set before the run:

```groovy
run {
    systemProperty 'java.library.path', file( 'build/natives/windows' )
}
```

Then you can run the application using:

`gradle run`

Granted, there are still issues to be resolved with the plugin. Currently, it is a little picky about when it is run. If you have tests that use the native libraries you will need to build without tests and then run the tests:

```
gradle clean build unpackNatives -x test

gradle test
```

Lastly, you can also specify the platforms whose library files are to be copied over using the `platforms` configuration property, for example:

```groovy
natives {
	jars = [
		'lwjgl-platform-2.9.1-natives-windows', 
		'lwjgl-platform-2.9.1-natives-osx', 
		'lwjgl-platform-2.9.1-natives-linux'
	]
	platforms = 'windows'
}
```

Will only copy the windows libraries into the build.

Feel free to create an issue for any bugs you find or features you would like to see. Also, I am open to bug fixes and pull requests from others.