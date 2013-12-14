package com.mikea.util

import com.googlecode.objectify.ObjectifyService._
import com.googlecode.objectify.{Work, Key}
import scala.reflect.runtime.universe._

/**
 * @author mike.aizatsky@gmail.com
 */
object Ofy {
  def loadAll[T : TypeTag] : (AnyRef) => List[T] = (s) => {
    val clazz: Class[T] = TypeTags.getClazz[T]

    import scala.collection.JavaConverters._
    ofy.load.`type`(clazz).list.asScala.toList
  }

  def loadNow[T : TypeTag] (id: String) : T = {
    val clazz: Class[T] = TypeTags.getClazz[T]
    ofy.load.`type`(clazz).id(id).now
  }

  def createKey[T : TypeTag](name: String): Key[T] = Key.create(TypeTags.getClazz[T], name)

  def transactNew[T](tx: => T) : T = {
    ofy.transactNew(new Work[T] {
      def run() = tx
    })
  }
}
