package com.mikea.gae.rx

import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.taskqueue.TaskOptions
import java.io.Serializable
import com.mikea.gae.rx.base.IObserver

/**
 * @author mike.aizatsky@gmail.com
 */
object RxTasks {
  def enqueue[T <: Serializable](queueName: String, value: RxTask[T]): Unit = {
    val taskOptions: TaskOptions = value.asTaskOptions()
    enqueue(queueName, taskOptions)
  }

  def enqueue[T <: Serializable](queueName: String, taskOptions: TaskOptions): Unit = {
    QueueFactory.getQueue(queueName).add(taskOptions)
  }

  def queue(queueName: String): IObserver[TaskOptions] = {
    (taskOptions: TaskOptions) => enqueue(queueName, taskOptions)
  }
}