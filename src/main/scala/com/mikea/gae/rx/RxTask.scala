package com.mikea.gae.rx

import com.google.common.base.Objects
import javax.servlet.http.HttpServletRequest
import java.io._
import resource._
import com.google.appengine.api.taskqueue.TaskOptions
import com.mikea.gae.rx
import scala.reflect.runtime.universe._
import scala.collection.immutable.HashMap

/**
 * @author mike.aizatsky@gmail.com
 */
object RxTask {
  def newBuilder[T <: Serializable : TypeTag]: RxTask.Builder[T] = {
    new RxTask.Builder[T]
  }

  def fromRequest[T <: Serializable : TypeTag](request: HttpServletRequest): rx.RxTask[T] = {
    if (typeOf[T] <:< typeOf[Map[String, String]]) {
      throw new UnsupportedOperationException()
    }

    for (ois <- managed(new ObjectInputStream(request.getInputStream))) {
      val payload: T = ois.readObject.asInstanceOf[T]

      var headers: Map[String, String] = HashMap()

      import scala.collection.JavaConverters._

      for (name <- request.getHeaderNames.asScala) {
        headers = headers + (name.toString -> request.getHeader(name.toString))
      }

      return new RxTask[T](payload, headers)
    }

    throw new IOException()
  }

  def getPayloadFn[T <: Serializable]: (RxTask[T]) => T = (task: RxTask[T]) => task.payload

  class Builder[T <: Serializable : TypeTag] {
    var _payload: Option[T] = None
    var headers: Map[String, String] = HashMap()

    def header(name: String, value: String) = {
      headers = headers + (name -> value)
      this
    }

    def build: RxTask[T] = new RxTask[T](_payload.get, headers)

    def payload(payload: T): RxTask.Builder[T] = {
      this._payload = Some(payload)
      this
    }
  }

}

class RxTask[T <: Serializable : TypeTag](_payload: T, _headers: Map[String, String]) {
  private def toPayLoad: Array[Byte] = {
    if (typeOf[T] <:< typeOf[Map[String, String]]) {
      throw new UnsupportedOperationException()
    }

    import resource._
    for (baos <- managed(new ByteArrayOutputStream)) {
      for (oos <- managed(new ObjectOutputStream(baos))) {
        oos.writeObject(_payload)
      }

      return baos.toByteArray
    }

    throw new IOException()
  }

  def asTaskOptions(): TaskOptions = {
    var options: TaskOptions = TaskOptions.Builder.withUrl(RxUrls.RX_TASKS_URL_BASE).payload(toPayLoad)
    _headers.foreach { case (name, value) => options = options.header(name, value)}
    options
  }

  override def toString: String = {
    Objects.toStringHelper(this).add("payload", _payload).toString
  }

  def payload: T = _payload
}