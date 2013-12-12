package com.mikea.gae.rx.tasks

import com.google.common.base.Objects
import javax.servlet.http.HttpServletRequest
import java.io._
import resource._
import com.google.appengine.api.taskqueue.TaskOptions
import scala.reflect.runtime.universe._
import scala.collection.immutable.HashMap
import java.util.regex.Pattern
import com.mikea.gae.rx.base.{Transformer, Observable, Observer, Subject}
import scala.concurrent.duration.Duration
import com.mikea.gae.rx.impl.RxUrls
import com.mikea.gae.rx.Rx
import com.twitter.bijection.Bijection
import com.mikea.gae.rx.events.RxHttpRequestEvent

/**
 * @author mike.aizatsky@gmail.com
 */
object RxTask {
  private val TASK_NAME_PREFIX: String = "1/"
  private val ILLEGAL_TASK_CHARACTERS: Pattern = Pattern.compile("[^a-zA-Z0-9_-]")

  private def fromRequest[T <: Serializable : TypeTag](request: HttpServletRequest): RxTask[T] = {
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

  def failFast[T <: Serializable : TypeTag](observer: Observer[RxTask[T]]) : Observer[RxTask[T]] = {
    observer.unmap((task: RxTask[T]) => task.copy(headers = task.headers + ("X-AppEngine-FailFast" -> "True")))
  }

  def failFast[T <: Serializable : TypeTag](subject: Subject[RxTask[T]]) : Subject[RxTask[T]] = {
    Subject.combine(
      subject,
      failFast(subject.asInstanceOf[Observer[RxTask[T]]]))
  }

  private def tasksObserver[T <: java.io.Serializable](queue : Observer[TaskOptions]): Observer[RxTask[T]] = {
    queue.unmap((task: RxTask[T]) => task.asTaskOptions())
  }

  private def tasksObservable[T <: java.io.Serializable : TypeTag](observable : Observable[RxHttpRequestEvent]): Observable[RxTask[T]] = {
    observable.map((event: RxHttpRequestEvent) => RxTask.fromRequest(event.request))
  }

  private[rx] def tasks[T <: Serializable : TypeTag](queue : Transformer[TaskOptions, RxHttpRequestEvent]): Subject[RxTask[T]] = {
    Subject.combine(tasksObservable(queue), tasksObserver(queue))
  }

  def factory(rx : Rx): RxTasksFactory =
    new RxTasksFactory {
      def apply[T <: Serializable : TypeTag](queueName: String) = tasks[T](rx.taskqueue(queueName))
    }

  import scala.language.implicitConversions

  // todo: rewrite all these helpers using high-level poly

  class RxTaskObservableHelper[T <: Serializable : TypeTag](observable : Observable[RxTask[T]]) {
    def mapPayload[S <: Serializable : TypeTag](fn : T => S) : Observable[RxTask[S]] = observable.map(_.map(fn))
  }

  implicit def asRxTaskObservableHelper[T <: Serializable : TypeTag](observable : Observable[RxTask[T]]) = new RxTaskObservableHelper[T](observable)

  class RxTaskObserverHelper[T <: Serializable : TypeTag](observer : Observer[RxTask[T]]) {
    def mapPayload[S <: Serializable : TypeTag](fn : S => T) : Observer[RxTask[S]] = observer.unmap(_.map(fn))
  }

  implicit def asRxTaskObserverHelper[T <: Serializable : TypeTag](observer : Observer[RxTask[T]]) = new RxTaskObserverHelper[T](observer)

  class RxTaskSubjectHelper[T <: Serializable : TypeTag](subject : Subject[RxTask[T]]) {
    def mapPayload[S <: Serializable : TypeTag](fn : Bijection[T, S]) : Subject[RxTask[S]] = subject.map(Bijection.build[RxTask[T], RxTask[S]](_.map(fn))(_.map(fn.invert)))
  }

  implicit def RxTaskSubjectHelper[T <: Serializable : TypeTag](subject : Subject[RxTask[T]]) = new RxTaskSubjectHelper[T](subject)
}

case class RxTask[T <: Serializable : TypeTag](payload: T,
                                               headers: Map[String, String] = Map(),
                                               name: Option[String] = None,
                                               countdown: Option[Duration] = None) {
  private def toPayLoad: Array[Byte] = {
    if (typeOf[T] <:< typeOf[Map[String, String]]) {
      throw new UnsupportedOperationException()
    }

    import resource._
    for (baos <- managed(new ByteArrayOutputStream)) {
      for (oos <- managed(new ObjectOutputStream(baos))) {
        oos.writeObject(payload)
      }

      return baos.toByteArray
    }

    throw new IOException()
  }

  private[tasks] def asTaskOptions(): TaskOptions = {
    var taskOptions: TaskOptions = TaskOptions.Builder.withUrl(RxUrls.RX_TASKS_URL_BASE).payload(toPayLoad)
    if (name.isDefined) {
      var actualTaskName: String = RxTask.TASK_NAME_PREFIX + name.get
      actualTaskName = RxTask.ILLEGAL_TASK_CHARACTERS.matcher(actualTaskName).replaceAll("-")
      taskOptions = taskOptions.taskName(actualTaskName)
    }
    if (countdown.isDefined) {
      taskOptions = taskOptions.countdownMillis(countdown.get.toMillis)
    }

    headers.foreach { case (k, v) => taskOptions = taskOptions.header(k, v)}
    taskOptions
  }

  override def toString: String = {
    Objects.toStringHelper(this).add("payload", payload).toString
  }

  def map[S <: Serializable : TypeTag](fn : T => S) : RxTask[S] = copy(payload = fn(this.payload))
}