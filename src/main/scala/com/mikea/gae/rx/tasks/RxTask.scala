package com.mikea.gae.rx.tasks

import com.google.common.base.Objects
import javax.servlet.http.HttpServletRequest
import java.io._
import resource._
import com.google.appengine.api.taskqueue.TaskOptions
import com.mikea.gae.rx
import scala.reflect.runtime.universe._
import scala.collection.immutable.HashMap
import java.util.regex.Pattern
import com.mikea.gae.rx.RxUrls

/**
 * @author mike.aizatsky@gmail.com
 */
object RxTask {
  private val TASK_NAME_PREFIX: String = "1/"
  private val ILLEGAL_TASK_CHARACTERS: Pattern = Pattern.compile("[^a-zA-Z0-9_-]")

  def newBuilder[T <: Serializable : TypeTag]: RxTask.Builder[T] = {
    new RxTask.Builder[T]
  }

  def fromRequest[T <: Serializable : TypeTag](request: HttpServletRequest): RxTask[T] = {
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

      return new RxTask[T](payload, headers, None, None)
    }

    throw new IOException()
  }

  def getPayloadFn[T <: Serializable]: (RxTask[T]) => T = (task: RxTask[T]) => task.payload

  class Builder[T <: Serializable : TypeTag] {
    private var _payload: Option[T] = None
    private var _headers: Map[String, String] = HashMap()
    private var _name: Option[String] = None
    private var _countdown: Option[Double] = None

    def header(name: String, value: String) = {
      _headers = _headers + (name -> value)
      this
    }

    def build: RxTask[T] = new RxTask[T](_payload.get, _headers, _name, _countdown)

    def payload(payload: T): RxTask.Builder[T] = {
      _payload = Some(payload)
      this
    }

    def name(n: String) = {
      _name = Some(n)
      this
    }
    
    def countdownSec(sec: Double) = {
      _countdown = Some(sec)
      this
    }
  }

}

class RxTask[T <: Serializable : TypeTag](_payload: T, _headers: Map[String, String], _name: Option[String], _countdownSec: Option[Double]) {
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
    var taskOptions: TaskOptions = TaskOptions.Builder.withUrl(RxUrls.RX_TASKS_URL_BASE).payload(toPayLoad)
    if (_name.isDefined) {
      var actualTaskName: String = RxTask.TASK_NAME_PREFIX + _name.get
      actualTaskName = RxTask.ILLEGAL_TASK_CHARACTERS.matcher(actualTaskName).replaceAll("-")
      taskOptions = taskOptions.taskName(actualTaskName)
    }
    if (_countdownSec.isDefined) {
      taskOptions = taskOptions.countdownMillis((_countdownSec.get * 1000).toLong)
    }

    _headers.foreach { case (name, value) => taskOptions = taskOptions.header(name, value)}
    taskOptions
  }

  override def toString: String = {
    Objects.toStringHelper(this).add("payload", _payload).toString
  }

  def payload: T = _payload
}