package com.mikea.gae.rx.base


/**
 * @author mike.aizatsky@gmail.com
 */
object Observables {
  @deprecated
  def flatten[T](src: IObservable[Iterable[T]]): IObservable[T] = {
    src.transform(new DoFn[Iterable[T], T] {
      def process(values: Iterable[T], emitFn: (T) => Unit) = {
        for (t <- values) {
          emitFn(t)
        }
      }
    })
  }
}
