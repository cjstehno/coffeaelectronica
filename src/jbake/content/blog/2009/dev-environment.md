title=Development Environment
date=2009-11-13
type=post
tags=blog,java
status=published
~~~~~~
One of the tasks I have been given at work is to help modernize the "old school" organization and implement more standardized Java development practices; it has gotten me thinking a lot about what makes a good development procedure/environment.

The list I came up with, should be very familiar to most developers, and though they are most often considered part of "agile" development, they are in my opinion, just good development practices that go beyond any specific development methodology:

* Coding to interfaces
* Unit testing
* Decoupled tools
* Automated build (with command line ability)
* Good source control
* Continuous integration
* Good bug tracking

## Coding to Interfaces

Write your services and DAOs based on interfaces rather than concrete classes. This allows you to test code in a more decoupled manner and allows you greater implementation flexability. It also makes team development more manageable, since you can agree on some interfaces early on in the project and then code to them separately withough the need to wait for the missing pieces.

## Unit Testing

Again... how do you know that your code works without proof. Unit testing in some automated, reproducable format is crucial to providing confidence that your code does what it is supposed to do. Into this category I also throw code coverage, which gives you some clues as to how thoroughly you have tested your code.

## Decoupled Tools

Your tools should work for you, not against you. Your IDEs, servers, build tools, etc should not be tied together in any way. Developers should be able to use whatever IDE/tools (other than servers) that they are familiar with.

## Automated Build

Your project(s) should be buildable from the command line in a repeatable manner with little effort. Tools like [Ant](http://ant.apache.org/) and [Maven](http://maven.apache.org/) provide rich environments for build production, while still allowing good integration with most IDEs. This goes along somewhat with the previous point about decoupled tools. There is no reason why a developer should not be able to easity edit a file with a plain text editor, run the tests and produce build from the command line with no less confidence than working in a full-featured IDE.

## Source Control

Whether it's a personal project, a prototype, or a project for work, it should have some sort of good source control setup for it. There are tons of free options ([git](http://gitscm.org/), [svn](http://subversion.tigris.org/), [bazar](http://bazaar-vcs.org/en/), [mercurial](http://mercurial.selenic.com/), cvs) and even some good commercial options ([perforce](http://perforce.com/)), each with varrying levels of IDE integration and ease of use. A good source control system should provide good IDE integration as well as good command line usability.

## Continuous Integration

If you have more than one person committing code to a project you will need a continuous integration server ([CruiseControl](http://cruisecontrol.sourceforge.net/), [Hudson](http://hudson-ci.org/), [Continuum](http://continuum.apache.org/) ) setup to do automated builds at least once a day. Having a build kicked off after a checkin is made is also very helpful. CI builds provide an addition level of assurance that the project compiles and is testable on something other than the developers machine. It also provides help with larger teams in keeping the repository clean from bad code commits. A failing build becomes a red flag that should be fixed right away.

## Bug Tracking

A good means of logging and managing bug tickets is very important to keeping track of what has been done as well as what needs to be done. Don't use an in-house spreadsheed, access database or some other bogus bug tracker. Install one of the many free options ([bugzilla](http://www.bugzilla.org/), [trac](http://trac.edgewall.org/), redmine, [mantis](http://www.mantisbt.org/)) or one of the commercial options (fogbugz). A good tracker should provide a number of access points so that developer tools can also integrate with the tracker and provide a more rich interaction experience. I am sure that most of this is nothing new, but I wanted to get it all down in one place for future reference. Let me know if I have missed or over/under-stated anything.
