package com.mikea.gae.objectify

import com.googlecode.objectify.Objectify

/**
 * @author mike.aizatsky@gmail.com
 */
object ScalaObjectify {
  implicit def asScalaObjectify(ofy : Objectify) : ScalaObjectify = new ScalaObjectify(ofy)
}

class ScalaObjectify(ofy : Objectify) {

}
