package com.mikea.gae.rx.base

import com.google.inject.Injector
import scala.reflect.runtime.universe._
import com.mikea.util.TypeTags

/**
 * @author mike.aizatsky@gmail.com
 */
trait Injectable {
  protected final def instantiate[C](aClass : Class[C])(implicit injector : Injector) : C = injector.getInstance(aClass)

  protected final def instantiate[C](implicit injector : Injector, tag : TypeTag[C]) : C = {
    instantiate(TypeTags.getClazz)
  }
}
