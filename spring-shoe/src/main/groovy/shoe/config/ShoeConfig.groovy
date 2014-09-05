package shoe.config

import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory
import org.springframework.boot.context.embedded.FilterRegistrationBean
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean
import org.springframework.boot.context.embedded.ServletRegistrationBean
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import shoe.servlet.HelloServlet
import shoe.servlet.LoggingListener
import shoe.servlet.RequestCountFilter

import javax.servlet.http.HttpServlet

@Configuration
@EnableWebMvc
@ComponentScan(basePackages=['shoe.controller'])
class ShoeConfig {

    @Bean EmbeddedServletContainerFactory embeddedServletContainerFactory(){
        def factory = new JettyEmbeddedServletContainerFactory( 10101 )
        factory.addServerCustomizers( new ShoeCustomizer() )
        return factory
    }

    @Bean HttpServlet dispatcherServlet(){
        new DispatcherServlet()
    }

    @Bean ServletRegistrationBean helloServlet(){
        new ServletRegistrationBean(
            urlMappings:[ '/hello' ],
            servlet: new HelloServlet()
        )
    }

    @Bean FilterRegistrationBean countingFilter(){
        new FilterRegistrationBean(
            urlPatterns:[ '/*' ],
            filter: new RequestCountFilter(),
            initParameters:[ 'startValue': '1000' ]
        )
    }

    @Bean ServletListenerRegistrationBean listener(){
        new ServletListenerRegistrationBean(
            listener: new LoggingListener()
        )
    }
}

