package com.mikea.gae.rx.base

import com.google.inject.Injector
import scala.reflect.runtime.universe._
import com.mikea.util.TypeNeq.=!=

object Transformer extends Injectable {

  def combine[In, Out](observer: Observer[In], observable: Observable[Out]) : Transformer[In, Out] = {
    new Transformer[In, Out] {
      def onError(e: Exception) = observer.onError(e)

      def onCompleted() = observer.onCompleted()

      def onNext(value: In) = observer.onNext(value)

      def subscribe[S >: Out](observer: Observer[S]) = observable.subscribe(observer)
    }
  }

  def map[In, Out](fn : In => Out) : Transformer[In, Out] = {
    val observable = new PushObservable[Out]
    val observer : Observer[In] = new Observer[In] {
      def onError(e: Exception) = observable.onError(e)

      def onCompleted() = observable.onCompleted()

      def onNext(value: In) = observable.onNext(fn(value))
    }

    combine(observer, observable)
  }

  def flatMap[In, Out](fn: (In) => Iterable[Out]) : Transformer[In, Out] = {
    val observable = new PushObservable[Out]
    val observer : Observer[In] = new Observer[In] {
      def onError(e: Exception) = observable.onError(e)

      def onCompleted() = observable.onCompleted()

      def onNext(value: In) = fn(value) map observable.onNext
    }

    combine(observer, observable)
  }


  def flatMap[In, Out, C <: (In) => Iterable[Out]](implicit injector : Injector, tag : TypeTag[C], d : C =!= Nothing): Transformer[In, Out] = flatMap(instantiate[C])

  def foreach[T](action: (T) => Unit): Transformer[T, T] = map[T, T]((t: T) => {action(t); t})
  def foreach[T, C <: (T => Unit)](implicit injector : Injector, tag : TypeTag[C], d : C =!= Nothing): Transformer[T, T] = foreach(instantiate[C])
}

trait Transformer[-In, +Out] extends Observer[In] with Observable[Out] {
  def mapIn[NewIn](fn : NewIn => In) : Transformer[NewIn, Out] = map(fn, identity[Out])
  def mapOut[NewOut](fn : Out => NewOut) : Transformer[In, NewOut] = map(identity[In], fn)

  def map[NewIn, NewOut](mapInFn : NewIn => In, mapOutFn: Out => NewOut) : Transformer[NewIn, NewOut] = Transformer.combine(this.unmap(mapInFn), this.map(mapOutFn))

  def >>>[NewOut](t : Transformer[Out, NewOut]) : Transformer[In, NewOut] = {
    // todo: should this be possible without subscription, i.e. without side effect?
    this.subscribe(t)  // todo: dispose?
    Transformer.combine(this, t)
  }

  def connect(t : Transformer[Out, In]) = {
    // todo: should this be possible without subscription, i.e. without side effect?
    this.subscribe(t)  // todo: dispose?
    t.subscribe(this)  // todo: dispose?
  }
}

