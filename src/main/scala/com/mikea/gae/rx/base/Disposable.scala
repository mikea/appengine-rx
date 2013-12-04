package com.mikea.gae.rx.base

trait Disposable {
  self =>

  def dispose(): Unit

  def join(otherDisposable: Disposable): Disposable = {
    new Disposable {
      def dispose() = {
        self.dispose()
        otherDisposable.dispose()
      }
    }
  }
}