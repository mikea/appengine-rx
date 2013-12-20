package com.mikea.util

import scala.reflect.runtime.universe._
import java.util.logging.Logger
import com.mikea.util.TypeNeq.=!=

/**
 * @author mike.aizatsky@gmail.com
 */
object TypeTags {
  private val log: Logger = Loggers.getContextLogger

  // todo: only one implicit should be used. Combine =!= and TypeTag together.
  def getClazz[C](implicit tag: TypeTag[C], d: C =!= Nothing): Class[C] = {
    val typ: Type = typeOf[C]
    assert(typ != typeOf[Nothing], "Type propagation failed - type is Nothing")

    val typeSymbol: Symbol = typ.typeSymbol
    val symbol: ClassSymbol = typeSymbol.asClass
    val mirror = runtimeMirror(getClass.getClassLoader)
    val clazz: RuntimeClass = mirror.runtimeClass(symbol)
    clazz.asInstanceOf[Class[C]]
  }
}
