package com.mikea.gae.rx

import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.taskqueue.TaskOptions
import java.io.Serializable
import com.mikea.gae.rx.base.Observer

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

  def taskqueue(queueName: String): Observer[TaskOptions] = {
    (taskOptions: TaskOptions) => enqueue(queueName, taskOptions)
  }

  def taskqueue() : Observer[(String, TaskOptions)] = {
    (tuple: (String, TaskOptions)) => enqueue(tuple._1, tuple._2)
  }
}