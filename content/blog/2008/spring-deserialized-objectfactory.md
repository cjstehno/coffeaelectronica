title=Spring Deserialized Object Factory
date=2008-04-28
type=post
tags=blog,java,spring
status=published
~~~~~~
I recently had the idea that it would be interesting to have a [Spring](http://springframework.org/) factory bean that
would load a serialized object as its object contribution. I have no idea at this point what it would be useful for; it
was just something that popped into my head. My example below describes a license key system that is not based on
anything real so please don't expect this to be a good license key implementation... it's just an example of the
deserialized bean factory idea.

First, we need something to serialize. As I mentioned earlier, I am doing a quick and dirty license key system so the
key itself would be a good candidate for the serializable object. Let's create a license key object as follows:

```java
public class LicenseKey implements Serializable {
    private static final long serialVersionUID = -3151020875557193150L;
    private long created;
    private String key;

    public LicenseKey(){
        super();
    }

    public LicenseKey(final long created, final String key){
        this.created = created;
        this.key = key;
    }

    public long getCreated() {return created;}

    public String getKey() {return key;}
}
```

Notice that it implements the Serializable interface and has a couple of fields that we can later use to prove that
the bean is deserialized and loaded properly. Next, we need to serialize this object into a license key file, which will
later be used by the factory bean. I threw together a simple serializer which also utilizes the
[Jakarta Commons - Lang API](http://commons.apache.org/lang):

```java
public class Serializer {
    public static void main(final String[] args) throws FileNotFoundException {
        final LicenseKey key = new LicenseKey(System.currentTimeMillis(),UUID.randomUUID().toString());
        SerializationUtils.serialize(key, new FileOutputStream(new File("license.key")));
        System.out.println("Done");
    }
}
```

This just creates a simple key object and serializes it to the current directory as the "license.key" file, which
we will use later. Now that we have a serialized license key, we need a Spring factory bean to load and instantiate the
`LicenseKey` object it represents. The `DeserializedObjectFactory` is simply an extension of Spring's `AbstractFactoryBean`
that deserializes the given file resource and verifies that it is an instance of the specified class.

```java
public class DeserializedObjectFactory extends AbstractFactoryBean {
    private final Class<? extends Serializable> objectType;
    private final Resource resource;

    public DeserializedObjectFactory(final Class<? extends Serializable> objectType, final Resource resource){
        this.objectType = objectType;
        this.resource = resource;
    }

    @Override
    protected Object createInstance() throws Exception {
        final Object obj = SerializationUtils.deserialize(resource.getInputStream());
        Assert.isInstanceOf(objectType, obj, "Serialized object must be of type: " + objectType);
        return obj;
    }

    @Override
    public Class<? extends Serializable> getObjectType() {
        return objectType;
    }
}
```

The generic parameter restrictions and the instance checking add a bit of safety to the factory such that it keeps
you from loading something completely unexpected. In order to make use of these beans, we will need some client class to
actually use the deserialized object, and that's where the `LicenseVerifier` class comes in. The license verifier
is a simple class that takes a license key as a parameter and has a verification method that will be used to ensure that
a proper license key is configured.

```java
public class LicenseVerifier {
    private static final Log log = LogFactory.getLog(LicenseVerifier.class);
    private LicenseKey licenseKey;

    public void setLicenseKey(final LicenseKey licenseKey) {
        this.licenseKey = licenseKey;
    }

    public void verify(){
        // do some license verification
        Assert.notNull(licenseKey, "No license key exists!");
        Assert.notNull(licenseKey.getKey(), "Invalid key!");
        log.info("License verified: created: " + licenseKey.getCreated() + ", key: " + licenseKey.getKey());
    }
}
```

Finally we need the glue that brings all of this together, the Spring context file:

```xml
<bean id="license.key" class="spring.DeserializedObjectFactory">
    <constructor-arg value="spring.LicenseKey" />
    <constructor-arg value="license.key" />
</bean>

<bean id="license.verifier" class="spring.LicenseVerifier" init-method="verify">
    <property name="licenseKey" ref="license.key" />
</bean>
```

Notice the `init-method` on the license verifier bean; it is used to ensure that the license is verified
when the context starts up. With that, you can startup the context and watch the magic happen:

```java
new FileSystemXmlApplicationContext("context.xml")
```

You should get some miscellaneous logging and then the license verification log entry with populated data, which will
look something like this (of course your values will be different):

```
INFO: License verified: created: 1209321756486, key: 5ed38eba-799a-4b8c-9bfc-dd539c20bafe
```

That's all there is to it. Like I said, I am not sure what it would really be useful for but it was an interesting
little experiment. I found it interesting because it would be simple to swap out serialized object files as needed to
provide different instances, in this case different licenses. This concept could also be used with other "serialization"
methods, such as XML or some other custom serializer. I chose the default Java serialization just for ease of
demonstration.

Again I must mention that this simply a demo and is NOT intended to be a legitimate licensing strategy and it
contains MANY security holes. If you use it, you are on your own and I take no responsibility for the results.
