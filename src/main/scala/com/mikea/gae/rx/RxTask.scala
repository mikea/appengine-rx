package com.mikea.gae.rx

import com.google.common.base.Function
import com.google.common.base.Objects
import javax.servlet.http.HttpServletRequest
import java.io._
import resource._

/**
 * @author mike.aizatsky@gmail.com
 */
object RxTask {
  def newBuilder[T <: Serializable]: RxTask.Builder[T] = {
    new RxTask.Builder[T]
  }

  def fromRequest[T <: Serializable](request: HttpServletRequest): RxTask[T] = {
    for (ois <- managed(new ObjectInputStream(request.getInputStream))) {
      val payload: T = ois.readObject.asInstanceOf[T]
      return new RxTask[T](payload)
    }

    throw new IOException()
  }

  def getPayloadFn[T <: Serializable]: (RxTask[T]) => T = (task: RxTask[T]) => task.getPayload

  class Builder[T <: Serializable] {
    var _payload: Option[T] = None

    def build: RxTask[T] = new RxTask[T](_payload.get)

    def payload(payload: T): RxTask.Builder[T] = {
      this._payload = Some(payload)
      this
    }
  }

}

class RxTask[T <: Serializable](_payload: T) {

  def toPayLoad: Array[Byte] = {
    import resource._

    for (baos <- managed(new ByteArrayOutputStream)) {
      for (oos <- managed(new ObjectOutputStream(baos))) {
        oos.writeObject(_payload)
      }

      return baos.toByteArray
    }

    throw new IOException()
  }

  override def toString: String = {
    Objects.toStringHelper(this).add("payload", _payload).toString
  }

  def getPayload: T = _payload
}