package com.stehno.ast.annotation

import com.stehno.ast.transform.CountedTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * When applied to a method, this annotation will cause all its invocations to be counted in a
 * thread-safe manner. The current count for a specific annotated method may be retrieved by
 * calling the generated getter method which will have the format "long get[MethodName]Count()"
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
@Documented
@GroovyASTTransformationClass(classes = [CountedTransformation])
@interface Counted {

    /**
     * Optional name value to be used - defaults to the name of the method. This override is useful
     * in cases where multiple methods have the same name.
     * The value must follow the same naming rules as a method name.
     */
    String value() default ''
}