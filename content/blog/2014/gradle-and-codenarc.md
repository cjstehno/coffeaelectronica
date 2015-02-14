title=Gradle and CodeNarc
date=2014-11-07
type=post
tags=blog,java,testing,mocking
status=published
~~~~~~
The subject of "code quality tools"  has lead to many developer holy wars over the years, so I'm not really going to touch the subject of their value or level of importance here, suffice to say that they are tools in your toolbox for helping to maintain a base level of "tedious quality", meaning style rules and general coding conventions enforced by your organization - it should never take the ultimate decision making from the developers.

That being said, let's talk about [CodeNarc](http://codenarc.sourceforge.net/). CodeNarc is a rule-based code quality analysis tool for Groovy-based projects. Groovy does not always play nice with other code analysis tools, so it's nice that there is one specially designed for it and [Gradle](http://gradle.org) provides access to it out of the box.

Using the [Gradle CodeNarc plugin](http://www.gradle.org/docs/current/dsl/org.gradle.api.plugins.quality.CodeNarc.html) is easy, apply the plugin to your build

    apply plugin: 'codenarc'

and then do a bit of rule configuration based on the needs of your code base. 

```groovy
codenarcMain {
    ignoreFailures false
    configFile file('config/codenarc/codenarc-main.rules')

    maxPriority1Violations 0
    maxPriority2Violations 10
    maxPriority3Violations 20
}

codenarcTest {
    ignoreFailures true
    configFile file('config/codenarc/codenarc-test.rules')

    maxPriority1Violations 0
    maxPriority2Violations 10
    maxPriority3Violations 20
}
```

The plugin allows you to have different configurations for your main code and your test code, and I recommend using that functionality since generally you may care about slightly different things in your production code versus your test code. Also, there are JUnit-specific rules that you can ignore in your production code scan.

Notice that in my example, I have ignored failures in the test code. This is handy when you are doing a lot of active development and don't really want to fail your build every time your test code quality drops slightly. You can also set the thresholds for allowed violations of the three priority levels - when the counts exceed one of the given thresholds, the build will fail, unless it's ignored. You will always get a report for both main and test code in your build reports directory, even if there are no violations. The threshold numbers are something you will need to determine based on your code base, your team and your needs.

The `.rules` files are really Groovy DSL files, but the extension is unimportant so I like to keep them out of the Groovy namespace. The CodeNarc web site has a sample "[kitchen sink](http://codenarc.sourceforge.net/StarterRuleSet-AllRulesByCategory.groovy.txt)" rule set to get things started - though it has a few rules that cause errors, you can comment those out or remove them from the file. Basically the file is a list of all the active rules, so removing one disables it. You can also configure some of them. LineLength is one I like to change:

    LineLength { length = 150 }

This will keep the rule active, but will allow line lengths of 150 rather than the default 120 characters. You will need to check the JavaDocs for configurable rule properties; for the most part, they seem to be on or off. 

Running the analysis is simple, the `check` task may be run by itself, or it will be run along with the `build` task.

    gradle check

The reports (main and test) will be available in the `build/reports/codenarc` directory as two html files. They are not the prettiest reports, but they are functional.

If you  are starting to use CodeNarc on an existing project, you may want to take a phased approach to applying and customizing rules so that you are not instantly bogged down with rule violations - do a few passes with the trimmed down rule set, fix what you can fix quickly and configure or disable the others and set your thresholds to a sensible level then make a goal to drop the numbers with each sprint or release so that progress is made.