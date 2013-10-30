package com.mikea.gae.rx.base

import com.google.common.base.Preconditions.checkNotNull
import com.google.inject.Injector

/**
 * @author mike.aizatsky@gmail.com
 */
object Observables {
  def transform[T, S](src: IObservable[S], f: (S) => T): IObservable[T] = {
    transform(src, new DoFn[S, T] {
      def process(s: S, emitFn: (T) => Unit): Unit = {
        emitFn(f.apply(s))
      }
    })
  }

  def apply[T](src: IObservable[T], action: (T) => Unit): IObservable[T] = {
    src.subscribe(Observers.asObserver(action))
    src
  }

  def filter[T](src: IObservable[T], predicate: (T) => Boolean): IObservable[T] = {
    new IObservable[T] {
      def subscribe(observer: IObserver[T]): IDisposable = {
        src.subscribe(new IObserver[T] {
          def onCompleted(): Unit = {
            observer.onCompleted()
          }

          def onError(e: Exception): Unit = {
            observer.onError(e)
          }

          def onNext(value: T): Unit = {
            if (predicate.apply(value)) {
              observer.onNext(value)
            }
          }
        })
      }
    }
  }

  def flatten[T](src: IObservable[Iterable[T]]): IObservable[T] = {
    new IObservable[T] {
      def subscribe(observer: IObserver[T]): IDisposable = {
        src.subscribe(new IObserver[Iterable[T]] {
          def onCompleted(): Unit = {
            observer.onCompleted()
          }

          def onError(e: Exception): Unit = {
            observer.onError(e)
          }

          def onNext(values: Iterable[T]): Unit = {
            for (t <- values) {
              observer.onNext(t)
            }
          }
        })
      }
    }
  }

  def sink[T](src: IObservable[T], sink: IObserver[T]): IObservable[T] = {
    src.subscribe(sink)
    src
  }

  def transformMany[T, U](src: IObservable[T], fnClass: Class[_ <: (T) => Iterable[U]], injector: Injector): IObservable[U] = {
    transformMany(src, injector.getInstance(fnClass))
  }

  def transformMany[T, U](src: IObservable[T], fn: (T) =>  Iterable[U]): IObservable[U] = {
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
            for (u <- checkNotNull(fn.apply(value))) {
              observer.onNext(u)
            }
          }
        })
      }
    }
  }

  def transform[T, U](src: IObservable[T], fn: DoFn[T, U]): IObservable[U] = {
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
    }
  }
}
