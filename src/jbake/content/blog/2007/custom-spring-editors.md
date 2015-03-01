title=Custom Spring Editors
date=2007-11-01
type=post
tags=blog,java,spring
status=published
~~~~~~
I had the need recently to inject an array of strings into a bean property and I was curious about whether or not I could
inject the strings as comma-separated values (CSV). With a little poking around in the [Spring API](http://springframework.org/)
I found that the supporting `PropertyEditor` is already there, but not configured by default. My next question was about
how you go about configuring custom property editors. Configuring custom property editors is quite easy, you add a
`CustomEditorConfigurer` bean to your context which will register itself with the bean factory at load-time. By mapping
your custom editors to the `CustomEditorConfigurer`, you register them with the enclosing bean factory... pretty simple:

```xml
<bean id="customEditorConfigurer" class="org.springframework.beans.factory.config.CustomEditorConfigurer">
    <property name="customEditors">
        <map>
            <entry key="java.lang.String[]">
                <bean class="org.springframework.beans.propertyeditors.StringArrayPropertyEditor">
                    <constructor-arg value=":" />
                </bean>
            </entry>
        </map>
    </property>
</bean>
```

Note that the value of the entry key attribute is the full class name of the property type to be handled by the editor.
Only one editor can be registered for a given type. The ```StringArrayPropertyEditor``` is available with the core
Spring API and it will convert a delimited string into a string array. The delimiter is configurable as a constructor
argument; it defaults to comma, but I have overridden it here to use a colon in this case as an example. Once you have
this in place, the added configuration work is done. Let's create a simple test bean to ensure that the editor is
registered properly:

```java
public class SomeBean {
    private String[] array;

    public void setArray(String[] array) {this.array = array;}

    public String[] getArray() {return array;}
}
```

Add it to the spring context:

```xml
<bean id="someBean" class="SomeBean">
    <property name="array" value="one:two:three:four" />
</bean>
```

Now if you load the context and pull the bean out you will find that the array property contains four elements, with
values of "one", "two", "three", and "four" respectively. It's just that easy! Just to verify that we have not lost any
pre-existing functionality, you can add another bean that loads the array using the spring list tag:

```xml
<bean id="someBean2" class="SomeBean">
    <property name="array">
        <list>
            <value>alpha</value>
            <value>bravo</value>
            <value>charlie</value>
        </list>
    </property>
</bean>
```

You will notice that this method still works fine as well. There are a few other custom editors that spring provides in
the `org.springframework.bean.propertyeditors` package, and it is also quite easy to implement your own, but I
will save that for another day.
