package com.mikea.gae.rx.events

import com.google.common.base.Objects

/**
 * @author mike.aizatsky@gmail.com
 */
class RxVersionUpdateEvent(version: String) {
  override def toString = Objects.toStringHelper(this).add("version", version).toString
}