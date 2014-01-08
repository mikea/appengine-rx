package com.mikea.gae.rx.base

import com.twitter.bijection.Bijection

/**
 * @author mike.aizatsky@gmail.com
 */
import scala.language.higherKinds

trait SubjectFactory[M[_]] {
  def apply[T]() : Subject[M[T]]

  def through[T](mapping : Bijection[T, M[T]]) : Subject[T] = {
    val self = this
    val subject: Subject[M[T]] = self[T]()
    subject.map(mapping.inverse)
  }
}
