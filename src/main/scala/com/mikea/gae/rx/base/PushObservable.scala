package com.mikea.gae.rx.base


// todo: rename
class PushObservable[T] extends Observable[T] {
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

  def subscribe[S >: T](observer: Observer[S]) = {
    observers = observers :+ observer

    new Disposable {
      def dispose(): Unit = ???
    }
  }

  private var observers: Vector[Observer[T]] = Vector()

}