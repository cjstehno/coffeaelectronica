package shoe

import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext

class Shoe {

    static void main( args ){
        EmbeddedWebApplicationContext context = new AnnotationConfigEmbeddedWebApplicationContext('shoe.config')
        println "Started context on ${new Date(context.startupDate)}"
    }
}
