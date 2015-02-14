title=Puzzle of the Jars and Pebbles
date=2009-04-08
type=post
tags=blog,java
status=published
~~~~~~
I had an interesting puzzle during a recent interview. I am paraphrasing this from memory so forgive me if
you know this one and I am misquoting it somehow:

> You have two types of pebbles, white and black, and three jars labeled white, black and black & white, respectively.
One jar contains all white pebbles, one contains all black pebbles and one contains a mixture of black and white pebbles.

The three jars a all mislabelled so that they will not contain the pebbles noted on the label. How many pebbles would you have to draw and from which jars in order to determine the true distribution?

Don't read any farther if you are going to try and solve this one for yourself. The next paragraph contains the
answer.

The answer is one, from the jar labelled "black & white". If you got it on your first try through, congratulations, I did not.
I figured it out on my second run through it.

Basically, you draw one pebble from the "black & white" jar. Say you draw a white one, you then know that the jar labelled "black & white" is the jar containing the white pebbles. This leaves you with two unknown jars, one labelled "white" and one labelled "black". Since you know that both of these are incorrect and you have a white pebble, you know that the jar labelled "white" contains the black pebbles and the one labelled "black" contains the black and white pebble mixture.

It's an interesting problem, but honestly I have never felt that these sorts of problems are useful in a technical interview. We would
usually have one question of this nature in our interviews more to see how they would go about solving it than looking
for an actual answer. Google apparently loves this type of question in an interview, but asks very little of a technical
nature.
