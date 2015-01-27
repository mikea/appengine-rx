package com.mikea.gae.rx.base


/**
 * @author mike.aizatsky@gmail.com
 */
object Subject {
  def combine[T](observer: Observer[T], observable: Observable[T]) : Subject[T] = Transformer.combine(observer, observable)
}
