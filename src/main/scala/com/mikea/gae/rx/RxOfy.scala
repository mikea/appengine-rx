package com.mikea.gae.rx

import com.googlecode.objectify.Key
import com.googlecode.objectify.Result
import com.googlecode.objectify.ObjectifyService.ofy

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
}