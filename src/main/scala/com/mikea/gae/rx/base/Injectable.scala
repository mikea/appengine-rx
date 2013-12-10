package com.mikea.gae.rx.base

import com.google.inject.Injector
import scala.reflect.runtime.universe._

/**
 * @author mike.aizatsky@gmail.com
 */
trait Injectable {
  protected final def instantiate[C](aClass : Class[C])(implicit injector : Injector) : C = injector.getInstance(aClass)

  protected final def instantiate[C](implicit injector : Injector, tag : TypeTag[C]) : C = {
    val mirror = runtimeMirror(getClass.getClassLoader)
    val clazz: Class[C] = mirror.runtimeClass(typeOf[C].typeSymbol.asClass).asInstanceOf[Class[C]]
    instantiate(clazz)
  }
}
