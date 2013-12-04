package com.mikea.gae.rx.base


/**
 * @author mike.aizatsky@gmail.com
 */
// todo: this can probably replaced by (observable => Unit) function instead
abstract class Processor[T] extends Observer[T] {
  def process(observable: Observable[T]): Unit

  def instantiate[C](aClass: Class[C]): C

  private val observable: PushObservable[T] = new PushObservable[T] {
    def instantiate[C](aClass: Class[C]) = Processor.this.instantiate(aClass)
  }

  process(observable)

  def onCompleted() = observable.onCompleted()

  def onError(e: Exception) = observable.onError(e)

  def onNext(value: T) = observable.onNext(value)
}
