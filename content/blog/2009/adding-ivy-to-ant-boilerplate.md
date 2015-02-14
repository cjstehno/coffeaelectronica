title=Adding Ivy to Ant Boilerplate
date=2009-04-16
type=post
tags=blog,java
status=published
~~~~~~
I have wanted to delve deeper into [Ivy](http://ant.apache.org/ivy) for a while now, but something always pulled me away
before I got very far into it. Well today I had some time, so I did a little digging.

Ivy is a dependency management system along similar lines to that provided by [Maven](http://maven.apache.org/) but without
all of Maven's other features. Ivy _just_ does dependency management. Ivy uses a project (module) description file for
project configuration, generally named `ivy.xml` and put in the root of your project. Then you can add additional ant
tasks to manage the dependencies.

Dependency management puts the common jars you (and everyone else) use in a common repository so that you can have quick
standardized access to the jar and version that you need for each project. Ivy knows how to find these based on the
configuration of your dependencies in the `ivy.xml` file. The dependencies are then cached locally for your use
and added to projects as required.

A simple `ivy.xml` file is shown below:

```xml
<ivy-module version="2.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="http://ant.apache.org/ivy/schemas/ivy.xsd">
    <info organisation="com.stehno" module="foobar" />
    <configurations>
        <conf name="prod" description="Production environment conf."/>
        <conf name="dev" transitive="false" description="Testing environment conf."/>
    </configurations>
    <dependencies>
        <!-- For Production -->
        <dependency org="org.springframework" name="spring-webmvc" rev="2.5.6" conf="prod->default">
            <exclude org="commons-attributes"/>
            <exclude org="commons-digester" />
            <exclude org="jexcelapi" module="jxl"/>
            <exclude org="velocity" />
            <exclude org="org.apache.poi" />
            <exclude org="struts" />
            <exclude org="org.apache.tiles" />
            <exclude module="freemarker" />
            <exclude module="xml-apis" />
            <exclude module="jasperreports" />
            <exclude module="velocity-tools-generic" />
            <exclude module="velocity-tools-view" />
            <exclude module="itext" />
            <exclude module="jfreechart" />
            <exclude module="jcommon" />
        </dependency>

        <dependency org="commons-lang" name="commons-lang" rev="2.4" conf="prod->default"/>
            <!-- For Development -->
            <dependency org="junit" name="junit" rev="4.4" conf="dev->default"/>
            <dependency org="org.springframework" name="spring-test" rev="2.5.6" conf="dev->default" />
    </dependencies>
</ivy-module>
```

You see basic module information (info element) which is used to identify your project. The configurations define
various configurations available. In this case I have one `prod` and one `dev` configuration so that I can
define one set of dependencies that will be pushed out with the production artifact and one that is only used locally
for testing and development. The dependencies are defined in dependency elements using the standard Maven repository
conventions, the conf attribute being a reference to which configuration the dependency belongs to. The
`->default` is something I am not totally clear on, but I think it means that the dependency will also be
associated with the `default` configuration.

One downside I have noticed about Ivy is that when it pulls down dependencies, it pulls them all down without prejudice.
So when you have something like [Spring](http://springframework.org/) which has a lot of non-required dependencies, you get them all and
have to exclude those you don't want (as I did in the sample with the exclude elements). It's not horrible but you have
to do it by hand. If you don't really care about the size of your application you can just forget about it and let it
pull down everything. Something missing along these lines is a general exclusion that would exclude a dependent jar from
any module. The way it stands, if module A and module B both depend on the module C, which is not required, you will
have to exclude it from both dependency definitions.

The dependency management strategy that I am going with here is one that I hope will "stay out of my way". I was thinking about when dependency resolution is really needed.
Maven checks for dependency changes whenever you do anything (at least pre-2.0 did, I am not sure about 2.0) so that
even running `clean` caused a dependency check... how wasteful is that? In my opinion, dependency management
should be done when you want it done. When is it relevant?

* when you add or remove a dependency
* when you change a dependency version

How often do these events really happen? In a structured working environment these events often require buy-in from
other developers and/or managers. Even at home on your own projects, they only happen when you feel the need to change
one... not every time you run your build script. On a related note, IDEs tend to get cranky when you keep
adding/removing jars out from under them too.

This leads me to add three new targets to my [Boilerplate Ant File](http://www.coffeaelectronica.com/2009/01/boilerplate-ant-build.html):

The `depends` target to update the local dependencies in your project.

```xml
<target name="depends" description="Update the dependencies for the project.">
    <ivy:retrieve sync="true" conf="prod" pattern="${web.src.dir}/WEB-INF/lib/[artifact].[ext]" />
    <ivy:retrieve sync="true" conf="dev" pattern="${lib.dir}/[artifact].[ext]" />
</target>
```

The `clean-depends` target to clean out the dependency directories.

```xml
<target name="clean-depends" description="Clean out the managed dependencies.">
    <delete>
        <fileset dir="${web.src.dir}/WEB-INF/lib" includes="*.jar" />
        <fileset dir="${lib.dir}" includes="*.jar" />
    </delete>
</target>
```

And finally, the `depends-report` target to generate a nice report of all the project dependencies.

```xml
<target name="depends-report" depends="depends" description="Generates dependency report for the project.">
    <ivy:report todir="${depends.report.dir}" conf="dev,prod" />
</target>
```

> _NOTE:_ At this point you will have to add these to the boilerplate file if you are using it - I will be creating a
project for this build management stuff and sharing it out soon in a more official manner so stay tuned.

With these new targets you can refresh your dependencies and be ready to code with:

```
ant clean-all clean-depends depends test
```

Do this whenever you add/update/remove dependencies or when you pull the project out of source control... you don't
want to put your jars in source control any more if you are currently doing that. You can usually write rules/configurations
in your source control to keep out the jar files. This keeps the storage space down and the transfer time down since you
will have the jars stored in your local cache when you need them.

If you have never done automated dependency management you may not really see the value of it. You get the most benefit when you are
working in a multi-project environment, which I will be supporting and blogging about soon.

Be warned that this post really only scratches the surface of what ivy can do. I recommend visiting their web site and checking out the
documentation. They have decent documentation of all the config elements; however, their examples are a little on the weak side.
You have to get into their sample code to get a real helpful guide.

> You can find an updated version of this build script in my [AntBoilerplate](http://github.com/cjstehno/AntBoilerplate) project.
