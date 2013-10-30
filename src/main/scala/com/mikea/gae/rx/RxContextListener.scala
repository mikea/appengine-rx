package com.mikea.gae.rx

import com.google.inject.Injector
import com.google.inject.servlet.GuiceServletContextListener
import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent

/**
 * @author mike.aizatsky@gmail.com
 */
abstract class RxContextListener extends GuiceServletContextListener {
  override def contextInitialized(servletContextEvent: ServletContextEvent): Unit = {
    super.contextInitialized(servletContextEvent)
    val servletContext: ServletContext = servletContextEvent.getServletContext
    val injector: Injector = servletContext.getAttribute(classOf[Injector].getName).asInstanceOf[Injector]
    injector.getInstance(classOf[Rx]).asInstanceOf[RxImpl].onContextInitialized()
  }
}