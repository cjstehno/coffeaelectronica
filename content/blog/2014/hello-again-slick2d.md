title=Hello Again Slick2D
date=2014-10-11
type=post
tags=blog,java,testing,mocking
status=published
~~~~~~
I am finally getting back around to working on my little game programming project and I realized that somewhere along the
way, my project stopped working. I am using the [Slick2D](http://slick.ninjacave.com/) library, which seems to have little 
in the way of formal release or distribution so it didn't surprise me. I think I had something hacked together making it 
work last time. I decided to try and put some more concrete and repeatable steps around basic setup, at least for how I use it - I'm no 
game programmer. 

I'm using Groovy as my development language and Gradle for building. In the interest of time and clarity, I am going to use a 
dump-and-describe approach here; there are only two files, so it should not be a big deal.

The `build.gradle` file is as follows:

```groovy
group = 'com.stehno.demo'
version = '0.1'

buildscript {
    repositories {
        jcenter()

        maven {
            url 'http://dl.bintray.com/cjstehno/public/'
        }
    }

    dependencies {
        classpath 'com.stehno:gradle-natives:0.2'
    }
}

apply plugin:'groovy'
apply plugin:'application'
apply plugin:'com.stehno.natives'

compileJava {
    sourceCompatibility = 1.8
    targetCompatibility = 1.8
}

mainClassName = 'helloslick.HelloSlick'

repositories {
    jcenter()
}

dependencies {
    compile 'org.codehaus.groovy:groovy-all:2.3.6'

    compile 'org.slick2d:slick2d-core:1.0.1'
}

test {
    systemProperty 'java.library.path', file('build/natives/windows')
}

run {
    systemProperty 'java.library.path', file('build/natives/windows')
}

natives {
    jars = [
        'lwjgl-platform-2.9.1-natives-windows.jar',
        'jinput-platform-2.0.5-natives-windows.jar'
    ]
    platforms = 'windows'
}

task wrapper(type: Wrapper) {
    gradleVersion = '2.1'
}
```

The first point of note, is that I am using my [Gradle Natives plugin](http://cjstehno.github.io/gradle-natives/), not as
a self-promotion, but since this is the reason I wrote it. This plugin takes care of extracting all the little native 
libraries and putting them in your build so that they are easily accessible by your code. The configuration is found near 
the bottom of the file, in the `natives` block - we want to extract the native libraries from the lwjgl and jinput libraries
for this project and in my case, I only care about the Windows versions (leave off `platforms` to get all platforms).

There was one interesting development during my time away from this project, a 3rd-party jar version of Slick2D has been pushed to maven central, which makes it a lot easier - I think I had to build it myself and fiddle with pushing it to my local maven repo or something. Now it's just another remote library (hopefully it works as expected - I have not played with it yet).

The last point of interest here is the use of the `application` plugin. This plugin provides an easy way to run your game
while specifying the `java.library.path` which is the painful part of running applications with native libraries. With the 
`application` plugin and the `run` configuration in place, you can run the game from Gradle - admittedly not ideal, but this 
is just development; I actually have a configuration set for the IzPack installer that I will write about later.

Now, we need some code to run, and the Slick2D wiki provides a simple Hello world sample that I have tweaked a bit for my 
use - mostly just cosmetic changes:

```groovy
package helloslick

import groovy.util.logging.Log
import org.newdawn.slick.*

import java.util.logging.Level

@Log
class HelloSlick extends BasicGame {

    HelloSlick(String gamename){
        super(gamename)
    }

    @Override
    public void init(GameContainer gc) throws SlickException {}

    @Override
    public void update(GameContainer gc, int i) throws SlickException {}

    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {
        g.drawString 'Hello Slick!', 50, 50
    }

    public static void main(String[] args){
        try {
            AppGameContainer appgc = new AppGameContainer(new HelloSlick('Simple Slick Game'))
            appgc.setDisplayMode(640, 480, false)
            appgc.start()

        } catch (SlickException ex) {
            log.log(Level.SEVERE, null, ex)
        }
    }
}
```

This just opens a game window and writes "Hello Slick!" in it, but if you have that working, you should be ready for playtime
with Slick2D.

Once you have the project setup (`build.gradle` in the root, and `HelloSlick.groovy` in `/src/main/groovy/helloslick`), you 
are ready to go. Run the following to run the project.

```gradle unpackNatives run```

And if all is well, you will see the game window and message.

Like I said, this is mostly just for getting my development environment up and running as a sanity check, but maybe it is useful to others.

> Yes, the explicit `unpackNatives` calls are annoying, it's something I am working on.
