title=Cruise Control Project Operations
date=2008-02-08
type=post
tags=blog,java
status=published
~~~~~~
One of the developers I work with figured out the URL for firing the various
[CruiseControl](http://cruisecontrol.sourceforge.net/) build operations (resume,
pause, build, etc) and it was jokingly noted that now all we need is a [Ruby](http://ruby-lang.org/) script to fire them.

And so, viola! Here is a simple ruby script that will do just that. It will run the specified operation command on one
project (or all if no project is specified). You do have to configure the script with your CruiseControl url and your
project names, but it's well worth it.

```ruby
#
# exec_in_cc.rb
#
# Used to perform actions on the projects managed by CruiseControl.
# If no project is specified, the given operation will be run on all projects (internal array of them).
#
# ruby exec_in_cc.rb operation [project]
#
# Christopher J. Stehno (2/7/2008)
#
require 'net/http'
require 'uri'

# customize these to fit your pojects
cruise_url = 'http://builder:8000'
projects = ['nightly-build','releases','site-build']

# require the operation param
if ARGV[0] == nil
    puts "You must specify an operation!"
    exit
else
    operation = ARGV[0]
end

# check for specified project, if none use all of them
unless ARGV[1] == nil then projects = [ARGV[1]] end

projects.each {|project|
    url = URI.parse(cruise_url)
    res = Net::HTTP.start(url.host, url.port) {|http|
        http.get("/invoke?operation=#{operation}&amp;objectname=CruiseControl+Project%3Aname%3D#{project}")
    }
    success = res.body.index('Invocation successful') != nil ? 'Success' : 'Failed'<
    puts "#{project}.#{operation}: #{success}"
}
```

This took about 30 minutes to write and will save at least that much time over the life of its use. Ruby is
excellent for this kind of scripting. Something I have tried to do more often is to script tasks like this. You may feel
that you are wasting time when you should be doing other things, but usually with a repetitive task like this you really
notice the value. If you find it useful or come up with some good improvements to it, I would love to hear about them.

After some additional thought, I figured a Groovy implementation would be interesting and easy.

```groovy
package cc

class CruiseExec {
    private static cruiseUrl = 'http://builder:8000'
    private static projects = ['nightly-build','releases','site-build']

    static void main(args) {
        def operation = null
        if(args.length == 0 || args[0] == null){
            println 'No operation specified!'
            System.exit(0)
        } else {
            operation = args[0]
        }

        if(args.length == 2 && args[1] != null) projects = [args[1]]

        projects.each { project ->
            def url = new URL("${cruiseUrl}/invoke?operation=${operation}&objectname=CruiseControl+Project%3Aname%3D${project}")
            def success = url.text.contains('Invocation successful')
            println "${project}.${operation}: ${success}"
        }
    }
}
```

It was very easy, easier than Ruby in fact since I work primarily in Java so I did not have to go looking up odd
syntax questions as I did when writing the Ruby version. It is interesting to note that in this version I took the more
Object oriented approach and wrote a class rather than a naked script. You could pull the meat out of the main method
and make an even shorter script version if you are so inclined. It's always interesting to compare the same
functionality across different languages, so I thought I'd share.

Also, it does perform the exact same functionality, so you are welcome to use this version as a replacement for the Ruby version.
