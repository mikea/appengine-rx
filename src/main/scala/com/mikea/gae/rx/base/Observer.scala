package com.mikea.gae.rx.base

import language.implicitConversions

object Observer {
  def asObserver[T](action: (T) => Unit): Observer[T] = {
    new Observer[T] {
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

trait Observer[T] {
  self =>

  // ----- Interface -----

  def onNext(value: T): Unit

  def onCompleted(): Unit

  def onError(e: Exception): Unit

  // ---- Helper Methods----

  def unmap[S](fn: S => T): Observer[S] = {
    new Observer[S] {
      def onError(e: Exception) = self.onError(e)

      def onCompleted() = self.onCompleted()

      def onNext(value: S) = self.onNext(fn(value))
    }
  }
}