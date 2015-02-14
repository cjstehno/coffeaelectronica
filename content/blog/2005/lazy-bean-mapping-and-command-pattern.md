title=Lazy Bean Mapping and the Command Pattern
date=2005-10-26
type=post
tags=blog,java
status=published
~~~~~~
In doing some refactoring recently, I came up with a useful base class for my command pattern managers (manager
beans that have a bunch of commands associated with them), here I will call it the `LazyBeanManager`.

Consider a case where you have a manger that manages a bunch of actions, and that you want the whole setup to be very
lazy -- only load the manager when it is used AND only load a command action when it is requested. This is very simple to
accomplish. First assume you have the following spring config:

```xml
<bean id="manager" class="example.MyManager" lazy-init="true">
    <property name="commands">
        <map>
            <entry key="cmdA" value="commandA" />
            <entry key="cmdB" value="commandB" />
        </map>
    </property>
</bean>

<bean id="commandA" class="example.CommandA" lazy-init="true" />

<bean id="commandB" class="example.CommandB" lazy-init="true" />
```

You see that we have a simple command pattern configured. There is a manager which somehow routes control to a
command bean. The first bit of laziness you should notice is the `lazy-init="true"` set on all three beans. This
tells Spring not to load these beans until something else references them. The second bit of laziness is that the
command beans are referenced by value (their bean id) rather than reference, which prevents the command beans from
loading when the manager bean loads. At this point, Spring does not know how to do anything with the map of command
beanIds. The following simple class will take care of that:

```java
public class MyManager implements ApplicationContextAware {
    private Map commands;
    private ApplicationContext context;

    public setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    public void setCommands(Map commands){ this.commands = commands; }

    public void execute(String commandId){
        Command cmd = findCommand(commandId);
        if(cmd != null){ cmd.execute(); }
    }

    protected Command findCommand(String cmdId){
        Command obj = null;
        String beanId = MapUtils.getString(mappings,cmdId);
        if(StringUtils.isNotEmpty(beanId)){
            obj = (Command)context.getBean(beanId,Command.class);
        }
        return(obj);
    }
}
```

> _Note:_ Some un-important classes are not shown here. This is just to give you the general idea of the manager.

You will notice that the `ApplicationContext` is injected into the manager by Spring through the implementation
of the `ApplicationContextAware` interface. Now, when your application needs to execute one of these commands,
you simply pull the manager bean out of spring (which will load only the manager), call the execute(String) method with
the desired command id. Then, only the desired command bean will be loaded and executed. This has come in very handy
recently for handling front end interface actions. This laziness allows the system to start up faster, be more stable,
and more memory efficient. Sometimes being lazy is a good thing.

In my development, I have created an abstract lazy mapping manager using this strategy... it works great. What I would like
to come up with is a version of this that not only does lazy loading, but then unloads the bean after a period of idle
time - I am not sure whether Spring currently supports this or not. Something to look into.
