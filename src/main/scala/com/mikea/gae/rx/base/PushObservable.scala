package com.mikea.gae.rx.base


abstract class PushObservable[T] extends Observable[T] {
  def onNext(t: T): Unit = {
    for (observer <- observers) {
      observer.onNext(t)
    }
  }

  def onCompleted() = {
    for (observer <- observers) {
      observer.onCompleted()
    }
  }

  def onError(e: Exception) = {
    for (observer <- observers) {
      observer.onError(e)
    }
  }

  def subscribe(observer: Observer[T]): Disposable = {
    observers = observers :+ observer

    new Disposable {
      def dispose(): Unit = {
        throw new UnsupportedOperationException
      }
    }
  }

  private var observers: Vector[Observer[T]] = Vector()
}