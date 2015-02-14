title=Personal Development Process
date=2012-05-06
type=post
tags=blog,javascript
status=published
~~~~~~
Lately my duties have started drifting more into the realm of team leader and mentor, and with that I am often quite surprised by the lack of understanding around best practices. I try not to take it for granted that everyone places the same value on these topics as I do, but there are some basic concepts that (at least to me) feel like basic "rules of development".

In general, on a daily (or so) basis, a developer should do most if not all of the following during an active coding cycle:

## Write Good Code

You should always be writing code in a manner that you expect others to be able to understand and build on. Your comments should be clear, concise and meaningful. Your formatting should follow the general standards of the language and any standards provided by your development team.

## Unit Testing

Write meaningful and useful unit tests while you are coding, not at the end of your development cycle. Testing should become second-nature and something that you are disturbed about not doing. Unit tests are your first line of proof that your functionality does what it is supposed to do. You should be writing tests for code you are adding or modifying as well as adding tests to improve the general stability of the project.

## Run Tests

While developing and writing unit tests, you need to actually run your tests and any test suites in related areas, generally your project and projects that depend on it. You need to be sure that you have not broken anything in the project you are working on and that you have not broken anything in projects that depend on yours.

## Test Coverage

Unit test coverage can provide a useful measure of the areas covered by your unit tests. It is not a fool-proof measurement, but it can point our areas of your code that should have additional tests. You need to be careful that you do not simply add tests to get good coverage numbers; you should view your coverage and ensure that the tests you add have value and are really testing the areas they cover.

## Code Analysis

Code analysis tools can provide useful insight into style and structure issues, as well as potential bugs. Most IDEs provide some level of code analysis either directly or though the use of plugins. You should become familiar with the analysis tools you have available and run them on a regular basis, then take action based on the results.

## Update and Commit

When you are working with other team members on a project, it is important that you all have up-to-date code so that you do not start to diverge away from each other. In general, during active development you should update your local workspace every morning and check in your code every evening.

## Never Break It

Never check in code that is broken or has failing unit tests. This can disrupt the work of your fellow team members and pollute the branch. With your daily commits, you should ensure that you have not broken anything.

## Always Make it Better

Always use the Boy Scout rule... leave the code you are working on in a better condition than when you found it. Brushing off "bad code" is not a valid excuse in general... if you are there and the fix is obvious, then fix it. If the fix is more complex or risky, then it should be noted and scheduled for rework. It should not be ignored.

## Ask Questions

No developer works alone. Ask questions about specifications, design, existing code, etc... especially when you are in a codebase that is new to you. Too often, I have seen a feature go way off course simply because the developer made assumptions and did not bother to ask for clarification.

## Conclusions

As with any guidelines, these can be broken or bent; however, you should have a good reason for doing so and be able to communicate it. More junior developers should take these as requirements, while more senior developers should have a bit more latitude since they _should_ have a better understanding of how their development affects the system as a whole, and they will probably already have most of these steps integrated into their development style in some form or another.

Also, I am not saying that these will work for everyone, they are just my suggestions... unless you work for me.
