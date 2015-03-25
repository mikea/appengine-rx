package com.mikea.gae.rx.events

import com.google.appengine.api.blobstore.BlobInfo
import com.google.common.base.{MoreObjects, Objects}
import com.mikea.gae.rx.Rx

/**
 * @author mike.aizatsky@gmail.com
 */
class RxUploadEvent(event: RxHttpRequest, _blobInfos: Map[String, Set[BlobInfo]]) extends RxHttpRequest(event) {
  override def toString: String = {
    MoreObjects.toStringHelper(this).add("blobInfos", _blobInfos).toString
  }

  def blobInfos() : Map[String, Set[BlobInfo]] = _blobInfos
}