package com.stehno.ast

import groovy.text.GStringTemplateEngine
import groovy.text.Template

class ClassBuilder {

    private final Template template
    private final injectedBlocks = []

    private ClassBuilder(String classBase){
        template = new GStringTemplateEngine().createTemplate(classBase)
    }

    static ClassBuilder forCode(String classBase){
        new ClassBuilder(classBase)
    }

    ClassBuilder inject(String code){
        injectedBlocks << code
        this
    }

    ClassBuilder reset(){
        injectedBlocks.clear()
        this
    }

    String source(){
        template.make(code:injectedBlocks.join('\n'))
    }

    Class compile(){
        GroovyClassLoader invoker = new GroovyClassLoader()
        def clazz = invoker.parseClass(source())
        clazz
    }

    def instantiate(){
        compile().newInstance()
    }
}