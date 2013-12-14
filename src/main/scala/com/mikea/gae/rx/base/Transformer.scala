package com.mikea.gae.rx.base

import com.google.inject.Injector
import scala.reflect.runtime.universe._

object Transformer extends Injectable {

  def combine[In, Out](observer: Observer[In], observable: Observable[Out]) : Transformer[In, Out] = {
    new Transformer[In, Out] {
      def onError(e: Exception) = observer.onError(e)

      def onCompleted() = observer.onCompleted()

      def subscribe(observer: Observer[Out]) = observable.subscribe(observer)

      def onNext(value: In) = observer.onNext(value)
    }
  }

  def map[In, Out](fn : In => Out) : Transformer[In, Out] = ???
  def flatMap[In, Out](fn: (In) => Iterable[Out]) : Transformer[In, Out] = ???
  def flatMap[In, Out, C <: (In) => Iterable[Out]](implicit injector : Injector, tag : TypeTag[C]): Transformer[In, Out] = flatMap(instantiate[C])

  def foreach[T](action: (T) => Unit): Transformer[T, T] = map[T, T]((t: T) => {action(t); t})
  def foreach[T, C <: (T => Unit)](implicit injector : Injector, tag : TypeTag[C]): Transformer[T, T] = foreach(instantiate[C])
}

trait Transformer[In, Out] extends Observer[In] with Observable[Out] {
  def mapIn[NewIn](fn : NewIn => In) : Transformer[NewIn, Out] = map(fn, identity[Out])
  def mapOut[NewOut](fn : Out => NewOut) : Transformer[In, NewOut] = map(identity[In], fn)

  def map[NewIn, NewOut](mapInFn : NewIn => In, mapOutFn: Out => NewOut) : Transformer[NewIn, NewOut] = Transformer.combine(this.unmap(mapInFn), this.map(mapOutFn))

  override def >>>[NewOut](t : Transformer[Out, NewOut]) : Transformer[In, NewOut] = {
    // todo: this should be possible without subscription, i.e. without side effect?
    this.subscribe(t)
    Transformer.combine(this, t)
  }
}
