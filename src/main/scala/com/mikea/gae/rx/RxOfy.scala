package com.mikea.gae.rx

import com.googlecode.objectify.Key
import com.googlecode.objectify.Result
import com.googlecode.objectify.ObjectifyService.ofy
import scala.reflect.runtime.universe._
import com.mikea.util.TypeTags

/**
 * @author mike.aizatsky@gmail.com
 */
object RxOfy {
  def save[T]: (T) => Result[Key[T]] = (t: T) => ofy.save.entity(t)

  def loadSafe[T]: (Key[T]) => T = (key : Key[T]) => ofy.load.key(key).safe

  def saveMulti[T]: (Iterable[T]) => Map[Key[T], T] = (values : Iterable[T]) => {
    import scala.collection.JavaConverters._
    ofy.save.entities(values.asJava).now().asScala.toMap
  }

  def loadAll[T : TypeTag] : (AnyRef) => List[T] = (s) => {
    val clazz: Class[T] = TypeTags.getClazz[T]

    import scala.collection.JavaConverters._
    ofy.load.`type`(clazz).list.asScala.toList
  }

  def createKey[T : TypeTag](name: String): Key[T] = Key.create(TypeTags.getClazz[T], name)
}