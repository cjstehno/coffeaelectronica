title=Simple Image Rotation
date=2009-01-08
type=post
tags=blog,java
status=published
~~~~~~
I did a little image rotation today so I figured I'd save my prototype code for future use.

I am using [Groovy](http://groovy.codehaus.org/) to demo the code for this but the Java is exactly the same, just with a
little more formality.

```groovy
import java.awt.image.*
import java.awt.geom.*
import javax.imageio.*

def image = ImageIO.read(new File('trees.jpg'))
def w = image.width
def h = image.height

def degrees = Integer.parseInt(args[0])

def sin = Math.abs(Math.sin(Math.toRadians(degrees)))
def cos = Math.abs(Math.cos(Math.toRadians(degrees)))
def neww = (int)Math.floor(w*cos+h*sin)
def newh = (int)Math.floor(h*cos+w*sin)

def dst = new BufferedImage(neww,newh,image.getType())
def g = dst.createGraphics()
g.translate((neww-w)/2, (newh-h)/2)
g.rotate(Math.toRadians(degrees), w/2, h/2)
g.drawRenderedImage(image, null)

if(ImageIO.write(dst,'jpg',new File('trees_rotated.jpg'))){
    println "Success!"
} else {
    println "Oh no!"
}
```

This reads in the original image file, rotates it by the specified number of degrees and corrects the translation
and new image size so that image does not have any odd artifacts from the rotation. This should work for any positive
and negative rotation value.
