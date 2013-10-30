package com.mikea.gae.rx.base

import java.io.IOException

trait IObserver[T] {
  def onCompleted()

  def onError(e: Exception)

  @throws[IOException]
  def onNext(value: T)
}