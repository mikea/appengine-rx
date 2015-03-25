package com.mikea.gae.rx.events

import com.google.common.base.{MoreObjects, Objects}

/**
 * @author mike.aizatsky@gmail.com
 */
class RxVersionUpdateEvent(version: String) {
  override def toString = MoreObjects.toStringHelper(this).add("version", version).toString
}