package com.mikea.gae.rx

import com.mikea.gae.rx.base.{IObservable, IDisposable, IObserver}
import com.google.inject.Injector

class RxPushStream[T](_injector: Injector) extends IObservable[T] {
  def onNext(t: T): Unit = {
    for (observer <- observers) {
      observer.onNext(t)
    }
  }

  def subscribe(observer: IObserver[T]): IDisposable = {
    observers = observers :+ observer

    new IDisposable {
      def dispose(): Unit = {
        throw new UnsupportedOperationException
      }
    }
  }

  private var observers: Vector[IObserver[T]] = Vector()

  def instantiate[C](aClass: Class[C]) = _injector.getInstance(aClass)
}