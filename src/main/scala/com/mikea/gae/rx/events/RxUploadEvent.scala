package com.mikea.gae.rx.events

import com.google.appengine.api.blobstore.BlobInfo
import com.google.common.base.Objects
import com.mikea.gae.rx.Rx

/**
 * @author mike.aizatsky@gmail.com
 */
class RxUploadEvent(rx: Rx, event: RxHttpRequest, _blobInfos: Map[String, Set[BlobInfo]]) extends RxHttpRequest(rx, event.httpRequest, event.httpResponse) {
  override def toString: String = {
    Objects.toStringHelper(this).add("blobInfos", _blobInfos).toString
  }

  def blobInfos() : Map[String, Set[BlobInfo]] = _blobInfos
}