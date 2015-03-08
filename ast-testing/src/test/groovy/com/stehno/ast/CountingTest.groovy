package com.stehno.ast

import org.junit.Test

import static com.stehno.ast.ClassBuilder.forCode

class CountingTest {

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

    @Test void 'multiple methods'(){
        def instance = code.inject('''
            @Counted
            String sayHello(String name){
                "Hello, $name"
            }

            @Counted
            String sayGoodbye(String name){
                "Goodbye, $name"
            }
        ''').instantiate()

        assert instance.sayHello('AST') == 'Hello, AST'
        assert instance.getSayHelloCount() == 1

        assert instance.sayGoodbye('AST') == 'Goodbye, AST'
        assert instance.getSayGoodbyeCount() == 1

        assert instance.sayHello('Counting') == 'Hello, Counting'
        assert instance.getSayHelloCount() == 2

        assert instance.sayGoodbye('Counting') == 'Goodbye, Counting'
        assert instance.getSayGoodbyeCount() == 2
    }

    @Test void 'multiple methods: with overload'(){
        def instance = code.inject('''
            @Counted
            String sayHello(String name){
                "Hello, $name"
            }

            @Counted('sayHelloFullname')
            String sayHello(String firstName, String lastName){
                "Hello, $firstName $lastName"
            }
        ''').instantiate()

        assert instance.sayHello('AST') == 'Hello, AST'
        assert instance.getSayHelloCount() == 1

        assert instance.sayHello('Chris','Stehno') == 'Hello, Chris Stehno'
        assert instance.getSayHelloFullnameCount() == 1

        assert instance.sayHello('Counting') == 'Hello, Counting'
        assert instance.getSayHelloCount() == 2

        assert instance.sayHello('John','Doe') == 'Hello, John Doe'
        assert instance.getSayHelloFullnameCount() == 2
    }
}
