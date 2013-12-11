package com.mikea.gae.rx.base

import com.twitter.bijection.Bijection

/**
 * @author mike.aizatsky@gmail.com
 */
object Subject {
  def combine[T](observable: Observable[T], observer: Observer[T]) : Subject[T] = {
    new Subject[T] {
      def onError(e: Exception) = observer.onError(e)

      def onCompleted() = observer.onCompleted()

      def subscribe(observer: Observer[T]) = observable.subscribe(observer)

      def onNext(value: T) = observer.onNext(value)
    }
  }
}

trait Subject[T] extends Transformer[T, T] {
  def map[S](bijection : Bijection[T, S]) : Subject[S] = Subject.combine(this.map(bijection.toFunction), this.unmap(bijection.inverse))
}
