package com.mikea.gae.rx.base

/**
 * @author mike.aizatsky@gmail.com
 */
object Observers {
  def asObserver[T](action: (T) => Unit): IObserver[T] = {
    new IObserver[T] {
      def onCompleted() {
      }

      def onError(e: Exception) {
        throw new UnsupportedOperationException(e)
      }

      def onNext(value: T) {
        action(value)
      }
    }
  }
}