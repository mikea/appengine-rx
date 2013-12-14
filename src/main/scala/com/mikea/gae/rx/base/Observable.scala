package com.mikea.gae.rx.base

import language.implicitConversions
import language.higherKinds
import scala.reflect.runtime.universe._
import com.google.inject.Injector


object Observable {
  class IterableObservableHelper[T, I[T] <: Iterable[T]](observable: Observable[I[T]]) {
    def flatten(): Observable[T] = {
      for {
        i <- observable
        t <- i
      } yield t
    }
  }

  implicit def asIterableObservable[T, I[T] <: Iterable[T]](observable: Observable[I[T]]) = new IterableObservableHelper[T, I](observable)

  class OptionObservableHelper[T, O[T] <: Option[T]](observable: Observable[O[T]]) {
    def flatten(): Observable[T] = {
      for {
        o <- observable
        t <- o
      } yield t
    }
  }

  implicit def asOptionObservable[T, O[T] <: Option[T]](observable: Observable[O[T]]) = new OptionObservableHelper[T, O](observable)
}

trait Observable[T] extends Injectable {
  self =>

  // ----- Interface -----

  def subscribe(observer: Observer[T]): Disposable

  // ---- Helper Methods----

  def map[U](f: (T) => U): Observable[U] = {
    // todo: one-line subscriber should be defined
    new Observable[U] {
      def subscribe(observer: Observer[U]) = {
        self.subscribe(new Observer[T] {
          def onError(e: Exception) = observer.onError(e)

          def onCompleted() = observer.onCompleted()

          def onNext(value: T) = observer.onNext(f(value))
        })
      }
    }
  }

  def map[U, C <: (T) => U](implicit injector : Injector, tag : TypeTag[C]) : Observable[U] = map(instantiate[C])

  def flatMap[U](fn: (T) => Iterable[U]): Observable[U] = {
    // todo: one-line subscriber should be defined
    new Observable[U] {
      def subscribe(observer: Observer[U]) = {
        self.subscribe(new Observer[T] {
          def onError(e: Exception) = observer.onError(e)

          def onCompleted() = observer.onCompleted()

          def onNext(value: T) = fn(value).foreach(observer.onNext)
        })
      }
    }
  }

  def flatMap[C <: (T) => Iterable[U], U](implicit injector : Injector, tag : TypeTag[C]): Observable[U] = flatMap(instantiate[C])

  def through[C  <: Subject[T]](implicit injector : Injector, tag : TypeTag[C]): Observable[T] = through(instantiate[C])
  def through(sink: Subject[T]): Observable[T] = {
    subscribe(sink)
    sink
  }

  def foreach[C <: (T => Unit)](implicit injector : Injector, tag : TypeTag[C]): Observable[T] = foreach(instantiate[C])
  def foreach(action: (T) => Unit): Observable[T] = sink(Observer.asObserver(action))

  def sink[C <: Observer[T]](implicit injector : Injector, tag : TypeTag[C]): Observable[T] = sink(instantiate[C])
  def sink(observer: Observer[T]): Observable[T] = {subscribe(observer); this}

  def withFilter(predicate: (T) => Boolean): Observable[T] = {
    // todo: one-line subscriber should be defined
    new Observable[T] {
      def subscribe(observer: Observer[T]) = {
        self.subscribe(new Observer[T] {
          def onError(e: Exception) = observer.onError(e)

          def onCompleted() = observer.onCompleted()

          def onNext(value: T) =  if (predicate(value)) observer.onNext(value)
        })
      }
    }
  }

  // todo: clean this up
  def either[S](other: Observable[S]) : Observable[Either[T, S]] = {
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
    }
  }

  def >>>[S](tr : Transformer[T, S]) : Observable[S] = ???
}