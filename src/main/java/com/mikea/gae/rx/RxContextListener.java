package com.mikea.gae.rx;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * @author mike.aizatsky@gmail.com
 */
public abstract class RxContextListener extends GuiceServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
        ServletContext servletContext = servletContextEvent.getServletContext();
        Injector injector = (Injector) servletContext.getAttribute(Injector.class.getName());

        RxImpl rx = (RxImpl) injector.getInstance(Rx.class);
        rx.onContextInitialized();
    }
}
