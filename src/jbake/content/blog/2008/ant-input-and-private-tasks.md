title=Ant Input Prompting and Private Targets
date=2008-10-21
type=post
tags=blog,java,ant
status=published
~~~~~~
I have found the [Ant](http://ant.apache.org/) `input` tag useful lately for setting up runtime parameters of an Ant build.

We have a few different server configuration settings that vary based on which server the artifact is being built for
and the `input` tag makes this really easy:

```xml
<input message="Enter configuration name: " addproperty="config.name" defaultvalue="${config.name.default}" />
```

The downside of this is that it will prompt you to enter this every time you run the build, which can become
annoying and really prohibits automated building. This is where the <tt>unless</tt> attribute of the `target` tag
comes into play. First create a private target (one whose name starts with "-") that will prompt for the config name:

```xml
<target name="-prompt-for-config">
    <input message="Enter configuration name: " addproperty="config.name" defaultvalue="${config.name.default}" />
</target>
```

Then add the `unless` attribute to check for the presence of the `config.name` property:

```xml
<target name="-prompt-for-config" unless="config.name">
    <input message="Enter configuration name: " addproperty="config.name" defaultvalue="${config.name.default}" />
</target>
```

which will cause this task to be run only if the specified property is not set. The you can have other tasks depend
on this private task, which will only run if you have not specified the `config.name` property on the ant command
line.

```xml
<target name="compile" depends="-prompt-for-config" description="Compiles the java sources.">
    <!-- do stuff -->
</target>
```

Calling ant with the following will not prompt the user for the `config.name`:

```
ant compile -Dconfig.name=foo
```

I have used this in a few places now to make the build a bit more flexible, such as for doing server deployments,
artifact installations, etc. It is a handy ant trick to keep in mind.
