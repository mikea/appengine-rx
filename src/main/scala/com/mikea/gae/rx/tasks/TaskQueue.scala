package com.mikea.gae.rx.tasks

import com.mikea.gae.rx.events.RxHttpRequest
import java.util.logging.Logger
import com.mikea.util.Loggers
import java.io.Serializable
import com.google.appengine.api.taskqueue.{QueueFactory, TaskOptions}
import com.mikea.gae.rx.base.Observer

object TaskQueue {
  private val log: Logger = Loggers.getContextLogger

  private def enqueue[T <: Serializable](queueName: String, taskOptions: TaskOptions): Unit = {
    log.fine(s"enqueue to $queueName : $taskOptions")
    QueueFactory.getQueue(queueName).add(taskOptions)
  }

  private[rx] def enqueue(queueName: String): Observer[TaskOptions] = {
    Observer.asObserver((taskOptions: TaskOptions) => enqueue(queueName, taskOptions))
  }

  def queueName(request : RxHttpRequest) : Option[String] = {
    val header: String = request.httpRequest.getHeader("X-AppEngine-QueueName")
    Option.apply(header)
  }

  def isTaskQueueRequest(request : RxHttpRequest) : Boolean = queueName(request).isDefined
}
