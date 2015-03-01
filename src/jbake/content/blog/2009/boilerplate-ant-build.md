title=Boilerplate Ant Build
date=2009-01-28
type=post
tags=blog,java,ant
status=published
~~~~~~
I have found it very useful to create a reusable boiler plate [Ant](http://ant.apache.org/) `build.xml` file that I use
as a starting point for all my projects. With a little bit of configuration property tweaking and perhaps a task
modification here or there you have a standardized build environment which can be used by your IDE or on the command line.

I have provided a copy of my boilerplate build file at the bottom of this posting. It is provided for use under a
creative commons license ([Creative Commons Attribution 3.0 United States License](http://creativecommons.org/licenses/by/3.0/us/)),
so please maintain the copyright header if you use or extend it.

If you look at the file you will see that the first section has all the properties you need. I used in-file properties rather
than a separate properties file because I don't really change them often for any given project and it keeps everything
all in one neat little file. Technically you could get more re-usability by using properties files, but that assumes
that you wont be modifying the build file itself for your project, which is something I do from time to time if
something is not quite right for the project.

The first thing you will want to do is update the project name attribute to reflect the name of your project. Then you
will want to update the configuration properties so that everything is correct for your build environment. I tried to
keep everything pretty standard so that they don't need to change much between projects.

The first property is the `war.name` which is the name you want the generated war file to have. I usually leave it set to
the default, which is the project name.

```xml
<property name="war.name" value="${ant.project.name}" />
```

The next set of properties are the locations of your source directories, which default to the same values that I have set in my IDE

```xml
<property name="src.dir" value="src" />
<property name="test.src.dir" value="test" />
<property name="web.src.dir" value="web" />
```

and then your external library directory.


```xml
<property name="lib.dir" value="lib" />
```

There has been some confusion around the external library directory property when I have shown this to others. This
is not where all your libraries go, but where you put the libraries you _don't_ want in the war file. Things like
JUnit and JMock jars go in there so that they are usable for testing and compiling but you don't really want them to go
into your deployed artifact. I the jars in this directory mapped on my classpath in my IDE too while those in the
WEB-INF/lib are picked up by default.

The next group of properties define the artifact build locations:

```xml
<property name="build.dir" value="build" />
<property name="src.build.dir" value="${build.dir}/classes" />
<property name="test.build.dir" value="${build.dir}/test-classes" />
<property name="test.report.dir" value="${build.dir}/test-reports" />
<property name="webapp.build.dir" value="${build.dir}/webapp" />
```

As you can see, even if your `build` directory is called something else, like `bin` you can change everything else by updating
that one property, `build.dir`.

The next property is the location of your local web server, where the war file would be deployed locally. The default is
my symlink to the `webapps` directory for my local [Tomcat](http://tomcat.apache.org/) server installation.

```xml
<property name="deploy.local.dir" value="/usr/local/tomcat/webapps" />
```

The final two properties are more environmental. You want to specifiy the JVM version you are targetting and whether or
not you want debugging information to be compiled with your classes.

```xml
<property name="jvm.version" value="1.6" />
<property name="debug.enabled" value="true" />
```

You probably wont change these all that often, though it might be a good idea to disable debugging on production
builds; I will have to look into supporting that. The `jvm.version` setting is nice because I think Ant still
defaults to 1.3 or something like that.

The next section of the file contains all the task definitions. You
will want to tweak these every now and then if you have special needs. You can run the Ant project help command (`ant -p`)
to see the descriptions for all the tasks. Yes, I actually added descriptions for all of them.

Some common tasks I use a lot are:

```
ant clean-all test
```

Run all the tests on a clean build.

```
ant clean-all redeploy-local
```

Clean the build content and do a full local server redeployment.

```
ant clean-all test war
```

Run all the tests on a clean build and produce the war file.

I have used this in about five different projects now and it comes in really handy to have a standardized base point, especially
when you are in a hurry and trying to do a quick command line build of the project; your commands are the same across your projects.

The follow-up article to this one will delve more into using the Ant build as a tool set and about adding scripting layers
on top of the build to make repeated tasks bulletproof and quick.

Let me know if you have any suggestions for modifications or additions to this basic build script. It is a work in progress as
I try to bring more simplicity into my development processes.

You can find an updated version of this build script in my [AntBoilerplate](http://github.com/cjstehno/AntBoilerplate) project.
