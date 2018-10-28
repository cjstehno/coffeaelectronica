package shoe.config

import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer

/**
 * Created by cjstehno on 9/5/14.
 */
class ShoeCustomizer implements JettyServerCustomizer {

    @Override
    void customize(Server server) {
        SelectChannelConnector myConn = server.getConnectors().find { Connector conn ->
            conn.port == 10101
        }

        myConn.maxIdleTime = 1000 * 60 * 60
        myConn.soLingerTime = -1

        server.setSendDateHeader(true)
    }
}
