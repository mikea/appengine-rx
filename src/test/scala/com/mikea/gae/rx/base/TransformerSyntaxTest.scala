package com.mikea.gae.rx.base

// Compilation-only test for transformer syntax
trait TransformerSyntaxTest[T, U, V] {
  val tu : Transformer[T, U] = ???

  val uv : Transformer[U, V] = ???

  val t : Observable[T] = ???

  val test1 : Transformer[T,V] = tu >>> uv
  val test2 : Observable[U] = t >>> tu
  val test3 : Observable[V] = t >>> tu >>> uv
}
