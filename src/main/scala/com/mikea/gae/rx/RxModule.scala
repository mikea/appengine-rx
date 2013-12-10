package com.mikea.gae.rx

import com.google.inject.servlet.ServletModule
import com.googlecode.objectify.ObjectifyService
import com.mikea.gae.rx.model.AppVersion
import com.mikea.gae.rx.impl.{RxFilter, RxImpl}

class RxModule extends ServletModule {
  protected override def configureServlets(): Unit = {
    super.configureServlets()

    ObjectifyService.register(classOf[AppVersion])

    filter("*").through(classOf[RxFilter])
    bind(classOf[Rx]).to(classOf[RxImpl])
  }
}