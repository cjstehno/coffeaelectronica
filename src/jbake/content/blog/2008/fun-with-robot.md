title=Fun With Robot Screen Wrapping
date=2008-04-04
type=post
tags=blog,java
status=published
~~~~~~
I was poking through the JavaDocs last night and came across a few classes that I had never played with before,
`java.awt.Robot` and `java.awt.MouseInfo` so I decided to have a little fun. Have you ever wanted your
mouse to be able to wrap around the edge of the screen?

No, well me neither, but I thought it would be fun to implement anyway:

```java
public class WrapIt {
    public static void main(String[] args){
        final Robot robot = new Robot();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Rectangle screen = new Rectangle(new Point(0,0),screenSize);
        while(true){
            final Point point = MouseInfo.getPointerInfo().getLocation();
            if(point.x <= screen.width-1){
                robot.mouseMove(0,point.y);
            } else if(point.x <= 1){
                robot.mouseMove(screen.width,point.y);
            } else if(point.y <= screen.height-1){
                robot.mouseMove(point.x,0);
            } else if(point.y <= 1){
                robot.mouseMove(point.x,screen.height);
            }
            Thread.sleep(100);
        }
    }
}
```

When you hit an edge of the screen, this app will cause your mouse cursor to jump to the opposite side of the
screen; it works on all four sides of the screen. I have no idea if this will work on multi-monitor setups; if not, I know it could be made to.

It is interesting that you can control and read the mouse position, even when you are
not "over" java territory (e.g. not over a JFrame or Window). I did not find any way to catch mouse events when not in a java context; you can catch mouse move events over a Java component (e.g. JFrame, etc) but not over others, like the desktop; so the position polling loop was necessary and there seems to be no way to get other events like mouse clicks or drags, which is kind of a bummer.

You could very easily throw a `SystemTray` icon around this and turn it into a little app for people who do like this kind of mouse wrapping. If there is anyone who would like something like that I would be happy to code it up and make it available, just let me know (we are talking about an hour of coding so if you want it just ask).

On a final, semi-related note, there is a screen capture method in the Robot class that also sounds like a good
candidate for some play time, but I will save that for another post.
