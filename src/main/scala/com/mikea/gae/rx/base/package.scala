package com.mikea.gae.rx

/**
 * @author mike.aizatsky@gmail.com
 */
package object base {
  type TransformerSlot[In, Out] = Transformer[Out, In]
}
