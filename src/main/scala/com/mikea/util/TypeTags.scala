package com.mikea.util

import scala.reflect.runtime.universe._

/**
 * @author mike.aizatsky@gmail.com
 */
object TypeTags {
  def getClazz[C : TypeTag]: Class[C] = {
    val mirror = runtimeMirror(getClass.getClassLoader)
    mirror.runtimeClass(typeOf[C].typeSymbol.asClass).asInstanceOf[Class[C]]
  }
}
