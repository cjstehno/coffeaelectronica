package shoe.servlet

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

class LoggingListener implements ServletContextListener {

    @Override
    void contextInitialized(ServletContextEvent sce) {
        println "Initialized: $sce"
    }

    @Override
    void contextDestroyed(ServletContextEvent sce) {
        println "Destroyed: $sce"
    }
}
