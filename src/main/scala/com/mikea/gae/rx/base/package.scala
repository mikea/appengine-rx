package com.mikea.gae.rx

import com.twitter.bijection.Bijection
import scala.language.implicitConversions


/**
 * @author mike.aizatsky@gmail.com
 */
package object base {
  type TransformerSlot[In, Out] = Transformer[Out, In]

  type Subject[T] = Transformer[T, T]

  class SubjectHelper[T](subject: Subject[T]) {
    def bimap[S](bijection : Bijection[T, S]) : Subject[S] = Transformer.combine(subject.unmap(bijection.inverse), subject.map(bijection.toFunction))
  }

  implicit def asSubjectHelper[T](subject: Subject[T]) = new SubjectHelper[T](subject)
}
