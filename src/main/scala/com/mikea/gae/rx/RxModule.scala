package com.mikea.gae.rx

import com.google.inject.servlet.ServletModule

class RxModule extends ServletModule {
  protected override def configureServlets(): Unit = {
    super.configureServlets()
    serve(RxUrls.RX_URL_BASE + "*").`with`(classOf[RxServlet])
    bind(classOf[Rx]).to(classOf[RxImpl])
  }
}