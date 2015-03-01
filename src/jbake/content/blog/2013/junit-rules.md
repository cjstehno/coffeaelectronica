title=JUnit Rules
date=2013-03-28
type=post
tags=blog,java,groovy,testing
status=published
~~~~~~
No, the title is not simply an expression of my love of [JUnit](http://junit.org/), but rather specifies that I will be talking
about the `@Rule` annotations provided by JUnit... and yes, they do "rule".

Out of the box, JUnit has a [handful of useful rules](https://github.com/junit-team/junit/wiki/Rules) defined for things like
temporary folder and test timeouts. With this post I am going to focus on writing my own rules using extensions of the
[ExternalResource](https://github.com/junit-team/junit/wiki/Rules#externalresource-rules) rule class.

Suppose we are doing some unit testing of database access code using JDBC. Mocking direct JDBC calls is tedious and not very
productive so we will be using a real database for testing. To keep this post a simple and straight-forward as possible
without forsaking useful content, I am going to use Groovy for the examples and assume that we are using the Spring JDBC
framework and some random database.<br /><br />We have a PersonDao for storing the name and email address of people in
the database.

```groovy
class PersonDao {
    JdbcTemplate jdbcTemplate

    void createPerson( person ){
        jdbcTemplate.update('insert into people (name,email) values (?,?)', person.name, person.email )
    }
}
```

We are only going to worry about a simple create operation since we are discussing the rules, not the testing itself.
We first need to have a test case to work with:

```groovy
class PersonDaoTest {
    private PersonDao personDao

    @Before void before(){
        personDao = new PersonDao(
            jdbcTemplate: null // ?
        )
    }
}
```

Right out of the gate we run into our first hurdle... we need a `JdbcTempate` to inject. We could just connect to a
database or fire up an embedded database right here and move on, but we can assume that if there is one of these
tests, there will be many so a reusable solution would be best. Enter the JUnit rules. Basically, the rules are just
reusable code that implements a simple interface to provide operations before and after test classes or methodes
(depending on the rule annotation).

For our first rule, we want to setup a database environment to test with.

```groovy
class DatabaseEnvironment extends ExternalResource {
    DataSource dataSource

    JdbcTemplate getJdbcTemplate(){
        new JdbcTemplate(dataSource: dataSource)
    }

    @Override
    protected void before() throws Throwable {
        Connection conn
        try {
            conn = getDataSource().getConnection()
            final Liquibase liquibase = new Liquibase(
                "src/main/resources/changelog.xml",
                new FileSystemResourceAccessor(),
                new JdbcConnection( conn )
            )
            liquibase.dropAll()
            liquibase.update( "test" )
        } catch( ex ){
            fail(ex.message)
        } finally {
            conn?.close()
        }
    }
}
```

Remember, we are assuming that you have some `DataSource` that you are using for testing. When the `before()` method is
called, our database is destroyed if it exists and is then recreated to the fresh empty state. I am using liquibase for
database management, but any means of creating and destroying your database would work here.

> Note: that I do not destroy the database in the `after()` method. This is intentional; it allows you to investigate the data conditions of a failed test.

We can now integrate this into the test case and move forward:

```groovy
class PersonDaoTest {

    @ClassRule public DatabaseEnvironment dbEnvironment = new DatabaseEnvironment(
        dataSource: myTestDataSource // you must define somewhere
    )

    private PersonDao personDao

    @Before void before(){
        personDao = new PersonDao(
            jdbcTemplate: dbEnvironment.jdbcTemplate
        )
    }
}
```

I defined the DatabaseEnvironment as a `@ClassRule` so that the database is created once for each test class, rather than
for every test method. Now we can add an actual test method.

```groovy
class PersonDaoTest {

    @ClassRule public DatabaseEnvironment dbEnvironment = new DatabaseEnvironment(
        dataSource: myTestDataSource // you must define somewhere
    )

    private PersonDao personDao

    @Before void before(){
        personDao = new PersonDao(
            jdbcTemplate: dbEnvironment.jdbcTemplate
        )
    }

    @Test void 'createPerson: simple'(){
        personDao.createPerson([ name:'Chris', email:'chris@stehno.com' ])

        assert 1 == JdbcTestUtils.countRowsInTable(dbEnvironment.jdbcTemplate, 'people')
    }
}
```

The test runs and passes with a fresh database every time. There is still a hidden problem here though, let's add another
test method. This is a bit arbitrary but let's test the case when you add a person with no email address (successfully);
we add the following test method:

```groovy
@Test void 'createPerson: simple'(){
    personDao.createPerson([ name:'Chris' ])

    assert 1 == JdbcTestUtils.countRowsInTable(dbEnvironment.jdbcTemplate, 'people')
}
```

Now, if you run all the tests (not just the one you added), the test will fail with a value of 2 where 1 was expected.
Why? The database is created and destroyed per-class, not per-test so you are working with a database that already has
data in it. To get around this we could make the database work per-test, but depending on how large your schema is,
this could be time consuming and greatly increase your test runtime. What we want is to clean up the existing database
in-place after each test. Another `ExternalResource` rule to the rescue!

```groovy
class DatabaseCleaner extends ExternalResource {
    JdbcTemplate jdbcTemplate

    def tables = []

    @Override
    protected void before() throws Throwable {
        tables.each { table->
            jdbcTemplate.execute("truncate table $table cascade")
        }
    }
}
```

Here we have defined an `ExternalResource` rule which will truncate a specified collection of tables each time the `before()`
method is called. We want to use this as an instance rule, and again, we do nothing in the `after()` method so that our
data is in a known-failed state for a failed test. Our test case becomes:

```groovy
class PersonDaoTest {

    @ClassRule public DatabaseEnvironment dbEnvironment = new DatabaseEnvironment(
        dataSource: myTestDataSource // you must define somewhere
    )

    @Rule public DatabaseCleaner dbCleaner = new DatabaseCleaner(
        jdbcTemplate: dbEnvironment.jdbcTemplate,
        tables:['people']
    )

    private PersonDao personDao

    @Before void before(){
        personDao = new PersonDao(
            jdbcTemplate: dbEnvironment.jdbcTemplate
        )
    }

    @Test void 'createPerson: simple'(){
        personDao.createPerson([ name:'Chris', email:'chris@stehno.com' ])

        assert 1 == JdbcTestUtils.countRowsInTable(dbEnvironment.jdbcTemplate, 'people')
    }

    @Test void 'createPerson: simple'(){
        personDao.createPerson([ name:'Chris' ])

        assert 1 == JdbcTestUtils.countRowsInTable(dbEnvironment.jdbcTemplate, 'people')
    }
}
```

Now when we run the whole test case, we have both tests passing because before each test method, the database is cleaned
in-place.

With just these two rules we have created a stable and flexible means of testing database code. With configuration you
can point your tests at an in-memory database, locally running database or shared database server.   For normal unit
testing I would recommend either an embedded database, or when that is not possible a database running local to the
testing machine, but those strategies will have to be discussed another time.
