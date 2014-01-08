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

      def onNext(value: T) = observer.onNext(value)

      def subscribe[S >: T](observer: Observer[S]) = observable.subscribe(observer)
    }
  }
}

// todo: it is not clear if we need this. Maybe use Transformer[T, T] everywhere?
trait Subject[T] extends Transformer[T, T] {
  def map[S](bijection : Bijection[T, S]) : Subject[S] = Subject.combine(this.map(bijection.toFunction), this.unmap(bijection.inverse))
}
