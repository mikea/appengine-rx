package com.mikea.gae.rx.base

trait Disposable {
  def dispose(): Unit

  def join(otherDisposable: Disposable): Disposable = {
    val self = this

    new Disposable {
      def dispose() = {
        self.dispose()
        otherDisposable.dispose()
      }
    }
  }
}