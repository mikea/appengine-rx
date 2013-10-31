package com.mikea.gae.rx.base

import com.google.common.base.Preconditions._

object Observable {
  @deprecated
  def flatten[T](src: Observable[Iterable[T]]): Observable[T] = {
    src.map(new DoFn[Iterable[T], T] {
      def process(values: Iterable[T], emitFn: (T) => Unit) = {
        for (t <- values) {
          emitFn(t)
        }
      }
    })
  }
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