package com.stehno.ast.transform

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import java.util.concurrent.atomic.AtomicLong

import static java.lang.reflect.Modifier.PUBLIC
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE
import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.block
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * AST Transformation performed when the Counted annotation is encountered in the source code during compilation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class CountedTransformation implements ASTTransformation {

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        def countedNode = nodes[0] as AnnotationNode
        def methodNode = nodes[1] as MethodNode
        def classNode = methodNode.getDeclaringClass()

        String methodName = countedNode.getMember('value')?.value ?: methodNode.name
        String fieldName = "_${methodName}Count"

        classNode.addField(new FieldNode(
            fieldName,
            PUBLIC,
            make(AtomicLong),
            classNode,
            ctorX(make(AtomicLong))
        ))

        Statement originalCode = methodNode.code
        methodNode.code = block(
            stmt(callX(varX(fieldName), 'incrementAndGet')),
            originalCode
        )

        classNode.addMethod(new MethodNode(
            "get${methodName.capitalize()}Count",
            PUBLIC,
            long_TYPE,
            [] as Parameter[],
            [] as ClassNode[],
            GeneralUtils.returnS(callX(varX(fieldName), 'get'))
        ))
    }
}

