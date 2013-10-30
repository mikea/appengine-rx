package com.mikea.gae.rx.base

import java.io.IOException

object IObserver {
  def asObserver[T](action: (T) => Unit): IObserver[T] = {
    new IObserver[T] {
      def onCompleted(): Unit = {
      }

      def onError(e: Exception): Unit = {
        throw new UnsupportedOperationException(e)
      }

      def onNext(value: T): Unit = {
        action(value)
      }
    }
  }
}

trait IObserver[T] {
  def onCompleted(): Unit

  def onError(e: Exception): Unit

  def onNext(value: T): Unit
}