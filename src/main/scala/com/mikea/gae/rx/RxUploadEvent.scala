package com.mikea.gae.rx

import com.google.appengine.api.blobstore.BlobInfo
import com.google.common.base.Objects

/**
 * @author mike.aizatsky@gmail.com
 */
class RxUploadEvent(rx: Rx, event: RxHttpRequestEvent, _blobInfos: Map[String, Set[BlobInfo]]) extends RxHttpRequestEvent(rx, event.request, event.response) {
  override def toString: String = {
    Objects.toStringHelper(this).add("blobInfos", _blobInfos).toString
  }

  def blobInfos() : Map[String, Set[BlobInfo]] = _blobInfos
}