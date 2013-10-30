package com.mikea.gae.rx.base


/**
 * @author mike.aizatsky@gmail.com
 */
trait DoFn[S, T] {
  def process(s: S, emitFn: (T) => Unit)
}