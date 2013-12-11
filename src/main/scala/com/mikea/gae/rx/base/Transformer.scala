package com.mikea.gae.rx.base

object Transformer {
  def combine[In, Out](observer: Observer[In], observable: Observable[Out]) : Transformer[In, Out] = {
    new Transformer[In, Out] {
      def onError(e: Exception) = observer.onError(e)

      def onCompleted() = observer.onCompleted()

      def subscribe(observer: Observer[Out]) = observable.subscribe(observer)

      def onNext(value: In) = observer.onNext(value)
    }
  }
}

trait Transformer[In, Out] extends Observer[In] with Observable[Out] {
  def mapIn[NewIn](fn : NewIn => In) : Transformer[NewIn, Out] = map(fn, identity[Out])
  def mapOut[NewOut](fn : Out => NewOut) : Transformer[In, NewOut] = map(identity[In], fn)

  def map[NewIn, NewOut](mapInFn : NewIn => In, mapOutFn: Out => NewOut) : Transformer[NewIn, NewOut] = Transformer.combine(this.unmap(mapInFn), this.map(mapOutFn))

  def pipe[NewOut](t : Transformer[Out, NewOut]) : Transformer[In, NewOut] = {
    // todo: this should be possible without subscription, i.e. without side effect?
    this.subscribe(t)
    Transformer.combine(this, t)
  }
}
