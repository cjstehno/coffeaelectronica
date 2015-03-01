title=Finding Your Center
date=2010-08-28
type=post
tags=blog,java
status=published
~~~~~~
Way back when I was in high school my typing teacher (yes, on a mechanical typewriter) taught us how to center blocks of text on a page. This has been a useful skill as I got into web development and programming as not everything has a built-in "center" alignment property.

I have never been asked how to do this, so it may be common knowledge, but I thought I would share anyway since I have not been blogging much. Basically, think about your viewing area as a page and what you are centering as a rectangle drawn on that page. Now fold that page in half and you will see that half of your rectangle is contained on half the page... makes sense. You are finding the horizontal position (x coordinate) of the left edge of your rectangle by subtracting half of its width from half the width of the page:

```
x = [page_width]/2 - [item_width]/2
```

or after a little simplification

```
x = ( [page_width] - [item_width] )/2
```

This is exactly the same way you would center vertically. The equation from above expressed with more meaningful terms:

```
y = ( [page_height] - [item_height] )/2
```

Now to bring this into the Java world you can center a JFrame in the center of your monitor screen:

```java
Dimension frameSize = new Dimension(800,600);
Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
int x = (screenSize.width-frameSize.width)/2;
int y = (screenSize.height-frameSize.height)/2;
frame.setBounds( x, y, frameSize.width, frameSize.height );
```

Not too bad at all, but it has always surprised me that Swing does not provide helper methods to center components inside of other components.
