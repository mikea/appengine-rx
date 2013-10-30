package com.mikea.gae.rx.base

trait IObservable[T] {
  def subscribe(observer: IObserver[T]): IDisposable
}