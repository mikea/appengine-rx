package com.mikea.gae.rx.tasks

import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.taskqueue.TaskOptions
import java.io.Serializable
import com.mikea.gae.rx.base.Observer
import java.util.logging.Logger
import com.mikea.util.Loggers

/**
 * @author mike.aizatsky@gmail.com
 */
object RxTasks {
  private val LOG: Logger = Loggers.getContextLogger

  private def enqueue[T <: Serializable](queueName: String, taskOptions: TaskOptions): Unit = {
    LOG.fine(s"enqueue to $queueName : $taskOptions")
    QueueFactory.getQueue(queueName).add(taskOptions)
  }

  private[rx] def taskqueue(queueName: String): Observer[TaskOptions] = {
    Observer.asObserver((taskOptions: TaskOptions) => enqueue(queueName, taskOptions))
  }

  private[tasks] def taskqueue() : Observer[(String, TaskOptions)] = {
    Observer.asObserver((tuple: (String, TaskOptions)) => enqueue(tuple._1, tuple._2))
  }
}