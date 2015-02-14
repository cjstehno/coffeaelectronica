title=Integration Testing with JUnit and Maven
date=2013-02-09
type=post
tags=blog,java,maven,testing
status=published
~~~~~~
When I talk about unit testing, I generally mean small quick and mocked tests of individual chunks of functionality,
traditional unit testing. In some cases I will be testing slightly larger chunks, such as the interaction of a
controller and a service, with everything else mocked out; however, when you start using a real database (especially
not an embedded one) or needing to connect to other services in order to get a good solid test, you cross over into the
realm of integration testing.

Recently on one of my projects I had DAO integration tests that require a real database to test against, and it is not
embeddable (PostgreSQL using some non-standard features). I used a system property check to ensure that these integration
tests only ran when I wanted them to so that I could ensure that there was a database ready to test against. You would
have something like the following in your unit test:

```java
assumeTrue( "true".equalsIgnoreCase(System.getProperty("integration")) );
```

which would only continue with the test if the assumption was true, otherwise it stops without failing the test. With
this, when I wanted to run the integration tests I would just run them with that property, e.g.:

```
mvn test -Dintegration=true
```

This works, and it's not a horrible solution, but it does get annoying when testing from an IDE since you have to keep
setting that property on any new test runners, which can slow things down.

A better way would be to have the test be no different than any other test so that if you want to run it, you run it,
if not, you don't. I figured that there had to be a solution to this problem out there so a little research was in order.
I found out that maven has an "integration-test" phase (since Maven 3) and that there is a fork of the
[surefire](http://maven.apache.org/surefire/maven-surefire-plugin/) testing plugin called
[failsafe](http://maven.apache.org/surefire/maven-failsafe-plugin/)
to take advantage of this build phase.

Surefire is the unit test running plugin used by maven. You usually don't even realize it's there; Unless you have some
special configuration needs it just runs your tests for you. The failsafe fork of the plugin does the same functionality
but with slightly different configuration. Failsafe looks for tests ending in "IT" for "integration test" rather than
"Test" as a normal JUnit test. Also, failsafe runs in the integration-test phase rather rather than the test phase.

Now what I needed was a simple way to take advantage of this build phase. I could have used the naming patterns, but I
prefer the "Test" suffix and wanted another option. I found an interesting alternative, the experimental categories
feature in [JUnit](http://junit.org/) (4.9+).

To get this running, you need to have something like the following config in your maven build plugins:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.13</version>
    <configuration>
        <excludedGroups>com.coffeaelectronica.Integration</excludedGroups>
    </configuration>
</plugin>
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-failsafe-plugin</artifactId>
        <version>2.13</version>
        <executions>
            <execution>
                <goals>
                    <goal>integration-test</goal>
                    <goal>verify</goal>
                </goals>
            </execution>
        </executions>
    <configuration>
        <groups>com.coffeaelectronica.Integration</groups>
        <includes>
            <include>**/*Test.*</include>
        </includes>
    </configuration>
</plugin>
```

This tells surefire to ignore any JUnit tests annotated with the Integration annotation and then configures failsafe to only consider classes ending in "Test" and having the Integration annotation. Remember, by default failsafe looks for "**/*IT.*".

With this, your tests either have no Category annotation, for a normal test, or something like the example shown below for Integration tests:

```java
@Category(Integration.class)
public class SomeDaoTest {
    // your integration test methods...
}
```

Now, when you run:

```
mvn test
```

You get only your normal unit tests being run, while running

```
mvn integration-test
```

will run only your integration tests.

Along with this, you retain the ability to run any single test in your IDE without having to do any extra configuration.

