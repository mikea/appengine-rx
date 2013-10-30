package com.mikea.gae.rx.base

import com.google.common.base.Preconditions._

trait IObservable[T] {
  def subscribe(observer: IObserver[T]): IDisposable

  def instantiate[C](aClass : Class[C]) : C

  def transform[U](f: (T) => U): IObservable[U] = {
    transform(new DoFn[T, U] {
      def process(t: T, emitFn: (U) => Unit): Unit = {
        emitFn(f.apply(t))
      }
    })
  }

  def transform[U](fn: DoFn[T, U]): IObservable[U] = {
    val src = this
    new IObservable[U] {
      def subscribe(observer: IObserver[U]): IDisposable = {
        src.subscribe(new IObserver[T] {
          def onCompleted(): Unit = {
            observer.onCompleted()
          }

          def onError(e: Exception): Unit = {
            observer.onError(e)
          }

          def onNext(value: T): Unit = {
            fn.process(value, (u: U) => observer.onNext(u))
          }
        })
      }

      def instantiate[C](aClass: Class[C]) = src.instantiate(aClass)
    }
  }

  def transformMany[U](fn: (T) => Iterable[U]): IObservable[U] = {
    transform(new DoFn[T, U] {
      def process(value: T, emitFn: (U) => Unit) = {
        for (u <- checkNotNull(fn.apply(value))) {
          emitFn(u)
        }
      }
    })
  }

  def sink(sink: IObserver[T]): IObservable[T] = {
    this.subscribe(sink)
    this
  }

  def apply(action: (T) => Unit): IObservable[T] = sink(Observers.asObserver(action))
  def apply(actionClass: Class[_ <: (T) => Unit]): IObservable[T] = apply(instantiate(actionClass))

  def filter(predicate: (T) => Boolean): IObservable[T] = {
    transform(new DoFn[T, T] {
      def process(value: T, emitFn: (T) => Unit) = {
        if (predicate.apply(value)) {
          emitFn(value)
        }
      }
    })
  }

  def transformMany[U](fnClass: Class[_ <: (T) => Iterable[U]]): IObservable[U] = transformMany(instantiate(fnClass))
}