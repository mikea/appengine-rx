package com.mikea.gae.rx

import com.google.inject.{Injector, Inject}
import com.mikea.gae.rx.base.DoFn
import com.mikea.gae.rx.base.IObservable
import com.mikea.gae.rx.base.IObserver
import com.mikea.gae.rx.base.Observables
import com.mikea.gae.rx.base.Observers

/**
 * @author mike.aizatsky@gmail.com
 */
abstract class RxStream[T] @Inject() (rx: Rx, injector: Injector) extends IObservable[T] {
  def transform[U](fn: (T) => U): RxStream[U] = {
    wrap(Observables.transform(this, fn))
  }

  def transformMany[U](fn: (T) => Iterable[U]): RxStream[U] = {
    wrap(Observables.transformMany(this, fn))
  }

  def transformMany[U](fnClass: Class[_ <: (T) => Iterable[U]]): RxStream[U] = {
    wrap(Observables.transformMany(this, fnClass, injector))
  }

  final def transform[U](fn: DoFn[T, U]): RxStream[U] = {
    wrap(Observables.transform(this, fn))
  }

  final def apply(action: (T) => Unit): RxStream[T] = {
    RxObservableWrapper.wrap(rx, Observables.apply(this, action))
  }

  private[rx] def wrap[T](src: IObservable[T]): RxStream[T] = {
    RxObservableWrapper.wrap(rx, src)
  }

  final def filter(predicate: (T) => Boolean): RxStream[T] = {
    wrap(Observables.filter(this, predicate))
  }

  final def sink(sink: IObserver[T]): Unit = {
    Observables.sink(this, sink)
  }

  final def sink(sink: (T) => Unit): Unit = {
    Observables.sink(this, Observers.asObserver(sink))
  }
}