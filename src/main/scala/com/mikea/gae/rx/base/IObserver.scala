package com.mikea.gae.rx.base


object IObserver {
  implicit def asObserver[T](action: (T) => Unit): IObserver[T] = {
    new IObserver[T] {
      def onCompleted(): Unit = {
      }

      def onError(e: Exception): Unit = {
        throw new UnsupportedOperationException(e)
      }

      def onNext(value: T): Unit = {
        action(value)
      }
    }
  }
}

trait IObserver[T] {
  def onCompleted(): Unit

  def onError(e: Exception): Unit

  def onNext(value: T): Unit

  def map[S](fn: S => T): IObserver[S] = {
    val self = this

    new IObserver[S] {
      def onError(e: Exception) = self.onError(e)

      def onCompleted() = self.onCompleted()

      def onNext(value: S) = self.onNext(fn(value))
    }
  }
}