title=Testing AST Transformations
date=2015-03-08
type=post
tags=blog,groovy,testing,vanilla
status=published
~~~~~~
While working on my [Effigy](https://github.com/cjstehno/effigy) project, I have gone deep into the world of Groovy AST Transformations and found that they are, in my opinion, the most interesting and useful feature of the Groovy language; however, developing them is a bit of a poorly-documented black art, especially around writing unit tests for your transformations. Since the code you are writing is run at compile-time, you generally have little access or view to what is going on at that point and it can be quite frustrating to try and figure out why something is failing.

After some Googling and experimentation, I have been able to piece together a good method for testing your transformation code, and it's actually not all that hard. Also, you can do your development and testing in a single project, rather than in a main project and testing project (to account for the need to compile the code for testing)

The key to making transforms testable is the `GroovyClassLoader` which gives you the ability to compile Groovy code on the fly:

    def clazz = new GroovyClassLoader().parseClass(sourceCode)

During that `parseClass` method is when all the AST magic happens. This means you can not only easily test your code, but also debug into your transformations to get a better feel for what is going wrong when things break - and they often do.

For my testing, I have started building a `ClassBuilder` code helper that is a shell for String-based source code. You provide a code template that acts as your class shell, and then you inject code for your specific test case. You end up with a reasonably clean means of building test code and instantiating it:

```groovy
private final ClassBuilder code = forCode('''
    package testing

    import com.stehno.ast.annotation.Counted

    class CountingTester {
        $code
    }
''')

@Test void 'single method'(){
    def instance = code.inject('''
        @Counted
        String sayHello(String name){
            "Hello, $name"
        }
    ''').instantiate()

    assert instance.sayHello('AST') == 'Hello, AST'
    assert instance.getSayHelloCount() == 1

    assert instance.sayHello('Counting') == 'Hello, Counting'
    assert instance.getSayHelloCount() == 2
}
```

The `forCode` method creates the builder and prepares the code shell. This construct may be reused for each of your tests.

The `inject` method adds in the actual code you care about, meaning your transformation code being tested.

The `instantiate` method uses the `GroovyClassLoader` internally to load the class and then instantiate it for testing.

I am going to add a version of the `ClassBuilder` to my [Vanilla](https://github.com/cjstehno/vanilla) project once it is more stable; however, I have a version of it and a simple AST testing demo project in the [ast-testing](https://github.com/cjstehno/coffeaelectronica/tree/master/ast-testing) CoffeaElectronica sub-repo. This sample code builds a simple AST Transformation for counting method invocations and writes normal unit tests for it (the code above is taken from one of the tests).

> Note: I have recently discovered the [groovy.tools.ast.TransformTestHelper](http://docs.groovy-lang.org/latest/html/gapi/org/codehaus/groovy/tools/ast/TransformTestHelper.html) class; I have not yet tried it out, but it seems to provide a similar base functionality set to what I have described here.
