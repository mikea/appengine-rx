package com.mikea.gae.rx

import com.google.common.base.Optional
import com.mikea.gae.rx.base.{DoFn, IObserver}

/**
 * @author mike.aizatsky@gmail.com
 */
object RxUtils {
  def redirect[T <: RxHttpRequestEvent](url: String): IObserver[T] = {
    IObserver.asObserver((value: T) => value.sendRedirect(url))
  }

  def skipAbsent[T](): DoFn[Option[T], T] = {
    new DoFn[Option[T], T] {
      def process(in: Option[T], emitFn: (T) => Unit): Unit = {
        if (in.isDefined) {
          emitFn(in.get)
        }
      }
    }
  }

  def flatten[T]: DoFn[Iterable[T], T] = {
    new DoFn[Iterable[T], T] {
      def process(ts: Iterable[T], emitFn: (T) => Unit): Unit = {
        for (t <- ts) {
          emitFn(t)
        }
      }
    }
  }
}