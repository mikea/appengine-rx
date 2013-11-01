package com.mikea.gae.rx.base

import com.google.common.base.Preconditions._
import language.implicitConversions
import language.higherKinds

object Observable {
  class IterableObservable[T, I[T] <: Iterable[T]](observable: Observable[I[T]]) {
    def flatten(): Observable[T] = {
      observable.map(new DoFn[I[T], T] {
        def process(s: I[T], emitFn: (T) => Unit) = s.map(emitFn)
      })
    }
  }

  implicit def asIterableObservable[T, I[T] <: Iterable[T]](observable: Observable[I[T]]) = new IterableObservable[T, I](observable)
}

trait Observable[T] {
  def subscribe(observer: Observer[T]): Disposable

  def instantiate[C](aClass : Class[C]) : C

  def map[U](f: (T) => U): Observable[U] = {
    map(new DoFn[T, U] {
      def process(t: T, emitFn: (U) => Unit): Unit = {
        emitFn(f.apply(t))
      }
    })
  }

  def map[U](fn: DoFn[T, U]): Observable[U] = {
    val src = this
    new Observable[U] {
      def subscribe(observer: Observer[U]): Disposable = {
        src.subscribe(new Observer[T] {
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

  def mapMany[U](fn: (T) => Iterable[U]): Observable[U] = {
    map(new DoFn[T, U] {
      def process(value: T, emitFn: (U) => Unit) = {
        for (u <- checkNotNull(fn.apply(value))) {
          emitFn(u)
        }
      }
    })
  }

  def mapMany[U](fnClass: Class[_ <: (T) => Iterable[U]]): Observable[U] = mapMany(instantiate(fnClass))


  def sink(sink: Observer[T]): Observable[T] = {
    this.subscribe(sink)
    this
  }
  def sink(sinkClass: Class[_ <: Observer[T]]): Observable[T] = sink(instantiate(sinkClass))
  def apply(action: (T) => Unit): Observable[T] = sink(action)
  def apply(actionClass: Class[_ <: (T) => Unit]): Observable[T] = apply(instantiate(actionClass))

  def filter(predicate: (T) => Boolean): Observable[T] = {
    map(new DoFn[T, T] {
      def process(value: T, emitFn: (T) => Unit) = {
        if (predicate.apply(value)) {
          emitFn(value)
        }
      }
    })
  }

  // todo: clean this up
  def either[S](other: Observable[S]) : Observable[Either[T, S]] = {
    var self = this
    new Observable[Either[T, S]] {
      def subscribe(observer: Observer[Either[T, S]]):Disposable = {
        var completed: Int = 0

        self.subscribe(new Observer[T] {
          def onError(e: Exception) = observer.onError(e)

          def onCompleted() = {
            completed += 1
            if (completed == 2) {
              observer.onCompleted()
            }
          }

          def onNext(value: T) = observer.onNext(Left(value))
        }).join(other.subscribe(new Observer[S] {
          def onError(e: Exception) = observer.onError(e)

          def onCompleted() = {
            completed += 1
            if (completed == 2) {
              observer.onCompleted()
            }
          }

          def onNext(value: S) = observer.onNext(Right(value))
        }))
      }

      def instantiate[C](aClass: Class[C]) = self.instantiate(aClass)
    }
  }
}