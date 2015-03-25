package com.mikea.gae.rx.tasks

import java.io._
import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest

import com.google.appengine.api.taskqueue.TaskOptions
import com.google.common.base.Objects
import com.mikea.gae.rx.base._
import com.mikea.gae.rx.events.RxTaskEvent
import com.mikea.gae.rx.impl.RxUrls
import com.mikea.gae.rx.{Rx, RxHttpResponse}
import com.twitter.bijection.Bijection
import resource._

import scala.collection.immutable.HashMap
import scala.concurrent.duration.Duration
import scala.reflect.runtime.universe._

/**
 * @author mike.aizatsky@gmail.com
 */
object RxTask {
  def failFast(factory: SubjectFactory[RxTask]): SubjectFactory[RxTask] = new SubjectFactory[RxTask] {
    def apply[T]() : Subject[RxTask[T]] = failFast(factory[T]())
  }

  def payloadBijection[S] () : Bijection[S, RxTask[S]] = Bijection.build((s: S) => new RxTask(s))((task: RxTask[S]) => task.payload)

  private val TASK_NAME_PREFIX: String = "1/"
  private val ILLEGAL_TASK_CHARACTERS: Pattern = Pattern.compile("[^a-zA-Z0-9_-]")

  private def fromRequest[T](request: HttpServletRequest): RxTask[T] = {
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

  def failFast[T](observer: Observer[RxTask[T]]) : Observer[RxTask[T]] = {
    observer.unmap((task: RxTask[T]) => task.copy(headers = task.headers + ("X-AppEngine-FailFast" -> "True")))
  }

  def failFast[T](subject: Subject[RxTask[T]]) : Subject[RxTask[T]] = {
    Subject.combine(failFast(subject.asInstanceOf[Observer[RxTask[T]]]),
                     subject)
  }

  private def tasksObserver[T](queue : Observer[TaskOptions]): Observer[RxTask[T]] = {
    queue.unmap((task: RxTask[T]) => task.asTaskOptions())
  }

  private def tasksObservable[T](allRequests : Observable[RxTaskEvent]): Observable[RxTask[T]] = {
    allRequests.map((event: RxTaskEvent) => RxTask.fromRequest(event.httpRequest))
  }

  def tasks[T](queueName: String)(implicit rx : Rx) : Subject[RxTask[T]] =  {
    val taskqueue: TransformerSlot[RxTaskEvent, RxHttpResponse] = rx.taskqueue(queueName)
    val enqueue: Observer[TaskOptions] = rx.enqueue(queueName)
    
    // todo: Shouldn't actually always connect to ok. should send errors as well. 
    taskqueue.connect(RxHttpResponse.ok())

    Subject.combine(tasksObserver(enqueue), tasksObservable(taskqueue))
  }

  // todo this factory stuff doesn't look neat, find a better representation
  def tasksFactory(queueName: String)(implicit rx : Rx) : SubjectFactory[RxTask] = new SubjectFactory[RxTask] {
    def apply[T]() = tasks(queueName)
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
    def mapPayload[S <: Serializable : TypeTag](fn : Bijection[T, S]) : Subject[RxTask[S]] = subject.bimap(Bijection.build[RxTask[T], RxTask[S]](_.map(fn))(_.map(fn.invert)))
  }

  implicit def RxTaskSubjectHelper[T <: Serializable : TypeTag](subject : Subject[RxTask[T]]) = new RxTaskSubjectHelper[T](subject)

  def throughTasks[T](subjects : SubjectFactory[RxTask]): Subject[T] = subjects.through(RxTask.payloadBijection[T]())
}

case class RxTask[T](payload: T,
                     headers: Map[String, String] = Map(),
                     name: Option[String] = None,
                     countdown: Option[Duration] = None) {
  private def toPayLoad: Array[Byte] = {
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

// todo: the existence of the factory doesn't seem to be like a good thing
trait RxTasksFactory {
  def apply(queueName : String) : SubjectFactory[RxTask]
}