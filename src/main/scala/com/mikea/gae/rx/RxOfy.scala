package com.mikea.gae.rx

import com.google.common.base.Function
import com.googlecode.objectify.Key
import com.googlecode.objectify.Result
import com.googlecode.objectify.ObjectifyService.ofy

/**
 * @author mike.aizatsky@gmail.com
 */
object RxOfy {
  def save[T]: Function[T, Result[Key[T]]] = {
    new Function[T, Result[Key[T]]] {
      def apply(input: T): Result[Key[T]] = {
        ofy.save.entity(input)
      }
    }
  }

  def loadSafe[T]: Function[Key[T], T] = {
    new Function[Key[T], T] {
      def apply(input: Key[T]): T = ofy.load.key(input).safe
    }
  }

  def saveMulti[T]: (Iterable[T]) => Map[Key[T], T] = (values : Iterable[T]) => {
    import scala.collection.JavaConverters._
    ofy.save.entities(values.asJava).now().asScala.toMap
  }
}