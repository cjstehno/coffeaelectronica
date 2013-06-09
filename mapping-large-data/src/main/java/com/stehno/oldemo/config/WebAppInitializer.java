package com.stehno.oldemo.config;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * Web application config.
 */
@SuppressWarnings("UnusedDeclaration")
public class WebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup( ServletContext servletContext ) throws ServletException{
        final AnnotationConfigWebApplicationContext root = new AnnotationConfigWebApplicationContext();
        root.setServletContext(servletContext);
        root.scan("com.stehno.oldemo.config");
        root.refresh();

        servletContext.addListener( new ContextLoaderListener( root ) );

        final ServletRegistration.Dynamic servlet = servletContext.addServlet( "spring", new DispatcherServlet( root ) );
        servlet.setLoadOnStartup( 0 );
        servlet.addMapping( "/" );
    }
}