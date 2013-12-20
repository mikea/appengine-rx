package com.mikea.util

/**
 * Forcing type difference through =!=.
 *
 * http://stackoverflow.com/questions/6909053/enforce-type-difference
 */
object TypeNeq {
  trait =!=[A, B]

  implicit def neq[A, B] : A =!= B = null

  // This pair excludes the A =:= B case
  implicit def neqAmbig1[A] : A =!= A = null
  implicit def neqAmbig2[A] : A =!= A = null
}